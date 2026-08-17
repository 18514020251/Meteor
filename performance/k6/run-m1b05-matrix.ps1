param(
    [Parameter(Mandatory=$true)][string]$Token,
    [Parameter(Mandatory=$true)][string]$ScreeningId,
    [string]$BaseUrl = "http://127.0.0.1:8085",
    [string]$Duration = "30s",
    [int[]]$Vus = @(50, 100, 150, 200)
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$results = Join-Path $root "results"
New-Item -ItemType Directory -Force -Path $results | Out-Null

$index = 0
foreach ($vu in $Vus) {
    $runId = "m1b05-vu${vu}-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    $out = Join-Path $results "$runId.json"
    Write-Host "=== $runId ==="
    k6 run `
      --summary-export "$out" `
      -e "TOKEN=$Token" `
      -e "SCREENING_ID=$ScreeningId" `
      -e "BASE_URL=$BaseUrl" `
      -e "VUS=$vu" `
      -e "DURATION=$Duration" `
      -e "RUN_ID=$runId" `
      (Join-Path $root "grab-m1b05-baseline.js")

    Write-Host "Result: $out"
    $index++
    if ($index -lt $Vus.Count) {
        $ScreeningId = Read-Host "Prepare a fresh high-stock screening, then enter its SCREENING_ID"
    }
}
