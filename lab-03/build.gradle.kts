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
    implementation("edu.aks:catalog-spring:1.0")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("tools.jackson.dataformat:jackson-dataformat-xml")
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")

    testImplementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

springBoot { mainClass.set("edu.aks.rest.RestApplication") }
tasks.bootJar { archiveFileName.set("catalog-rest.jar") }
jacoco { toolVersion = "0.8.16" }
tasks.test {
    useJUnitPlatform()
    providers.gradleProperty("catalogUrl").orNull?.let {
        systemProperty("catalog.url", it)
        outputs.upToDateWhen { false }
    }
    finalizedBy(tasks.jacocoTestReport)
}
tasks.jacocoTestReport { dependsOn(tasks.test) }
