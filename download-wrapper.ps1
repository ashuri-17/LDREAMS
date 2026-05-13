$wrapperUrl = "https://services.gradle.org/distributions/gradle-8.9-bin.zip"
$jarUrl = "https://github.com/gradle/gradle/raw/v8.9.0/gradle/wrapper/gradle-wrapper.jar"
$jarPath = Join-Path $PSScriptRoot "gradle\wrapper\gradle-wrapper.jar"

try {
    Invoke-WebRequest -Uri $jarUrl -OutFile $jarPath -ErrorAction Stop
    Write-Host "Downloaded gradle-wrapper.jar"
} catch {
    Write-Host "Direct JAR download failed, trying alternative..."
    # Alternative: download the gradle zip and extract wrapper jar
    $zipPath = Join-Path $env:TEMP "gradle-8.9-bin.zip"
    Invoke-WebRequest -Uri $wrapperUrl -OutFile $zipPath -ErrorAction Stop
    $tempExtract = Join-Path $env:TEMP "gradle-extract"
    Expand-Archive -Path $zipPath -DestinationPath $tempExtract -Force
    Copy-Item "$tempExtract\gradle-8.9\lib\gradle-wrapper-*.jar" $jarPath
    Write-Host "Extracted gradle-wrapper.jar"
}

# Also check for Android SDK
$possibleSdkPaths = @(
    "$env:LOCALAPPDATA\Android\Sdk",
    "$env:USERPROFILE\AppData\Local\Android\Sdk",
    "C:\Android\Sdk"
)

$found = $false
foreach ($path in $possibleSdkPaths) {
    if (Test-Path $path) {
        Write-Host "Android SDK found at: $path"
        Set-Content -Path (Join-Path $PSScriptRoot "local.properties") -Value "sdk.dir=$path"
        $found = $true
        break
    }
}

if (-not $found) {
    Write-Host "Android SDK not found at common paths"
    Write-Host "Setting ANDROID_HOME to environment if available"
    if ($env:ANDROID_HOME) {
        Set-Content -Path (Join-Path $PSScriptRoot "local.properties") -Value "sdk.dir=$env:ANDROID_HOME"
    } elseif ($env:ANDROID_SDK_ROOT) {
        Set-Content -Path (Join-Path $PSScriptRoot "local.properties") -Value "sdk.dir=$env:ANDROID_SDK_ROOT"
    }
}

Write-Host "Done"
