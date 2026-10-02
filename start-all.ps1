# 스프링(8080)과 Next.js(3000)를 함께 실행
#   브라우저 -> Next.js(:3000) 페이지/라우트 핸들러 -> 스프링(:8080) API
# 사전 준비:
#   1) ..\route-handlers-demo 에서 npm run deploy  (frontend 폴더 생성)
#   2) 이 폴더에서 .\gradlew.bat bootJar          (build\libs\*.jar 생성)
# 실행: powershell -ExecutionPolicy Bypass -File .\start-all.ps1
param(
    [int]$SpringPort = 8080,
    [int]$NextPort = 3000
)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$jar = Get-ChildItem build\libs\*.jar | Where-Object { $_.Name -notlike '*-plain.jar' } | Select-Object -First 1
if (-not $jar) { throw 'build\libs 에 실행 가능한 jar가 없습니다. 먼저 .\gradlew.bat bootJar 를 실행하세요.' }
if (-not (Test-Path frontend\server.js)) { throw 'frontend\server.js 가 없습니다. route-handlers-demo 에서 npm run deploy 를 실행하세요.' }

Write-Host "[spring] $($jar.Name) 시작 (port $SpringPort)"
$spring = Start-Process java -ArgumentList '-jar', $jar.FullName, "--server.port=$SpringPort" -NoNewWindow -PassThru

try {
    # 스프링이 뜰 때까지 대기 (/health)
    $healthUrl = "http://localhost:$SpringPort/health"
    $ready = $false
    for ($i = 0; $i -lt 60 -and -not $spring.HasExited; $i++) {
        try { Invoke-RestMethod $healthUrl -TimeoutSec 2 | Out-Null; $ready = $true; break } catch { Start-Sleep -Seconds 1 }
    }
    if (-not $ready) { throw "스프링 서버가 시작되지 않았습니다 ($healthUrl)" }

    Write-Host "[next] http://localhost:$NextPort 시작 (스프링: http://localhost:$SpringPort)"
    $env:PORT = $NextPort
    $env:HOSTNAME = '0.0.0.0'
    $env:SPRING_API_URL = "http://localhost:$SpringPort"
    # 포그라운드로 실행: Ctrl+C로 종료하면 finally에서 스프링도 함께 종료
    node frontend\server.js
}
finally {
    if (-not $spring.HasExited) {
        Write-Host '[spring] 종료'
        Stop-Process -Id $spring.Id -Force
    }
}
