package me.panjohnny.trenovacmaturity.pdf;

import java.awt.image.BufferedImage;

public class ImageUtil {
    public static boolean isBlack(BufferedImage image, int x, int y) {
        int rgb = image.getRGB(x, y);
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        int brightness = (r + g + b) / 3;

        // získané analýzou černé barvy (bolest), mělo by být 33, ale některý odstíny šedý se tam renderujou, takže takhle no
        return brightness < 45;
    }
}
