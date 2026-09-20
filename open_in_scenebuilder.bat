@echo off
REM ======================================================================
REM Open MainView.fxml in Scene Builder
REM ======================================================================

set "SB_JAVA=C:\Users\Madiha_Rahman\AppData\Local\SceneBuilder\runtime\bin\javaw.exe"
set "SB_JAR=C:\Users\Madiha_Rahman\AppData\Local\SceneBuilder\app\scenebuilder-26.0.0-all.jar"
set "FXML_FILE=%~dp0src\main\resources\views\MainView.fxml"

echo Opening MainView.fxml in Scene Builder...
start "" "%SB_JAVA%" --enable-native-access=javafx.graphics -cp "%SB_JAR%" com.oracle.javafx.scenebuilder.app.SceneBuilderApp "%FXML_FILE%"
