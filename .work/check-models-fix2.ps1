# Farmstorm FIX-2: 解引用生成物里的 parent / 贴图 / blockstate 模型引用（farmstorm 与 minecraft 两侧都查）。
$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem

$root   = "D:\project_farm"
$gen    = Join-Path $root "src\generated\resources\assets\farmstorm"
$assets = Join-Path $root "src\main\resources\assets\farmstorm"
$jar    = Join-Path $root ".tools\gradle-home\caches\neoformruntime\artifacts\minecraft_1.21.1_client.jar"

$zip = [System.IO.Compression.ZipFile]::OpenRead($jar)
function Test-VanillaModel([string]$path) { return ($null -ne $zip.GetEntry("assets/minecraft/models/$path.json")) }
function Test-VanillaTexture([string]$path) { return ($null -ne $zip.GetEntry("assets/minecraft/textures/$path.png")) }
function Test-ModTexture([string]$path) {
    $png = Join-Path $assets ("textures\" + ($path -replace "/", "\") + ".png")
    return (Test-Path -LiteralPath $png)
}

$counts = [ordered]@{}
foreach ($pair in @(@("blockstates", "blockstates"), @("models\block", "models/block"), @("models\item", "models/item"))) {
    $n = (Get-ChildItem -Path (Join-Path $gen $pair[0]) -Filter *.json).Count
    $counts[$pair[1]] = $n
}

$dangling = New-Object System.Collections.ArrayList
$checked  = 0
$modelsDir = Join-Path $gen "models"

# 1) 模型 parent
foreach ($m in (Get-ChildItem -Path $modelsDir -Recurse -Filter *.json)) {
    $rel = $m.FullName.Substring($modelsDir.Length + 1)
    $j = Get-Content -Raw -LiteralPath $m.FullName | ConvertFrom-Json
    if ($null -eq $j.parent) { continue }
    $checked++
    $p = [string]$j.parent
    if ($p.Contains(":")) { $parts = $p.Split(":"); $ns = $parts[0]; $path = $parts[1] } else { $ns = "minecraft"; $path = $p }
    if ($ns -eq "farmstorm") {
        $target = (Join-Path $modelsDir ($path -replace "/", "\")) + ".json"
        if (-not (Test-Path -LiteralPath $target)) { [void]$dangling.Add("PARENT $rel -> $p") }
    } elseif ($ns -eq "minecraft") {
        if (-not (Test-VanillaModel $path)) { [void]$dangling.Add("PARENT $rel -> $p") }
    } else { [void]$dangling.Add("PARENT-NS $rel -> $p") }
}

# 2) 模型贴图（#变量 引用跳过）
$texChecked = 0
foreach ($m in (Get-ChildItem -Path $modelsDir -Recurse -Filter *.json)) {
    $rel = $m.FullName.Substring($modelsDir.Length + 1)
    $j = Get-Content -Raw -LiteralPath $m.FullName | ConvertFrom-Json
    if ($null -eq $j.textures) { continue }
    foreach ($prop in $j.textures.PSObject.Properties) {
        $v = [string]$prop.Value
        if ($v.StartsWith("#")) { continue }
        $texChecked++
        if ($v.Contains(":")) { $parts = $v.Split(":"); $ns = $parts[0]; $path = $parts[1] } else { $ns = "minecraft"; $path = $v }
        if ($ns -eq "farmstorm") { if (-not (Test-ModTexture $path)) { [void]$dangling.Add("TEXTURE $rel $($prop.Name)=$v") } }
        elseif ($ns -eq "minecraft") { if (-not (Test-VanillaTexture $path)) { [void]$dangling.Add("TEXTURE $rel $($prop.Name)=$v") } }
        else { [void]$dangling.Add("TEXTURE-NS $rel $($prop.Name)=$v") }
    }
}

# 3) blockstate 指向的模型
$bsChecked = 0
foreach ($b in (Get-ChildItem -Path (Join-Path $gen "blockstates") -Filter *.json)) {
    $j = Get-Content -Raw -LiteralPath $b.FullName | ConvertFrom-Json
    foreach ($variant in $j.variants.PSObject.Properties) {
        $bsChecked++
        $model = [string]$variant.Value.model
        $parts = $model.Split(":"); $ns = $parts[0]; $path = $parts[1]
        if ($ns -eq "farmstorm") {
            $target = (Join-Path $modelsDir ($path -replace "/", "\")) + ".json"
            if (-not (Test-Path -LiteralPath $target)) { [void]$dangling.Add("BLOCKSTATE $($b.Name) -> $model") }
        } else { [void]$dangling.Add("BLOCKSTATE-NS $($b.Name) -> $model") }
    }
}
$zip.Dispose()

foreach ($k in $counts.Keys) { "{0} = {1}" -f $k, $counts[$k] }
"model parent refs examined = $checked"
"texture refs examined = $texChecked"
"blockstate variants examined = $bsChecked"
"dangling refs = $($dangling.Count)"
$dangling | ForEach-Object { "  DANGLING: $_" }
if ($dangling.Count -eq 0) { "RESULT: CLEAN" } else { "RESULT: BROKEN" }
