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

configurations.configureEach {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.10"))
    implementation(platform("org.apache.activemq:artemis-bom:2.44.0"))
    // Artemis uses the Netty 4.1 buffer API for its persistent journal.
    implementation(enforcedPlatform("io.netty:netty-bom:4.1.137.Final"))
    implementation("edu.aks:catalog-spring:1.0")
    implementation("edu.aks:catalog-rest:1.0")
    implementation("org.springframework.boot:spring-boot-starter-artemis")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("tools.jackson.core:jackson-databind")
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")
    runtimeOnly("org.apache.activemq:artemis-jakarta-server")
    runtimeOnly("org.slf4j:slf4j-simple")

    testImplementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

springBoot { mainClass.set("edu.aks.messaging.MessagingApplication") }
tasks.bootJar { archiveFileName.set("catalog-jms.jar") }
jacoco { toolVersion = "0.8.16" }
tasks.test {
    useJUnitPlatform()
    for (property in listOf("catalogUrl", "mailpitUrl")) {
        providers.gradleProperty(property).orNull?.let {
            systemProperty(property, it)
            outputs.upToDateWhen { false }
        }
    }
    finalizedBy(tasks.jacocoTestReport)
}
tasks.jacocoTestReport { dependsOn(tasks.test) }
