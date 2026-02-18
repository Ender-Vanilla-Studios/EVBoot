plugins {
    kotlin("jvm") version "2.1.0"
}

group = "net.Mirik9724"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-util:9.7")
}

kotlin {
    jvmToolchain(17)
}