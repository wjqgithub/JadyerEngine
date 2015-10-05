@echo off
title OpenAPI MockServer¡¾ĞşÓñÖÆ×÷¡¿
color 02
REM https://repo1.maven.org/maven2/com/github/dreamhead/moco-runner/0.10.2/moco-runner-0.10.2-standalone.jar
REM java -jar moco-runner-0.10.2-standalone.jar http -p 8082 -c openapi.json
java -jar moco-runner-0.10.2-standalone.jar start -p 8082 -g whole.json