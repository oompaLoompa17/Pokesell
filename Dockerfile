# Build Angular
FROM node:23 AS ng-build
WORKDIR /src
RUN npm i -g @angular/cli
COPY client/public public
COPY client/src src
COPY client/*.json .
RUN npm ci && ng build

# Build Spring Boot
FROM openjdk:23-jdk AS j-build
WORKDIR /src
COPY server/.mvn .mvn
COPY server/src src
COPY server/mvnw .
COPY server/pom.xml .
COPY --from=ng-build /src/dist/client/browser/ src/main/resources/static/
RUN chmod a+x ./mvnw && ./mvnw package -Dmaven.test.skip=true

# Runtime
FROM openjdk:23-jdk 
WORKDIR /app
COPY --from=j-build /src/target/Pokemon_TCG_TEST-0.0.1-SNAPSHOT.jar app.jar
ENV PORT=80
EXPOSE ${PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]

# # Build Angular
# FROM node:23 AS ng-build

# WORKDIR /src

# RUN npm i -g @angular/cli

# COPY client/public public
# COPY client/src src
# COPY client/*.json .

# RUN npm ci && ng build

# # Build Spring Boot
# FROM openjdk:23-jdk AS j-build

# WORKDIR /src

# COPY server/.mvn .mvn
# COPY server/src src
# COPY server/mvnw .
# COPY server/pom.xml .

# # Copy angular files over to static
# # for files and folders leave out the *
# # for files only include *
# COPY --from=ng-build /src/dist/client/browser/ src/main/resources/static/

# # RUN chmod a+x mvnw && ./mvnw package -Dmaven.test.skip=true
# RUN chmod a+x ./mvnw && ./mvnw package -Dmaven.test.skip=true

# # Copy the JAR file over to the final container
# FROM openjdk:23-jdk 

# WORKDIR /app

# COPY --from=j-build /src/target/server-0.0.1-SNAPSHOT.jar app.jar

# ENV server.port=8443

# ENV JWT_SECRET=

# # redis
# ENV SPRING_DATA_REDIS_HOST=
# ENV SPRING_DATA_REDIS_PASSWORD=
# ENV SPRING_DATA_REDIS_USERNAME=
# ENV SPRING_DATA_REDIS_PORT=

# # sql
# ENV SPRING_DATASOURCE_PASSWORD=
# ENV SPRING_DATASOURCE_USERNAME=
# ENV SPRING_DATASOURCE_URL=

# # Enabling multipart/form-data
# ENV spring.servlet.multipart.enabled=true
# ENV spring.servlet.multipart.max-file-size=200MB
# ENV spring.servlet.multipart.max-request-size=200MB
# ENV spring.servlet.multipart.file-size-threshold=1MB

# # timezone
# ENV spring.jackson.time-zone=Asia/Singapore
# ENV spring.jpa.properties.hibernate.jdbc.time_zone=Asia/Singapore

# # pokemon tcg api
# ENV pokemon.api.key=
# ENV pokemon.api.url=

# # Ximilar api
# ENV ximilar.api.token=

# # Stripe
# ENV stripe.secret.key=

# # JWT secret key

# # HTTPS
# ENV server.ssl.key-store=
# ENV server.ssl.key-store-password=
# ENV server.ssl.keyStoreType=
# ENV server.ssl.keyAlias=

# # Google Sheets
# ENV spring.security.oauth2.client.registration.google.client-id=128503279503-7rbr19i48n7gmfu8cu09ukbr9lq5m0l6.apps.googleusercontent.com
# ENV spring.security.oauth2.client.registration.google.client-secret=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
# ENV spring.security.oauth2.client.registration.google.scope=https://www.googleapis.com/auth/spreadsheets
# ENV spring.security.oauth2.client.registration.google.redirect-uri=https://localhost:8443/api/marketplace/export-sold-callback

# # OAuth2
# ENV spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth
# ENV spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token
# ENV spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v3/userinfo
# ENV spring.security.oauth2.client.provider.google.user-name-attribute=email

# EXPOSE ${PORT}

# SHELL [ "/bin/sh", "-c" ]
# ENTRYPOINT SERVER_PORT=${PORT} java -jar app.jar