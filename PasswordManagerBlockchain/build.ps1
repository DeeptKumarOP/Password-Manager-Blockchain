$jars = @(
    @{ Url = "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar"; File = "sqlite-jdbc.jar" },
    @{ Url = "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar"; File = "slf4j-api.jar" },
    @{ Url = "https://repo1.maven.org/maven2/org/slf4j/slf4j-nop/1.7.36/slf4j-nop-1.7.36.jar"; File = "slf4j-nop.jar" }
)

foreach ($jar in $jars) {
    if (-Not (Test-Path $jar.File)) {
        Write-Host "Downloading $($jar.File)..."
        Invoke-WebRequest -Uri $jar.Url -OutFile $jar.File
    }
}

Write-Host "Compiling Java files..."
$javaFiles = Get-ChildItem -Path . -Recurse -Filter *.java | Select-Object -ExpandProperty FullName

javac -cp ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-nop.jar" $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful! You can now run the app using .\run.ps1" -ForegroundColor Green
} else {
    Write-Host "Compilation failed." -ForegroundColor Red
}
