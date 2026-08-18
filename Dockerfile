# ---------- 构建阶段 ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

# 先复制 pom，利用 Docker 层缓存 Maven 依赖
COPY pom.xml .
RUN mvn -B dependency:go-offline

# 再复制源码并打包 Spring Boot 应用
COPY src ./src
RUN mvn -B clean package -Dmaven.test.skip=true


# ---------- 运行阶段 ----------
FROM eclipse-temurin:21-jre

# 用于 Docker 健康检查访问 Actuator
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /build/target/community-*.jar app.jar

EXPOSE 8080

# 可通过 JAVA_OPTS 在部署时调整 JVM 参数
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
