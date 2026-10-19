# Chroma Key (Java)

A small command-line green-screen tool written in plain Java. It removes a solid
background color from an image and either replaces it with another image or makes
it transparent. The output is saved as a PNG.

No external libraries: only `java.awt.image.BufferedImage` and `javax.imageio.ImageIO`.

## How it works

For every pixel, the program measures the Euclidean distance between the pixel's
color and the key color:

```
distance = sqrt((R1 - R2)^2 + (G1 - G2)^2 + (B1 - B2)^2)
```

- If `distance < tolerance`, the pixel is replaced with the background pixel at the
  same position (shifted by the `-p` offset). If there is no background image, or
  the background doesn't cover that pixel, the pixel becomes transparent.
- Otherwise, the original pixel is kept.

The largest possible distance is about `441.67` (black vs. white).

## Requirements

- Java 11 or newer (JDK)

## Build

```bash
javac -d out src/chromakey/*.java
```

## Usage

```bash
java -cp out chromakey.Main -i <input> -o <output.png> -c <color> -d <distance> [-b <background>] [-p <x,y>]
```

| Option | Required | Description |
|--------|----------|-------------|
| `-i`   | yes | Input image path (photo with the green background) |
| `-o`   | yes | Output image path, must end in `.png` |
| `-c`   | yes | Key color: `"0,255,0"`, `"0 255 0"`, `"#00FF00"` or `"#0F0"` |
| `-d`   | yes | Distance / tolerance, a number `>= 0` |
| `-b`   | no  | Background image path. If omitted, keyed pixels become transparent |
| `-p`   | no  | Background offset as `x,y` (default `0,0`). Negative values are allowed |
| `-h`   | no  | Show help |

Supported input formats are whatever `ImageIO` can read: PNG, JPEG, BMP, GIF, WBMP.

## Examples

Replace a green background with another image:

```bash
java -cp out chromakey.Main -i photo.jpg -o result.png -c "0,255,0" -d 120 -b beach.jpg
```

Make the green background transparent:

```bash
java -cp out chromakey.Main -i photo.jpg -o cutout.png -c "#00FF00" -d 120
```

Shift the background 50 px left and 30 px down:

```bash
java -cp out chromakey.Main -i photo.jpg -o result.png -c "#00FF00" -d 120 -b beach.jpg -p -50,30
```

## Background of a different size

The background is not resized. It is placed with its top-left corner at the `-p`
offset relative to the input image:

- If it is larger than the input, only the overlapping part is used (move it with `-p`).
- If it is smaller, or the offset leaves gaps, keyed pixels outside it become transparent.

The output always has the same size as the input image.

## Choosing a distance

- Too low: green edges and shadows stay visible.
- Too high: parts of the subject (green clothes, skin tones) start disappearing.
- Start around `100`–`150` for a typical green screen and adjust.

## Error handling

The program prints a clear message and exits with a non-zero code when:

- a required option is missing or an unknown option is given (exit code `1`)
- the color, distance or position has an invalid format (exit code `1`)
- an image file does not exist, is not readable, or has an unsupported format (exit code `2`)
- the output file cannot be written (exit code `2`)

## Project structure

```
src/chromakey/
├── Main.java          entry point, file I/O and error reporting
├── ArgsParser.java    command-line parsing and validation
├── ChromaKeyer.java   the chroma key algorithm
└── ColorUtils.java    color parsing and RGB distance
```
