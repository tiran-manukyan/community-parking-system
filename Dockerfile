FROM gradle:8.13.0-jdk21

WORKDIR /opt/app

COPY . .

RUN gradle build --no-daemon

ENTRYPOINT java -jar build/libs/app.jar