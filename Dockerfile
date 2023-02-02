# Apache Tomcat
FROM tomcat:9.0
RUN rm -rf /usr/local/tomcat/webapps/*
COPY ./platform-modules/service/target/learning-service.war /usr/local/tomcat/webapps/
COPY ./platform-modules/service/src/main/resources/application.conf /usr/local/tomcat/config/
CMD ["catalina.sh", "run", "-Dconfig.file=/usr/local/tomcat/config/application.conf"]

