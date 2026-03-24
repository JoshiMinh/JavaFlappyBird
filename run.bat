@echo off
REM === Flappy Bird Java Launcher ===

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin

REM Compile all Java source files
javac -d bin -sourcepath app app\joshiminh\flappybird\*.java app\joshiminh\flappybird\components\*.java
if errorlevel 1 (
	echo.
	echo Compilation failed. Please check for errors above.
	pause
	exit /b 1
)

REM Run the game
java -cp bin joshiminh.flappybird.Launcher
if errorlevel 1 (
	echo.
	echo Game failed to launch. Ensure Java is installed and try again.
	pause
	exit /b 1
)
