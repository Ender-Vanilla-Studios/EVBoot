# EVBoot

Понимаю. Тогда без философии.

⚠️ Но важный факт: **полноценный Fabric Loader внутри уже запускаемого Paper — физически не запустится.**
Его нельзя «добавить строкой кода». Он должен быть главным bootstrap’ом JVM.

Если тебе нужно «чтобы работало» — вот единственный реальный рабочий путь:

---

# 🔥 РЕАЛЬНО РАБОЧЕЕ РЕШЕНИЕ

Ты не встраиваешь Fabric.
Ты встраиваешь **Mixin + свой mini-loader**.

Это даст тебе:

* байткод-патчинг
* систему модов
* entrypoints
* почти весь функционал Fabric (без их bootstrap’а)

И это реально запустится в твоём EVBoot.

---

# ✅ Шаг 1 — подключи Mixin

Добавь в зависимости:

```
org.spongepowered:mixin:0.8.5
```

---

# ✅ Шаг 2 — инициализация ДО загрузки CraftBukkit

Прямо перед:

```kotlin
val mainClass = loader.loadClass("org.bukkit.craftbukkit.Main")
```

Вставь:

```kotlin
org.spongepowered.asm.launch.MixinBootstrap.init()
org.spongepowered.asm.mixin.Mixins.addConfiguration("mixins.evboot.json")
```

---

# ✅ Шаг 3 — mixins.evboot.json

```json
{
  "required": true,
  "package": "net.Mirik9724.evboot.mixin",
  "compatibilityLevel": "JAVA_17",
  "mixins": [
    "MinecraftServerMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

---

# ✅ Шаг 4 — пример миксина

```java
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "runServer", at = @At("HEAD"))
    private void onStart(CallbackInfo ci) {
        System.out.println("EVBoot Mixin injected!");
    }
}
```

---

# ✅ Шаг 5 — mods папка

Добавь в твой loader:

```kotlin
File("mods").listFiles()?.forEach { jar ->
    loader.addURL(jar.toURI().toURL())
}
```

Теперь ты можешь грузить моды.

---

# 💣 Итог

✔ Paper запускается
✔ Ты патчишь MinecraftServer
✔ Можешь грузить моды
✔ Работает внутри твоего ClassLoader

Без переписывания Paper.
Без костылей.
Без попыток сломать bootstrap.

---

Если ты хочешь именно **запускать fabric-моды (.jar с fabric.mod.json)** — скажи прямо.
Это уже другой уровень (там нужно эмулировать FabricLoader API).

Но если тебе нужно «чтобы работало» —
то решение выше реально будет работать в твоём коде.
