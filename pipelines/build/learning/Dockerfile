FROM tomcat:9.0.62-jdk11-openjdk
RUN rm -rf /usr/local/tomcat/webapps/*
COPY platform-modules/service/target/learning-service.war /usr/local/tomcat/webapps/
# COPY ./platform-modules/service/src/main/resources/application.conf /usr/local/tomcat/config/
ENV JAVA_OPTS -Dconfig.file=/usr/local/tomcat/config/application.conf
CMD ["catalina.sh", "run"]
