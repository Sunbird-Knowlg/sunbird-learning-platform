
redis-server --daemonize yes --dir /data/redis/ --save 60 10
export JAVA_OPTS="-XX:+PrintGC -XX:+PrintGCTimeStamps -Xms512m -Xmx2048m -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"
catalina.sh run
