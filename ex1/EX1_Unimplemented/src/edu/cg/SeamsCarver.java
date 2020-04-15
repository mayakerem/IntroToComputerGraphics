package edu.cg;

import java.awt.image.BufferedImage;

public class SeamsCarver extends ImageProcessor {

	// MARK: An inner interface for functional programming.
	@FunctionalInterface
	interface ResizeOperation {
		BufferedImage resize();
	}

	// MARK: Fields
	private int numOfSeams;
	private ResizeOperation resizeOp;
	boolean[][] imageMask;
	// TODO: Add some additional fields
	/**
	 * Added these
	 */
	private int[][] greyScaleImage;
	private long[][] m;
	private int[][] minPaths;
	private int[][] xIndices;
	private boolean[][] shiftedMask;
	private int[][] seams;
	private int k;
	private boolean[][] maskAfterSeamCarving;


	
	
	
	
	public SeamsCarver(Logger logger, BufferedImage workingImage, int outWidth, RGBWeights rgbWeights,
			boolean[][] imageMask) {
		super((s) -> logger.log("Seam carving: " + s), workingImage, rgbWeights, outWidth, workingImage.getHeight());

		numOfSeams = Math.abs(outWidth - inWidth);
		this.imageMask = imageMask;
		if (inWidth < 2 | inHeight < 2)
			throw new RuntimeException("Can not apply seam carving: workingImage is too small");

		if (numOfSeams > inWidth / 2)
			throw new RuntimeException("Can not apply seam carving: too many seams...");

		// Setting resizeOp by with the appropriate method reference
		if (outWidth > inWidth)
			resizeOp = this::increaseImageWidth;
		else if (outWidth < inWidth)
			resizeOp = this::reduceImageWidth;
		else
			resizeOp = this::duplicateWorkingImage;

		// TODO: You may initialize your additional fields and apply some preliminary
		// calculations.
		/**
		 * Added these
		 *
		 */
		this.maskAfterSeamCarving = null;
		this.logger.log("begins preliminary calculations.");
		if (this.numOfSeams > 0) {
			this.logger.log("initializes some additional fields.");
			this.k = 0;
			this.m = new long[this.inHeight][this.inWidth];
			this.minPaths = new int[this.inHeight][this.inWidth];
			this.seams = new int[this.numOfSeams][this.inHeight];
			this.initGreyScaleImage();
			this.initXIndices();
			this.shiftedMask = this.duplicateWorkingMask();
			this.findSeams();
		}


		this.logger.log("preliminary calculations were ended.");
	}

	public BufferedImage resize() {
		return resizeOp.resize();
	}

	private BufferedImage reduceImageWidth() {
		// TODO: Implement this method, remove the exception.
		/**
		 * Added this
		 */
		this.logger.log("reduces image width by " + this.numOfSeams + " pixels.");
		final int[][] image = this.duplicateWorkingImageAs2DArray();
		int[][] seams;
		for (int length = (seams = this.seams).length, i = 0; i < length; ++i) {
			final int[] seam = seams[i];
			final Object o;
			final int x2;
			final Object o2;
			final int rgbMid;
			int rgbLeft;
			int rgbMix;
			int rgbRight;
			int rgbMix2;
			this.forEachHeight(y -> {
				x2 = o[y];
				rgbMid = o2[y][x2];
				if (x2 > 0) {
					rgbLeft = o2[y][x2 - 1];
					rgbMix = mixRGB(rgbMid, rgbLeft);
					o2[y][x2 - 1] = rgbMix;
				}
				if (x2 + 1 < this.inWidth) {
					rgbRight = o2[y][x2 + 1];
					rgbMix2 = mixRGB(rgbMid, rgbRight);
					o2[y][x2 + 1] = rgbMix2;
				}
				return;
			});
		}
		final BufferedImage ans = this.newEmptyOutputSizedImage();
		this.pushForEachParameters();
		this.setForEachWidth(this.outWidth);
		final int originalX;
		final Object o3;
		final int rgb;
		final Color c;
		final BufferedImage bufferedImage;
		this.forEach((y, x) -> {
			originalX = this.xIndices[y][x];
			rgb = o3[y][originalX];
			c = new Color(getRed(rgb), getGreen(rgb), getBlue(rgb));
			bufferedImage.setRGB(x, y, c.getRGB());
			return;
		});
		this.popForEachParameters();
		this.setMaskAfterWidthReduce();
		return ans;

		//throw new UnimplementedMethodException("reduceImageWidth");
	}

	private BufferedImage increaseImageWidth() {
		// TODO: Implement this method, remove the exception.
		/**
		 * Added this
		 */
		this.logger.log("increases image width by + " + this.numOfSeams + " pixels.");
		this.maskAfterSeamCarving = new boolean[this.inHeight][this.outWidth];
		final int[][] image = this.duplicateWorkingImageAs2DArray();
		final int[][] rotatedSeams = this.rotateSeams();
		final Object o;
		this.forEachHeight(y -> o[y] = this.merge(o[y], rotatedSeams[y], (int)y));
		final BufferedImage ans = this.newEmptyOutputSizedImage();
		this.pushForEachParameters();
		this.setForEachWidth(this.outWidth);
		final Object o2;
		final int rgb;
		final Color c;
		final BufferedImage bufferedImage;
		this.forEach((y, x) -> {
			rgb = o2[y][x];
			c = new Color(getRed(rgb), getGreen(rgb), getBlue(rgb));
			bufferedImage.setRGB(x, y, c.getRGB());
			return;
		});
		this.popForEachParameters();
		return ans;
//		throw new UnimplementedMethodException("increaseImageWidth");
	}

	public BufferedImage showSeams(int seamColorRGB) {
		// TODO: Implement this method (bonus), remove the exception.
		/**
		 * Added this
		 *
		 */

		final BufferedImage ans = this.duplicateWorkingImage();
		if (this.numOfSeams > 0) {
			int[][] seams;
			for (int length = (seams = this.seams).length, i = 0; i < length; ++i) {
				final int[] seam = seams[i];
				final Object o;
				final int x;
				final BufferedImage bufferedImage;
				this.forEachHeight(y -> {
					x = o[y];
					bufferedImage.setRGB(x, y, seamColorRGB);
					return;
				});
			}
		}
		return ans;
//	}
//		throw new UnimplementedMethodException("showSeams");
	}

	public boolean[][] getMaskAfterSeamCarving() {
		// TODO: Implement this method, remove the exception.
		// This method should return the mask of the resize image after seam carving.
		// Meaning, after applying Seam Carving on the input image,
		// getMaskAfterSeamCarving() will return a mask, with the same dimensions as the
		// resized image, where the mask values match the original mask values for the
		// corresponding pixels.
		// HINT: Once you remove (replicate) the chosen seams from the input image, you
		// need to also remove (replicate) the matching entries from the mask as well.
		throw new UnimplementedMethodException("getMaskAfterSeamCarving");
	}
}
