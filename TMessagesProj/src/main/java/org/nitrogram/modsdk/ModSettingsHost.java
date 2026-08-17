package org.nitrogram.modsdk;

/**
 * Хост, через который мод читает и сохраняет значения настроек.
 * Персистентность полностью за клиентом: мод только вызывает getValue/setValue.
 */
public interface ModSettingsHost {

    /** Прочитать сырое значение (Boolean / Integer / Double / String) либо def. */
    Object getValue(String key, Object def);

    /** Сохранить значение (Boolean / Integer / Float / Double / String). */
    void setValue(String key, Object value);

    boolean getBool(String key, boolean def);

    int getInt(String key, int def);

    float getFloat(String key, float def);

    String getString(String key, String def);
}
