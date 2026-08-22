package org.telegram.messenger;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public final class M3ShapeHelper {

    public static final int SHAPE_CIRCLE = 0;
    public static final int SHAPE_SQUIRCLE = 1;
    public static final int SHAPE_SQUARE = 2;
    public static final int SHAPE_DIAMOND = 3;
    public static final int SHAPE_CLAMSHELL = 4;
    public static final int SHAPE_PENTAGON = 5;
    public static final int SHAPE_GEM = 6;
    public static final int SHAPE_VERY_SUNNY = 7;
    public static final int SHAPE_SUNNY = 8;
    public static final int SHAPE_COOKIE_4 = 9;
    public static final int SHAPE_COOKIE_6 = 10;
    public static final int SHAPE_COOKIE_7 = 11;
    public static final int SHAPE_COOKIE_9 = 12;
    public static final int SHAPE_COOKIE_12 = 13;
    public static final int SHAPE_CLOVER_4 = 14;
    public static final int SHAPE_CLOVER_8 = 15;
    public static final int SHAPE_BURST = 16;
    public static final int SHAPE_SOFT_BURST = 17;
    public static final int SHAPE_BOOM = 18;
    public static final int SHAPE_SOFT_BOOM = 19;
    public static final int SHAPE_FLOWER = 20;
    public static final int SHAPE_PUFFY = 21;
    public static final int SHAPE_PUFFY_DIAMOND = 22;
    public static final int SHAPE_TEARDROP = 23;
    public static final int SHAPE_CUT_CORNER = 24;
    public static final int SHAPE_HEXAGON = 25;

    public static final int SHAPES_COUNT = 26;

    public static class ShapeInfo {
        public final int id;
        public final String name;
        public final String category;

        public ShapeInfo(int id, String name, String category) {
            this.id = id;
            this.name = name;
            this.category = category;
        }
    }

    public static final ShapeInfo[] ALL_SHAPES = new ShapeInfo[]{
        new ShapeInfo(SHAPE_CIRCLE, "Circle (Круг)", "Классические"),
        new ShapeInfo(SHAPE_SQUIRCLE, "Squircle (Сквиркл)", "Классические"),
        new ShapeInfo(SHAPE_SQUARE, "Square (Квадрат)", "Классические"),
        new ShapeInfo(SHAPE_DIAMOND, "Diamond (Ромб)", "Геометрические"),
        new ShapeInfo(SHAPE_CLAMSHELL, "Clamshell (Ракушка)", "Геометрические"),
        new ShapeInfo(SHAPE_PENTAGON, "Pentagon (Пятиугольник)", "Геометрические"),
        new ShapeInfo(SHAPE_GEM, "Gem (Драгоценный камень)", "Геометрические"),
        new ShapeInfo(SHAPE_HEXAGON, "Hexagon (Шестиугольник)", "Геометрические"),
        new ShapeInfo(SHAPE_VERY_SUNNY, "Very sunny (Яркое солнце)", "Expressive M3"),
        new ShapeInfo(SHAPE_SUNNY, "Sunny (Солнце)", "Expressive M3"),
        new ShapeInfo(SHAPE_COOKIE_4, "4-sided cookie (Печенье 4)", "Expressive M3"),
        new ShapeInfo(SHAPE_COOKIE_6, "6-sided cookie (Печенье 6)", "Expressive M3"),
        new ShapeInfo(SHAPE_COOKIE_7, "7-sided cookie (Печенье 7)", "Expressive M3"),
        new ShapeInfo(SHAPE_COOKIE_9, "9-sided cookie (Печенье 9)", "Expressive M3"),
        new ShapeInfo(SHAPE_COOKIE_12, "12-sided cookie (Печенье 12)", "Expressive M3"),
        new ShapeInfo(SHAPE_CLOVER_4, "4-leaf clover (Клевер 4)", "Expressive M3"),
        new ShapeInfo(SHAPE_CLOVER_8, "8-leaf clover (Клевер 8)", "Expressive M3"),
        new ShapeInfo(SHAPE_BURST, "Burst (Вспышка)", "Expressive M3"),
        new ShapeInfo(SHAPE_SOFT_BURST, "Soft burst (Мягкая вспышка)", "Expressive M3"),
        new ShapeInfo(SHAPE_BOOM, "Boom (Взрыв)", "Expressive M3"),
        new ShapeInfo(SHAPE_SOFT_BOOM, "Soft boom (Мягкий взрыв)", "Expressive M3"),
        new ShapeInfo(SHAPE_FLOWER, "Flower (Цветок)", "Expressive M3"),
        new ShapeInfo(SHAPE_PUFFY, "Puffy (Облачко)", "Expressive M3"),
        new ShapeInfo(SHAPE_PUFFY_DIAMOND, "Puffy diamond (Пышный ромб)", "Expressive M3"),
        new ShapeInfo(SHAPE_TEARDROP, "Teardrop (Капля)", "Специальные"),
        new ShapeInfo(SHAPE_CUT_CORNER, "Cut Corner (Скошенные углы)", "Специальные")
    };

    public static String getShapeName(int shapeId) {
        for (ShapeInfo s : ALL_SHAPES) {
            if (s.id == shapeId) return s.name;
        }
        return "Circle (Круг)";
    }

    public static Path getShapePath(int shape, RectF rect) {
        Path path = new Path();
        float w = rect.width();
        float h = rect.height();
        float cx = rect.centerX();
        float cy = rect.centerY();
        float size = Math.min(w, h);
        float r = size / 2.0f;

        switch (shape) {
            case SHAPE_DIAMOND: {
                path.moveTo(cx, rect.top);
                path.lineTo(rect.right, cy);
                path.lineTo(cx, rect.bottom);
                path.lineTo(rect.left, cy);
                path.close();
                break;
            }
            case SHAPE_CLAMSHELL: {
                float corner = size * 0.35f;
                path.moveTo(rect.left + corner, rect.top);
                path.lineTo(rect.right - corner, rect.top);
                path.lineTo(rect.right, cy);
                path.lineTo(rect.right - corner, rect.bottom);
                path.lineTo(rect.left + corner, rect.bottom);
                path.lineTo(rect.left, cy);
                path.close();
                break;
            }
            case SHAPE_PENTAGON: {
                for (int i = 0; i < 5; i++) {
                    double angle = Math.toRadians(-90 + 72 * i);
                    float x = (float) (cx + r * Math.cos(angle));
                    float y = (float) (cy + r * Math.sin(angle));
                    if (i == 0) path.moveTo(x, y);
                    else path.lineTo(x, y);
                }
                path.close();
                break;
            }
            case SHAPE_GEM: {
                float topWidth = size * 0.35f;
                float sideHeight = size * 0.35f;
                path.moveTo(cx - topWidth, rect.top);
                path.lineTo(cx + topWidth, rect.top);
                path.lineTo(rect.right, rect.top + sideHeight);
                path.lineTo(cx + topWidth, rect.bottom);
                path.lineTo(cx - topWidth, rect.bottom);
                path.lineTo(rect.left, rect.top + sideHeight);
                path.close();
                break;
            }
            case SHAPE_HEXAGON: {
                for (int i = 0; i < 6; i++) {
                    double angle = Math.toRadians(-90 + 60 * i);
                    float x = (float) (cx + r * Math.cos(angle));
                    float y = (float) (cy + r * Math.sin(angle));
                    if (i == 0) path.moveTo(x, y);
                    else path.lineTo(x, y);
                }
                path.close();
                break;
            }
            case SHAPE_SUNNY: {
                buildStarPath(path, cx, cy, 8, r, r * 0.82f);
                break;
            }
            case SHAPE_VERY_SUNNY: {
                buildStarPath(path, cx, cy, 10, r, r * 0.72f);
                break;
            }
            case SHAPE_COOKIE_4: {
                buildScallopPath(path, cx, cy, r, 4, 0.16f);
                break;
            }
            case SHAPE_COOKIE_6: {
                buildScallopPath(path, cx, cy, r, 6, 0.12f);
                break;
            }
            case SHAPE_COOKIE_7: {
                buildScallopPath(path, cx, cy, r, 7, 0.11f);
                break;
            }
            case SHAPE_COOKIE_9: {
                buildScallopPath(path, cx, cy, r, 9, 0.09f);
                break;
            }
            case SHAPE_COOKIE_12: {
                buildScallopPath(path, cx, cy, r, 12, 0.07f);
                break;
            }
            case SHAPE_CLOVER_4: {
                buildCloverPath(path, cx, cy, r, 4);
                break;
            }
            case SHAPE_CLOVER_8: {
                buildCloverPath(path, cx, cy, r, 8);
                break;
            }
            case SHAPE_BURST: {
                buildStarPath(path, cx, cy, 12, r, r * 0.65f);
                break;
            }
            case SHAPE_SOFT_BURST: {
                buildStarPath(path, cx, cy, 8, r, r * 0.70f);
                break;
            }
            case SHAPE_BOOM: {
                buildStarPath(path, cx, cy, 16, r, r * 0.42f);
                break;
            }
            case SHAPE_SOFT_BOOM: {
                buildStarPath(path, cx, cy, 16, r, r * 0.55f);
                break;
            }
            case SHAPE_FLOWER: {
                buildFlowerPath(path, cx, cy, r, 8);
                break;
            }
            case SHAPE_PUFFY: {
                buildScallopPath(path, cx, cy, r, 10, 0.15f);
                break;
            }
            case SHAPE_PUFFY_DIAMOND: {
                buildCloverPath(path, cx, cy, r, 4);
                break;
            }
            case SHAPE_TEARDROP: {
                float[] radii = new float[]{ r, r, 0, 0, r, r, 0, 0 };
                path.addRoundRect(rect, radii, Path.Direction.CW);
                break;
            }
            case SHAPE_CUT_CORNER: {
                float cut = size * 0.22f;
                path.moveTo(rect.left + cut, rect.top);
                path.lineTo(rect.right - cut, rect.top);
                path.lineTo(rect.right, rect.top + cut);
                path.lineTo(rect.right, rect.bottom - cut);
                path.lineTo(rect.right - cut, rect.bottom);
                path.lineTo(rect.left + cut, rect.bottom);
                path.lineTo(rect.left, rect.bottom - cut);
                path.lineTo(rect.left, rect.top + cut);
                path.close();
                break;
            }
            case SHAPE_SQUIRCLE: {
                path.addRoundRect(rect, size * 0.28f, size * 0.28f, Path.Direction.CW);
                break;
            }
            case SHAPE_SQUARE: {
                path.addRect(rect, Path.Direction.CW);
                break;
            }
            case SHAPE_CIRCLE:
            default: {
                path.addCircle(cx, cy, r, Path.Direction.CW);
                break;
            }
        }
        return path;
    }

    private static void buildStarPath(Path path, float cx, float cy, int points, float rOuter, float rInner) {
        int total = points * 2;
        for (int i = 0; i < total; i++) {
            double angle = Math.toRadians(-90 + (360.0 / total) * i);
            float curR = (i % 2 == 0) ? rOuter : rInner;
            float x = (float) (cx + curR * Math.cos(angle));
            float y = (float) (cy + curR * Math.sin(angle));
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.close();
    }

    private static void buildScallopPath(Path path, float cx, float cy, float rBase, int waves, float amplitude) {
        int steps = 180;
        for (int i = 0; i < steps; i++) {
            double theta = Math.toRadians((360.0 / steps) * i);
            double curR = rBase * (1.0 - amplitude + amplitude * Math.cos(waves * theta));
            float x = (float) (cx + curR * Math.cos(theta - Math.PI / 2.0));
            float y = (float) (cy + curR * Math.sin(theta - Math.PI / 2.0));
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.close();
    }

    private static void buildCloverPath(Path path, float cx, float cy, float rBase, int leaves) {
        int steps = 180;
        for (int i = 0; i < steps; i++) {
            double theta = Math.toRadians((360.0 / steps) * i);
            double curR = rBase * (0.68 + 0.32 * Math.abs(Math.cos(leaves * theta / 2.0)));
            float x = (float) (cx + curR * Math.cos(theta - Math.PI / 2.0));
            float y = (float) (cy + curR * Math.sin(theta - Math.PI / 2.0));
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.close();
    }

    private static void buildFlowerPath(Path path, float cx, float cy, float rBase, int petals) {
        int steps = 180;
        for (int i = 0; i < steps; i++) {
            double theta = Math.toRadians((360.0 / steps) * i);
            double curR = rBase * (0.65 + 0.35 * Math.abs(Math.sin(petals * theta / 2.0)));
            float x = (float) (cx + curR * Math.cos(theta - Math.PI / 2.0));
            float y = (float) (cy + curR * Math.sin(theta - Math.PI / 2.0));
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.close();
    }

    public static void drawM3Shape(Canvas canvas, RectF rect, int[] radius, Paint paint) {
        if (canvas == null) return;
        int shape = NitrogramConfig.getAvatarShape();
        if (shape == SHAPE_CIRCLE) {
            if (radius != null && radius[0] == 0) {
                canvas.drawRect(rect, paint);
            } else if (radius != null && radius[0] > 0) {
                canvas.drawRoundRect(rect, radius[0], radius[0], paint);
            } else {
                canvas.drawCircle(rect.centerX(), rect.centerY(), Math.min(rect.width(), rect.height()) / 2.0f, paint);
            }
            return;
        } else if (shape == SHAPE_SQUIRCLE) {
            float r = Math.min(rect.width(), rect.height()) * 0.28f;
            canvas.drawRoundRect(rect, r, r, paint);
            return;
        } else if (shape == SHAPE_SQUARE) {
            canvas.drawRect(rect, paint);
            return;
        }

        Path path = getShapePath(shape, rect);
        canvas.drawPath(path, paint);
    }
}
