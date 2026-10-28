# Farmstorm M5-1: verify that every texture reference in generated models resolves to a real PNG.
$ErrorActionPreference = "Stop"
$root   = "D:\project_farm"
$gen    = Join-Path $root "src\generated\resources\assets\farmstorm"
$assets = Join-Path $root "src\main\resources\assets"

$count = {
    param($dir, $label)
    $n = (Get-ChildItem -Path (Join-Path $gen $dir) -Filter *.json).Count
    "{0} = {1}" -f $label, $n
}
& $count "blockstates" "blockstates"
& $count "models\block" "models/block"
& $count "models\item"  "models/item"

$missingTex    = New-Object System.Collections.ArrayList
$missingParent = New-Object System.Collections.ArrayList
$foreign       = New-Object System.Collections.ArrayList
$texRefs = 0
$parentRefs = 0

$modelsDir = Join-Path $gen "models"
foreach ($m in (Get-ChildItem -Path $modelsDir -Recurse -Filter *.json)) {
    $rel = $m.FullName.Substring($modelsDir.Length + 1)
    $j = Get-Content -Raw -LiteralPath $m.FullName | ConvertFrom-Json
    if ($null -ne $j.parent -and ([string]$j.parent).StartsWith("farmstorm:")) {
        $parentRefs++
        $p = [string]$j.parent
        $target = (Join-Path $modelsDir ($p.Substring(10) -replace "/", "\")) + ".json"
        if (-not (Test-Path -LiteralPath $target)) { [void]$missingParent.Add("$rel -> $p") }
    }
    if ($null -ne $j.textures) {
        foreach ($prop in $j.textures.PSObject.Properties) {
            $v = [string]$prop.Value
            if ($v.StartsWith("#")) { continue }
            $texRefs++
            if ($v.Contains(":")) { $parts = $v.Split(":"); $ns = $parts[0]; $path = $parts[1] }
            else { $ns = "minecraft"; $path = $v }
            if ($ns -ne "farmstorm") { [void]$foreign.Add("$rel $($prop.Name)=$v"); continue }
            $png = (Join-Path (Join-Path $assets "farmstorm\textures") ($path -replace "/", "\")) + ".png"
            if (-not (Test-Path -LiteralPath $png)) { [void]$missingTex.Add("$rel $($prop.Name)=$v") }
        }
    }
}

"texture refs examined = $texRefs"
"farmstorm parent refs examined = $parentRefs"
"dangling texture refs = $($missingTex.Count)"
"dangling parent models = $($missingParent.Count)"
"non-farmstorm texture refs = $($foreign.Count)"
$missingTex    | ForEach-Object { "  MISSING-PNG: $_" }
$missingParent | ForEach-Object { "  MISSING-MODEL: $_" }
$foreign       | ForEach-Object { "  FOREIGN-NS: $_" }
if (($missingTex.Count + $missingParent.Count + $foreign.Count) -eq 0) { "RESULT: CLEAN" } else { "RESULT: BROKEN" }
