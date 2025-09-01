plugins {
    java
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.spring") version "2.2.10"
}

group = "com.musicserver"
version = "1.0.0"
description = "企业级音乐播放器后端服务"

java {
    toolchain{
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
}

dependencies {
    // Spring Boot 核心依赖
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Spring Security 安全框架
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JWT 认证依赖
    implementation("io.jsonwebtoken:jjwt-api:0.12.7")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    "developmentOnly"("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.7")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.7")

    // MyBatis Plus 数据库框架
    implementation("com.baomidou:mybatis-plus-spring-boot3-starter:3.5.13")
    implementation("com.baomidou:mybatis-plus-generator:3.5.12")
    implementation("com.baomidou:mybatis-plus-jsqlparser:3.5.12")

    // MinIO 文件存储服务
    implementation("io.minio:minio:8.5.17")

    // MySQL 数据库驱动
    runtimeOnly("com.mysql:mysql-connector-j")

    // 数据库连接池
    implementation("com.alibaba:druid-spring-boot-starter:1.2.23")

    // Redis 客户端
    implementation("org.apache.commons:commons-pool2")

    // Knife4j API 文档
    implementation("com.github.xiaoymin:knife4j-openapi3-jakarta-spring-boot-starter:4.5.0")

    // Lombok 工具
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    // IP 地理位置查询
    implementation("org.lionsoul:ip2region:2.7.0")

    // 本地缓存
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // Jackson JSON 处理
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Apache Commons 工具库
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("commons-codec:commons-codec:1.17.1")

    // 文件上传处理
    implementation("commons-fileupload:commons-fileupload:1.5")
    implementation("commons-io:commons-io:2.18.0")

    // 测试依赖
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

// 打包配置
tasks.jar {
    enabled = false
    archiveClassifier = ""
}

tasks.bootJar {
    enabled = true
    archiveClassifier = ""
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}