package chromakey;

public final class ArgsParser {

    public static final class Options {
        public String inputPath;
        public String outputPath;
        public int keyColor;
        public double distance;
        public String backgroundPath;
        public int offsetX;
        public int offsetY;
    }

    private ArgsParser() {
    }

    public static Options parse(String[] args) {
        Options options = new Options();
        String color = null;
        String distance = null;
        String position = null;

        for (int i = 0; i < args.length; i++) {
            String flag = args[i];
            switch (flag) {
                case "-i":
                    options.inputPath = value(args, ++i, flag);
                    break;
                case "-o":
                    options.outputPath = value(args, ++i, flag);
                    break;
                case "-c":
                    color = value(args, ++i, flag);
                    break;
                case "-d":
                    distance = value(args, ++i, flag);
                    break;
                case "-b":
                    options.backgroundPath = value(args, ++i, flag);
                    break;
                case "-p":
                    position = value(args, ++i, flag);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown option: " + flag);
            }
        }

        if (options.inputPath == null) {
            throw new IllegalArgumentException("Missing required option -i (input image path).");
        }
        if (options.outputPath == null) {
            throw new IllegalArgumentException("Missing required option -o (output image path).");
        }
        if (color == null) {
            throw new IllegalArgumentException("Missing required option -c (key color, e.g. \"0,255,0\" or \"#00FF00\").");
        }
        if (distance == null) {
            throw new IllegalArgumentException("Missing required option -d (distance / tolerance).");
        }
        if (!options.outputPath.toLowerCase().endsWith(".png")) {
            throw new IllegalArgumentException("Output path must end with .png: " + options.outputPath);
        }

        options.keyColor = ColorUtils.parseColor(color);
        options.distance = parseDistance(distance);

        if (position != null) {
            int[] xy = parsePosition(position);
            options.offsetX = xy[0];
            options.offsetY = xy[1];
        }
        return options;
    }

    public static String usage() {
        return String.join(System.lineSeparator(),
                "Usage: java -cp out chromakey.Main -i <input> -o <output.png> -c <color> -d <distance> [-b <background>] [-p <x,y>]",
                "",
                "Options:",
                "  -i  input image path (photo with the green background)",
                "  -o  output image path (PNG)",
                "  -c  key color, e.g. \"0,255,0\" or \"#00FF00\"",
                "  -d  distance (tolerance), a number >= 0, max ~441.67",
                "  -b  background image path (optional; keyed pixels become transparent if omitted)",
                "  -p  background position as x,y offset (optional, default 0,0)",
                "  -h  show this help");
    }

    private static String value(String[] args, int index, String flag) {
        if (index >= args.length || isFlag(args[index])) {
            throw new IllegalArgumentException("Option " + flag + " requires a value.");
        }
        return args[index];
    }

    private static boolean isFlag(String s) {
        return s.matches("-[iocdbph]");
    }

    private static double parseDistance(String text) {
        double value;
        try {
            value = Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Distance must be a number, got: \"" + text + "\"");
        }
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0) {
            throw new IllegalArgumentException("Distance must be a finite number >= 0, got: " + text);
        }
        return value;
    }

    private static int[] parsePosition(String text) {
        String[] parts = text.trim().split("\\s*,\\s*");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Position must be in the form x,y (e.g. \"10,20\"), got: \"" + text + "\"");
        }
        try {
            return new int[] {Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Position values must be whole numbers, got: \"" + text + "\"");
        }
    }
}
