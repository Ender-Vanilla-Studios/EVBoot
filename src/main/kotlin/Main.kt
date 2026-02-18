package net.Mirik9724.stopTiks

import java.io.File
import java.net.URL
import java.net.URLClassLoader

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        val serverJar = File("paper-1.20.1-196.jar")

        val loader = object : URLClassLoader(
            arrayOf(serverJar.toURI().toURL()),
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

        val mainClass = loader.loadClass("org.bukkit.craftbukkit.Main")
        val mainMethod = mainClass.getMethod("main", Array<String>::class.java)

        mainMethod.invoke(null, args)
    }

    fun patch(name: String, originalBytes: ByteArray): ByteArray {
        if (!name.endsWith("MinecraftServer")) {
            return originalBytes
        }

        println("Patched MinecraftServer: $name (тики мира всегда заморожены)")

        val cr = ClassReader(originalBytes)
        val cw = ClassWriter(cr, ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)

        val cv = object : ClassVisitor(ASM9, cw) {
             fun visitMethod(
                access: Int,
                name: String?,
                descriptor: String?,
                signature: String?,
                exceptions: Array<out String>?
            ): MethodVisitor {
                val mv = super.visitMethod(access, name, descriptor, signature, exceptions)

                // Целевой метод — tickServer (в 1.20.1 Mojang mappings обычно "tickServer")
                // Дескриптор: (Ljava/util/function/BooleanSupplier;)V
                if (name == "tickServer" && descriptor == "(Ljava/util/function/BooleanSupplier;)V") {
                    return object : MethodVisitor(ASM9, mv) {
                         fun visitCode() {
                            super.visitCode()

                            // Вставляем в самое начало метода:
                            // this.getPlayerList().tick();
                            // this.getCommands().tick();   // или getCommandManager в зависимости от mappings
                            // return;  // выходим — ничего больше не тикает

                            mv.visitVarInsn(ALOAD, 0) // this (MinecraftServer)
                            mv.visitMethodInsn(
                                INVOKEVIRTUAL,
                                "net/minecraft/server/MinecraftServer",
                                "getPlayerList",
                                "()Lnet/minecraft/server/players/PlayerList;",
                                false
                            )
                            mv.visitMethodInsn(
                                INVOKEVIRTUAL,
                                "net/minecraft/server/players/PlayerList",
                                "tick",
                                "()V",
                                false
                            )

                            mv.visitVarInsn(ALOAD, 0)
                            mv.visitMethodInsn(
                                INVOKEVIRTUAL,
                                "net/minecraft/server/MinecraftServer",
                                "getCommands",              // проверь имя метода! Может быть getCommandManager или getCommandDispatcher
                                "()Lnet/minecraft/commands/Commands;",
                                false
                            )
                            mv.visitMethodInsn(
                                INVOKEVIRTUAL,
                                "net/minecraft/commands/Commands",
                                "tick",
                                "()V",
                                false
                            )

                            mv.visitInsn(RETURN) // Выходим из метода — остальной тик пропущен навсегда
                        }
                    }
                }

                return mv
            }
        }

        cr.accept(cv, 0)
        return cw.toByteArray()
    }
}