package me.panjohnny.trenovacmaturity.pdf;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageUtil {
    public static final int DPI = 150;
    public static final int TABLE_Y_THRESHOLD = 10 * DPI / 150;
    public static final int MIN_REGION_HEIGHT = 100 * DPI / 150; // n pixels at 150 DPI

    public static final int BLACK_THRESHOLD_EXAM = 45;
    public static final int BLACK_THRESHOLD_ANSWER_SET = 130;
    public static final int MIN_ANSWER_SET_REGION_HEIGHT = 20 * DPI / 150;

    public static boolean isBlack(BufferedImage image, int x, int y, int treshold) {
        int rgb = image.getRGB(x, y);
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        int brightness = (r + g + b) / 3;

        // získané analýzou černé barvy (bolest), mělo by být 33, ale některý odstíny šedý se tam renderujou, takže takhle no
        return brightness < treshold;
    }

    public static boolean isBlackExam(BufferedImage image, int x, int y) {
        return isBlack(image, x, y, BLACK_THRESHOLD_EXAM);
    }

    public static boolean isBlackAnswerSet(BufferedImage image, int x, int y) {
        return isBlack(image, x, y, BLACK_THRESHOLD_ANSWER_SET);
    }

    public static List<Integer> detectHorizontalLines(BufferedImage image) {
        List<Integer> lines = new ArrayList<>();

        int lastLineY = -MIN_REGION_HEIGHT;
        loopHeight:
        for (int y = 0; y < image.getHeight(); y++) {
            int consecutiveBlackPixels = 0;
            for (int x = 0; x < image.getWidth(); x++) {
                if (isBlackExam(image, x, y)) {
                    consecutiveBlackPixels++;
                } else {
                    if (consecutiveBlackPixels > image.getWidth() * 0.7 && consecutiveBlackPixels < image.getWidth() * 0.81) {
                        // table check
                        if (!isBlackExam(image, x - 1, y + TABLE_Y_THRESHOLD) && !isBlackExam(image, x - 1, y - TABLE_Y_THRESHOLD) && lastLineY + MIN_REGION_HEIGHT < y) {
                            lines.add(y);
                            lastLineY = y;
                        }
                        continue loopHeight;
                    }
                    consecutiveBlackPixels = 0;
                }
            }
        }
        return lines;
    }

    public static List<Integer> detectHorizontalLinesOfHeight(BufferedImage image, int height) {
        List<Integer> lines = new ArrayList<>();

        int lastLineY = -height;

        for (int y = 0; y < image.getHeight() - height; y++) {
            int consecutiveBlackPixels = 0;
            int minX = -1;
            for (int x = 0; x < image.getWidth(); x++) {
                if (isBlackAnswerSet(image, x, y)) {
                    if (minX == -1) {
                        minX = x;
                    }
                    consecutiveBlackPixels++;
                } else {
                    if (consecutiveBlackPixels > image.getWidth() * 0.7) {
                        boolean allBlack = true;
                        int checkX = Math.min(image.getWidth(), minX + 10);
                        if (minX == -1) {
                            checkX = image.getWidth();
                        }

                        for (int h = y; h < y + height; h++) {
                            if (h >= image.getHeight() || !isBlackAnswerSet(image, checkX, h)) {
                                allBlack = false;
                                break;
                            }
                        }
                        if (allBlack && lastLineY + MIN_ANSWER_SET_REGION_HEIGHT < y) {
                            lines.add(y);
                            lastLineY = y;
                        }
                        break;
                    }
                    consecutiveBlackPixels = 0;
                }
            }
        }

        return lines;
    }

    public static BufferedImage removeWhitespace(BufferedImage img) {
        int topY = 0;

        final int margin = 60 * DPI / 150;

        int bottomY = img.getHeight() - margin;

        // Find bottom Y
        outerBottom:
        for (int y = img.getHeight() - margin; y >= 0; y--) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (isBlackExam(img, x, y)) {
                    bottomY = y + 1;
                    break outerBottom;
                }
            }
        }

        // Ensure crop height is within image bounds
        int cropBottom = Math.min(bottomY + margin, img.getHeight());
        if (cropBottom < 0) {
            cropBottom = 0;
        }
        int cropHeight = cropBottom - topY;
        if (cropHeight == 0) {
            // Nothing valid to crop; return original image
            return img;
        }

        // Crop the image
        return img.getSubimage(0, topY, img.getWidth(), cropHeight);
    }
}
