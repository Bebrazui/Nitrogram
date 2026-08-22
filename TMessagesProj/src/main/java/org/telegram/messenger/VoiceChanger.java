package org.telegram.messenger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VoiceChanger {

    public static final int EFFECT_NONE = 0;
    public static final int EFFECT_CHIPMUNK = 1;
    public static final int EFFECT_DEEP = 2;
    public static final int EFFECT_ROBOT = 3;
    public static final int EFFECT_RADIO = 4;
    public static final int EFFECT_ECHO = 5;
    public static final int EFFECT_ALIEN = 6;

    private static int phase = 0;
    private static final short[] echoBuffer = new short[8000];
    private static int echoIndex = 0;

    public static int getEffect() {
        return NitrogramConfig.getVoiceEffect();
    }

    public static void setEffect(int effect) {
        NitrogramConfig.setVoiceEffect(effect);
    }

    public static String getEffectName(int effect) {
        switch (effect) {
            case EFFECT_CHIPMUNK: return "🐿 Бурундук (Высокий тон)";
            case EFFECT_DEEP: return "👹 Монстр (Низкий тон)";
            case EFFECT_ROBOT: return "🤖 Робот (Ring Modulator)";
            case EFFECT_RADIO: return "📻 Рация (Walkie-Talkie)";
            case EFFECT_ECHO: return "🎤 Эхо (Echo / Reverb)";
            case EFFECT_ALIEN: return "👽 Пришелец (Alien)";
            case EFFECT_NONE:
            default: return "Выключен (Обычный)";
        }
    }

    public static void process(ByteBuffer buffer, int sampleRate) {
        int effect = getEffect();
        if (effect == EFFECT_NONE || buffer == null) return;

        int pos = buffer.position();
        int limit = buffer.limit();
        int sampleCount = (limit - pos) / 2;
        if (sampleCount <= 0) return;

        buffer.order(ByteOrder.nativeOrder());
        short[] samples = new short[sampleCount];
        buffer.asShortBuffer().get(samples);

        switch (effect) {
            case EFFECT_CHIPMUNK: {
                for (int i = 0; i < sampleCount; i++) {
                    int src = (int) (i * 1.45f) % sampleCount;
                    samples[i] = samples[src];
                }
                break;
            }
            case EFFECT_DEEP: {
                for (int i = sampleCount - 1; i >= 0; i--) {
                    int src = (int) (i * 0.72f);
                    if (src < sampleCount) {
                        samples[i] = (short) (samples[src] * 1.15f);
                    }
                }
                break;
            }
            case EFFECT_ROBOT: {
                double freq = 55.0;
                for (int i = 0; i < sampleCount; i++) {
                    double carrier = Math.sin(2.0 * Math.PI * (phase++) * freq / sampleRate);
                    samples[i] = (short) (samples[i] * carrier);
                }
                break;
            }
            case EFFECT_RADIO: {
                for (int i = 0; i < sampleCount; i++) {
                    int val = (int) (samples[i] * 1.8f);
                    if (val > 18000) val = 18000;
                    else if (val < -18000) val = -18000;
                    samples[i] = (short) val;
                }
                break;
            }
            case EFFECT_ECHO: {
                for (int i = 0; i < sampleCount; i++) {
                    short prev = echoBuffer[echoIndex];
                    int mixed = samples[i] + (int) (prev * 0.45f);
                    if (mixed > 32767) mixed = 32767;
                    else if (mixed < -32768) mixed = -32768;
                    samples[i] = (short) mixed;
                    echoBuffer[echoIndex] = samples[i];
                    echoIndex = (echoIndex + 1) % echoBuffer.length;
                }
                break;
            }
            case EFFECT_ALIEN: {
                for (int i = 0; i < sampleCount; i++) {
                    double mod = Math.sin(2.0 * Math.PI * (phase++) * 220.0 / sampleRate) * 0.7;
                    samples[i] = (short) (samples[i] * (0.6 + mod));
                }
                break;
            }
        }

        buffer.position(pos);
        buffer.asShortBuffer().put(samples);
        buffer.position(pos);
    }
}
