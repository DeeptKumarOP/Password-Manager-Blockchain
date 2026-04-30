$cp = ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-nop.jar"

if (-Not (Test-Path "sqlite-jdbc.jar") -Or -Not (Test-Path "slf4j-api.jar")) {
    Write-Host "Warning: Required JAR files not found. Please run .\build.ps1 first." -ForegroundColor Yellow
    exit
}

java -cp $cp Main
