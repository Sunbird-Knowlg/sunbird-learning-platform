#
# ekStep Learning Platform 
# Taxonomy Service Docker image
# This image contains Tomcat, Neo4J (embedded) and Redis
#

# pull base image.
FROM tomcat:8-jre7

# maintainer details
MAINTAINER Feroz Sheikh "feroz@ilimi.in"

# Make to setup Redis
RUN \
  apt-get update && \
  apt-get install -y \
  build-essential

# Setup Redis Server
ENV REDIS_HOME /usr/local/redis
RUN mkdir -p "$REDIS_HOME"
WORKDIR $REDIS_HOME

# Download and build redis binaries
RUN wget http://download.redis.io/redis-stable.tar.gz
RUN tar xvzf redis-stable.tar.gz
WORKDIR redis-stable
RUN make
RUN cp src/redis-server /usr/local/bin/
RUN cp src/redis-cli /usr/local/bin/

# Switch to tomcat director
WORKDIR $CATALINA_HOME

# Copy the webapp
COPY taxonomy-service.war $CATALINA_HOME/webapps/

# Start services (redis, followed by tomcat)
COPY startServices.sh /usr/local/ilimi/startServices.sh
CMD ["/bin/bash", "/usr/local/ilimi/startServices.sh"]