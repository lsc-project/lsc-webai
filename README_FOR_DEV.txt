If you plan to test an unpackaged version of LSC, please consider the
different following methods depending on your environment. These method
are dedicated to WebAI development and does not feet to LSC development
because libraries are used in their static already build format ...

Through Eclipse
---------------

Go inside target subdirectory (create it if not found) and make the following link :
$LSC_HOME/target# ln -s lsc-webai-dist/lsc-webai-1.0-SNAPSHOT/jetty/webapps/lsc-webai/WEB-INF/lib .

If your are running WebAI on Windows, consider copy this directory but care that next launches uses your latest updates by copying each time !
$LSC_HOME\target> mkdir lib
$LSC_HOME\target> copy lsc-webai-dist\lsc-webai-1.0-SNAPSHOT\jetty\webapps\lsc-webai\WEB-INF\lib lib

Use Run-jetty-run eclipse plugin and start a debug configuration with the following settings :
- Select the WebAI project
- WebApp dir : src/main/webapp
- VM arguments : -Dorg.mortbay.jetty.webapp.parentLoaderPriority=true -Dtapestry.production-mode=false -DLSC_HOME=${project_loc}/target/lsc-webai-dist/lsc-webai-1.0-SNAPSHOT
- working directory : ${project_loc}

Before launching the debug environment, launch maven on the command line :
$LSC_HOME# mvn -Dtarget=distribution package

From a simple command line
--------------------------
Run maven with following options :
$LSC_HOME# mvn -Dtarget=distribution package

Go inside the lsc-webai-dist/lsc-webai-1.0-SNAPSHOT and launch the lsc-webai.sh script :
$LSC_HOME/target/lsc-webai-dist/lsc-webai-1.0-SNAPSHOT# ./lsc-webai.sh start


Debugging LSC via JDWP
-----------------------

If you want to be able to debug LSC when it is launched through webai, set the 
following environment variable to the local TCP Port you want to connect to through
your prefered debugger : 

LSC_DEBUG_PORT=8010

Then, when you have reached your console and clicked on "Start LSC engine ...", you
must see the following message : 

Listening for transport dt_socket at address: 8010

This means that the LSC is waiting to start that you connect through JDWP to this port. 