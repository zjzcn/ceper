wmic process where (commandline like "%%ceper.Engine%%" and not name="wmic.exe") delete
rem ps ax | grep -i 'ceper.Engine' | grep -v grep | awk '{print $1}' | xargs kill -SIGTERM