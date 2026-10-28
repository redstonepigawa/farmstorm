#Requires -Version 5.1
<#
.SYNOPSIS
    Farmstorm build-baseline verifier.

.DESCRIPTION
    Resolves every item that docs/zh/02-TECH-SPEC.md section 10.2 currently marks as
    "subject to what is actually pulled at build time", then prints a Markdown table
    that can be pasted back into section 10.2 of both the zh and en tech spec.

    Checks:
      1. Highest NeoForge 21.1.x on the NeoForged Maven repository.
      2. Highest ModDevGradle 2.0.x on the Gradle Plugin Portal.
      3. Explosion#finalizeExplosion visibility  - the key hook of the explosion system.
      4. FoodProperties$Builder members          - Power Beet and Lucky Berry food stats.
      5. PotionContents constructors             - Toxic Ubi Server effect cloud.
      6. Ingredient CODEC field                  - every recipe JSON.
      7. build.gradle extension block spelling   - neoForge (correct) vs neoforge (wrong).

    Checks 3-6 need a JDK (javap) and a project that has resolved dependencies once.
    When either is missing the script reports it and continues. Every check is isolated,
    so one failure never stops the rest.

.PARAMETER ProjectRoot
    Repository root. Defaults to the parent of this script (tools/..).

.PARAMETER ListCount
    How many recent versions to show per dependency. Default 5.

.PARAMETER SkipJavap
    Skip checks 3-6. Useful without a JDK.

.PARAMETER OutFile
    Optional path. When given, the Markdown table is also written there.

.EXAMPLE
    pwsh -File tools/verify_api.ps1

.EXAMPLE
    pwsh -File tools/verify_api.ps1 -SkipJavap -OutFile docs/version-check.md
#>
[CmdletBinding()]
param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot),
    [int]$ListCount = 5,
    [switch]$SkipJavap,
    [string]$OutFile
)

$ErrorActionPreference = 'Stop'
try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12 } catch { }

$results = New-Object System.Collections.Generic.List[object]

function AddR {
    param([string]$Item, [string]$Result, [string]$Source, [string]$Confidence)
    $results.Add([pscustomobject]@{ Item = $Item; Result = $Result; Source = $Source; Confidence = $Confidence })
    Write-Host ('[' + $Confidence + '] ' + $Item) -ForegroundColor Cyan
    Write-Host ('       ' + $Result)
    Write-Host ''
}

function Get-MavenVersions {
    param([string]$Url)
    $resp = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 25
    $xml = [string]$resp.Content
    $ms = [regex]::Matches($xml, '<version>([^<]+)</version>')
    $list = New-Object System.Collections.Generic.List[string]
    foreach ($m in $ms) { $list.Add($m.Groups[1].Value) }
    return $list
}

function Get-VersionKey {
    param([string]$Version, [string]$Prefix)
    if (-not $Version.StartsWith($Prefix)) { return -1 }
    $rest = $Version.Substring($Prefix.Length)
    $digits = ($rest -split '[^0-9]')[0]
    $n = 0
    if ([int]::TryParse($digits, [ref]$n)) { return $n }
    return -1
}

Write-Host ''
Write-Host '== 1. NeoForge 21.1.x ==' -ForegroundColor Yellow
$neoLatest = $null
try {
    $all = Get-MavenVersions 'https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml'
    $v211 = @($all | Where-Object { $_ -like '21.1.*' })
    $sorted = @($v211 | Sort-Object { Get-VersionKey $_ '21.1.' })
    $neoLatest = ($sorted | Select-Object -Last 1)
    $tail = @($sorted | Select-Object -Last $ListCount)
    AddR 'neo_version (NeoForge)' ('highest published 21.1.x = ' + $neoLatest + '; recent: ' + (@($tail | Sort-Object -Descending) -join ', ')) 'maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml' 'high'
} catch {
    AddR 'neo_version (NeoForge)' ('FAILED - ' + $_.Exception.Message) 'network' 'unknown'
}

Write-Host '== 2. ModDevGradle 2.0.x ==' -ForegroundColor Yellow
$mdgLatest = $null
try {
    $mdgAll = Get-MavenVersions 'https://plugins.gradle.org/m2/net/neoforged/moddev/net.neoforged.moddev.gradle.plugin/maven-metadata.xml'
    $mdg2 = @($mdgAll | Where-Object { $_ -like '2.*' })
    $mdgSorted = @($mdg2 | Sort-Object { Get-VersionKey $_ '2.' })
    $mdgLatest = ($mdgSorted | Select-Object -Last 1)
    $mdgTail = @($mdgSorted | Select-Object -Last $ListCount)
    AddR 'ModDevGradle plugin version' ('highest published 2.x = ' + $mdgLatest + '; recent: ' + (@($mdgTail | Sort-Object -Descending) -join ', ')) 'plugins.gradle.org/m2/net/neoforged/moddev' 'high'
} catch {
    AddR 'ModDevGradle plugin version' ('FAILED - ' + $_.Exception.Message + ' ; browse https://plugins.gradle.org/plugin/net.neoforged.moddev') 'network' 'unknown'
}

Write-Host '== 7. build.gradle extension block ==' -ForegroundColor Yellow
$gradleFile = $null
foreach ($candidate in @('build.gradle', 'build.gradle.kts')) {
    $p = Join-Path $ProjectRoot $candidate
    if (Test-Path $p) { $gradleFile = $p; break }
}
if ($gradleFile) {
    $text = Get-Content -Raw -LiteralPath $gradleFile
    $good = [regex]::IsMatch($text, '\bneoForge\s*\{')
    $bad = [regex]::IsMatch($text, '\bneoforge\s*\{')
    if ($bad) {
        AddR 'Extension block spelling' 'WRONG: neoforge { } found. The DSL is case sensitive; use neoForge { }' $gradleFile 'high'
    } elseif ($good) {
        AddR 'Extension block spelling' 'correct: neoForge { } found, no neoforge { }' $gradleFile 'high'
    } else {
        AddR 'Extension block spelling' 'neither neoForge { } nor neoforge { } found; file may still be a template' $gradleFile 'low'
    }
} else {
    AddR 'Extension block spelling' 'skipped: no build.gradle in project root yet' $ProjectRoot 'n/a'
}

if ($SkipJavap) {
    AddR 'API signatures (javap)' 'skipped by -SkipJavap' '-' 'n/a'
} else {
    Write-Host '== 3-6. API signatures ==' -ForegroundColor Yellow
    $javap = $null
    $cmd = Get-Command javap -ErrorAction SilentlyContinue
    if ($cmd) {
        $javap = $cmd.Source
    } elseif ($env:JAVA_HOME) {
        $p = Join-Path $env:JAVA_HOME 'bin\javap.exe'
        if (Test-Path $p) { $javap = $p }
    }

    $jars = @()
    $searchDirs = @()
    if ($ProjectRoot) {
        $searchDirs += (Join-Path $ProjectRoot 'build\moddev\artifacts')
        $searchDirs += (Join-Path $ProjectRoot '.gradle')
    }
    $neoCache = Join-Path $env:USERPROFILE '.gradle\caches\modules-2\files-2.1\net.neoforged'
    if (Test-Path $neoCache) { $searchDirs += $neoCache }
    foreach ($d in $searchDirs) {
        if (Test-Path $d) {
            $jars += @(Get-ChildItem -LiteralPath $d -Recurse -Filter '*.jar' -File -ErrorAction SilentlyContinue |
                Where-Object { $_.Name -notmatch '(-sources|-javadoc|-changelog)\.jar$' } |
                Select-Object -ExpandProperty FullName)
        }
    }
    $jars = @($jars | Select-Object -Unique | Select-Object -First 40)

    if (-not $javap) {
        AddR 'API signatures (javap)' 'skipped: javap not found. A JDK is required; a JRE is not enough' '-' 'n/a'
    } elseif ($jars.Count -eq 0) {
        AddR 'API signatures (javap)' 'skipped: no resolved NeoForge jars found. Run gradlew build once first' '-' 'n/a'
    } else {
        $cp = ($jars -join ';')

        function Invoke-Javap {
            param([string]$Class, [string]$Pattern)
            $raw = & $javap -p -classpath $cp $Class 2>&1
            if ($LASTEXITCODE -ne 0) { return $null }
            $hits = @($raw | Where-Object { $_ -match $Pattern })
            return $hits
        }

        $r = Invoke-Javap 'net.minecraft.world.level.Explosion' 'finalizeExplosion'
        if ($null -eq $r) {
            AddR 'Explosion#finalizeExplosion' 'could not load net.minecraft.world.level.Explosion from the jars found' 'javap' 'unknown'
        } elseif ($r.Count -eq 0) {
            AddR 'Explosion#finalizeExplosion' 'NOT FOUND: the hook is gone or renamed. Use the fallback design: do not extend Explosion, write a standalone executor' 'javap' 'high'
        } else {
            AddR 'Explosion#finalizeExplosion' ($r -join ' | ') 'javap' 'high'
        }

        $r = Invoke-Javap 'net.minecraft.world.food.FoodProperties$Builder' 'saturation|alwaysEdible|nutrition'
        if ($null -eq $r) {
            AddR 'FoodProperties$Builder' 'could not load the class' 'javap' 'unknown'
        } elseif ($r.Count -eq 0) {
            AddR 'FoodProperties$Builder' 'no saturation / nutrition / alwaysEdible members matched; inspect manually' 'javap' 'low'
        } else {
            AddR 'FoodProperties$Builder' ($r -join ' | ') 'javap' 'high'
        }

        $r = Invoke-Javap 'net.minecraft.world.item.alchemy.PotionContents' 'PotionContents\('
        if ($null -eq $r) {
            AddR 'PotionContents constructors' 'could not load the class' 'javap' 'unknown'
        } elseif ($r.Count -eq 0) {
            AddR 'PotionContents constructors' 'record style constructor not found; check the static factory methods' 'javap' 'low'
        } else {
            AddR 'PotionContents constructors' ($r -join ' | ') 'javap' 'high'
        }

        $r = Invoke-Javap 'net.minecraft.world.item.crafting.Ingredient' 'CODEC'
        if ($null -eq $r) {
            AddR 'Ingredient CODEC' 'could not load the class' 'javap' 'unknown'
        } elseif ($r.Count -eq 0) {
            AddR 'Ingredient CODEC' 'no CODEC member; the recipe codec API changed, inspect IngredientCodecs' 'javap' 'low'
        } else {
            AddR 'Ingredient CODEC' ($r -join ' | ') 'javap' 'high'
        }
    }
}

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add('| Item | Result | Source / command | Confidence |')
$lines.Add('|---|---|---|---|')
foreach ($x in $results) {
    $safe = ($x.Result -replace '\|', '/' -replace '\r?\n', ' ')
    $src = ($x.Source -replace '\|', '/' -replace '\r?\n', ' ')
    $lines.Add(('| {0} | {1} | {2} | {3} |' -f $x.Item, $safe, $src, $x.Confidence))
}
$md = ($lines -join [Environment]::NewLine)

Write-Host ''
Write-Host '=== Markdown for 02-TECH-SPEC section 10.2 ===' -ForegroundColor Green
Write-Host $md
Write-Host ''

if ($OutFile) {
    $parent = Split-Path -Parent $OutFile
    if ($parent -and -not (Test-Path $parent)) { New-Item -ItemType Directory -Path $parent -Force | Out-Null }
    Set-Content -LiteralPath $OutFile -Value $md -Encoding UTF8
    Write-Host ('Written to ' + $OutFile) -ForegroundColor Green
}

if ($neoLatest) {
    Write-Host ('Suggested gradle.properties: neo_version=' + $neoLatest) -ForegroundColor Green
}
Write-Host 'Done. Paste the table into docs/zh/02-TECH-SPEC.md and docs/en/02-TECH-SPEC.md section 10.2.'
exit 0
