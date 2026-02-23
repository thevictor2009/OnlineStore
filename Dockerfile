FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Копируем Maven wrapper и исходники
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Даем права на выполнение mvnw (для Linux/Mac)
RUN chmod +x mvnw

# Собираем приложение
RUN ./mvnw clean package -DskipTests

# Финальный образ
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /workspace/app/target/*.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]