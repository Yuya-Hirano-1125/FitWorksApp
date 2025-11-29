# --- ビルドステージ ---
# GradleとJava 21を使ってアプリをビルドします
FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY . .
# テストをスキップしてビルド時間を短縮 (-x test)
RUN gradle build --no-daemon -x test

# --- 実行ステージ ---
# 軽量なJava 21環境でアプリを動かします
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# ビルドステージで作られたjarファイルをコピー (Gradleの出力先 build/libs/ を指定)
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]