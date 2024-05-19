FROM openjdk:21
RUN mkdir /opt/app
COPY ./build /opt/app
WORKDIR /opt/app
EXPOSE 5260/tcp
EXPOSE 5260/udp
CMD ["java", "-jar", "/opt/app/SparkSRV.jar"]
