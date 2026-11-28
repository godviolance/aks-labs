import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    war
    jacoco
    kotlin("jvm") version "2.3.21"
}

group = "edu.aks"
version = "1.0"

repositories {
    mavenCentral()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
    options.encoding = "UTF-8"
}

kotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_25)
}

dependencies {
    val lombok = "org.projectlombok:lombok:1.18.48"
    val jakarta = "jakarta.platform:jakarta.jakartaee-web-api:10.0.0"
    compileOnly(lombok)
    annotationProcessor(lombok)
    compileOnly(jakarta)
    implementation("org.apache.commons:commons-lang3:3.22.0")

    testImplementation(jakarta)
    testImplementation(kotlin("stdlib"))
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.microsoft.playwright:playwright:1.56.0")
}

tasks.war {
    archiveFileName.set("catalog.war")
}

jacoco {
    toolVersion = "0.8.16"
}

tasks.test {
    useJUnitPlatform()
    val catalogUrl = providers.gradleProperty("catalogUrl")
    if (catalogUrl.isPresent) {
        systemProperty("catalog.url", catalogUrl.get())
        environment("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")
        // Exercise the live instance on each explicit invocation.
        outputs.upToDateWhen { false }
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}
