# Apache Tomcat
FROM tomcat:9.0
RUN rm -rf /usr/local/tomcat/webapps/*
COPY ./platform-modules/service/target/learning-service.war /usr/local/tomcat/webapps/
CMD ["catalina.sh","run"]