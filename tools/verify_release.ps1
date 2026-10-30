# Release consistency check: jar / install notes / release body / tag target must agree.
# Usage:
#   powershell.exe -NoProfile -ExecutionPolicy Bypass -File tools\verify_release.ps1 -Tag v0.0.3
# Any mismatch => prints FAIL and exits 1.
#
# CAUTION: this file is ASCII on purpose. PowerShell 5.1 decodes a BOM-less UTF-8
# script using the ANSI code page, which silently drops characters and can make an
# array literal lose an element. The repo's editor tooling also strips a BOM on a
# rewrite, so a BOM is not a reliable guard either. Keep this file pure ASCII.
param(
    [string]$Tag = "v0.0.3",
    [string]$Jar = "dist\farmstorm-0.0.3+mc1.21.1.jar",
    [string]$InstallNotes = "dist\install-notes.txt",
    [string]$Repo = "redstonepigawa/farmstorm"
)

$ErrorActionPreference = "Stop"
$fail = 0
function Ok($msg)   { Write-Host ("  PASS  " + $msg) -ForegroundColor Green }
function Bad($msg)  { Write-Host ("  FAIL  " + $msg) -ForegroundColor Red; $script:fail++ }
function Sub12($s)  { if ([string]::IsNullOrWhiteSpace($s)) { return "?" } return $s.Substring(0, [Math]::Min(12, $s.Length)) }
function Sub7($s)   { if ([string]::IsNullOrWhiteSpace($s)) { return "?" } return $s.Substring(0, [Math]::Min(7, $s.Length)) }

Set-Location (Split-Path $PSScriptRoot -Parent)
$gh = "C:\Program Files\GitHub CLI\gh.exe"

# the word for "update" built from code points, so the source stays ASCII
$updWord = [string]([char]0x66F4) + [string]([char]0x65B0)

Write-Host "=== 1) tag target equals current HEAD ==="
$remoteTagSha = (& $gh api "repos/$Repo/git/refs/tags/$Tag" 2>$null | ConvertFrom-Json).object.sha
$localTagSha  = (git rev-parse "$Tag^{commit}" 2>$null | Out-String).Trim()
$headSha      = (git rev-parse HEAD).Trim()
if (-not $remoteTagSha) { Bad "remote tag $Tag not found" }
elseif ($remoteTagSha -ne $localTagSha) { Bad "remote tag($(Sub7 $remoteTagSha)) != local tag($(Sub7 $localTagSha))" }
else { Ok "remote tag == local tag == $(Sub7 $localTagSha)" }
if ($localTagSha -ne $headSha) {
    Bad "tag($(Sub7 $localTagSha)) is behind HEAD($(Sub7 $headSha)): GitHub's auto Source code would be a stale snapshot"
} else { Ok "tag == HEAD, source archive matches the repo" }

Write-Host "=== 2) release asset: jar hash ==="
$assets = (& $gh release view $Tag --repo $Repo --json assets 2>$null | ConvertFrom-Json).assets
$jarAsset = $assets | Where-Object { $_.name -like "*.jar" }
if (-not $jarAsset) { Bad "release has no jar asset" }
else {
    $localJar = (Get-FileHash $Jar -Algorithm SHA256).Hash.ToLower()
    $remoteJar = $jarAsset.digest -replace "sha256:", ""
    if ($localJar -ne $remoteJar) { Bad "jar mismatch: local $(Sub12 $localJar) vs remote $(Sub12 $remoteJar)" }
    else { Ok "jar matches ($($jarAsset.size) bytes, sha $(Sub12 $localJar))" }
}

Write-Host "=== 3) release asset: install notes hash ==="
$noteAsset = $assets | Where-Object { $_.name -eq "install-notes.txt" }
if (-not $noteAsset) { Bad "release has no install-notes.txt" }
else {
    $localNote = (Get-FileHash $InstallNotes -Algorithm SHA256).Hash.ToLower()
    $remoteNote = $noteAsset.digest -replace "sha256:", ""
    if ($localNote -ne $remoteNote) { Bad "install notes mismatch: local $(Sub12 $localNote) vs remote $(Sub12 $remoteNote)" }
    else { Ok "install notes match ($($noteAsset.size) bytes)" }
}

Write-Host "=== 4) release body mentions this tag's changes ==="
$body = (& $gh release view $Tag --repo $Repo --json body 2>$null | ConvertFrom-Json).body
if (-not $body -or $body.Length -lt 200) { Bad "release body is empty or too short" }
else {
    if ($body -match [regex]::Escape($updWord) -or $body -match [regex]::Escape($Tag)) {
        Ok "body is non-empty and mentions the update section or the tag ($($body.Length) chars)"
    } else { Bad "body mentions neither the update section nor the tag; it may be stale" }
}

Write-Host ""
if ($fail -gt 0) { Write-Host "RESULT: $fail mismatch(es) -- do not release" -ForegroundColor Red; exit 1 }
Write-Host "RESULT: all four checks agree -- safe to release" -ForegroundColor Green
exit 0
