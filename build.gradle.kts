plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.6"
}

group = "dev.sbs"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven(url = "https://central.sonatype.com/repository/maven-snapshots")
    maven(url = "https://jitpack.io")
}

dependencies {
    // Simplified Annotations
    annotationProcessor(libs.simplified.annotations)

    // Lombok Annotations
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    // Tests
    testImplementation(libs.hamcrest)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.platform.launcher)

    // SpringDoc (implementation-specific, not in server-api)
    implementation(libs.springdoc.openapi.scalar) {
        exclude(group = "org.jboss.logging", module = "jboss-logging")
    }

    // Simplified infrastructure (formerly transitive via minecraft-api)
    implementation("com.github.simplified-dev:client:master-SNAPSHOT")
    implementation("com.github.simplified-dev:gson-extras:master-SNAPSHOT")
    implementation("com.github.simplified-dev:manager:master-SNAPSHOT")

    // Split minecraft-api modules
    implementation("com.github.skyblock-simplified:skyblock-data-api:master-SNAPSHOT")
    implementation("com.github.simplified-api:mojang:master-SNAPSHOT")
    implementation("com.github.skyblock-simplified:sbs-api:master-SNAPSHOT")
    implementation("com.github.simplified-api:hypixel:master-SNAPSHOT")

    // Projects
    implementation("com.github.simplified-dev:spring-framework:master-SNAPSHOT")
}

tasks {
    withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }

    test {
        useJUnitPlatform()
    }

    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()
        append("META-INF/spring.factories")
        append("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")

        manifest {
            attributes["Main-Class"] = "dev.sbs.simplifiedserver.SimplifiedServer"
            attributes["Multi-Release"] = "true"
        }

        exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

    build {
        dependsOn(shadowJar)
    }

    register<Exec>("deploy") {
        description = "Build and deploy to remote Docker host via SSH over VPN"
        group = "deployment"
        dependsOn(shadowJar)

        doFirst {
            project.file(".env").readLines()
                .filter { it.contains('=') && !it.startsWith('#') }
                .forEach { environment(it.substringBefore('='), it.substringAfter('=')) }
        }

        commandLine("docker", "compose", "up", "-d", "--build", "--remove-orphans")
    }
}
