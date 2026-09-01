. (Join-Path $PSScriptRoot 'local-env.ps1')

$env:DB_URL = "jdbc:postgresql://localhost:5432/$env:POSTGRES_DB"
$env:DB_USER = $env:POSTGRES_USER
$env:DB_PASSWORD = $env:POSTGRES_PASSWORD

Set-Location (Join-Path $script:ProjectRoot 'backend')
Write-Host 'Backend iniciando em http://localhost:8081'
mvn spring-boot:run
