$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location (Join-Path $projectRoot 'frontend')
Write-Host 'Frontend iniciando em http://localhost:4200'
npm start
