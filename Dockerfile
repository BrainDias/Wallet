# Используем образ с Java
FROM openjdk:17-jdk-alpine
LABEL authors="BrainDias"

# Указываем рабочую директорию в контейнере
WORKDIR /app

# Копируем JAR-файл в контейнер
COPY build/libs/Wallet-0.0.1-SNAPSHOT.jar app.jar

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
