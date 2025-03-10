# 第一阶段：使用 Maven 构建 JAR（适配 JDK 1.8）
FROM maven:3.8.7-eclipse-temurin-8 AS build

# 配置阿里云 Maven 镜像加速
RUN mkdir -p /usr/share/maven/ref/ && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"' > /usr/share/maven/ref/settings.xml && \
    echo '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' >> /usr/share/maven/ref/settings.xml && \
    echo '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">' >> /usr/share/maven/ref/settings.xml && \
    echo '  <mirrors>' >> /usr/share/maven/ref/settings.xml && \
    echo '    <mirror>' >> /usr/share/maven/ref/settings.xml && \
    echo '      <id>aliyun</id>' >> /usr/share/maven/ref/settings.xml && \
    echo '      <name>Aliyun Maven Mirror</name>' >> /usr/share/maven/ref/settings.xml && \
    echo '      <url>https://maven.aliyun.com/repository/public</url>' >> /usr/share/maven/ref/settings.xml && \
    echo '      <mirrorOf>central</mirrorOf>' >> /usr/share/maven/ref/settings.xml && \
    echo '    </mirror>' >> /usr/share/maven/ref/settings.xml && \
    echo '  </mirrors>' >> /usr/share/maven/ref/settings.xml && \
    echo '</settings>' >> /usr/share/maven/ref/settings.xml

WORKDIR /app
COPY pom.xml .
# 预下载依赖，减少构建时间
RUN mvn -s /usr/share/maven/ref/settings.xml dependency:go-offline

COPY src ./src
# 使用 Maven 打包，跳过测试
RUN mvn -s /usr/share/maven/ref/settings.xml package -DskipTests

# 第二阶段：使用 JDK 8 运行 JAR
FROM harbor.meta42.indc.vnet.com/library/jdk8:v4

# 设置环境变量
ARG JAVA_OPT
ENV JAVA_OPT=${JAVA_OPT}
ENV TZ=Asia/Shanghai
ENV LANG=C.UTF-8

WORKDIR /app
COPY --from=build /app/target/log-simulator-1.0.0.jar ./app.jar

ENTRYPOINT java -Dfile.encoding=UTF-8 $JAVA_OPT -jar app.jar
