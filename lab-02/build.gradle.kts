import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.0.10"
    kotlin("jvm") version "2.3.21"
}

group = "edu.aks"
version = "1.0"
repositories { mavenCentral() }

java { toolchain.languageVersion.set(JavaLanguageVersion.of(25)) }
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }
kotlin { compilerOptions.jvmTarget.set(JvmTarget.JVM_25) }

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.10"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.apache.commons:commons-lang3:3.22.0")
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation(kotlin("stdlib"))
    testImplementation("org.springframework:spring-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.microsoft.playwright:playwright:1.56.0")
}

tasks.bootJar { archiveFileName.set("catalog-spring.jar") }
jacoco { toolVersion = "0.8.16" }
tasks.test {
    useJUnitPlatform()
    val catalogUrl = providers.gradleProperty("catalogUrl")
    if (catalogUrl.isPresent) {
        systemProperty("catalog.url", catalogUrl.get())
        environment("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")
        outputs.upToDateWhen { false }
    }
    finalizedBy(tasks.jacocoTestReport)
}
tasks.jacocoTestReport { dependsOn(tasks.test) }
