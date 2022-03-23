import org.graalvm.buildtools.gradle.tasks.BuildNativeImageTask

val branch: String by project
val revision: String by project
val cliktVersion: String by project
val markdownVersion: String by project
val inquirerVersion: String by project
val mordantVersion: String by project

plugins {
    application
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.graalvm.buildtools.native") version "0.9.11"
}

group = "dev.shellbook"
version = revision

repositories {
    mavenCentral()
    maven (url="https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.kotlin-inquirer:kotlin-inquirer:$inquirerVersion")
    implementation("com.github.ajalt.mordant:mordant:$mordantVersion")
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")
    implementation("org.jetbrains:markdown:$markdownVersion")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("dev.shellbook.ShellbookCliKt")
}

graalvmNative {
    binaries {
        named("main") {
            buildArgs.add("--allow-incomplete-classpath")
            buildArgs.add("--enable-url-protocols=https")
            buildArgs.add("-H:IncludeResources=application.properties")
        }
    }
}

tasks {
    register<Copy>("installLocalGitHook") {
        from("hooks/")
        into(".git/hooks")
        fileMode=0b111101101
    }

    processResources {
        dependsOn("installLocalGitHook")
        filesMatching("application.properties") {
            expand("version" to revision)
        }
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    shadowJar {
        manifest {
            attributes(mapOf(
                "Implementation-Version" to archiveVersion
            ))
        }
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
        archiveVersion.set("")
        mergeServiceFiles()
    }
}

tasks.withType<Tar> {
    compression = Compression.GZIP
    archiveExtension.set("tar.gz")
}

distributions {
    create("native") {
        distributionBaseName.set("shellbook-native")

        contents {
            from(tasks.named<BuildNativeImageTask>("nativeCompile").get().outputs)
        }
    }
}

