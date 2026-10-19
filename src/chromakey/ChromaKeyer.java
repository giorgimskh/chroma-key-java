package chromakey;

import java.awt.image.BufferedImage;

public class ChromaKeyer {

    private static final int TRANSPARENT = 0x00000000;

    private final int keyColor;
    private final double distance;

    public ChromaKeyer(int keyColor, double distance) {
        this.keyColor = keyColor;
        this.distance = distance;
    }

    /**
     * Replaces every pixel whose color is closer than {@code distance} to the key color.
     * With a background, the replacement is the background pixel at (x - offsetX, y - offsetY);
     * where the background does not cover the input (different size or offset), or when no
     * background is given, the pixel becomes transparent.
     */
    public BufferedImage apply(BufferedImage input, BufferedImage background, int offsetX, int offsetY) {
        int width = input.getWidth();
        int height = input.getHeight();
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = input.getRGB(x, y);
                if (ColorUtils.distance(pixel, keyColor) < distance) {
                    output.setRGB(x, y, replacement(background, x - offsetX, y - offsetY));
                } else {
                    output.setRGB(x, y, pixel);
                }
            }
        }
        return output;
    }

    private static int replacement(BufferedImage background, int bx, int by) {
        if (background == null
                || bx < 0 || by < 0
                || bx >= background.getWidth() || by >= background.getHeight()) {
            return TRANSPARENT;
        }
        return background.getRGB(bx, by);
    }
}
