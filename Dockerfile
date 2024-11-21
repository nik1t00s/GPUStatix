# Используем образ Ubuntu
FROM ubuntu:latest

# Обновляем пакеты и устанавливаем необходимые зависимости
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk curl unzip lm-sensors neofetch && \
    apt-get clean

# Устанавливаем Gradle
RUN curl -sSL https://services.gradle.org/distributions/gradle-8.10.2-bin.zip -o gradle.zip && \
    unzip gradle.zip -d /opt && \
    rm gradle.zip
ENV PATH="/opt/gradle-8.10.2/bin:${PATH}"

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы проекта в контейнер
COPY . .

# Даем права на выполнение скрипта Gradle
RUN chmod +x ./gradlew

# Сборка проекта с использованием Gradle и shadowJar
RUN ./gradlew shadowJar

# Запуск скомпилированного fat JAR
CMD ["java", "-jar", "build/libs/GPUStatix-fat.jar"]
