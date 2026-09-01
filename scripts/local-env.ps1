$ErrorActionPreference = 'Stop'

$script:ProjectRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $script:ProjectRoot '.env'

if (-not (Test-Path -LiteralPath $envFile)) {
    throw "Arquivo .env não encontrado. Copie .env.example para .env e configure as variáveis."
}

Get-Content -LiteralPath $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith('#')) {
        $parts = $line.Split('=', 2)
        if ($parts.Count -eq 2) {
            [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), 'Process')
        }
    }
}

if (-not $env:POSTGRES_DB -or -not $env:POSTGRES_USER -or -not $env:POSTGRES_PASSWORD) {
    throw 'POSTGRES_DB, POSTGRES_USER e POSTGRES_PASSWORD são obrigatórios no .env.'
}
