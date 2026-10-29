# tools/convert_media.ps1 -- v0.0.2 audio + effect-icon pipeline (D-53 / D-66 / RT-14)
#
# Converts the three out-of-repository source assets into what a Minecraft jar can actually load:
#   Animals_from_3m04s.mp3 -> assets/farmstorm/sounds/wild_hunter_loop.ogg     (mono, 64 kbps Ogg Vorbis)
#     (作者于 2026-10-29 指定改用 3 分 04 秒起的那段剪辑，47.14 s；旧的整体版 Animals.mp3 不再作为源)
#   SF6 clip .mp3          -> assets/farmstorm/sounds/zangief_piledriver.ogg   (mono, 64 kbps Ogg Vorbis)
#   effect icon .jpg       -> assets/farmstorm/textures/mob_effect/wild_hunter.png (18x18 PNG, centre-cropped)
#
# Contracts enforced here (do not weaken them):
#   * never downloads anything
#   * never renames an mp3 to .ogg, never substitutes a silent placeholder
#   * missing ffmpeg / missing libvorbis / missing source = exit 1, nothing written
#   * a failed conversion = exit 2, and the remaining files are not touched
#   * re-running is a no-op when every output is newer than its source (use -Force to rebuild)
#     ride along inside the .ogg and Minecraft only expects one vorbis stream)
#
# NOTE: the .ogg files are NOT byte-reproducible. The Ogg muxer writes a random stream serial
# number on every run, so two encodings of the same input differ in md5 even though the audio
# is identical (-fflags +bitexact does not change this). Never use the md5 of an .ogg as an
# acceptance criterion; compare duration, channels, bitrate or speech instead.
#
# Usage:
#   pwsh -File tools/convert_media.ps1 -CheckOnly
#   pwsh -File tools/convert_media.ps1 -Ffmpeg "D:\dsh\default\sf6_audio\tools\ffmpeg.exe"
#   pwsh -File tools/convert_media.ps1 -OutRoot ".work/media-probe"

[CmdletBinding()]
param(
    [string] $Ffmpeg,
    [switch] $CheckOnly,
    [switch] $Force,
    [string] $OutRoot = "src/main/resources",
    [string] $Animals = "D:\dsh\default\Animals_from_3m04s.mp3",
    [string] $Sfx     = "D:\dsh\default\sf6_audio\out\BV1qu411W7ha_846-850.mp3",
    [string] $Icon    = "C:\Users\DASHpig\Desktop\e91bb6997d8e24cda1f596267c41da97500924726.jpg"
)

$ErrorActionPreference = "Stop"

$Plan = @(
    @{ Tag = "audio"; Src = $Animals; Rel = "assets/farmstorm/sounds/wild_hunter_loop.ogg" },
    @{ Tag = "audio"; Src = $Sfx;     Rel = "assets/farmstorm/sounds/zangief_piledriver.ogg" },
    @{ Tag = "icon";  Src = $Icon;    Rel = "assets/farmstorm/textures/mob_effect/wild_hunter.png" }
)

function Resolve-Ffmpeg {
    param([string] $Explicit)
    if ($Explicit) {
        if (-not (Test-Path -LiteralPath $Explicit)) { throw "ffmpeg not found at the path given by -Ffmpeg: $Explicit" }
        return (Resolve-Path -LiteralPath $Explicit).Path
    }
    $onPath = Get-Command ffmpeg -ErrorAction SilentlyContinue
    if ($onPath) { return $onPath.Source }
    $candidates = @(
        (Join-Path $PSScriptRoot "ffmpeg\bin\ffmpeg.exe"),
        (Join-Path (Split-Path $PSScriptRoot -Parent) ".tools\ffmpeg\bin\ffmpeg.exe")
    )
    foreach ($c in $candidates) { if (Test-Path -LiteralPath $c) { return $c } }
    return $null
}

function Invoke-Captured {
    # ffmpeg writes its normal progress to stderr; with ErrorActionPreference=Stop that
    # would throw a NativeCommandError and abort the run. Capture it as text instead.
    param([string] $Exe, [string[]] $Arguments)
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $out = & $Exe @Arguments 2>&1 | ForEach-Object { $_.ToString() }
    $code = $LASTEXITCODE
    $ErrorActionPreference = $prev
    return @{ Output = $out; Code = $code }
}

function Get-FfmpegProblem {
    param([string] $Exe)
    $r = Invoke-Captured -Exe $Exe -Arguments @("-hide_banner", "-encoders")
    if ($r.Code -ne 0) { return "ffmpeg refused to list its encoders (exit " + $r.Code + ")" }
    $enc = $r.Output
    $hit = $enc | Select-String -Pattern "libvorbis" -SimpleMatch
    if (-not $hit) { return "this ffmpeg has no libvorbis encoder (trimmed build); run: ffmpeg -encoders | Select-String vorbis" }
    return $null
}

Write-Host "== farmstorm v0.0.2 media pipeline ==" -ForegroundColor Cyan

$exe = $null
try { $exe = Resolve-Ffmpeg -Explicit $Ffmpeg } catch { Write-Host ("FAIL  " + $_.Exception.Message) -ForegroundColor Red; exit 1 }

if (-not $exe) {
    Write-Host "FAIL  no ffmpeg found." -ForegroundColor Red
    Write-Host "      Looked at: -Ffmpeg argument, then PATH, then tools\ffmpeg\bin, then .tools\ffmpeg\bin."
    Write-Host ""
    Write-Host "      Install one of these ways and re-run:"
    Write-Host "        winget install Gyan.FFmpeg"
    Write-Host "        or unzip an ffmpeg build into tools\ffmpeg\bin\ffmpeg.exe"
    Write-Host "        or pass an absolute path:  pwsh -File tools\convert_media.ps1 -Ffmpeg <path to ffmpeg.exe>"
    Write-Host ""
    Write-Host "      The build MUST carry libvorbis. Trae CN ships a trimmed 6.1.1 ffmpeg without it."
    exit 1
}

Write-Host ("  ffmpeg : " + $exe)
$ver = (Invoke-Captured -Exe $exe -Arguments @("-hide_banner", "-version")).Output | Select-Object -First 1
Write-Host ("  version: " + $ver)

$problem = Get-FfmpegProblem -Exe $exe
if ($problem) { Write-Host ("FAIL  " + $problem) -ForegroundColor Red; exit 1 }
Write-Host "  vorbis : libvorbis encoder present"

$missing = @()
foreach ($p in $Plan) { if (-not (Test-Path -LiteralPath $p.Src)) { $missing += $p.Src } }
if ($missing.Count -gt 0) {
    Write-Host "FAIL  source asset(s) missing:" -ForegroundColor Red
    foreach ($m in $missing) { Write-Host ("        " + $m) }
    Write-Host "      These live outside the repository; nothing was written."
    exit 1
}
Write-Host "  sources: all 3 present"

if ($CheckOnly) {
    Write-Host ""
    Write-Host "CheckOnly: environment is usable, no file written." -ForegroundColor Green
    exit 0
}

$rows = @()
$written = 0
$skipped = 0
foreach ($p in $Plan) {
    $dst = Join-Path $OutRoot $p.Rel
    $dir = Split-Path -Parent $dst
    if (-not (Test-Path -LiteralPath $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }

    if (-not $Force -and (Test-Path -LiteralPath $dst)) {
        $srcTime = (Get-Item -LiteralPath $p.Src).LastWriteTimeUtc
        $dstTime = (Get-Item -LiteralPath $dst).LastWriteTimeUtc
        if ($dstTime -ge $srcTime) {
            Write-Host ("  skip    " + $p.Rel + " (output is newer than source; -Force to rebuild)") -ForegroundColor DarkGray
            $rows += [pscustomobject]@{ File = $p.Rel; "Size (bytes)" = (Get-Item -LiteralPath $dst).Length; "Source (bytes)" = (Get-Item -LiteralPath $p.Src).Length }
            $skipped++
            continue
        }
    }

    Write-Host ("  convert " + (Split-Path -Leaf $p.Src) + " -> " + $p.Rel)
    if ($p.Tag -eq "audio") {
        $ffArgs = @("-hide_banner", "-y", "-vn", "-i", $p.Src, "-ac", "1", "-c:a", "libvorbis", "-b:a", "64k", $dst)
    } else {
        $ffArgs = @("-hide_banner", "-y", "-i", $p.Src, "-vf", "crop=ih:ih,scale=18:18:flags=lanczos", "-pix_fmt", "rgba", $dst)
    }
    $r = Invoke-Captured -Exe $exe -Arguments $ffArgs
    if ($r.Code -ne 0 -or -not (Test-Path -LiteralPath $dst)) {
        Write-Host ("FAIL  ffmpeg returned " + $r.Code + " for " + $p.Src) -ForegroundColor Red
        $r.Output | ForEach-Object { Write-Host ("        " + $_) }
        exit 2
    }
    $rows += [pscustomobject]@{
        File = $p.Rel
        "Size (bytes)" = (Get-Item -LiteralPath $dst).Length
        "Source (bytes)" = (Get-Item -LiteralPath $p.Src).Length
    }
    $written++
}

Write-Host ""
$rows | Format-Table -AutoSize | Out-String | Write-Host
Write-Host ("Done. " + $written + " written, " + $skipped + " skipped, under " + $OutRoot) -ForegroundColor Green
exit 0