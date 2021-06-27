FROM adoptopenjdk/openjdk14

EXPOSE 5678

ADD target/metbank-banking-service-0.0.1-SNAPSHOT.jar /opt/metbank-banking-service-0.0.1-SNAPSHOT.jar

WORKDIR /opt

ENTRYPOINT ["java", "-jar" , "metbank-banking-service-0.0.1-SNAPSHOT.jar", "&"]