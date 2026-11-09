FROM eclipse-temurin:8-jdk
WORKDIR /app
COPY Servidor2.java .
RUN mkdir out && javac -d out Servidor2.java
EXPOSE 5000
CMD ["java", "-cp", "out", "redes2.Servidor2"]
