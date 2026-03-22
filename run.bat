@echo off
if not exist bin mkdir bin
javac -d bin -sourcepath app app\joshiminh\flappybird\*.java app\joshiminh\flappybird\components\*.java
java -cp bin joshiminh.flappybird.Launcher
