param([int]$Port = 8080)
$ErrorActionPreference = 'Stop'
$projectPath = $PSScriptRoot
$jarPath = Join-Path $projectPath 'target/gym-fitness-center-1.0.0.jar'
$nativePath = Join-Path $projectPath 'native/windows-x64'
if (-not (Test-Path -LiteralPath $jarPath)) {
    throw 'Primero compila el proyecto: mvn package -DskipTests'
}
$javaCommand = Get-Command java -ErrorAction SilentlyContinue
if (-not $javaCommand) { throw 'Instala o selecciona un JDK 21 o posterior y agrega java al PATH.' }
& $javaCommand.Source "-Djava.library.path=$nativePath" -jar $jarPath '--spring.profiles.active=local-windows' "--server.port=$Port"
exit $LASTEXITCODE
