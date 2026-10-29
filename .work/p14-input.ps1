# Lane B / Phase 14 client evidence helper (ASCII only: Windows PowerShell reads UTF-8 without BOM as ANSI)
# Load with:  $src = Get-Content -Raw 'D:\project_farm\.work\p14-input.ps1'; Invoke-Expression $src

Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.Windows.Forms

if (-not ('P14Win' -as [type])) {
Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;
using System.Text;

public struct RECT { public int Left; public int Top; public int Right; public int Bottom; }

public static class P14Win {
    [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr hWnd, out RECT rect);
    [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr hWnd);
    [DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr hWnd, int cmd);
    [DllImport("user32.dll")] public static extern void keybd_event(byte bVk, byte bScan, uint dwFlags, UIntPtr dwExtraInfo);
    [DllImport("user32.dll")] public static extern void mouse_event(uint dwFlags, int dx, int dy, int dwData, UIntPtr dwExtraInfo);
    [DllImport("user32.dll")] public static extern bool SetCursorPos(int x, int y);
    [DllImport("user32.dll")] public static extern bool EnumWindows(EnumWindowsProc lpEnumFunc, IntPtr lParam);
    [DllImport("user32.dll")] public static extern int GetWindowTextLength(IntPtr hWnd);
    [DllImport("user32.dll")] public static extern int GetWindowText(IntPtr hWnd, StringBuilder text, int count);
    [DllImport("user32.dll")] public static extern bool IsWindowVisible(IntPtr hWnd);
    public delegate bool EnumWindowsProc(IntPtr hWnd, IntPtr lParam);

    public static string TitleOf(IntPtr hWnd) {
        int len = GetWindowTextLength(hWnd);
        if (len <= 0) return "";
        StringBuilder sb = new StringBuilder(len + 1);
        GetWindowText(hWnd, sb, sb.Capacity);
        return sb.ToString();
    }
}
'@
}

function Get-McWindow {
    param([string]$Title = 'Minecraft|Multiplayer')
    $script:found = [IntPtr]::Zero
    $script:foundTitle = ''
    $cb = [P14Win+EnumWindowsProc]{
        param($hWnd, $lParam)
        if ([P14Win]::IsWindowVisible($hWnd)) {
            $t = [P14Win]::TitleOf($hWnd)
            if ($t -and ($t -match $Title)) {
                $script:found = $hWnd
                $script:foundTitle = $t
                return $false
            }
        }
        return $true
    }
    [P14Win]::EnumWindows($cb, [IntPtr]::Zero) | Out-Null
    if ($script:found -eq [IntPtr]::Zero) { throw "no window matching '$Title'" }
    return @{ Handle = $script:found; Title = $script:foundTitle }
}

function Get-McWindowList {
    $script:all = @()
    $cb = [P14Win+EnumWindowsProc]{
        param($hWnd, $lParam)
        if ([P14Win]::IsWindowVisible($hWnd)) {
            $t = [P14Win]::TitleOf($hWnd)
            if ($t) { $script:all += $t }
        }
        return $true
    }
    [P14Win]::EnumWindows($cb, [IntPtr]::Zero) | Out-Null
    return $script:all
}

function Capture-Window {
    param([string]$Title = 'Minecraft|Multiplayer', [string]$Out = 'D:/project_farm/.work/shot.png')
    $win = Get-McWindow -Title $Title
    $rect = New-Object RECT
    [P14Win]::GetWindowRect($win.Handle, [ref]$rect) | Out-Null
    $w = $rect.Right - $rect.Left
    $h = $rect.Bottom - $rect.Top
    $bmp = New-Object System.Drawing.Bitmap $w, $h
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.CopyFromScreen($rect.Left, $rect.Top, 0, 0, (New-Object System.Drawing.Size $w, $h))
    $g.Dispose()
    $bmp.Save($Out, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Output ("captured [{0}] {1}x{2} -> {3}" -f $win.Title, $w, $h, $Out)
}

function Focus-Mc {
    param([string]$Title = 'Minecraft|Multiplayer')
    $win = Get-McWindow -Title $Title
    [P14Win]::ShowWindow($win.Handle, 9) | Out-Null
    [P14Win]::SetForegroundWindow($win.Handle) | Out-Null
    Start-Sleep -Milliseconds 250
    return $win
}

function Hold-Key {
    param([int]$Vk, [int]$Ms = 500, [string]$Title = 'Minecraft|Multiplayer')
    Focus-Mc -Title $Title | Out-Null
    [P14Win]::keybd_event([byte]$Vk, 0, 0, [UIntPtr]::Zero)
    Start-Sleep -Milliseconds $Ms
    [P14Win]::keybd_event([byte]$Vk, 0, 2, [UIntPtr]::Zero)
    Write-Output ("held key 0x{0:X2} for {1} ms" -f $Vk, $Ms)
}

function Press-Key {
    param([int]$Vk, [int]$Ms = 80, [string]$Title = 'Minecraft|Multiplayer')
    Focus-Mc -Title $Title | Out-Null
    [P14Win]::keybd_event([byte]$Vk, 0, 0, [UIntPtr]::Zero)
    Start-Sleep -Milliseconds $Ms
    [P14Win]::keybd_event([byte]$Vk, 0, 2, [UIntPtr]::Zero)
    Write-Output ("pressed key 0x{0:X2}" -f $Vk)
}

function Click-Mouse {
    param([ValidateSet('left', 'right')][string]$Button = 'left', [int]$HoldMs = 90, [string]$Title = 'Minecraft|Multiplayer')
    $win = Focus-Mc -Title $Title
    $rect = New-Object RECT
    [P14Win]::GetWindowRect($win.Handle, [ref]$rect) | Out-Null
    [P14Win]::SetCursorPos([int](($rect.Left + $rect.Right) / 2), [int](($rect.Top + $rect.Bottom) / 2)) | Out-Null
    Start-Sleep -Milliseconds 80
    if ($Button -eq 'left') {
        [P14Win]::mouse_event(0x0002, 0, 0, 0, [UIntPtr]::Zero)
        Start-Sleep -Milliseconds $HoldMs
        [P14Win]::mouse_event(0x0004, 0, 0, 0, [UIntPtr]::Zero)
    } else {
        [P14Win]::mouse_event(0x0008, 0, 0, 0, [UIntPtr]::Zero)
        Start-Sleep -Milliseconds $HoldMs
        [P14Win]::mouse_event(0x0010, 0, 0, 0, [UIntPtr]::Zero)
    }
    Write-Output ("clicked {0} (hold {1} ms)" -f $Button, $HoldMs)
}

function Wheel-Mouse {
    param([int]$Delta = 120, [string]$Title = 'Minecraft|Multiplayer')
    Focus-Mc -Title $Title | Out-Null
    [P14Win]::mouse_event(0x0800, 0, 0, $Delta, [UIntPtr]::Zero)
    Write-Output ("wheel delta {0}" -f $Delta)
}


function Capture-Screen {
    param([string]$Out = 'D:/project_farm/.work/shot-screen.png')
    Add-Type -AssemblyName System.Windows.Forms
    $b = [System.Windows.Forms.Screen]::PrimaryScreen.Bounds
    $bmp = New-Object System.Drawing.Bitmap $b.Width, $b.Height
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.CopyFromScreen($b.X, $b.Y, 0, 0, (New-Object System.Drawing.Size $b.Width, $b.Height))
    $g.Dispose()
    $bmp.Save($Out, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Output ("screen {0}x{1} -> {2}" -f $b.Width, $b.Height, $Out)
}

function Maximize-Window {
    param([string]$Title = 'Minecraft|Multiplayer')
    $win = Get-McWindow -Title $Title
    [P14Win]::ShowWindow($win.Handle, 3) | Out-Null    # SW_MAXIMIZE
    [P14Win]::SetForegroundWindow($win.Handle) | Out-Null
    Start-Sleep -Milliseconds 700
    Write-Output ("maximized [{0}]" -f $win.Title)
}
