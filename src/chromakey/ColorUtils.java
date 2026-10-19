package chromakey;

public final class ColorUtils {

    private ColorUtils() {
    }

    public static int red(int argb) {
        return (argb >> 16) & 0xFF;
    }

    public static int green(int argb) {
        return (argb >> 8) & 0xFF;
    }

    public static int blue(int argb) {
        return argb & 0xFF;
    }

    public static int alpha(int argb) {
        return (argb >>> 24) & 0xFF;
    }

    public static int rgb(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /** Euclidean distance between two colors in RGB space: sqrt(dR^2 + dG^2 + dB^2). */
    public static double distance(int c1, int c2) {
        int dr = red(c1) - red(c2);
        int dg = green(c1) - green(c2);
        int db = blue(c1) - blue(c2);
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    /** Parses "R,G,B", "R G B", "#RRGGBB" or "#RGB" into an opaque ARGB int. */
    public static int parseColor(String text) {
        String s = text.trim();
        if (s.startsWith("#")) {
            return parseHex(s.substring(1), text);
        }

        String[] parts = s.split("\\s*[,\\s]\\s*");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid color \"" + text + "\". Use \"R,G,B\" (e.g. 0,255,0) or \"#RRGGBB\" (e.g. #00FF00).");
        }
        int[] c = new int[3];
        for (int i = 0; i < 3; i++) {
            try {
                c[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid color component \"" + parts[i] + "\" in \"" + text + "\". Components must be whole numbers 0-255.");
            }
            if (c[i] < 0 || c[i] > 255) {
                throw new IllegalArgumentException("Color component " + c[i] + " in \"" + text + "\" is out of range 0-255.");
            }
        }
        return rgb(c[0], c[1], c[2]);
    }

    private static int parseHex(String hex, String original) {
        if (hex.length() == 3) {
            hex = "" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2);
        }
        if (!hex.matches("[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException("Invalid hex color \"" + original + "\". Use \"#RRGGBB\" (e.g. #00FF00).");
        }
        return 0xFF000000 | Integer.parseInt(hex, 16);
    }
}
