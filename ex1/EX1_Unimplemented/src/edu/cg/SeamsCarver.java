package edu.cg;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;

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

		boolean[][] isChosen;
		long[][] costMatrix;
		int[][] workingImageGreyscaled;
		int[][] indecies;
		char[][] backTrack;
		int curWidth;


		public SeamsCarver(Logger logger, BufferedImage workingImage, int outWidth, RGBWeights rgbWeights,
						   boolean[][] imageMask) {
			super((s) -> logger.log("Seam carving: " + s), workingImage, rgbWeights, outWidth, workingImage.getHeight());

			numOfSeams = Math.abs(outWidth - inWidth);
			this.imageMask = imageMask;
			if (inWidth < 2 | inHeight < 2)
				throw new RuntimeException("Can not apply seam carving: workingImage is too small");

			if (numOfSeams > inWidth / 2)
				throw new RuntimeException("Can not apply seam carving: too many seams...");

			// Initialize all values needed for the calculation
			curWidth = inWidth;
			isChosen = new boolean[inHeight][inWidth];
			indecies = new int[inHeight][inWidth];
			initIndices();
			workingImageGreyscaled = imageToGreyscale();
			costMatrix = new long[inHeight][inWidth];
			calculateCostMatrix();

			// Find and remove numOfSeams seams
			for (int i = 0; i < numOfSeams; i++) {
				findSeam();
			}

			// Setting resizeOp by with the appropriate method reference
			if (outWidth > inWidth)
				resizeOp = this::increaseImageWidth;
			else if (outWidth < inWidth)
				resizeOp = this::reduceImageWidth;
			else
				resizeOp = this::duplicateWorkingImage;

			this.logger.log("preliminary calculations were ended.");
		}


		// Initialize the indices
		private void initIndices() {
			setForEachInputParameters();
			forEach((i, j) -> {
				indecies[i][j] = j;
			});
		}

		// Transfer input image to grayscale array using weights from the user
		private int[][] imageToGreyscale() {
			int[][] greyScaled = new int[inHeight][inWidth];
			BufferedImage greyed = greyscale();

			forEach((y, x) -> {
				Color c = new Color(greyed.getRGB(x, y));
				int intToG = c.getBlue();
			    greyScaled[y][x] = intToG;
			});
			return greyScaled;
		}

		// Calculate the cost matrix
		private void calculateCostMatrix(){
			// First row
			for (int j = 0; j < curWidth; j++) {
				costMatrix[0][j] = pixelEnergy(0,j);
			}
			backTrack = new char[inHeight][curWidth];
			for (int i = 1; i < costMatrix.length; i++) {
				for (int j = 0; j < curWidth; j++) {
					long min;
					long topMid = topMid(i,j);
					if (j == 0) {
						min = Math.min(topMid, topRight(i,j));
						if (min == topMid) {
							backTrack[i][j] = 'M';
						} else {
							backTrack[i][j] = 'R';
						}
					} else if (j == curWidth-1) {
						min = Math.min(topLeft(i,j), topMid);
						if (min == topMid) {
							backTrack[i][j] = 'M';
						} else {
							backTrack[i][j] = 'L';
						}
					} else {
						long topLeft = topLeft(i,j);
						min = Math.min(Math.min(topLeft, topMid), topRight(i,j));
						if (min == topMid) {
							backTrack[i][j] = 'M';
						} else if (min == topLeft){
							backTrack[i][j] = 'L';
						} else {
							backTrack[i][j] = 'R';
						}
					}
					costMatrix[i][j] = pixelEnergy(i,j) + min;
				}
			}
		}

		private long topMid(int i, int j){
			long res = costMatrix[i-1][j];
			if (j == curWidth - 1) {
				res += getPixel(i, j - 1);
			} else if (j == 0) {
				res += getPixel(i, j + 1);
			} else {
				res += Math.abs(getPixel(i, j + 1) - getPixel(i, j - 1));
			}
			return res;
		}

		private long topRight(int i, int j) {
			long res = costMatrix[i-1][j+1];
			if (j == 0){
				res += Math.abs(getPixel(i, j+1) - getPixel(i-1, j));
			} else {
				res += Math.abs(getPixel(i, j+1) - getPixel(i, j-1))
						+ Math.abs(getPixel(i-1,j) - getPixel(i, j+1));
			}
			return res;
		}

		private long topLeft(int i, int j){
			long res = costMatrix[i-1][j-1];
			if (j < curWidth-1) {
				res += Math.abs(getPixel(i, j+1) - getPixel(i, j-1))
						+ Math.abs(getPixel(i-1, j) - getPixel(i,j+1));
			} else {
				res += Math.abs(getPixel(i-1, j) - getPixel(i,j-1));
			}
			return res;
		}

		// Calculate pixelEnergy for a specific pixel
		private long pixelEnergy(int i, int j) {
			if (getMaskValue(i,j)) {
				return Integer.MAX_VALUE;
			}
			long E1, E2;
			long currentPixel = getPixel(i,j);
			if (j < curWidth - 1) {
				E1 = Math.abs(currentPixel - getPixel(i, j+1));
			} else {
				E1 = Math.abs(currentPixel - getPixel(i, j-1));
			}

			if (i < inHeight - 1) {
				E2 = Math.abs(currentPixel - getPixel(i+1, j));
			} else {
				E2 = Math.abs(currentPixel - getPixel(i-1,j));
			}

			return (E1 + E2);
		}

		// Get value of the mask in a specific index.
		private boolean getMaskValue(int i, int j) {
			int ind = getOrigIndexJ(i, j);
			return imageMask[i][ind];
		}

		// Get the original index from indices array
		private int getOrigIndexJ(int i, int j){
			return indecies[i][j];
		}

		// get the pixel value (in greyscale)
		private long getPixel(int i, int j) {
			return workingImageGreyscaled[i][getOrigIndexJ(i,j)];
		}

		public BufferedImage resize() {
			return resizeOp.resize();
		}

		// create image without removed seams
		private BufferedImage reduceImageWidth() {
			logger.log("reduceImageWidth");
			setForEachOutputParameters();
			BufferedImage ans = newEmptyOutputSizedImage();
			forEach((y, x) -> {
				int color = getColor(y, x);
				ans.setRGB(x, y, color);
			});
			return ans;
		}

		// Get the original image color in a specific image
		private int getColor(int i, int j) {
			int realJ = getOrigIndexJ(i, j);
			return workingImage.getRGB(realJ, i);
		}

		// Find the smallest value seam, remove it and then recalculate the costMatrix.
		private int[] findSeam(){
			int[] seam = new int[inHeight];
			int minX = minElemInRow(costMatrix.length -1);
			for (int i = seam.length - 1; i > 0; i--) {
				seam[i] = minX;
				if (backTrack[i][minX] == 'L'){
					minX = minX - 1;
				} else if (backTrack[i][minX] == 'R'){
					minX = minX + 1;
				}
			}
			seam[0] = minX;
			removeFromIndecies(seam);
			calculateCostMatrix();
			return seam;
		}

		// Remove a specific index from the indecies matrix.
		private void removeFromIndecies(int[] seam){
			for (int i = indecies.length - 1; i >=0; i--){
	            isChosen[i][ getOrigIndexJ(i, seam[i])] = true;
	            for (int j = seam[i]; j < curWidth - 1; j++) {
					indecies[i][j] = indecies[i][j+1];
				}
			}
			curWidth--;
		}

		// find index of minimal element in row i of costMatrix
		private int minElemInRow(int i){
			int min = 0;
			for (int j = 1; j < curWidth; j++) {
				if (costMatrix[i][j] < costMatrix[i][min]) {
					min = j;
				}
			}
			return min;
		}

		// Create an output image with removed seams duplicated.
		private BufferedImage increaseImageWidth() {
			logger.log("increaseImageWidth");
			int[][] newIndecies = new int[outHeight][outWidth];
			int offset;

			for (int i = 0; i < workingImageGreyscaled.length; i++) {
				offset = 0;
				for (int j = 0; j < workingImageGreyscaled[0].length; j++) {
					newIndecies[i][offset + j] = j;
					if (isChosen[i][j]) {
						offset++;
						newIndecies[i][offset + j] = j;
					}
				}
			}

			indecies = newIndecies;
			setForEachOutputParameters();
			BufferedImage ans = newEmptyOutputSizedImage();
			forEach((y, x) -> {
				ans.setRGB(x, y, workingImage.getRGB(newIndecies[y][x], y));
			});
			return ans;
		}

		// Implemented bonus - show the seams chosen on the image.
		public BufferedImage showSeams(int seamColorRGB) {
			BufferedImage ans = workingImage;
			setForEachInputParameters();
			forEach((y, x) -> {
				if (isChosen[y][x]) {
					ans.setRGB(x, y, seamColorRGB);
				}
			});
			return ans;
		}

		public boolean[][] getMaskAfterSeamCarving() {
			boolean[][] outMask = new boolean[outHeight][outWidth];
			setForEachOutputParameters();
			forEach((y, x) -> {
				outMask[y][x] = getMaskValue(y, x);
			});

			return outMask;
		}
	}	
	
	
	
	
	//
//	// MARK: An inner interface for functional programming.
//	@FunctionalInterface
//	interface ResizeOperation {
//		BufferedImage resize();
//	}
//
//	// MARK: Fields
//	private int numOfSeams;
//	private ResizeOperation resizeOp;
//	boolean[][] imageMask;
//	/**
//	 * Additional fields
//	 */
//	private int[][] greyScaleImage;
//	private long[][] m;
//	private int[][] minPaths;
//	private int[][] xIndices;
//	private boolean[][] shiftedMask;
//	private int[][] seams;
//	private int k;
//	private boolean[][] maskAfterSeamCarving;
//
//
//	
//	
//	
//	
//	public SeamsCarver(Logger logger, BufferedImage workingImage, int outWidth, RGBWeights rgbWeights,
//			boolean[][] imageMask) {
//		super((s) -> logger.log("Seam carving: " + s), workingImage, rgbWeights, outWidth, workingImage.getHeight());
//
//		numOfSeams = Math.abs(outWidth - inWidth);
//		this.imageMask = imageMask;
//		if (inWidth < 2 | inHeight < 2)
//			throw new RuntimeException("Can not apply seam carving: workingImage is too small");
//
//		if (numOfSeams > inWidth / 2)
//			throw new RuntimeException("Can not apply seam carving: too many seams...");
//
//		// Setting resizeOp by with the appropriate method reference
//		if (outWidth > inWidth)
//			resizeOp = this::increaseImageWidth;
//		else if (outWidth < inWidth)
//			resizeOp = this::reduceImageWidth;
//		else
//			resizeOp = this::duplicateWorkingImage;
//
//		/**
//		 * Initialization of additional fields
//		 */
//		this.maskAfterSeamCarving = null;
//
//		this.logger.log("begins preliminary calculations.");
//		
//		if (this.numOfSeams > 0) {
//			this.logger.log("initializes some additional fields.");
//			this.k = 0;
//			this.m = new long[this.inHeight][this.inWidth];
//			this.minPaths = new int[this.inHeight][this.inWidth];
//			this.seams = new int[this.numOfSeams][this.inHeight];
//			this.initGreyScaleImage();
//			this.initXIndices();
//			this.shiftedMask = this.duplicateWorkingMask();
//			this.findSeams();
//		}
//
//		this.logger.log("preliminary calculations were ended.");
//	}
//
//	
//	private void initGreyScaleImage() {
//        final BufferedImage grey = this.greyscale();
//        this.greyScaleImage = new int[this.inHeight][this.inWidth];
//        this.forEach((y, x) -> this.greyScaleImage[y][x] = new Color(grey.getRGB(x, y)).getRed());
//    }
//
//	
//	private void initXIndices() {
//        this.logger.log("creates a 2D matrix of original \"x\" indices.");
//        this.xIndices = new int[this.inHeight][this.inWidth];
//        this.forEach((y, x) -> this.xIndices[y][x] = x);
//    }
//
//	
//	private void findSeams() {
//        this.logger.log("finds the " + this.numOfSeams + " minimal seams.");
//        do {
//            this.calculateCostsMatrix();
//            this.logger.log("finds seam no: " + (this.k + 1) + ".");
//            this.removeSeam();
//        } while (++this.k < this.numOfSeams);
//    }
//
//	private void removeSeam() {
//        this.logger.log("looking for the \"x\" index of the bottom row that holds the minimal cost.");
//        int minX = 0;
//        for (int x = 0; x < this.inWidth - this.k; ++x) {
//            if (this.m[this.inHeight - 1][x] < this.m[this.inHeight - 1][minX]) {
//                minX = x;
//            }
//        }
//        this.logger.log("minX = " + minX + ".");
//        this.logger.log("constructs the path of the minimal seam.");
//        this.logger.log("stores the path.");
//        for (int y = this.inHeight - 1; y > -1; --y) {
//            this.seams[this.k][y] = this.xIndices[y][minX];
//            final int greyColor = this.greyScaleImage[y][minX];
//            if (minX > 0) {
//                this.greyScaleImage[y][minX - 1] = (this.greyScaleImage[y][minX - 1] + greyColor) / 2;
//            }
//            if (minX + 1 < this.inWidth - this.k) {
//                this.greyScaleImage[y][minX + 1] = (this.greyScaleImage[y][minX + 1] + greyColor) / 2;
//            }
//            this.shiftLeft(y, minX);
//            minX = this.minPaths[y][minX];
//        }
//        this.logger.log("removes the seam.");
//    }
//    
//
//	private void shiftLeft(final int y, final int seamX) {
//        for (int x = seamX + 1; x < this.inWidth - this.k; ++x) {
//            this.xIndices[y][x - 1] = this.xIndices[y][x];
//            this.greyScaleImage[y][x - 1] = this.greyScaleImage[y][x];
//            this.shiftedMask[y][x - 1] = this.shiftedMask[y][x];
//        }
//    }
//
//	private void calculateCostsMatrix() {
//        this.logger.log("calculates the costs matrix \"m\".");
//        this.pushForEachParameters();
//        this.setForEachWidth(this.inWidth - this.k);
//        final MinCost minCost;
//        this.forEach((y, x) -> {
//            minCost = this.getMinCost(y, x);
//            this.minPaths[y][x] = minCost.minX;
//            this.m[y][x] = this.energyAt(y, x) + minCost.min;
//            return;
//        });
//        this.popForEachParameters();
//    }
//
//	
//	private long energyAt(Integer y, Integer x) {
//        final int nextX = (x + 1 < this.inWidth - this.k) ? (x + 1) : (x - 1);
//        final int nextY = (y + 1 < this.inHeight) ? (y + 1) : (y - 1);
//        final long forbidden = this.shiftedMask[y][x] ? -2147483648L : 0L;
//        return (Math.abs(this.greyScaleImage[y][nextX] - this.greyScaleImage[y][x]) + Math.abs(this.greyScaleImage[nextY][x] - this.greyScaleImage[y][x]) + forbidden);
//    }
//
//	
//	private MinCost getMinCost(Integer y, Integer x) {
//        long min = 0L;
//        int minX = x;
//        if (y > 0) {
//            final long mv = this.m[y - 1][x];
//            long cr;
//            long cl;
//            long cv;
//            if (x > 0 & x + 1 < this.inWidth - this.k) {
//                cv = (cl = (cr = Math.abs(this.greyScaleImage[y][x - 1] - this.greyScaleImage[y][x + 1])));
//            }
//            else {
//                cv = (cl = (cr = 255L));
//            }
//            long ml;
//            if (x > 0) {
//                cl += Math.abs(this.greyScaleImage[y - 1][x] - this.greyScaleImage[y][x - 1]);
//                ml = this.m[y - 1][x - 1];
//            }
//            else {
//                cl = 0L;
//                ml = 2147483647L;
//            }
//            long mr;
//            if (x + 1 < this.inWidth - this.k) {
//                cr += Math.abs(this.greyScaleImage[y - 1][x] - this.greyScaleImage[y][x + 1]);
//                mr = this.m[y - 1][x + 1];
//            }
//            else {
//                cr = 0L;
//                mr = 2147483647L;
//            }
//            final long sumL = ml + cl;
//            final long sumV = mv + cv;
//            final long sumR = mr + cr;
//            min = Math.min(Math.min(sumL, sumV), sumR);
//            if (min == sumR & x + 1 < this.inWidth - this.k) {
//                minX = x + 1;
//            }
//            else if (min == sumL & x > 0) {
//                minX = x - 1;
//            }
//        }
//        return new MinCost(min, minX);
//    }
//
//	private boolean[][] duplicateWorkingMask() {
//		final boolean[][] res = new boolean[this.inHeight][this.inWidth];
//	    this.forEach((y, x) -> res[y][x] = this.imageMask[y][x]);
//	    return res;
//	}
//
//	public BufferedImage resize() {
//		return resizeOp.resize();
//	}
//
//	private BufferedImage reduceImageWidth() {
//		this.logger.log("reduces image width by " + this.numOfSeams + " pixels.");
//		final int[][] image = this.duplicateWorkingImageAs2DArray();
//		int[][] seams;
//		for (int length = (seams = this.seams).length, i = 0; i < length; ++i) {
//			final int[] seam = seams[i];
//			final Object o;
//			final int x2;
//			final Object o2;
//			final int rgbMid;
//			int rgbLeft;
//			int rgbMix;
//			int rgbRight;
//			int rgbMix2;
//			this.forEachHeight(y -> {
//				x2 = o[y];
//				rgbMid = o2[y][x2];
//				if (x2 > 0) {
//					rgbLeft = o2[y][x2 - 1];
//					rgbMix = mixRGB(rgbMid, rgbLeft);
//					o2[y][x2 - 1] = rgbMix;
//				}
//				if (x2 + 1 < this.inWidth) {
//					rgbRight = o2[y][x2 + 1];
//					rgbMix2 = mixRGB(rgbMid, rgbRight);
//					o2[y][x2 + 1] = rgbMix2;
//				}
//				return;
//			});
//		}
//		
//		final BufferedImage ans = this.newEmptyOutputSizedImage();
//		this.pushForEachParameters();
//		this.setForEachWidth(this.outWidth);
//		final int originalX;
//		final Object o3;
//		final int rgb;
//		final Color c;
//		final BufferedImage bufferedImage;
//		this.forEach((y, x) -> {
//			originalX = this.xIndices[y][x];
//			rgb = o3[y][originalX];
//			c = new Color(getRed(rgb), getGreen(rgb), getBlue(rgb));
//			bufferedImage.setRGB(x, y, c.getRGB());
//			return;
//		});
//		this.popForEachParameters();
//		this.setMaskAfterWidthReduce();
//		return ans;
//		//throw new UnimplementedMethodException("reduceImageWidth");
//	}
//
//    private void setMaskAfterWidthReduce() {
//        this.maskAfterSeamCarving = new boolean[this.inHeight][this.outWidth];
//        this.pushForEachParameters();
//        this.setForEachWidth(this.outWidth);
//        this.forEach((y, x) -> this.maskAfterSeamCarving[y][x] = this.shiftedMask[y][x]);
//        this.popForEachParameters();
//    }
//
//
//	private static int getRed(final int rgb) {
//        return rgb >> 16;
//    }
//    
//    private static int getGreen(final int rgb) {
//        return rgb >> 8 & 0xFF;
//    }
//    
//    private static int getBlue(final int rgb) {
//        return rgb & 0xFF;
//    }
//
//	private int mixRGB(final int rgb1, final int rgb2) {
//        final int r = (getRed(rgb1) + getRed(rgb2)) / 2;
//        final int g = (getGreen(rgb1) + getGreen(rgb2)) / 2;
//        final int b = (getBlue(rgb1) + getBlue(rgb2)) / 2;
//        return getRGB(r, g, b);
//    }
//
//
//	private int[][] duplicateWorkingImageAs2DArray() {
//        final int[][] image = new int[this.inHeight][this.inWidth];
//        final Color c;
//        final int r;
//        final int g;
//        final int b;
//        final Object o;
//        this.forEach((y, x) -> {
//            c = new Color(this.workingImage.getRGB(x, y));
//            r = c.getRed();
//            g = c.getGreen();
//            b = c.getBlue();
//            o[y][x] = getRGB(r, g, b);
//            return;
//        });
//        return image;
//    }
//	
//    private static int getRGB(final int r, final int g, final int b) {
//        return r << 16 | g << 8 | b;
//    }
//
//	private BufferedImage increaseImageWidth() {
//        this.logger.log("increases image width by + " + this.numOfSeams + " pixels.");
//        this.maskAfterSeamCarving = new boolean[this.inHeight][this.outWidth];
//        final int[][] image = this.duplicateWorkingImageAs2DArray();
//        final int[][] rotatedSeams = this.rotateSeams();
//        final Object o;
//        this.forEachHeight(y -> o[y] = this.merge(o[y], rotatedSeams[y], (int)y));
//        final BufferedImage ans = this.newEmptyOutputSizedImage();
//        this.pushForEachParameters();
//        this.setForEachWidth(this.outWidth);
//        final Object o2;
//        final int rgb;
//        final Color c;
//        final BufferedImage bufferedImage;
//        this.forEach((y, x) -> {
//            rgb = o2[y][x];
//            c = new Color(getRed(rgb), getGreen(rgb), getBlue(rgb));
//            bufferedImage.setRGB(x, y, c.getRGB());
//            return;
//        });
//        this.popForEachParameters();
//        return ans;
////		throw new UnimplementedMethodException("increaseImageWidth");
//	}
//
//	private int[][] rotateSeams() {
//        final int[][] rotatedSeams = new int[this.inHeight][this.numOfSeams];
//        this.pushForEachParameters();
//        this.setForEachWidth(this.numOfSeams);
//        this.forEach((y, i) -> rotatedSeams[y][i] = this.seams[i][y]);
//        this.popForEachParameters();
//        int[][] array;
//        for (int length = (array = rotatedSeams).length, j = 0; j < length; ++j) {
//            final int[] arr = array[j];
//            Arrays.sort(arr);
//        }
//        return rotatedSeams;
//    }
//
//
//	public BufferedImage showSeams(final int seamColorRGB) {
//		final BufferedImage ans = this.duplicateWorkingImage();
//		if (this.numOfSeams > 0) {
//			int[][] seams;
//			for (int length = (seams = this.seams).length, i = 0; i < length; ++i) {
//				final int[] seam = seams[i];
//				final Object o;
//				final int x;
//				final BufferedImage bufferedImage;
//				this.forEachHeight(y -> {
//					x = o[y];
//					bufferedImage.setRGB(x, y, seamColorRGB);
//					return;
//				});
//			}
//		}
//		return ans;
//		// throw new UnimplementedMethodException("showSeams");
//	}
//
//	public boolean[][] getMaskAfterSeamCarving() {
//        return (this.maskAfterSeamCarving != null) ? this.maskAfterSeamCarving : this.duplicateWorkingMask();
//   
//				// This method should return the mask of the resize image after seam carving.
//		// Meaning, after applying Seam Carving on the input image,
//		// getMaskAfterSeamCarving() will return a mask, with the same dimensions as the
//		// resized image, where the mask values match the original mask values for the
//		// corresponding pixels.
//		// HINT: Once you remove (replicate) the chosen seams from the input image, you
//		// need to also remove (replicate) the matching entries from the mask as well.
//		//throw new UnimplementedMethodException("getMaskAfterSeamCarving");
//	}
//	private static class MinCost {
//		final long min;
//	    final int minX;
//	        
//	    MinCost(final long min, final int minX) {
//	        this.min = min;
//	        this.minX = minX;
//	    }
//	}

//}
