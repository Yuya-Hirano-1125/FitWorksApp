# Java 17（Spring Boot 推奨）を使う
FROM eclipse-temurin:17-jdk

# jar をコンテナにコピー
COPY target/*.jar app.jar

# アプリがListenするポート
EXPOSE 8080

# Spring Boot 起動
ENTRYPOINT ["java", "-jar", "/app.jar"]





























