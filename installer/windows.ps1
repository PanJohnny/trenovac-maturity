# --- Configuration ---
$InstallDir = "$env:APPDATA\trenovac-maturity"
$Repo = "PanJohnny/trenovac-maturity"
$IconUrl = "https://raw.githubusercontent.com/PanJohnny/trenovac-maturity/master/src/main/resources/me/panjohnny/trenovacmaturity/icon.png"

# --- 1. Helper: Check & Install Java 25 ---
function Ensure-Java {
    Write-Host "Checking for Java 25..." -ForegroundColor Cyan
    $javaPath = Get-Command java -ErrorAction SilentlyContinue

    $needsInstall = $false
    if (-not $javaPath) {
        $needsInstall = $true
    } else {
        $versionInfo = & java -version 2>&1 | Out-String
        if ($versionInfo -match '"(\d+)') {
            $majorVersion = [int]$matches[1]
            if ($majorVersion -lt 25) { $needsInstall = $true }
        }
    }

    if ($needsInstall) {
        Write-Host "Java 25 or higher is required." -ForegroundColor Yellow
        $choice = Read-Host "Would you like me to install it for you using Windows Package Manager? (y/n)"
        if ($choice -eq 'y') {
            Write-Host "Installing Microsoft OpenJDK 25... This may require Admin permission." -ForegroundColor Cyan
            # Using Winget to install Java 25
            winget install Microsoft.OpenJDK.25 --accept-source-agreements --accept-package-agreements
            if ($LASTEXITCODE -ne 0) {
                Write-Host "Auto-install failed. Please install Java 25 manually: https://adoptium.net/" -ForegroundColor Red
                pause
                exit
            }
        } else {
            Write-Host "Please install Java 25 manually to continue." -ForegroundColor Red
            pause
            exit
        }
    }
}

# --- 2. Fetch Latest Release ---
Ensure-Java

Write-Host "Fetching latest release from GitHub..." -ForegroundColor Cyan
$ApiUrl = "https://api.github.com/repos/$Repo/releases/latest"
try {
    $Response = Invoke-RestMethod -Uri $ApiUrl -Headers @{"Accept"="application/vnd.github+json"}
} catch {
    Write-Host "Failed to connect to GitHub." -ForegroundColor Red
    pause
    exit
}

$TagName = $Response.tag_name
$Asset = $Response.assets | Where-Object { $_.name -like "*.jar" } | Select-Object -First 1

if (-not $Asset) {
    Write-Host "Could not find a .jar file in the latest release ($TagName)." -ForegroundColor Red
    pause
    exit
}

$DownloadUrl = $Asset.browser_download_url
$FileName = $Asset.name

# --- 3. Install Logic ---
if (-not (Test-Path $InstallDir)) { New-Item -ItemType Directory -Path $InstallDir | Out-Null }
Set-Location $InstallDir

if (Test-Path $FileName) {
    Write-Host "You already have the latest version ($TagName)." -ForegroundColor Green
} else {
    Write-Host "New version found: $TagName. Downloading..." -ForegroundColor Cyan
    # Clean up old JARs
    Get-ChildItem -Path $InstallDir -Filter "*.jar" | Remove-Item -Force
    Invoke-WebRequest -Uri $DownloadUrl -OutFile $FileName
    Write-Host "Download complete." -ForegroundColor Green
}

# --- 4. Desktop Shortcut & Icon ---
$ShortcutChoice = Read-Host "Create a desktop shortcut? (y/n)"
if ($ShortcutChoice -eq 'y') {
    $IconPath = "$InstallDir\icon.ico"
    # Note: Windows prefers .ico files. If only .png is available, it will use a generic icon
    # unless we convert it. For simplicity, we download the PNG and hope for the best,
    # or point to the Java icon.
    Invoke-WebRequest -Uri $IconUrl -OutFile "$InstallDir\icon.png" -ErrorAction SilentlyContinue

    $WshShell = New-Object -ComObject WScript.Shell
    $Shortcut = $WshShell.CreateShortcut("$env:USERPROFILE\Desktop\Trénovač Maturity.lnk")
    $Shortcut.TargetPath = "java.exe"
    $Shortcut.Arguments = "-jar `"$InstallDir\$FileName`""
    $Shortcut.WorkingDirectory = $InstallDir
    $Shortcut.IconLocation = "$InstallDir\icon.png" # Windows can sometimes use PNGs for icons
    $Shortcut.Save()
    Write-Host "Shortcut created on Desktop." -ForegroundColor Green
}

# --- 5. Launch ---
$LaunchChoice = Read-Host "Launch the app now? (y/n)"
if ($LaunchChoice -eq 'y') {
    Start-Process java -ArgumentList "-jar `"$FileName`""
}

Write-Host "Installation finished!" -ForegroundColor Green
pause