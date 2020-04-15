package edu.cg;

import java.awt.Color;
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
		// Newly added fields
		boolean[][] isChosen;
		long[][] costMatrix;
		int[][] workingImageGreyscaled;
		int[][] indecies;
		char[][] backTrack;
		int curWidth;

		
		public SeamsCarver(Logger logger, BufferedImage workingImage, int outWidth, RGBWeights rgbWeights, boolean[][] imageMask) {
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

			this.logger.log("Construtor calculations complete.");
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
			this.logger.log("Starting greyscaling process.");
			int[][] greyScaled = new int[inHeight][inWidth];
			BufferedImage greyed = greyscale();

			forEach((y, x) -> {
				Color c = new Color(greyed.getRGB(x, y));
				int intToG = c.getBlue();
			    greyScaled[y][x] = intToG;
			});
			this.logger.log("Greyscaling Completed.");
			return greyScaled;
		}
		
		// Calculate the cost matrix
		private void calculateCostMatrix(){
			this.logger.log("Starting to calculate cost matrix");
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
//			throw new UnimplementedMethodException("reduceImageWidth");
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
		// find index of minimal element in row i of costMatrix
		private int minElemInRow(int i) {
			int min = 0;
			for (int j = 1; j < curWidth; j++) {
				if (costMatrix[i][j] < costMatrix[i][min]) {
					min = j;
				}
			}
			return min;
		}

		// Remove a specific index from the indecies matrix.
		private void removeFromIndecies(int[] seam) {
			for (int i = indecies.length - 1; i >=0; i--){
	            isChosen[i][ getOrigIndexJ(i, seam[i])] = true;
	            for (int j = seam[i]; j < curWidth - 1; j++) {
					indecies[i][j] = indecies[i][j+1];
				}
			}
			curWidth--;			
		}

		// Create an output image with removed seams duplicated.

		private BufferedImage increaseImageWidth() {
			//throw new UnimplementedMethodException("increaseImageWidth");
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

		
		public BufferedImage showSeams(int seamColorRGB) {
			//throw new UnimplementedMethodException("showSeams");
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
			// This method should return the mask of the resize image after seam carving.
			// Meaning, after applying Seam Carving on the input image,
			// getMaskAfterSeamCarving() will return a mask, with the same dimensions as the
			// resized image, where the mask values match the original mask values for the
			// corresponding pixels.
			// HINT: Once you remove (replicate) the chosen seams from the input image, you
			// need to also remove (replicate) the matching entries from the mask as well.
			//throw new UnimplementedMethodException("getMaskAfterSeamCarving");
			boolean[][] outMask = new boolean[outHeight][outWidth];
			setForEachOutputParameters();
			forEach((y, x) -> {
				outMask[y][x] = getMaskValue(y, x);
			});

			return outMask;		
		}
	}
