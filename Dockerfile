# 使用 Linux 版 OpenJDK 17
FROM openjdk:17-slim AS build

# 设置工作目录
WORKDIR /app

# 复制项目文件
COPY . .

# 安装 Maven 并编译 Spring Boot 项目
RUN apt update && apt install -y maven && mvn clean install -DskipTests

# 运行环境
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar

# 暴露端口
EXPOSE 8080

# 运行 Spring Boot 应用
CMD ["java", "-jar", "/app/app.jar"]
