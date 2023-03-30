FROM tomcat:9.0.62-jdk11-openjdk
RUN rm -rf /usr/local/tomcat/webapps/*
COPY ./platform-modules/service/target/learning-service.war /usr/local/tomcat/webapps/
# ENV JAVA_OPTS -Dconfig.file=/usr/local/tomcat/config/application.conf
CMD ["catalina.sh", "run"]
# COPY ./platform-modules/service/src/main/resources/application.conf /usr/local/tomcat/config/
# CMD ["catalina.sh", "run", "-Dconfig.file=/usr/local/tomcat/config/application.conf"]
# CMD '/usr/bin/java' -Djava.util.logging.config.file=/usr/share/tomcat/conf/logging.properties -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Xms2048m -Xmx5096m -Dconfig.file=/data/properties/application.conf -XX:NewRatio=3 -XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -Dfile.encoding=UTF8 -Djdk.tls.ephemeralDHKeySize=2048 -Djava.endorsed.dirs=/usr/share/tomcat/endorsed -classpath /usr/share/tomcat/bin/bootstrap.jar:/usr/share/tomcat/bin/tomcat-juli.jar -Dcatalina.base=/usr/share/tomcat -Dcatalina.home=/usr/share/tomcat -Djava.io.tmpdir=/usr/share/tomcat/temp org.apache.catalina.startup.Bootstrap start
#CMD '/usr/local/openjdk-11/bin/java' -Djava.util.logging.config.file=/usr/local/tomcat/conf/logging.properties -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Djdk.tls.ephemeralDHKeySize=2048 -Djava.protocol.handler.pkgs=org.apache.catalina.webresources -Dorg.apache.catalina.security.SecurityListener.UMASK=0027 -Dignore.endorsed.dirs= -classpath /usr/local/tomcat/bin/bootstrap.jar:/usr/local/tomcat/bin/tomcat-juli.jar -Dcatalina.base=/usr/local/tomcat -Dcatalina.home=/usr/local/tomcat -Djava.io.tmpdir=/usr/local/tomcat/temp -Dconfig.file=/usr/local/tomcat/config/application.conf org.apache.catalina.startup.Bootstrap start
