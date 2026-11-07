<#
.SYNOPSIS
  Reobfuscate the Farmstorm jar into the NeoForge/Forge 1.20.1 production namespace
  (Mojang official class names + SRG member names: "m_12345_" / "f_12345_").

.DESCRIPTION
  NeoForge userdev 7.0.171 for 1.20.1 has no reobf/remap task, so the jar produced by
  "gradle build" still uses official member names and dies at runtime with e.g.

    NoSuchMethodError: net.minecraft.util.StringRepresentable.fromEnum(Supplier)

  This script performs the missing step:
    1. builds an "official -> SRG" TSRG mapping from local, already verified inputs
         - Mojang proguard mappings      .gradle\caches\minecraft\versions\<v>\client.txt / server.txt
         - obf -> SRG (official classes) build\neoForm\...\steps\mergeMappings\output.tsrg
         - plus hierarchy entries for the mod's own overriding methods and for inherited
           call sites (tools\SrgMappingBuilder.java)
    2. runs net.neoforged:AutoRenamingTool over the jar
    3. verifies the result (SRG names present, official names gone, jar byte size)

.PARAMETER InputJar   jar to remap (default: build\libs\farmstorm-0.2.4+mc1.20.1.jar)
.PARAMETER OutputJar  destination jar (default: <input base>-srg.jar)
.PARAMETER WorkDir    where the generated mapping and report go (default: build\remap)
.PARAMETER DistJar    optional extra copy, e.g. dist\farmstorm-0.2.4+mc1.20.1.jar
.PARAMETER Force      rebuild the mapping even if it already exists
.PARAMETER SkipVerify skip the post-remap checks (not recommended)

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File tools\remap-to-srg.ps1 -Force

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File tools\remap-to-srg.ps1 -OutputJar dist\farmstorm.jar
#>
[CmdletBinding()]
param(
    [string]$InputJar,
    [string]$OutputJar,
    [string]$WorkDir,
    [string]$DistJar,
    [switch]$Force,
    [switch]$SkipVerify
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem | Out-Null

# ---------------------------------------------------------------- paths
$Root = Split-Path -Parent $PSScriptRoot
if ($Root -eq '') { $Root = (Get-Location).Path }

if ($InputJar -eq '')  { $InputJar  = Join-Path $Root 'build\libs\farmstorm-0.2.4+mc1.20.1.jar' }
if ($OutputJar -eq '') {
    $base = $InputJar
    if ($base -like '*.jar') { $base = $base.Substring(0, $base.Length - 4) }
    $OutputJar = $base + '-srg.jar'
}
if ($WorkDir -eq '') { $WorkDir = Join-Path $Root 'build\remap' }
if (-not [System.IO.Path]::IsPathRooted($InputJar))  { $InputJar  = Join-Path $Root $InputJar }
if (-not [System.IO.Path]::IsPathRooted($OutputJar)) { $OutputJar = Join-Path $Root $OutputJar }
if ($DistJar -ne '' -and -not [System.IO.Path]::IsPathRooted($DistJar)) { $DistJar = Join-Path $Root $DistJar }

$ToolchainRoot = 'D:\project_farm\.tools'
if ($env:FARMSTORM_TOOLS) { $ToolchainRoot = $env:FARMSTORM_TOOLS }
$GradleHome = Join-Path $ToolchainRoot 'gradle-home'
if ($env:FARMSTORM_GRADLE_HOME) { $GradleHome = $env:FARMSTORM_GRADLE_HOME }
$Jdk = Join-Path $ToolchainRoot 'jdk21\bin\java.exe'
if ($env:FARMSTORM_JDK) { $Jdk = $env:FARMSTORM_JDK }

$McVersion = '1.20.1'
if ($env:FARMSTORM_MC) { $McVersion = $env:FARMSTORM_MC }
$NeoForgeVersion = '1.20.1-47.1.106'
if ($env:FARMSTORM_NEOFORGE) { $NeoForgeVersion = $env:FARMSTORM_NEOFORGE }

$MappingFile = Join-Path $WorkDir 'official-to-srg.tsrg'
$ReportFile  = Join-Path $WorkDir 'mapping-report.txt'
$BuilderSrc  = Join-Path $PSScriptRoot 'SrgMappingBuilder.java'

function Fail([string]$msg, [int]$code) {
    Write-Host "[remap] FATAL: $msg" -ForegroundColor Red
    exit $code
}
function FirstFile([string[]]$paths) {
    foreach ($p in $paths) { if ($p -ne '' -and (Test-Path -LiteralPath $p)) { return $p } }
    return $null
}
function FirstMatch([string]$dir, [string]$pattern, [string]$leaf) {
    if (-not (Test-Path -LiteralPath $dir)) { return $null }
    $hits = @(Get-ChildItem -Path $dir -Recurse -File -Filter $leaf -ErrorAction SilentlyContinue |
              Where-Object { $_.FullName -match $pattern })
    if ($hits.Count -eq 0) { return $null }
    return $hits[0].FullName
}
function Get-ZipEntryBytes([string]$jarPath, [string]$entry) {
    $zip = [System.IO.Compression.ZipFile]::OpenRead($jarPath)
    try {
        $e = $zip.Entries | Where-Object { $_.FullName -eq $entry } | Select-Object -First 1
        if ($e -eq $null) { return $null }
        $ms = New-Object System.IO.MemoryStream
        $s = $e.Open(); $s.CopyTo($ms); $s.Close()
        return $ms.ToArray()
    } finally { $zip.Dispose() }
}

# ---------------------------------------------------------------- inputs
if (-not (Test-Path -LiteralPath $InputJar))   { Fail "input jar not found: $InputJar" 2 }
if (-not (Test-Path -LiteralPath $Jdk))        { Fail "jdk not found: $Jdk" 2 }
if (-not (Test-Path -LiteralPath $BuilderSrc)) { Fail "missing $BuilderSrc" 2 }
if (-not (Test-Path -LiteralPath $WorkDir))    { New-Item -ItemType Directory -Force -Path $WorkDir | Out-Null }

$ArtJar = FirstMatch (Join-Path $GradleHome 'caches\modules-2\files-2.1\net.neoforged\AutoRenamingTool') 'AutoRenamingTool.*-all\.jar$' 'AutoRenamingTool-*-all.jar'
if ($ArtJar -eq $null) { Fail "AutoRenamingTool-*-all.jar not found under $GradleHome (run a gradle build once)" 3 }

$PgClient = FirstFile @((Join-Path $Root ".gradle\caches\minecraft\versions\$McVersion\client.txt"), (Join-Path $Root "probe\.gradle\caches\minecraft\versions\$McVersion\client.txt"))
$PgServer = FirstFile @((Join-Path $Root ".gradle\caches\minecraft\versions\$McVersion\server.txt"), (Join-Path $Root "probe\.gradle\caches\minecraft\versions\$McVersion\server.txt"))
if ($PgClient -eq $null) { Fail "Mojang proguard mappings (client.txt) not found - build once so NeoForm downloads them" 3 }
if ($PgServer -eq $null) { Fail "Mojang proguard mappings (server.txt) not found" 3 }

$NeoFormDir = Join-Path $Root 'build\neoForm'
$StepsDir = $null
if (Test-Path -LiteralPath $NeoFormDir) {
    $d = @(Get-ChildItem -Path $NeoFormDir -Directory | Where-Object { $_.Name -like "neoFormJoined$McVersion-*" } | Sort-Object Name -Descending)
    if ($d.Count -gt 0) { $StepsDir = $d[0].FullName }
}
if ($StepsDir -eq $null) { Fail "NeoForm step output not found under $NeoFormDir - run a gradle build first" 3 }

$Tsrg = Join-Path $StepsDir 'steps\mergeMappings\output.tsrg'
$McSrgJar = FirstFile @((Join-Path $StepsDir 'steps\rename\output.jar'), (Join-Path $StepsDir 'steps\packRecomp\output.jar'))
$McLibJar = FirstFile @((Join-Path $StepsDir 'steps\packRecomp\output.jar'), (Join-Path $StepsDir 'steps\rename\output.jar'))
if (-not (Test-Path -LiteralPath $Tsrg)) { Fail "tsrg not found: $Tsrg" 3 }
if ($McSrgJar -eq $null) { Fail "no minecraft jar found under $StepsDir\steps" 3 }

$NeoForgeJar = FirstMatch (Join-Path $GradleHome 'caches\modules-2\files-2.1\net.neoforged\forge') "forge-$NeoForgeVersion-universal\.jar$" "forge-$NeoForgeVersion-universal.jar"
if ($NeoForgeJar -eq $null) {
    $NeoForgeJar = FirstMatch (Join-Path $GradleHome 'caches\modules-2\files-2.1\net.neoforged\neoforge') 'neoforge.*-universal\.jar$' 'neoforge-*-universal.jar'
}

Write-Host "[remap] jdk        = $Jdk"
Write-Host "[remap] art        = $ArtJar"
Write-Host "[remap] input      = $InputJar"
Write-Host "[remap] output     = $OutputJar"
Write-Host "[remap] proguard   = $PgClient"
Write-Host "[remap] tsrg       = $Tsrg"
Write-Host "[remap] mc srg jar = $McSrgJar"
Write-Host "[remap] mc lib jar = $McLibJar"
Write-Host "[remap] neoforge   = $NeoForgeJar"

# ---------------------------------------------------------------- 1. mapping
function New-SrgMapping([string]$checkJar) {
    $argv = @($BuilderSrc,
              '--proguard-client', $PgClient,
              '--proguard-server', $PgServer,
              '--tsrg', $Tsrg,
              '--mc-jar', $McSrgJar,
              '--mod-jar', $InputJar,
              '--out', $MappingFile,
              '--report', $ReportFile)
    if ($NeoForgeJar -ne $null) { $argv += @('--extra-jar', $NeoForgeJar) }
    if ($checkJar -ne '' -and (Test-Path -LiteralPath $checkJar)) { $argv += @('--check-jar', $checkJar) }
    & $Jdk @argv
    if ($LASTEXITCODE -ne 0) { Fail "SrgMappingBuilder failed (exit $LASTEXITCODE)" 4 }
}

if ($Force -or -not (Test-Path -LiteralPath $MappingFile)) {
    Write-Host '[remap] building official -> SRG mapping ...'
    New-SrgMapping ''
    if (-not (Test-Path -LiteralPath $MappingFile)) { Fail 'mapping was not written' 4 }
}
else {
    Write-Host "[remap] reusing mapping $MappingFile (use -Force to rebuild)"
}

# ---------------------------------------------------------------- 2. remap
Write-Host '[remap] running AutoRenamingTool ...'
$artArgs = @('-jar', $ArtJar, '--input', $InputJar, '--output', $OutputJar, '--map', $MappingFile, '--lib', $McLibJar)
if ($NeoForgeJar -ne $null) { $artArgs += @('--lib', $NeoForgeJar) }
& $Jdk @artArgs
$artExit = $LASTEXITCODE
if ($artExit -ne 0) { Fail "AutoRenamingTool failed (exit $artExit)" 5 }
if (-not (Test-Path -LiteralPath $OutputJar)) { Fail "remapped jar was not written: $OutputJar" 5 }
Write-Host "[remap] ART exit code: $artExit"

# ---------------------------------------------------------------- 3. verify
if (-not $SkipVerify) {
    Write-Host '[remap] verifying ...'
    New-SrgMapping $OutputJar

    function Test-Class([string]$jarPath, [string]$entry, [string]$label) {
        $b = Get-ZipEntryBytes $jarPath $entry
        if ($b -eq $null) { Fail "class not found in $jarPath : $entry" 6 }
        $t = [System.Text.Encoding]::GetEncoding(28591).GetString($b)
        $srg = @([regex]::Matches($t, 'm_[0-9]+_') | ForEach-Object { $_.Value } | Sort-Object -Unique)
        $old = @([regex]::Matches($t, 'fromEnum|getSerializedName') | ForEach-Object { $_.Value } | Sort-Object -Unique)
        Write-Host ("[remap] {0}.class: {1} bytes, SRG member names = {2} [{3}], leftover official = [{4}]" -f $label, $b.Length, $srg.Count, ($srg -join ','), ($old -join ','))
        return ,$srg
    }

    $srgNames = Test-Class $OutputJar 'com/dashpig/farmstorm/util/CropCategory.class' 'CropCategory'
    if ($srgNames.Count -eq 0) { Fail 'CropCategory.class contains no SRG names - remap did not work' 7 }
    if ($srgNames -notcontains 'm_216439_') { Fail 'CropCategory.class does not call StringRepresentable.fromEnum as m_216439_' 7 }
    if ($srgNames -notcontains 'm_7912_')   { Fail 'CropCategory.class does not declare getSerializedName as m_7912_' 7 }
    Test-Class $OutputJar 'com/dashpig/farmstorm/util/CropTier.class' 'CropTier' | Out-Null

    function Show-Mapping([string]$class, [string]$name) {
        $cur = ''
        $found = 0
        foreach ($line in (Get-Content -LiteralPath $MappingFile)) {
            if ($line -notmatch '^\s') { $cur = ($line -split ' ')[0]; continue }
            if ($cur -ne $class) { continue }
            $body = $line.Trim()
            if ($body.StartsWith($name + ' ') -or $body -eq $name) {
                Write-Host ("[remap] spot check: {0} {1}" -f $class, $body)
                $found++
            }
        }
        if ($found -eq 0) { Write-Host ("[remap] spot check: {0}.{1} -> (not in mapping)" -f $class, $name) }
    }
    $spots = @(@('net/minecraft/util/StringRepresentable','fromEnum'), @('net/minecraft/world/item/ItemStack','shrink'), @('net/minecraft/world/level/block/state/BlockState','setValue'))
    foreach ($pair in $spots) { Show-Mapping $pair[0] $pair[1] }

    $checkLine = @(Get-Content -LiteralPath $ReportFile | Where-Object { $_ -match 'members still official but mapped to something else' })
    if ($checkLine.Count -eq 0) { Fail 'jar check missing from the report' 8 }
    Write-Host ("[remap] {0}" -f $checkLine[0].Trim())
    if ($checkLine[0] -notmatch ':\s+0\s*$') { Fail 'remapped jar still contains official member names (see report)' 8 }

    Write-Host ("[remap] output jar: {0} ({1} bytes)" -f $OutputJar, (Get-Item -LiteralPath $OutputJar).Length)
}

if ($DistJar -ne '') {
    $distDir = Split-Path -Parent $DistJar
    if (-not (Test-Path -LiteralPath $distDir)) { New-Item -ItemType Directory -Force -Path $distDir | Out-Null }
    Copy-Item -LiteralPath $OutputJar -Destination $DistJar -Force
    Write-Host ("[remap] copied to {0} ({1} bytes)" -f $DistJar, (Get-Item -LiteralPath $DistJar).Length)
}

Write-Host '[remap] done'
exit 0
