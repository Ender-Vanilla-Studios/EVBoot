package net.Mirik9724.EVBoot

import java.io.File
import java.net.URLClassLoader

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        val origJar = File("paper-1.20.1-196.jar")
        if (!origJar.exists()) {
            println("Paper JAR not found at ${origJar.absolutePath}!")
            return
        }

        val serverJar = File("versions/1.20.1/paper-1.20.1.jar")
        if (!serverJar.exists()) {
            println("Preparing Paper files (patch-only)... Please wait.")

            val javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"

            val processBuilder = ProcessBuilder(
                javaBin,
                "-Dpaperclip.patchonly=true",
                "-jar",
                origJar.name
            ).inheritIO()

            val process = processBuilder.start()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                println("Patching completed successfully!")
            } else {
                println("Error preparing paper! Exit code: $exitCode")
                return
            }
        }

        val librariesDir = File("libraries")

        val classpathUrls = mutableListOf(serverJar.toURI().toURL())

        if (librariesDir.exists() && librariesDir.isDirectory) {
            librariesDir.walkTopDown()
                .filter { it.isFile && it.extension.equals("jar", ignoreCase = true) }
                .forEach { jarFile ->
                    classpathUrls.add(jarFile.toURI().toURL())
                }
        } else {
            println("libraries/ does not exist")
            return
        }

        val loader = object : URLClassLoader(
            classpathUrls.toTypedArray(),
            ClassLoader.getSystemClassLoader()
        ) {

            override fun findClass(name: String): Class<*> {

                val path = name.replace('.', '/') + ".class"
                val stream = getResourceAsStream(path)
                    ?: return super.findClass(name)

                val originalBytes = stream.readBytes()

                val patchedBytes = patch(name, originalBytes)

                return defineClass(name, patchedBytes, 0, patchedBytes.size)
            }
        }

        Thread.currentThread().contextClassLoader = loader
        try {
            val tempLoader = URLClassLoader(arrayOf(serverJar.toURI().toURL()), null)
            val customLogManagerClass = tempLoader.loadClass("io.papermc.paper.log.CustomLogManager")
            println("Preloaded CustomLogManager: ${customLogManagerClass.name}")
        } catch (e: Exception) {
            println("Failed to preload CustomLogManager: ${e.message}")
            e.printStackTrace()
        }

//        System.setProperty("java.util.logging.manager", "io.papermc.paper.log.CustomLogManager")


        net.Mirik9724.EVBoot.Config

        val mainClass = loader.loadClass("org.bukkit.craftbukkit.Main")
        val mainMethod = mainClass.getMethod("main", Array<String>::class.java)

        mainMethod.invoke(null, args)
    }

    fun patch(name: String, originalBytes: ByteArray): ByteArray {
        if (name == "net.minecraft.server.MinecraftServer") {
            println("--- [EVBoot] Patching MinecraftServer ---")
        }
        return originalBytes
    }
}
