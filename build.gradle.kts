plugins {
    kotlin("jvm") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.Mirik9724"
version = "0.1"
val namec = "EVBoot"
val mcv = "1.20.1"

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
}

dependencies {
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-util:9.7")

    implementation("org.spongepowered:mixin:0.8.5")
}

kotlin {
    jvmToolchain(17)
}

tasks.shadowJar {
    archiveFileName.set("$namec-$version-mc$mcv.jar")
    mergeServiceFiles()

    manifest { attributes["Main-Class"] = "net.Mirik9724.EVBoot.Main" }
}

tasks.register<Exec>("runServer") {
    dependsOn(tasks.shadowJar)

    group = "application"
    val bootDir = layout.projectDirectory.dir("run").asFile

    doFirst {
        project.copy {
            from(tasks.shadowJar.get().archiveFile)
            into(bootDir)
            rename { "EVBoot.jar" }
        }
    }

    workingDir = bootDir
    commandLine("cmd", "/c", "start.bat")
}
