#!/bin/bash
sudo ps -ef | grep apt
sudo ps -ef | grep dpkg
sleep 60
sudo ps -ef | grep apt
sudo ps -ef | grep dpkg
sudo apt update
sudo apt install redis-server -y
curl -O https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.3.2.deb
sudo dpkg -i elasticsearch-6.3.2.deb
sudo service elasticsearch start
sudo service elasticsearch status
