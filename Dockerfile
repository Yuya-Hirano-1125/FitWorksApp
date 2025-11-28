# ==========================================================
# ステージ 1: ビルドステージ (JARファイル生成)
# Java 21とGradle 8.5を使用
# ==========================================================
FROM gradle:8.5-jdk21-jammy AS builder

# 作業ディレクトリを設定
WORKDIR /app

# Gradle関連ファイルとソースコードをコピー
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# 実行権限を付与
RUN chmod +x gradlew

# アプリケーションをビルドし、実行可能 JAR ファイルを作成
# このタスクでReactの静的ファイルも組み込まれることが期待されます
RUN ./gradlew clean bootJar --no-daemon

# ==========================================================
# ステージ 2: 実行ステージ (軽量ランタイム)
# 役割: 実行に必要な最小限の環境（JREのみ）を持つ
# ==========================================================
FROM eclipse-temurin:21-jre-jammy

# アプリケーションポート (8085) を公開
EXPOSE 8085

# ビルドステージで作成された JAR ファイルをコピー
ARG JAR_FILE=/app/build/libs/*.jar
COPY --from=builder ${JAR_FILE} app.jar

# 変更後: Renderの環境変数 $PORT をSpring Bootに渡して起動
ENTRYPOINT ["java","-Dserver.port=${PORT}","-jar","/app.jar"]

# Spring Bootアプリケーションを起動
ENTRYPOINT ["java","-jar","/app.jar"]