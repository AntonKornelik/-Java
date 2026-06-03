$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$src = Join-Path $root "src"
$out = Join-Path $root "out"

if (-not (Test-Path $out)) {
    New-Item -ItemType Directory -Path $out | Out-Null
}

$sources = Get-ChildItem -Path $src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d $out $sources
$port = if ($args.Count -gt 0) { $args[0] } else { "8090" }

$listeners = netstat -ano | Select-String ":$port " | ForEach-Object {
    $parts = ($_ -replace '^\s+', '') -split '\s+'
    if ($parts.Count -ge 5 -and $parts[3] -eq 'LISTENING') { $parts[4] }
} | Sort-Object -Unique

foreach ($processId in $listeners) {
    if ($processId -and $processId -ne "0") {
        Stop-Process -Id ([int]$processId) -Force -ErrorAction SilentlyContinue
    }
}

java -cp $out ru.renthome.RentHomeApplication $port
