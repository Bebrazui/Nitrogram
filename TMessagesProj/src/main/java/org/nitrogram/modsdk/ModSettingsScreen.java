package org.nitrogram.modsdk;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Единый стандарт экранов настроек мода.
 *
 * Мод описывает свой экран настроек в коде: реализует этот интерфейс и
 * возвращает экземпляр через статический метод {@code ModEntry.createSettingsScreen()}.
 * Клиент хостит полученный экран внутри своего фрагмента и сам сохраняет значения
 * через {@link ModSettingsHost}. Если у мода нет настроек, метод должен вернуть null
 * (или не быть объявленным вовсе).
 */
public interface ModSettingsScreen {

    /**
     * Построить View экрана настроек.
     *
     * @param context контекст
     * @param host    хост для чтения/записи значений (персистентность за клиентом)
     * @param parent  родительский контейнер (можно использовать для addView или проигнорировать)
     * @return корневая View экрана либо null
     */
    View createView(Context context, ModSettingsHost host, ViewGroup parent);
}
