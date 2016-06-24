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

# Install image magick
RUN apt-get update && apt-get install -y imagemagick

# Install ffmpeg
ENV FFMPEG_HOME /usr/local/ffmpeg
RUN mkdir -p "$FFMPEG_HOME"
WORKDIR $FFMPEG_HOME
RUN wget http://johnvansickle.com/ffmpeg/releases/ffmpeg-release-64bit-static.tar.xz
RUN tar xf ffmpeg-release-64bit-static.tar.xz
WORKDIR ffmpeg-3.0.2-64bit-static
RUN cp ffmpeg /usr/local/bin/
RUN cp ffprobe /usr/local/bin/
RUN cp ffserver /usr/local/bin/

# Switch to tomcat director
WORKDIR $CATALINA_HOME

# Add URI Encoding to server.xml
RUN sed -i -e 's|redirectPort.*|redirectPort="8443" URIEncoding="utf-8"/>|' $CATALINA_HOME/conf/server.xml

# Copy the webapp
COPY taxonomy-service.war $CATALINA_HOME/webapps/

# Start services (redis, followed by tomcat)
COPY startServices.sh /usr/local/ilimi/startServices.sh
CMD ["/bin/bash", "/usr/local/ilimi/startServices.sh"]