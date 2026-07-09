$baseDir = $PSScriptRoot
if (-not $baseDir) { $baseDir = Get-Location }

$libDir = "$baseDir\lib"
$srcDir = "$baseDir\src"
$binDir = "$baseDir\bin"

# 1. Create target binary folder
New-Item -ItemType Directory -Force -Path $binDir | Out-Null

# 2. Build classpath from the lib directory
$jarFiles = Get-ChildItem -Path $libDir -Filter *.jar
$jarPaths = $jarFiles | ForEach-Object { $_.FullName }
$classpath = ($jarPaths -join ";") + ";$binDir"

Write-Host "--- Classpath built successfully ---"
Write-Host "Classpath: $classpath" -ForegroundColor Gray

# 3. Gather all java source files
$javaSources = Get-ChildItem -Path $srcDir -Filter *.java -Recurse | ForEach-Object { $_.FullName }

Write-Host "--- Compiling Java source files... ---"
javac -d $binDir -cp $classpath -encoding UTF-8 $javaSources

if ($LASTEXITCODE -eq 0) {
    Write-Host "--- Compilation successful! ---" -ForegroundColor Green
    
    # 4. Copy image resources to binary output folder
    $imgSrc = "$srcDir\com\hackathon\manager\ui\login_banner.png"
    $imgDestDir = "$binDir\com\hackathon\manager\ui"
    New-Item -ItemType Directory -Force -Path $imgDestDir | Out-Null
    
    if (Test-Path $imgSrc) {
        Copy-Item -Path $imgSrc -Destination "$imgDestDir\login_banner.png" -Force
        Write-Host "Resources copied to bin directory." -ForegroundColor Green
    } else {
        Write-Warning "Resource login_banner.png not found at $imgSrc"
    }

    # 5. Execute application
    Write-Host "--- Launching Hackathon Management System... ---" -ForegroundColor Cyan
    java -cp $classpath com.hackathon.manager.Main
} else {
    Write-Error "Compilation failed with exit code $LASTEXITCODE"
}
