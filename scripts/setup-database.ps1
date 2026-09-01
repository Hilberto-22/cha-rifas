. (Join-Path $PSScriptRoot 'local-env.ps1')

$psqlCandidates = @(
    'C:\Program Files\PostgreSQL\17\bin\psql.exe',
    'C:\Program Files\PostgreSQL\16\bin\psql.exe',
    'C:\Program Files\PostgreSQL\15\bin\psql.exe'
)
$psql = $psqlCandidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
if (-not $psql) {
    $command = Get-Command psql -ErrorAction SilentlyContinue
    if ($command) { $psql = $command.Source }
}
if (-not $psql) { throw 'psql não foi encontrado. Instale o PostgreSQL ou adicione sua pasta bin ao PATH.' }

$env:PGPASSWORD = $env:POSTGRES_PASSWORD
if ($env:POSTGRES_DB -notmatch '^[A-Za-z0-9_]+$') {
    throw 'POSTGRES_DB pode conter apenas letras, números e sublinhado.'
}
$databaseExists = & $psql -h localhost -U $env:POSTGRES_USER -d postgres -tAc "select 1 from pg_database where datname = '$($env:POSTGRES_DB.Replace("'", "''"))'"
if ($LASTEXITCODE -ne 0) { throw 'Não foi possível conectar ao PostgreSQL com as credenciais do .env.' }

if (-not $databaseExists) {
    & $psql -h localhost -U $env:POSTGRES_USER -d postgres -c "create database `"$env:POSTGRES_DB`""
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível criar o banco de dados.' }
    Write-Host "Banco '$env:POSTGRES_DB' criado."
} else {
    Write-Host "Banco '$env:POSTGRES_DB' já existe."
}
