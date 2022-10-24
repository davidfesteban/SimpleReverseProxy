FROM openjdk:18
MAINTAINER naivetardis.de
COPY target/SimpleReverseProxy/SimpleReverseProxy.jar app.jar

EXPOSE 80/tcp
EXPOSE 80/udp
EXPOSE 8000/tcp
EXPOSE 8000/udp
EXPOSE 8080/tcp
EXPOSE 8080/udp

ENTRYPOINT ["java","-jar","/app.jar"]