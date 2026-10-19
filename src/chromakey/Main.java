package chromakey;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0 || contains(args, "-h") || contains(args, "--help")) {
            System.out.println(ArgsParser.usage());
            return;
        }

        try {
            ArgsParser.Options options = ArgsParser.parse(args);

            BufferedImage input = readImage(options.inputPath, "input");
            BufferedImage background = options.backgroundPath == null
                    ? null
                    : readImage(options.backgroundPath, "background");

            ChromaKeyer keyer = new ChromaKeyer(options.keyColor, options.distance, options.softness, options.despill);
            BufferedImage result = keyer.apply(input, background, options.offsetX, options.offsetY);

            writePng(result, options.outputPath);
            System.out.println("Saved result to " + options.outputPath);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            System.err.println(ArgsParser.usage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(2);
        }
    }

    private static BufferedImage readImage(String path, String label) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("The " + label + " image was not found: " + path);
        }
        if (!file.isFile()) {
            throw new IOException("The " + label + " image path is not a file: " + path);
        }
        if (!file.canRead()) {
            throw new IOException("The " + label + " image cannot be read (permission denied): " + path);
        }

        BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            throw new IOException("Could not read the " + label + " image '" + path + "': " + e.getMessage(), e);
        }
        if (image == null) {
            throw new IOException("The " + label + " image format is not supported: " + path
                    + " (use PNG, JPEG, BMP, GIF or WBMP)");
        }
        return image;
    }

    private static void writePng(BufferedImage image, String path) throws IOException {
        File file = new File(path);
        File parent = file.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create the output directory: " + parent);
        }
        try {
            if (!ImageIO.write(image, "png", file)) {
                throw new IOException("No PNG writer is available in this Java runtime.");
            }
        } catch (IOException e) {
            throw new IOException("Could not write the output image '" + path + "': " + e.getMessage(), e);
        }
    }

    private static boolean contains(String[] args, String flag) {
        for (String arg : args) {
            if (arg.equals(flag)) {
                return true;
            }
        }
        return false;
    }
}
