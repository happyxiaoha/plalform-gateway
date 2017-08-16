FROM registry.paas:443/admin/gateway:4.0
RUN yum install -y openssl
ADD server.crt /home/liycq/lua/nginx1.10.2/conf
ADD server.key /home/liycq/lua/nginx1.10.2/conf
ADD nginx /home/liycq/lua/nginx1.10.2/sbin/
RUN chmod -R 777 /home/liycq/lua/nginx1.10.2
RUN chmod -R 777 /home/liycq/lua/tomcat
RUN chmod -R 777 /home/liycq/lua/startAll.sh
RUN chmod -R 777  /home/liycq/lua/jdk1.8.0_112
RUN chmod -R 777 /home/liycq/lua/tomcat
RUN rm -rf /home/liycq/lua/tomcat/webapps/*
ADD hosts /etc/
ADD gateway-web-1.8.0.war /home/liycq/lua/tomcat/webapps/
ADD sslserver.conf /home/liycq/lua/nginx1.10.2/conf
ADD catalina.sh /home/liycq/lua/tomcat/bin/
RUN chmod +x /home/liycq/lua/tomcat/bin/catalina.sh
ADD api-error-report2.lua /home/liycq/lua/nginx1.10.2/logs
ADD api-access-rewrite.lua /home/liycq/lua/nginx1.10.2/logs
ADD api-access-if.lua /home/liycq/lua/nginx1.10.2/logs
ADD cache-handler.lua /home/liycq/lua/nginx1.10.2/logs
ADD body-filter.lua /home/liycq/lua/nginx1.10.2/logs
ADD nginx.conf /home/liycq/lua/nginx1.10.2/conf
ADD server.xml /home/liycq/lua/tomcat/conf/
RUN chmod +x /home/liycq/lua/tomcat/conf/server.xml
#ADD startup.sh /home/liycq/lua/tomcat/bin/
#RUN chmod +x /home/liycq/lua/tomcat/bin/startup.sh
ADD start.sh /data/start.sh
#ADD MockSystem /home/liycq/lua/MockSystem
#RUN chmod +x /home/liycq/lua/MockSystem
ADD lua-resty-redis-master.tar /home/liycq/lua/
ADD cjson.so /home/liycq/lua/
RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN chmod +x /home/liycq/lua/lua-resty-redis-master/


ENTRYPOINT bash /data/start.sh