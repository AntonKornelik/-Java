$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Write-Host "Building RentHome..." -ForegroundColor Cyan
$sources = Get-ChildItem -Path .\src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d .\out $sources

$port = 8090
$listeners = netstat -ano | Select-String ":$port " | ForEach-Object {
    $parts = ($_ -replace '^\s+', '') -split '\s+'
    if ($parts.Count -ge 5 -and $parts[3] -eq 'LISTENING') { $parts[4] }
} | Sort-Object -Unique

foreach ($processId in $listeners) {
    if ($processId -and $processId -ne "0") {
        Stop-Process -Id ([int]$processId) -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "RentHome is starting on http://localhost:8090" -ForegroundColor Green
Write-Host "Do not close this window while using the site." -ForegroundColor Yellow
java -cp .\out ru.renthome.RentHomeApplication 8090
