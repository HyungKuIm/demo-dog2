@echo off
rem 더블클릭/cmd 에서 실행용: start-all.ps1 호출
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-all.ps1" %*
