# 发布一致性校验：jar / 安装说明 / release 正文 / tag 指向，四处必须对得上。
# 用法：
#   powershell.exe -NoProfile -ExecutionPolicy Bypass -File tools\verify_release.ps1 -Tag v0.0.2
# 任一项不一致 => 打印 FAIL 并以 exit 1 结束。
param(
    [string]$Tag = "v0.0.2",
    [string]$Jar = "dist\farmstorm-0.0.2+mc1.21.1.jar",
    [string]$InstallNotes = "dist\安装说明.txt",
    [string]$Repo = "redstonepigawa/farmstorm"
)

$ErrorActionPreference = "Stop"
$fail = 0
function Ok($msg)   { Write-Host ("  PASS  " + $msg) -ForegroundColor Green }
# 安全截断：取不到值时返回 "?"，避免把整条 git 命令拼进报错信息里
function Sub7($s)   { if ([string]::IsNullOrWhiteSpace($s)) { return "?" } return $s.Substring(0, [Math]::Min(7, $s.Length)) }
function Bad($msg)  { Write-Host ("  FAIL  " + $msg) -ForegroundColor Red; $script:fail++ }

Set-Location (Split-Path $PSScriptRoot -Parent)
$gh = "C:\Program Files\GitHub CLI\gh.exe"

Write-Host "=== 1) tag 指向是否等于当前 HEAD ==="
$remoteTagSha = (& $gh api "repos/$Repo/git/refs/tags/$Tag" 2>$null | ConvertFrom-Json).object.sha
$localTagSha  = (git rev-parse "$Tag^{commit}" 2>$null | Out-String).Trim()
$headSha      = (git rev-parse HEAD).Trim()
if (-not $remoteTagSha) { Bad "远端取不到 tag $Tag" }
elseif ($remoteTagSha -ne $localTagSha) { Bad "远端 tag($(Sub7 $remoteTagSha)) != 本地 tag($(Sub7 $localTagSha))" }
else { Ok "远端 tag == 本地 tag == $(Sub7 $localTagSha)" }
if ($localTagSha -ne $headSha) {
    Bad "tag($(Sub7 $localTagSha)) 落后于 HEAD($(Sub7 $headSha))：GitHub 自动生成的 Source code 会是旧快照"
} else { Ok "tag == HEAD，源码包与仓库一致" }

Write-Host "=== 2) release 资产：jar 哈希 ==="
$assets = (& $gh release view $Tag --repo $Repo --json assets 2>$null | ConvertFrom-Json).assets
$jarAsset = $assets | Where-Object { $_.name -like "*.jar" }
if (-not $jarAsset) { Bad "release 里没有 jar 资产" }
else {
    $localJar = (Get-FileHash $Jar -Algorithm SHA256).Hash.ToLower()
    $remoteJar = $jarAsset.digest -replace "sha256:", ""
    $remoteJarShort = if ($remoteJar) { $remoteJar.Substring(0, [Math]::Min(12, $remoteJar.Length)) } else { "取不到" }
    if ($localJar -ne $remoteJar) { Bad "jar 不一致：本地 $($localJar.Substring(0,12)) vs 远端 $remoteJarShort" }
    else { Ok "jar 一致（$($jarAsset.size) 字节，sha $($localJar.Substring(0,12))）" }
}

Write-Host "=== 3) release 资产：安装说明哈希 ==="
$noteAsset = $assets | Where-Object { $_.name -eq "install-notes.txt" }
if (-not $noteAsset) { Bad "release 里没有 install-notes.txt" }
else {
    $localNote = (Get-FileHash $InstallNotes -Algorithm SHA256).Hash.ToLower()
    $remoteNote = $noteAsset.digest -replace "sha256:", ""
    if ($localNote -ne $remoteNote) { Bad "安装说明不一致：本地 $($localNote.Substring(0,12)) vs 远端 $($remoteNote.Substring(0,12))" }
    else { Ok "安装说明一致（$($noteAsset.size) 字节）" }
}

Write-Host "=== 4) release 正文是否提到本 tag 的最新改动 ==="
$body = (& $gh release view $Tag --repo $Repo --json body 2>$null | ConvertFrom-Json).body
if (-not $body -or $body.Length -lt 200) { Bad "release 正文为空或过短" }
else {
    # 正文里必须出现「更新」小节或本 tag 版本号，防止正文停在旧内容
    if ($body -match "更新" -or $body -match [regex]::Escape($Tag)) { Ok "正文非空且含版本/更新说明（$($body.Length) 字符）" }
    else { Bad "正文里既没有「更新」也没有 tag 号，可能停在旧内容" }
}

Write-Host ""
if ($fail -gt 0) { Write-Host "结果：$fail 项不一致 —— 不要发布" -ForegroundColor Red; exit 1 }
Write-Host "结果：四处一致，可以发布" -ForegroundColor Green
exit 0
