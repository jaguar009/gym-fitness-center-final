param([ValidatePattern('^[A-Za-z0-9_]+$')][string]$InstanceName = 'SQLEXPRESS')
$ErrorActionPreference = 'Stop'
$sqlCommand = Get-Command sqlcmd.exe -ErrorAction SilentlyContinue
$sqlExecutable = if ($sqlCommand) { $sqlCommand.Source } else {
    'C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\170\Tools\Binn\SQLCMD.EXE'
}
if (-not (Test-Path -LiteralPath $sqlExecutable)) { throw 'Instale sqlcmd o agréguelo al PATH.' }
$mavenCommand = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if (-not $mavenCommand) { throw 'Agregue Maven 3.9 al PATH antes de ejecutar las pruebas.' }
$databaseName = 'gymfitnessdb_it_' + [guid]::NewGuid().ToString('N')
if ($databaseName -notmatch '^gymfitnessdb_it_[a-f0-9]{32}$') { throw 'Nombre de base de pruebas no válido.' }
$instance = '.\' + $InstanceName
$temporarySql = New-TemporaryFile
$previousUrl = $env:GYM_SQL_IT_URL
$creationStarted = $false
$testExit = 1
try {
    & $sqlExecutable -S $instance -E -C -d master -b -Q "IF DB_ID(N'$databaseName') IS NOT NULL THROW 51001, 'La base ya existe; no reutilizar.', 1;"
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo reservar una base de pruebas nueva.' }
    $sourceSql = [IO.File]::ReadAllText((Join-Path $PSScriptRoot 'sql/gymfitnessdb.sql'))
    $isolatedSql = [regex]::Replace($sourceSql, '\bgymfitnessdb\b', $databaseName)
    if ($isolatedSql -match '\bgymfitnessdb\b') { throw 'El script conserva una referencia a la base de trabajo.' }
    [IO.File]::WriteAllText($temporarySql.FullName, $isolatedSql, [Text.UTF8Encoding]::new($true))
    $creationStarted = $true
    & $sqlExecutable -S $instance -E -C -d master -b -i $temporarySql.FullName
    if ($LASTEXITCODE -ne 0) { throw 'No se completó el esquema de pruebas.' }
    $env:GYM_SQL_IT_URL = "jdbc:sqlserver://localhost;instanceName=$InstanceName;databaseName=$databaseName;integratedSecurity=true;authenticationScheme=NativeAuthentication;encrypt=true;trustServerCertificate=true;loginTimeout=5;queryTimeout=15;socketTimeout=20000;"
    Push-Location -LiteralPath $PSScriptRoot
    try {
        & $mavenCommand.Source -B clean verify -Psqlserver-it
        $testExit = $LASTEXITCODE
    } finally { Pop-Location }
} finally {
    $env:GYM_SQL_IT_URL = $previousUrl
    if ($creationStarted -and $databaseName -match '^gymfitnessdb_it_[a-f0-9]{32}$') {
        & $sqlExecutable -S $instance -E -C -d master -b -Q "IF DB_ID(N'$databaseName') IS NOT NULL BEGIN ALTER DATABASE [$databaseName] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [$databaseName]; END;"
        if ($LASTEXITCODE -ne 0) { Write-Warning "Revise la eliminación de la base temporal $databaseName" }
    }
    Remove-Item -LiteralPath $temporarySql.FullName -ErrorAction SilentlyContinue
}
exit $testExit
