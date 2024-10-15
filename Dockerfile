# Используем образ OpenJDK
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем все файлы проекта в контейнер
COPY . .

# Сборка проекта с использованием Gradle
RUN ./gradlew build

# Запуск скомпилированного JAR
CMD ["java", "-jar", "build/libs/GPUStatix.jar"]
