package chromakey;

import java.awt.image.BufferedImage;

public class ChromaKeyer {

    private static final int TRANSPARENT = 0x00000000;

    private final int keyColor;
    private final double distance;
    private final double softness;
    private final boolean despill;
    private final int keyChannel;

    public ChromaKeyer(int keyColor, double distance) {
        this(keyColor, distance, 0, false);
    }

    /**
     * @param softness width of the band beyond {@code distance} in which pixels are blended
     *                 with the replacement instead of being kept as-is (0 = hard edges)
     * @param despill  whether to remove the key color's tint from pixels that are kept
     */
    public ChromaKeyer(int keyColor, double distance, double softness, boolean despill) {
        this.keyColor = keyColor;
        this.distance = distance;
        this.softness = softness;
        this.despill = despill;
        this.keyChannel = dominantChannel(keyColor);
    }

    /**
     * Replaces every pixel whose color is closer than {@code distance} to the key color.
     * With a background, the replacement is the background pixel at (x - offsetX, y - offsetY);
     * where the background does not cover the input (different size or offset), or when no
     * background is given, the pixel becomes transparent. Pixels between {@code distance} and
     * {@code distance + softness} are blended with the replacement, more opaque the farther
     * they are from the key color.
     */
    public BufferedImage apply(BufferedImage input, BufferedImage background, int offsetX, int offsetY) {
        int width = input.getWidth();
        int height = input.getHeight();
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = input.getRGB(x, y);
                double d = ColorUtils.distance(pixel, keyColor);
                if (d < distance) {
                    output.setRGB(x, y, replacement(background, x - offsetX, y - offsetY));
                    continue;
                }

                int kept = despill ? despill(pixel) : pixel;
                if (d < distance + softness) {
                    double opacity = (d - distance) / softness;
                    kept = ColorUtils.blend(kept, replacement(background, x - offsetX, y - offsetY), opacity);
                }
                output.setRGB(x, y, kept);
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

    /** Limits the key color's dominant channel to the average of the other two channels. */
    private int despill(int argb) {
        int[] c = {ColorUtils.red(argb), ColorUtils.green(argb), ColorUtils.blue(argb)};
        int limit = (c[(keyChannel + 1) % 3] + c[(keyChannel + 2) % 3]) / 2;
        if (c[keyChannel] <= limit) {
            return argb;
        }
        c[keyChannel] = limit;
        return ColorUtils.argb(ColorUtils.alpha(argb), c[0], c[1], c[2]);
    }

    /** 0 = red, 1 = green, 2 = blue. */
    private static int dominantChannel(int argb) {
        int r = ColorUtils.red(argb);
        int g = ColorUtils.green(argb);
        int b = ColorUtils.blue(argb);
        if (g >= r && g >= b) {
            return 1;
        }
        return b >= r ? 2 : 0;
    }
}
