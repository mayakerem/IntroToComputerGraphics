package edu.cg;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageProcessor extends FunctioalForEachLoops {
	// MARK: fields
	public final Logger logger;
	public final BufferedImage workingImage;
	public final RGBWeights rgbWeights;
	public final int inWidth;
	public final int inHeight;
	public final int workingImageType;
	public final int outWidth;
	public final int outHeight;

	// MARK: constructors
	public ImageProcessor(Logger logger, BufferedImage workingImage, RGBWeights rgbWeights, int outWidth,
			int outHeight) {
		super(); // initializing for each loops...

		this.logger = logger;
		this.workingImage = workingImage;
		this.rgbWeights = rgbWeights;
		inWidth = workingImage.getWidth();
		inHeight = workingImage.getHeight();
		workingImageType = workingImage.getType();
		this.outWidth = outWidth;
		this.outHeight = outHeight;
		setForEachInputParameters();
	}

	public ImageProcessor(Logger logger, BufferedImage workingImage, RGBWeights rgbWeights) {
		this(logger, workingImage, rgbWeights, workingImage.getWidth(), workingImage.getHeight());
	}

	// Changes the picture's hue - example
	public BufferedImage changeHue() {
		logger.log("Prepareing for hue changing...");

		int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;
		int max = rgbWeights.maxWeight;

		BufferedImage ans = newEmptyInputSizedImage();

		forEach((y, x) -> {
			Color c = new Color(workingImage.getRGB(x, y));
			int red = r * c.getRed() / max;
			int green = g * c.getGreen() / max;
			int blue = b * c.getBlue() / max;
			Color color = new Color(red, green, blue);
			ans.setRGB(x, y, color.getRGB());
		});

		logger.log("Changing hue done!");

		return ans;
	}

	// Sets the ForEach parameters with the input dimensions
	public final void setForEachInputParameters() {
		setForEachParameters(inWidth, inHeight);
	}

	// Sets the ForEach parameters with the output dimensions
	public final void setForEachOutputParameters() {
		setForEachParameters(outWidth, outHeight);
	}

	// A helper method that creates an empty image with the specified input dimensions.
	public final BufferedImage newEmptyInputSizedImage() {
		return newEmptyImage(inWidth, inHeight);
	}

	// A helper method that creates an empty image with the specified output dimensions.
	public final BufferedImage newEmptyOutputSizedImage() {
		return newEmptyImage(outWidth, outHeight);
	}

	// A helper method that creates an empty image with the specified dimensions.
	public final BufferedImage newEmptyImage(int width, int height) {
		return new BufferedImage(width, height, workingImageType);
	}

	// A helper method that deep copies the current working image.
	public final BufferedImage duplicateWorkingImage() {
		BufferedImage output = newEmptyInputSizedImage();

		forEach((y, x) -> output.setRGB(x, y, workingImage.getRGB(x, y)));

		return output;
	}

	public BufferedImage greyscale() {
		logger.log("Prepareing for greyscaling...");
		// Setting up a new Image which will have the greyscale result
		BufferedImage ans = newEmptyInputSizedImage();
		// Iterating over each pixel
		forEach((y, x) -> {
			// Take the original color of hte pixel
			Color c = new Color(workingImage.getRGB(x, y));
			// Implementing provided greyscale formula
			double nom = (c.getRed() * rgbWeights.redWeight) + (c.getGreen() * rgbWeights.greenWeight)
					+ (c.getBlue() * rgbWeights.blueWeight);
			double denom = rgbWeights.redWeight + rgbWeights.greenWeight + rgbWeights.blueWeight;
			double g = (nom/denom);
			// Converting to int
			int gToInt = (int) g;
			Color color = new Color(gToInt, gToInt, gToInt);
			// Setting new color of the pixel
			ans.setRGB(x, y, color.getRGB());
		});

		logger.log("Changing to greyscale done!");

		return ans;
	}

	public BufferedImage nearestNeighbor() {
		logger.log("Prepareing for Nearest Neighbor resize...");
		// Obtaining the ratio of the image inrelation to the new image and the working image
		double wRatio = (double)inWidth / outWidth;
		double hRatio = (double)inHeight / outHeight;
		// Setting up a new Image which will have resize result using NN
		BufferedImage ans = newEmptyOutputSizedImage();
		// Applying the parameters for the new image
		setForEachOutputParameters();
		// Iterating over all pixels in the image
		forEach((y, x) -> {
			// Finding the original (x,y) coordinate we want to take the color from
			int xOrig = (int)Math.floor(x * wRatio);
			int yOrig = (int)Math.floor(y * hRatio);
			// Taking the color
			Color c = new Color(workingImage.getRGB(xOrig, yOrig));
			// Setting the color to the x,y of the new Image
			ans.setRGB(x , y, c.getRGB());
		});

		logger.log("Nearest Neighbor resize done!");

		return ans;
	}

}
