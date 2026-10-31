# tools/convert_media.ps1 -- v0.0.4 audio + effect-icon pipeline (D-53 / D-66 / RT-14)
#
# Converts the seven out-of-repository source assets into what a Minecraft jar can actually load:
#   (the 0.0.2 three are untouched; 0.0.3 only appends the later four, still one single script -- F-131 check 3)
#   Animals_from_3m04s.mp3 -> assets/farmstorm/sounds/wild_hunter_loop.ogg     (mono, 64 kbps Ogg Vorbis)
#     (the author picked the clip from 3:04 on 2026-10-29, 47.14 s; the old whole-track Animals.mp3 is no longer a source)
#   SF6 clip .mp3          -> assets/farmstorm/sounds/zangief_piledriver.ogg   (mono, 64 kbps Ogg Vorbis)
#   effect icon .jpg       -> assets/farmstorm/textures/mob_effect/wild_hunter.png (18x18 PNG, centre-cropped)
#   -- added in 0.0.3 --
#   pengyoudejiu.mp3       -> assets/farmstorm/sounds/friend_chives.ogg       (mono, 64 kbps Ogg Vorbis)
#     (the source file name is kept as-is, D-66; the event id is friend_chives, see sounds.json)
#     -- changed in 0.0.4 --
#     0.0.3 encoded the WHOLE 158.256 s track (1,242,039 B). The hit is a one-shot, so this
#     entry now carries Seconds = 10 and only the first 10 s of the source is kept (-t as an
#     output option: single pass, trim of the encode, not a second encode).
#   preview.jpg            -> assets/farmstorm/textures/mob_effect/q_bounce.png (18x18 PNG, centre-cropped)
#     (Steam Workshop 3796093604 preview image = the Q-bounce icon source, D-81; 18x18 is the D-53 icon size)
#   author item artwork x2 (1254x1254 RGBA; paths in plan0.0.3.txt)
#                          -> assets/farmstorm/textures/item/friend_chives.png (16x16 PNG)
#                          -> assets/farmstorm/textures/item/chive_seeds.png   (16x16 PNG)
#     (this is the "final artwork" of doc 17 section 4.7: pixel art on a transparent canvas whose
#      subject fills only part of the frame, so each entry carries its own square framing box -- see Vf)
#
# CAUTION: this file is ASCII on purpose. Windows PowerShell 5.1 decodes a BOM-less UTF-8 script with
# the local ANSI code page, and non-ASCII comments silently corrupt the parse: a CJK comment line
# inside the $Plan array literal dropped one plan entry (count 6 instead of 7, no error), and CJK
# comments directly above the icon branch made $dim come out empty (ffmpeg: "Cannot parse
# expression for width"). Both were found by bisection in F-130/F-131. Keep comments English.
#
# Contracts enforced here (do not weaken them):
#   * never downloads anything
#   * never renames an mp3 to .ogg, never substitutes a silent placeholder
#   * missing ffmpeg / missing libvorbis / missing source = exit 1, nothing written
#   * a failed conversion = exit 2, and the remaining files are not touched
#   * re-running is a no-op when every output is newer than its source (use -Force to rebuild)
#     ride along inside the .ogg and Minecraft only expects one vorbis stream)
#
# NOTE (corrected in 0.0.4): the .ogg files ARE byte-reproducible now. Without -fflags +bitexact
# the muxer writes a random Ogg stream serial number on every run, so two encodings of the same
# input differ in md5 even though the audio is identical. -serial_offset alone does NOT fix it
# (measured: still two different hashes); -fflags +bitexact on the OUTPUT file does (measured:
# two runs -> one SHA256). The audio branch passes it, so a rebuild can be diffed byte for
# byte. On another ffmpeg build without that flag the md5 will differ again -- then fall back
# to comparing duration / channels / bitrate.
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
    [string] $Icon    = "C:\Users\DASHpig\Desktop\e91bb6997d8e24cda1f596267c41da97500924726.jpg",
    [string] $QIcon   = "C:\Program Files (x86)\Steam\steamapps\workshop\content\431960\3796093604\preview.jpg",
    [string] $Chives  = "D:\dsh\default\pengyoudejiu.mp3",
    [string] $ChivesArt = "C:\Users\DASHpig\Documents\Tencent Files\3207667404\nt_qq\nt_data\Pic\2026-10\Thumb\525a9e860f990952781b0f6e52540c21_720.png",
    [string] $SeedsArt  = "C:\Users\DASHpig\Documents\Tencent Files\3207667404\nt_qq\nt_data\Pic\2026-10\Thumb\231896ac3a027c87bbb8f96f8b1a72ad_720.png"
)

$ErrorActionPreference = "Stop"

$Plan = @(
    @{ Tag = "audio"; Src = $Animals; Rel = "assets/farmstorm/sounds/wild_hunter_loop.ogg" },
    @{ Tag = "audio"; Src = $Sfx;     Rel = "assets/farmstorm/sounds/zangief_piledriver.ogg" },
    @{ Tag = "icon";  Src = $Icon;    Rel = "assets/farmstorm/textures/mob_effect/wild_hunter.png"; Size = 18 },
    # Seconds = 10 (0.0.4): 0.0.3 shipped the whole 158.256 s track here; the hit is a one-shot,
    # so only the first 10 s are kept. The key is optional -- entries without it encode in full.
    @{ Tag = "audio"; Src = $Chives;  Rel = "assets/farmstorm/sounds/friend_chives.ogg"; Seconds = 10 },
    @{ Tag = "icon";  Src = $QIcon;   Rel = "assets/farmstorm/textures/mob_effect/q_bounce.png";   Size = 18 },
    # Framing boxes of the two author-art entries = a square grown 6% from the alpha>=32 bbox
    # (side 1152 / 694, centred on the subject and clamped into the canvas); recompute with the
    # recipe in .work/f130_crop_calc.py. premultiply/unpremultiply stop lanczos from bleeding the
    # transparent area black into the edges (otherwise 16x16 gets a dirty dark fringe).
    # NOTE: keep the comment lines INSIDE this array literal ASCII. PowerShell 5.1 decodes a
    # BOM-less UTF-8 script with the ANSI code page, and a CJK comment line in here silently ate
    # one $Plan entry (F-130/F-131 bisection: count 6 instead of 7). CJK comments elsewhere in
    # this file are fine. The trailing lut zeroes the near-transparent lanczos ringing (alpha<8),
    # which at 16x16 only shows up as coloured speckle around the silhouette.
    @{ Tag = "icon";  Src = $ChivesArt; Rel = "assets/farmstorm/textures/item/friend_chives.png"; Size = 16; Vf = "crop=1152:1152:102:64,format=rgba,premultiply=inplace=1,scale=16:16:flags=lanczos,unpremultiply=inplace=1,lut=a='if(lt(val\,8)\,0\,val)'" },
    @{ Tag = "icon";  Src = $SeedsArt;  Rel = "assets/farmstorm/textures/item/chive_seeds.png"; Size = 16; Vf = "crop=694:694:300:271,format=rgba,premultiply=inplace=1,scale=16:16:flags=lanczos,unpremultiply=inplace=1,lut=a='if(lt(val\,8)\,0\,val)'" }
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

Write-Host "== farmstorm v0.0.4 media pipeline ==" -ForegroundColor Cyan

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
Write-Host ("  sources: all " + $Plan.Count + " present")

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
        $ffArgs = @("-hide_banner", "-y", "-vn", "-i", $p.Src, "-ac", "1", "-c:a", "libvorbis", "-b:a", "64k")
        # Optional clip: only the first $p.Seconds of the SOURCE is encoded. -t sits after -i, so it is
        # an output option and trims the encoded stream in the same pass (no temp file, no 2nd encode).
        if ($p.ContainsKey("Seconds")) { $ffArgs += @("-t", [string]$p.Seconds) }
        # Byte-reproducible output -- see the NOTE in the header. Without it the muxer picks a random
        # Ogg stream serial number on every run.
        $ffArgs += @("-fflags", "+bitexact", $dst)
    } else {
        # Two kinds of icon source:
        #   * a single image WITH a background (wild_hunter.jpg, the Steam Workshop preview.jpg):
        #     centre square crop + lanczos downscale. preview.jpg is a 1024x1024 workshop thumbnail
        #     that carries a background, so it is inevitably soft at 18x18 -- that is the source, not
        #     the encoding: if a clean Q-bounce icon turns up later, point -QIcon at it and re-run
        #     this same branch; no code change needed.
        #   * author artwork (pixel art on transparent): the entry carries its own Vf (a square
        #     framing box derived from the alpha bbox), then the downscale.
        # The trailing lut zeroes the near-transparent lanczos ringing (alpha<8), which at 16x16
        # otherwise shows up as colour speckle around the silhouette.
        $dim = if ($p.ContainsKey("Size")) { $p.Size } else { 18 }
        if ($p.ContainsKey("Vf")) {
            $vf = $p.Vf
        } else {
            $vf = "crop=ih:ih,scale=${dim}:${dim}:flags=lanczos"
        }
        $ffArgs = @("-hide_banner", "-y", "-i", $p.Src, "-vf", $vf, "-pix_fmt", "rgba", $dst)
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
