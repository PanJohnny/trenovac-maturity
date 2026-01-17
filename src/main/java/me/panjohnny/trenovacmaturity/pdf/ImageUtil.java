package me.panjohnny.trenovacmaturity.pdf;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageUtil {
    public static final int DPI = 150;
    public static final int TABLE_Y_THRESHOLD = 10 * DPI / 150;
    public static final int MIN_REGION_HEIGHT = 100 * DPI / 150; // n pixels at 150 DPI

    public static boolean isBlack(BufferedImage image, int x, int y) {
        int rgb = image.getRGB(x, y);
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        int brightness = (r + g + b) / 3;

        // získané analýzou černé barvy (bolest), mělo by být 33, ale některý odstíny šedý se tam renderujou, takže takhle no
        return brightness < 45;
    }

    public static List<Integer> detectHorizontalLines(BufferedImage image) {
        List<Integer> lines = new ArrayList<>();

        int lastLineY = -MIN_REGION_HEIGHT;
        loopHeight: for (int y = 0; y < image.getHeight(); y++) {
            int consecutiveBlackPixels = 0;
            for (int x = 0; x < image.getWidth(); x++) {
                if (isBlack(image, x, y)) {
                    consecutiveBlackPixels++;
                } else {
                    if (consecutiveBlackPixels > image.getWidth() * 0.7 && consecutiveBlackPixels < image.getWidth() * 0.81) {
                        // table check
                        if (!isBlack(image, x-1, y + TABLE_Y_THRESHOLD) && !isBlack(image, x-1, y - TABLE_Y_THRESHOLD) && lastLineY + MIN_REGION_HEIGHT < y) {
                            lines.add(y);
                            lastLineY = y;
                            System.out.println("Detected line at y=" + y);
                        } else {
                            System.out.println("Skipped line at y=" + y + " due to table detection.");
                        }
                        continue loopHeight;
                    }
                    consecutiveBlackPixels = 0;
                }
            }
        }
        return lines;
    }
}
