
redis-server --daemonize yes --dir /data/redis/ --save 60 10
export JAVA_OPTS="-Xms512m -Xmx2048m -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled -XX:MaxPermSize=256M"
catalina.sh run
