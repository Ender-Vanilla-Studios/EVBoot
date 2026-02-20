package net.Mirik9724.EVBoot

import java.io.File

object Config {
    val fldr = File("boot")

    init {
        if (!fldr.exists()) {
            fldr.mkdirs()
        }
    }
}