package net.Mirik9724.EVBoot

import org.spongepowered.asm.launch.MixinBootstrap
import org.spongepowered.asm.mixin.Mixins
import java.io.File
import java.net.URLClassLoader
import java.lang.reflect.Method

class MixinLoader {

    companion object {
        @JvmStatic
        fun init(loader: ClassLoader) {
            try {
                // 1. Инициализация Mixin
                MixinBootstrap.init()
                Mixins.addConfiguration("mixins.evboot.json")

                // 2. Загрузка всех модов из папки
                loadMods(loader)

                println("[EVBoot] Loader initialized successfully!")
            } catch (e: Exception) {
                e.printStackTrace()
                System.err.println("[EVBoot] Loader initialization failed!")
            }
        }

        private fun loadMods(loader: ClassLoader) {
            val modsDir = File("boot/mods")
            if (!modsDir.exists() || !modsDir.isDirectory) {
                println("[EVBoot] No mods directory found at boot/mods")
                return
            }

            val files = modsDir.listFiles { _, name -> name.endsWith(".jar") } ?: return

            // Используем рефлексию, так как в Java 9+ ClassLoader не всегда URLClassLoader
            val method: Method = URLClassLoader::class.java.getDeclaredMethod("addURL", java.net.URL::class.java)
            method.isAccessible = true

            for (jar in files) {
                try {
                    method.invoke(loader, jar.toURI().toURL())
                    println("[EVBoot] Loaded mod: ${jar.name}")
                } catch (e: Exception) {
                    println("[EVBoot] Failed to load mod ${jar.name}: ${e.message}")
                }
            }
        }
    }
}
