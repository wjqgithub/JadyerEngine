#!/bin/sh
APP_NAME=engine
APP_WARS=JadyerEngine-web/target
APP_PATH=/app/tomcat-6.0.43
APP_CODE=sourcecode
SVN_URL=https://svn.sinaapp.com/jadyer/2/repository/JadyerEngine
SVN_USER=jadyer@yeah.net
SVN_PSWD=jadyer

appPID=0
getAppPID(){
    pidInfo=`ps aux|grep java|grep $APP_PATH|grep -v grep`
    if [ -n "$pidInfo" ]; then
        appPID=`echo $pidInfo | awk '{print $2}'`
    else
        appPID=0
    fi
}

downloadAndCompileSourceCode(){
    cd $APP_PATH
    mkdir $APP_CODE
    svn --username $SVN_USER --password $SVN_PSWD checkout $SVN_URL $APP_CODE
    cd $APP_CODE
    mvn clean package -DskipTests
}

shutdown(){
    getAppPID
    echo "[玄玉] ======================================================================================================================================================"
    if [ $appPID -ne 0 ]; then
        echo -n "[玄玉] Stopping $APP_PATH(PID=$appPID)..."
        kill -9 $appPID
        if [ $? -eq 0 ]; then
            echo "[Success]"
            echo "[玄玉] ======================================================================================================================================================"
        else
            echo "[Failed]"
            echo "[玄玉] ======================================================================================================================================================"
        fi
        getAppPID
        if [ $appPID -ne 0 ]; then
            shutdown
        fi
    else
        echo "[玄玉] $APP_PATH is not running"
        echo "[玄玉] ======================================================================================================================================================"
    fi
}

deploy(){
    cd $APP_PATH/webapps/
    rm -rf $APP_NAME
    rm -rf $APP_NAME.war
    cp $APP_PATH/$APP_CODE/$APP_WARS/*.war $APP_NAME.war
    cd $APP_PATH/logs/
    rm -rf *
    cd $APP_PATH
    rm -rf $APP_CODE
}

startup(){
    cd $APP_PATH/bin
    ./startup.sh
    tail -100f ../logs/catalina.out
}

downloadAndCompileSourceCode
shutdown
deploy
startup