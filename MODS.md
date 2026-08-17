# Nitrogram Mods — руководство для создателей модов

Мод в Nitrogram — это **самодостаточная нативная библиотека `.so`** (arm64-v8a),
внутри которой лежит вшитый DEX с Java-логикой мода. Клиент (`org.telegram.messenger.ModManager`)
выступает только **инъектором**: он загружает `.so` через `System.load`, а вся логика
мода (эффекты, хуки, экран настроек) живёт внутри самой библиотеки.

Преимущество такой схемы: мод = один файл `.so`, который можно передать в чате, импортировать
в приложении и удалить в любой момент. Клиент не содержит никакой логики конкретных модов.

---

## 1. Структура файла мода

```
motion_blur_fx.so
├── [C-код]  метаданные между маркерами NITROGRAM_MOD_META_START … NITROGRAM_MOD_META_END
├── [C-код]  JNI_OnLoad — грузит вшитый DEX и регистрирует класс входа мода
└── [DEX]    класс org.nitrogram.mod.ModEntry (и всё, что моду нужно)
```

DEX упакован прямо в бинарь как C-массив `DEX_BYTES` / `DEX_LEN` (см. `dex_payload.h`).
При загрузке `JNI_OnLoad` читает эти байты через `InMemoryDexClassLoader` и получает класс
`org.nitrogram.mod.ModEntry`.

---

## 2. Метаданные (обязательно)

Внутри `.c`-файла должен быть C-строковый литерал с JSON между маркерами:

```c
static const char MOD_META[] =
"NITROGRAM_MOD_META_START"
"{"
"\"name\":\"Motion Blur FX\","
"\"description\":\"Направленное размытие в движении.\","
"\"version\":\"1.1\","
"\"extra\":\"Self-contained mod.\","
"\"settings\":true,"
"\"icon\":\"<BASE64_JPEG_OR_PNG>\""
"}"
"NITROGRAM_MOD_META_END";
```

Поля:

| Поле         | Назначение                                                        |
|--------------|-------------------------------------------------------------------|
| `name`       | Отображаемое имя мода (обязательно).                              |
| `description`| Короткое описание.                                                |
| `version`    | Версия мода (влияет на id мода).                                  |
| `extra`      | Любая дополнительная информация.                                  |
| `settings`   | `true`, если мод предоставляет экран настроек (покажет шестерёнку).|
| `icon`       | **Сырой** base64 JPEG/PNG — иконка мода (аватарка в списке и чате). Без префикса `data:image/...`. |

> id мода вычисляется как `m` + хэш(`name` + `version` + размер файла). Если изменить
> размер `.so` (например, пересобрать), id поменяется — старую карточку нужно удалить и
> импортировать заново.

---

## 3. Контракт класса входа `ModEntry`

В DEX обязан быть класс `org.nitrogram.mod.ModEntry` со статическими методами:

```java
package org.nitrogram.mod;

public final class ModEntry {

    /** Вызывается один раз при загрузке мода. Здесь подключаются хуки к UI. */
    public static void apply() { ... }

    /** Вызывается при загрузке и при изменении сохранённых настроек.
     *  @param json — JSON-объект с ключами настроек. */
    public static void applySettings(String json) { ... }

    /** Возвращает экран настроек (org.nitrogram.modsdk.ModSettingsScreen)
     *  либо null, если настроек нет (тогда в метаданных settings:false). */
    public static Object createSettingsScreen() { return new MySettingsScreen(); }
}
```

`apply()` вызывается автоматически из `JNI_OnLoad` (см. ниже). `applySettings(json)`
вызывается клиентом при старте (с ранее сохранёнными значениями) и каждый раз, когда
пользователь меняет настройку в экране.

---

## 4. SDK настроек (`org.nitrogram.modsdk`)

Чтобы мод мог описать свой экран настроек **в коде**, используются два интерфейса,
которые уже скомпилированы в приложение. При сборке мода вы компилируетесь против
заглушки `sdk.jar` (лежит в шаблоне), а в рантайме интерфейсы берутся из приложения.

```java
public interface ModSettingsScreen {
    /** Построй и верни View экрана настроек.
     *  @param host — хост для чтения/записи значений (persisted клиентом). */
    View createView(Context context, ModSettingsHost host, ViewGroup parent);
}

public interface ModSettingsHost {
    Object  getValue(String key, Object def);
    boolean getBool(String key, boolean def);
    int     getInt(String key, int def);
    float   getFloat(String key, float def);
    String  getString(String key, String def);
    void    setValue(String key, Object value);
}
```

Типичный паттерн внутри `createView`: читаем значение через `host.getFloat("intensity", 0.7f)`,
строим `SeekBar`/`Switch`, и в обработчике вызываем `ModEntry.setSetting(key, v)` (чтобы мод
сразу применил изменение) и `host.setValue(key, v)` (чтобы клиент сохранил значение).

---

## 5. Регистрация в `JNI_OnLoad` (важный нюанс)

Самое важное: **не используйте `env->FindClass` для классов приложения** из `JNI_OnLoad` —
в этом контексте виден только bootstrap-загрузчик, и `org.telegram.messenger.ModManager`
не находится (возвращается `NULL`, регистрация молча пропадает). Резолвьте `ModManager`
через **загрузчик приложения**, полученный из `ActivityThread.currentApplication()`.

Минимальный рабочий `JNI_OnLoad` (полный — в `mods/template/mod.c`):

```c
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    // 1. Получаем загрузчик приложения
    jobject loader = ... ActivityThread.currentApplication().getClassLoader() ...;

    // 2. Грузим вшитый DEX через InMemoryDexClassLoader(parent = loader)
    jobject imdc = NewObject(InMemoryDexClassLoader, ByteBuffer.wrap(DEX_BYTES), loader);

    // 3. Класс входа мода
    jclass entryClass = (jclass) CallObjectMethod(imdc, loadClass, "org.nitrogram.mod.ModEntry");

    // 4. РЕГИСТРАЦИЯ через загрузчик приложения (НЕ FindClass!):
    jclass mmClass = (jclass) CallObjectMethod(loader, loadClass,
                                               "org.telegram.messenger.ModManager");
    CallStaticVoidMethod(mmClass, onModEntryClass, entryClass);

    // 5. Запуск логики мода (обязательно после регистрации)
    CallStaticVoidMethod(entryClass, apply);
    return JNI_VERSION_1_6;
}
```

Если `onModEntryClass` не вызван — экран настроек не откроется («у мода нет настроек»).

---

## 6. Сборка

Цепочка (скрипт `build.bat` уже есть в шаблоне):

```
javac  -encoding UTF-8 -cp "android.jar;sdk.jar" -d dexout ModEntry.java
jar    cf classes.jar -C dexout .
d8     --output dexbuild --min-api 21 classes.jar        # -> dexbuild/classes.dex
python genheader.py                                     # classes.dex -> dex_payload.h
clang  --target=aarch64-linux-android29 -shared -fPIC -o lib<id>_fx.so mod.c
```

Требуется:
- JDK 17, Android SDK `platforms/android-34/android.jar`, build-tools `d8`,
  NDK `clang` (arm64 target), Python (для `genheader.py`).

Результат — `lib<id>_fx.so`, готовый к импорту.

---

## 7. Установка и тестирование

1. Положите `.so` в `Download` телефона (или пришлите себе в чат).
2. В Nitrogram: «Моды» → импортировать из `Download`.
3. Карточка мода появится со иконкой, версией и (если `settings:true`) шестерёнкой.
4. Меняйте в моде логику → пересобирайте `.so` → удаляйте старую карточку → импортируйте снова.

---

## 8. Пример: `mods/motion-blur-fx`

Полностью рабочий мод — направленное размытие в движении (скролл/свайпы/меню) с
плавным затуханием. Содержит `ModEntry.java`, `motion_blur_mod.c`, `dex_payload.h`,
`build.bat`, `sdk.jar`. Отличный старт для экспериментов.

## 9. Стартовый шаблон: `mods/template`

Минимальный каркас: `ModEntry.java` (скелет с TODO), `mod.c` (готовый `JNI_OnLoad` с
метаданными-плейсхолдерами), `build.bat`, `genheader.py`, `sdk.jar`. Собирается в
валидный (пустой) мод — можно сразу импортировать и наращивать логику.
