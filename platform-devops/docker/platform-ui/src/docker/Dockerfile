#
# ekStep Learning Platform UI Docker image
# This image contains MEAN stack and the application UI code. 
#

# pull base image.
FROM node:0.12

# maintainer details
MAINTAINER Feroz Sheikh "feroz@ilimi.in"

# Setup the MongoDB varification key
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10

# Add the MongoDB repository
RUN echo "deb http://repo.mongodb.org/apt/debian wheezy/mongodb-org/3.0 main" | tee /etc/apt/sources.list.d/mongodb-org-3.0.list

# install required set of stack
RUN \
  apt-get update && \
  apt-get install -y \
  mongodb-org-server \
  mongodb-org-shell

# cleanup some unwanted packages
RUN apt-get -y autoremove

# Copy the config and rename routes
RUN npm install -g grunt-cli

# Copy the app to the container
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# Build the dependencies for the app
ADD . /usr/src/app
RUN npm install
RUN grunt

ADD startServices.sh /usr/local/ilimi/startServices.sh
CMD ["/bin/bash", "/usr/local/ilimi/startServices.sh"]
