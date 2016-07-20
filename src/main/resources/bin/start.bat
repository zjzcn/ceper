@echo off

SetLocal

rem Using pushd popd to set BASE_DIR to the absolute path
pushd %~dp0..
set BASE_DIR=%CD%
popd

set JAVA_MAIN=com.github.zjzcn.ceper.Engine

rem Generic jvm settings you want to add
IF ["%JAVA_OPTS%"] EQU [""] (
	set JAVA_OPTS=
)

rem JMX settings
IF ["%JMX_OPTS%"] EQU [""] (
	set JMX_OPTS=-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false  -Dcom.sun.management.jmxremote.ssl=false
)

rem JMX port to use
IF ["%JMX_PORT%"] NEQ [""] (
	set JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.port=%JMX_PORT%
)

set DEFAULT_JAVA_DEBUG_PORT=8401
set DEFAULT_DEBUG_SUSPEND_FLAG=n
rem Set Debug options if enabled
IF ["%JAVA_DEBUG%"] NEQ [""] (

	IF ["%JAVA_DEBUG_PORT%"] EQU [""] (
		set JAVA_DEBUG_PORT=%DEFAULT_JAVA_DEBUG_PORT%
	)

	IF ["%DEBUG_SUSPEND_FLAG%"] EQU [""] (
		set DEBUG_SUSPEND_FLAG=%DEFAULT_DEBUG_SUSPEND_FLAG%
	)
	set DEFAULT_JAVA_DEBUG_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=%DEBUG_SUSPEND_FLAG%,address=%JAVA_DEBUG_PORT%

	IF ["%JAVA_DEBUG_OPTS%"] EQU [""] (
		set JAVA_DEBUG_OPTS=%DEFAULT_JAVA_DEBUG_OPTS%
	)
)

rem Which java to use
IF ["%JAVA_HOME%"] EQU [""] (
	set JAVA=java
) ELSE (
	set JAVA="%JAVA_HOME%/bin/java"
)

rem Memory options
IF ["%JAVA_HEAP_OPTS%"] EQU [""] (
	set JAVA_HEAP_OPTS=-Xmx256M
)

rem JVM performance options
IF ["%JVM_PERFORMANCE_OPTS%"] EQU [""] (
	set JVM_PERFORMANCE_OPTS=-server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+DisableExplicitGC -Djava.awt.headless=true
)

IF ["%CLASSPATH%"] EQU [""] (
	set CLASSPATH=%BASE_DIR%\lib\*
)

set COMMAND=%JAVA% %JAVA_HEAP_OPTS% %JVM_PERFORMANCE_OPTS% %JMX_OPTS% %JAVA_DEBUG_OPTS% -cp %CLASSPATH% %JAVA_OPTS% %JAVA_MAIN%
rem echo.
rem echo %COMMAND%
rem echo.

%COMMAND%

EndLocal
