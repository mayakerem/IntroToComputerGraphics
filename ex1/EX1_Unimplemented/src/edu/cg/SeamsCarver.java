//package edu.cg;
//
//import java.awt.Color;
//import java.awt.image.BufferedImage;
//
//public class SeamsCarver extends ImageProcessor {
//
//	// MARK: An inner interface for functional programming.
//		@FunctionalInterface
//		interface ResizeOperation {
//			BufferedImage resize();
//		}
//		// MARK: Fields
//		private int numOfSeams;
//		private ResizeOperation resizeOp;
//		boolean[][] imageMask;
//		// Newly added fields
//		boolean[][] isChosen;
//		long[][] costMatrix;
//		int[][] workingImageGreyscaled;
//		int[][] indecies;
//		char[][] backTrack;
//		int curWidth;
//
//		
//		public SeamsCarver(Logger logger, BufferedImage workingImage, int outWidth, RGBWeights rgbWeights, boolean[][] imageMask) {
//			super((s) -> logger.log("Seam carving: " + s), workingImage, rgbWeights, outWidth, workingImage.getHeight());
//
//			numOfSeams = Math.abs(outWidth - inWidth);
//			this.imageMask = imageMask;
//			if (inWidth < 2 | inHeight < 2)
//				throw new RuntimeException("Can not apply seam carving: workingImage is too small");
//
//			if (numOfSeams > inWidth / 2)
//				throw new RuntimeException("Can not apply seam carving: too many seams...");
//
//			// Setting resizeOp by with the appropriate method reference
//			if (outWidth > inWidth)
//				resizeOp = this::increaseImageWidth;
//			else if (outWidth < inWidth)
//				resizeOp = this::reduceImageWidth;
//			else
//				resizeOp = this::duplicateWorkingImage;
//
//			// Initialize all values needed for the calculation
//			curWidth = inWidth;
//			isChosen = new boolean[inHeight][inWidth];
//			indecies = new int[inHeight][inWidth];
//			initIndices();
//			workingImageGreyscaled = imageToGreyscale();
//			costMatrix = new long[inHeight][inWidth];
//			calculateCostMatrix();
//
//			// Find and remove numOfSeams seams
//			for (int i = 0; i < numOfSeams; i++) {
//				findSeam();
//			}
//
//			// Setting resizeOp by with the appropriate method reference
//			if (outWidth > inWidth)
//				resizeOp = this::increaseImageWidth;
//			else if (outWidth < inWidth)
//				resizeOp = this::reduceImageWidth;
//			else
//				resizeOp = this::duplicateWorkingImage;
//
//			this.logger.log("Construtor calculations complete.");
//		}
//
//		// Initialize the indices
//		private void initIndices() {
//			setForEachInputParameters();
//			forEach((i, j) -> {
//				indecies[i][j] = j;
//			});
//		}
//
//		// Transfer input image to grayscale array using weights from the user
//		private int[][] imageToGreyscale() {
//			this.logger.log("Starting greyscaling process.");
//			int[][] greyScaled = new int[inHeight][inWidth];
//			BufferedImage greyed = greyscale();
//
//			forEach((y, x) -> {
//				Color c = new Color(greyed.getRGB(x, y));
//				int intToG = c.getBlue();
//			    greyScaled[y][x] = intToG;
//			});
//			this.logger.log("Greyscaling Completed.");
//			return greyScaled;
//		}
//		
//		// Calculate the cost matrix
//		private void calculateCostMatrix(){
//			this.logger.log("Starting to calculate cost matrix");
//			// First row
//			for (int j = 0; j < curWidth; j++) {
//				costMatrix[0][j] = pixelEnergy(0,j);
//			}
//			backTrack = new char[inHeight][curWidth];
//			for (int i = 1; i < costMatrix.length; i++) {
//				for (int j = 0; j < curWidth; j++) {
//					long min;
//					long topMid = topMid(i,j);
//					if (j == 0) {
//						min = Math.min(topMid, topRight(i,j));
//						if (min == topMid) {
//							backTrack[i][j] = 'M';
//						} else {
//							backTrack[i][j] = 'R';
//						}
//					} else if (j == curWidth-1) {
//						min = Math.min(topLeft(i,j), topMid);
//						if (min == topMid) {
//							backTrack[i][j] = 'M';
//						} else {
//							backTrack[i][j] = 'L';
//						}
//					} else {
//						long topLeft = topLeft(i,j);
//						min = Math.min(Math.min(topLeft, topMid), topRight(i,j));
//						if (min == topMid) {
//							backTrack[i][j] = 'M';
//						} else if (min == topLeft){
//							backTrack[i][j] = 'L';
//						} else {
//							backTrack[i][j] = 'R';
//						}
//					}
//					costMatrix[i][j] = pixelEnergy(i,j) + min;
//				}
//			}
//		}
//
//		private long topMid(int i, int j){
//			long res = costMatrix[i-1][j];
//			if (j == curWidth - 1) {
//				res += getPixel(i, j - 1);
//			} else if (j == 0) {
//				res += getPixel(i, j + 1);
//			} else {
//				res += Math.abs(getPixel(i, j + 1) - getPixel(i, j - 1));
//			}
//			return res;
//		}
//
//		private long topRight(int i, int j) {
//			long res = costMatrix[i-1][j+1];
//			if (j == 0){
//				res += Math.abs(getPixel(i, j+1) - getPixel(i-1, j));
//			} else {
//				res += Math.abs(getPixel(i, j+1) - getPixel(i, j-1))
//						+ Math.abs(getPixel(i-1,j) - getPixel(i, j+1));
//			}
//			return res;
//		}
//
//		private long topLeft(int i, int j){
//			long res = costMatrix[i-1][j-1];
//			if (j < curWidth-1) {
//				res += Math.abs(getPixel(i, j+1) - getPixel(i, j-1))
//						+ Math.abs(getPixel(i-1, j) - getPixel(i,j+1));
//			} else {
//				res += Math.abs(getPixel(i-1, j) - getPixel(i,j-1));
//			}
//			return res;
//		}
//
//		// Calculate pixelEnergy for a specific pixel
//		private long pixelEnergy(int i, int j) {
//			if (getMaskValue(i,j)) {
//				return Integer.MAX_VALUE;
//			}
//			long E1, E2;
//			long currentPixel = getPixel(i,j);
//			if (j < curWidth - 1) {
//				E1 = Math.abs(currentPixel - getPixel(i, j+1));
//			} else {
//				E1 = Math.abs(currentPixel - getPixel(i, j-1));
//			}
//
//			if (i < inHeight - 1) {
//				E2 = Math.abs(currentPixel - getPixel(i+1, j));
//			} else {
//				E2 = Math.abs(currentPixel - getPixel(i-1,j));
//			}
//			return (E1 + E2);
//		}
//
//		// Get value of the mask in a specific index.
//		private boolean getMaskValue(int i, int j) {
//			int ind = getOrigIndexJ(i, j);
//			return imageMask[i][ind];
//		}
//
//		// Get the original index from indices array
//		private int getOrigIndexJ(int i, int j){
//			return indecies[i][j];
//		}
//
//		// get the pixel value (in greyscale)
//		private long getPixel(int i, int j) {
//			return workingImageGreyscaled[i][getOrigIndexJ(i,j)];
//		}
//
//
//		public BufferedImage resize() {
//			return resizeOp.resize();
//		}
//
//		// create image without removed seams
//		private BufferedImage reduceImageWidth() {
////			throw new UnimplementedMethodException("reduceImageWidth");
//			logger.log("reduceImageWidth");
//			setForEachOutputParameters();
//			BufferedImage ans = newEmptyOutputSizedImage();
//			forEach((y, x) -> {
//				int color = getColor(y, x);
//				ans.setRGB(x, y, color);
//			});
//			return ans;
//		}
//		
//		// Get the original image color in a specific image
//		private int getColor(int i, int j) {
//			int realJ = getOrigIndexJ(i, j);
//			return workingImage.getRGB(realJ, i);
//		}
//
//		// Find the smallest value seam, remove it and then recalculate the costMatrix.
//		private int[] findSeam(){
//			int[] seam = new int[inHeight];
//			int minX = minElemInRow(costMatrix.length -1);
//			for (int i = seam.length - 1; i > 0; i--) {
//				seam[i] = minX;
//				if (backTrack[i][minX] == 'L'){
//					minX = minX - 1;
//				} else if (backTrack[i][minX] == 'R'){
//					minX = minX + 1;
//				}
//			}
//			seam[0] = minX;
//			removeFromIndecies(seam);
//			calculateCostMatrix();
//			return seam;
//		}
//		// find index of minimal element in row i of costMatrix
//		private int minElemInRow(int i) {
//			int min = 0;
//			for (int j = 1; j < curWidth; j++) {
//				if (costMatrix[i][j] < costMatrix[i][min]) {
//					min = j;
//				}
//			}
//			return min;
//		}
//
//		// Remove a specific index from the indecies matrix.
//		private void removeFromIndecies(int[] seam) {
//			for (int i = indecies.length - 1; i >=0; i--){
//	            isChosen[i][ getOrigIndexJ(i, seam[i])] = true;
//	            for (int j = seam[i]; j < curWidth - 1; j++) {
//					indecies[i][j] = indecies[i][j+1];
//				}
//			}
//			curWidth--;			
//		}
//
//		// Create an output image with removed seams duplicated.
//
//		private BufferedImage increaseImageWidth() {
//			//throw new UnimplementedMethodException("increaseImageWidth");
//			logger.log("increaseImageWidth");
//			int[][] newIndecies = new int[outHeight][outWidth];
//			int offset;
//
//			for (int i = 0; i < workingImageGreyscaled.length; i++) {
//				offset = 0;
//				for (int j = 0; j < workingImageGreyscaled[0].length; j++) {
//					newIndecies[i][offset + j] = j;
//					if (isChosen[i][j]) {
//						offset++;
//						newIndecies[i][offset + j] = j;
//					}
//				}
//			}
//			indecies = newIndecies;
//			setForEachOutputParameters();
//			BufferedImage ans = newEmptyOutputSizedImage();
//			forEach((y, x) -> {
//				ans.setRGB(x, y, workingImage.getRGB(newIndecies[y][x], y));
//			});
//			return ans;
//		}
//
//		
//		public BufferedImage showSeams(int seamColorRGB) {
//			//throw new UnimplementedMethodException("showSeams");
//			BufferedImage ans = workingImage;
//			setForEachInputParameters();
//			forEach((y, x) -> {
//				if (isChosen[y][x]) {
//					ans.setRGB(x, y, seamColorRGB);
//				}
//			});
//			return ans;
//		}
//
//		public boolean[][] getMaskAfterSeamCarving() {
//			// This method should return the mask of the resize image after seam carving.
//			// Meaning, after applying Seam Carving on the input image,
//			// getMaskAfterSeamCarving() will return a mask, with the same dimensions as the
//			// resized image, where the mask values match the original mask values for the
//			// corresponding pixels.
//			// HINT: Once you remove (replicate) the chosen seams from the input image, you
//			// need to also remove (replicate) the matching entries from the mask as well.
//			//throw new UnimplementedMethodException("getMaskAfterSeamCarving");
//			boolean[][] outMask = new boolean[outHeight][outWidth];
//			setForEachOutputParameters();
//			forEach((y, x) -> {
//				outMask[y][x] = getMaskValue(y, x);
//			});
//
//			return outMask;		
//		}
//	}

package edu.cg;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class SeamsCarver extends ImageProcessor {
	private enum path {L, V, R};

	/**
	 * Represent a greyscale pixel with it's associated
	 * energy and the parent direction.
	 */
	private static class EnergyPixel {
		private long energy;
		private path parent;

		EnergyPixel(long e) {
			this.energy = e;
		}

		// Getters and setters for parent and energy
		public long getEnergy() { return this.energy; }
		public void setEnergy(long e) { this.energy = e; }
		public path getParent() { return this.parent; }
		public void setParent(path p) { this.parent = p; }
	}

	// MARK: An inner interface for functional programming.
	@FunctionalInterface
	interface ResizeOperation {
		BufferedImage resize();
	}

	// MARK: Fields
	private int numOfSeams;
	private ResizeOperation resizeOp;
	boolean[][] imageMask;
	int[][] greyscale;
	EnergyPixel[][] costMatrix;
	int k; // Number of seams that were handled.
	BufferedImage tempImg;
	int[][] shiftedSeams;
	int[][] increasedSeams;

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

		k = 0; // init number of seams

		this.logger.log("preliminary calculations were ended.");
	}

	public BufferedImage resize() {
		return resizeOp.resize();
	}

	private BufferedImage reduceImageWidth() {
		logger.log("Starting to reduce image...");
		BufferedImage tempImg = newEmptyInputSizedImage();
		BufferedImage result = newEmptyOutputSizedImage();

		// Copy working image
		forEach((y, x) -> tempImg.setRGB(x, y, workingImage.getRGB(x, y)));
		this.tempImg = tempImg;

		// Find all seams...
		int[][] seams = this.findSeams();

		// Trim temp image.
		setForEachOutputParameters();
		forEach((y, x) -> result.setRGB(x, y, tempImg.getRGB(x, y)));
		logger.log("Done reducing image.");
		return result;
	}

	private BufferedImage increaseImageWidth() {
		logger.log("Starting to increase image...");
		BufferedImage result = newEmptyOutputSizedImage();
		boolean[][] tempMask = new boolean[outHeight][outWidth];

		// Find all seams...
		int[][] seams = this.findSeams();

		for (int y = 0; y < outHeight; y++) {
			int indent = 0;
			for (int x = 0; x < outWidth; x++) {
				result.setRGB(x, y, workingImage.getRGB(x - indent, y));
				tempMask[y][x] = imageMask[y][x - indent];

				if (isPartOfSeam(x, y)) indent++;
			}
		}

		imageMask = tempMask;
		logger.log("Done increase image.");
		return result;
	}

	private boolean isPartOfSeam(int x, int row) {
		for (int[] seam : increasedSeams) if (seam[row] == x) return true;
		return false;
	}

	public BufferedImage showSeams(int seamColorRGB) {
		// TODO: Implement this method (bonus), remove the exception.
		throw new UnimplementedMethodException("showSeams");
	}

	public boolean[][] getMaskAfterSeamCarving() {
		boolean[][] mask = new boolean[outHeight][outWidth];
		setForEachOutputParameters();
		forEach((y, x) -> mask[y][x] = imageMask[y][x]);
		return mask;

		// TODO: Implement this method, remove the exception.
		// This method should return the mask of the resize image after seam carving.
		// Meaning, after applying Seam Carving on the input image,
		// getMaskAfterSeamCarving() will return a mask, with the same dimensions as the
		// resized image, where the mask values match the original mask values for the
		// corresponding pixels.
		// HINT: Once you remove (replicate) the chosen seams from the input image, you
		// need to also remove (replicate) the matching entries from the mask as well.
	}

	private void setGreyscale() {
		logger.log("Converting to greyscale...");
		int[][] result = new int[inHeight][inWidth];

		forEach((y, x) -> {
			Color c =  new Color(workingImage.getRGB(x, y));
			int greyVal = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
			Color greyCol = new Color(greyVal, greyVal, greyVal);
			result[y][x] = greyCol.getRGB();
		});

		this.greyscale = result;
		logger.log("Done converting to greyscale.");
	}

	/**
	 * Calculate the gradient magnitude matrix.
	 */
	private void initCostMatrix(int height, int width) {
		logger.log("Initiating cost matrix...");
		EnergyPixel[][] E = new EnergyPixel[height][width];
		forEach((y, x) -> E[y][x] = new EnergyPixel(this.calcEnergy(y, x)));

		logger.log("Done initiating cost matrix by pixel energies.");
		this.costMatrix = E;
	}

	private void calcForwardCostMatrix() {
		logger.log("Calculating forward looking cost matrix...");
		forEach(this::calcRecursiveCost);
		logger.log("Done calculating cost matrix.");
	}

	private void updateForwardCostMatrix() {
		setForEachWidth(inWidth - k);
		this.initCostMatrix(inHeight, inWidth - k);
		this.calcForwardCostMatrix();
	}

	private void updateMatrices(int[] seam) {
		logger.log("Updating matrices...");
		// Update gradient to pixels who are left to the seam
		for (int y = 0; y < costMatrix.length; y++) {
			// Shift left all pixels that are right to the seam
			for (int x = seam[y]; x < inWidth - k; x++) {
				imageMask[y][x] = imageMask[y][x + 1];
				greyscale[y][x] = greyscale[y][x + 1];

				if (outWidth < inWidth) {
					// Relevant only for when reducing the image.
					tempImg.setRGB(x, y, tempImg.getRGB(x + 1, y));
				}
			}
		}

		this.updateForwardCostMatrix();
		logger.log("Done updating matrices for seam #" + k);
	}

	private long calcEnergy(int y, int x) {
		int neighborX = (x < inWidth - 1 - k) ? x + 1 : x - 1;
		int neighborY = (y < inHeight - 1) ? y + 1 : y - 1;

		long energy;
		if (imageMask[y][x]) {
			return Integer.MIN_VALUE;
		}

		int deltaX = greyscale[y][neighborX] - greyscale[y][x];
		int deltaY = greyscale[neighborY][x] - greyscale[y][x];
		return Math.abs(deltaX) + Math.abs(deltaY);
	}

	private int[][] findSeams() {
		logger.log("Searching seams...");
		shiftedSeams = new int[numOfSeams][inHeight];
		increasedSeams = new int[numOfSeams][inHeight];
		int [][] seams = new int[numOfSeams][inHeight];

		this.setGreyscale();
		this.initCostMatrix(inHeight, inWidth);
		this.calcForwardCostMatrix();

		for (int[] shiftedSeam : shiftedSeams) {
			for (int j = 0; j < shiftedSeam.length; j++) {
				shiftedSeam[j] = Integer.MAX_VALUE;
			}
		}

		for (k = 1; k <= numOfSeams; ++k) {
			int[] seam = findSeam();
			this.updateMatrices(seam);

			seams[k - 1] = this.restoreSeamIdxs(seam, seams);
			increasedSeams[k - 1] = this.calculateIncreasedSeamIdxs(seam);
			shiftedSeams[k - 1] = seam;
		}
		logger.log("Done searching for new seams.");
		return seams;
	}

	private int[] findSeam() {
		logger.log("Finding optimal seam #" + k);
		int[] seam = new int[inHeight];

		// Get the index of the minimum cost from the
		// last row of the cost matrix.
		int j = inHeight - 1;
		int idx = this.findMinCostIdx(this.costMatrix[j]);

		for (int i = seam.length - 1; i >= 0; i--) {
			seam[i] = idx;
			path p = costMatrix[j][idx].getParent();
			if (p == path.L) { idx -= 1; }
			else if (p == path.R) { idx += 1; }
			j--;
		}

		return seam;
	}

	private int[] calculateIncreasedSeamIdxs(int[] seam){
		int[] expected = new int[seam.length];

		for (int i = 0; i < seam.length; i++){
			int s = 0; // Number of seams preceding current seam
			for (int[] shiftedSeam : shiftedSeams) {
				if (seam[i] >= shiftedSeam[i]) s++;
			}
			expected[i] = seam[i] + (2 * s);
		}

		// We might have found the new seam in a smaller x to the the previous ones
		// shift by 1 the previous ones.
		for (int i = 0; i < seam.length; i++) {
			for (int[] s : increasedSeams) {
				if (expected[i] <= s[i]) s[i]++;
			}
		}

		return expected;
	}

	private int[] restoreSeamIdxs(int[] seam, int[][] prevSeams) {
		int[] restored = new int[seam.length];

		for (int i = 0; i < seam.length; i++) {
			restored[i] = seam[i];
			for (int[] shiftedSeam : shiftedSeams) {
				if (seam[i] >= shiftedSeam[i]) restored[i]++;
			}
		}

		// Shift right all seams that have higher x's compared
		// to the new seam.
		for (int i = 0; i < seam.length; i++) {
			for (int[] s : prevSeams) {
				if (restored[i] <= s[i]) restored[i]++;
			}
		}

		return restored;
	}

	private int findMinCostIdx(EnergyPixel[] arr) {
		int minIdx = 0;
		long min = arr[0].getEnergy();

		for (int i = 1; i < arr.length; i++) {
			if (arr[i].getEnergy() < min) {
				min = arr[i].getEnergy();
				minIdx = i;
			}
		}

		return minIdx;
	}

	private void calcRecursiveCost(int y, int x) {
		if (y == 0) {
			return; // Avoid calculating for base case - first row.
		}

		long left = Integer.MAX_VALUE;
		long right = Integer.MAX_VALUE;

		// All cases
		boolean isBorder = (x == 0 || x == (this.costMatrix[0].length - 1));

		int cv = isBorder ? 0 : Math.abs(greyscale[y][x + 1] - greyscale[y][x - 1]);
		long center = costMatrix[y - 1][x].getEnergy() + cv;

		// Excluding first column
		if (x != 0) {
			int cl = isBorder ? 0 : Math.abs(greyscale[y][x + 1] - greyscale[y][x - 1]);
			cl += Math.abs(greyscale[y - 1][x] - greyscale[y][x - 1]);

			left = costMatrix[y - 1][x - 1].getEnergy() + cl;
		}

		// Excluding last column
		if (x != (this.costMatrix[0].length - 1)) {
			// There is no left edge for the first column.
			int cr = isBorder ? 0 : Math.abs(greyscale[y][x + 1] - greyscale[y][x - 1]);
			cr += Math.abs(greyscale[y][x + 1] - greyscale[y - 1][x]);

			right = costMatrix[y - 1][x + 1].getEnergy() + cr;
		}

		path p = getPathByMinimum(left, center, right);

		long val;
		if (p == path.L) val = left;
		else if (p == path.V) val = center;
		else val = right;

		costMatrix[y][x].setEnergy(costMatrix[y][x].getEnergy() + val);
		costMatrix[y][x].setParent(p);
	}

	private path getPathByMinimum(long a, long b, long c) {
		long minVal = Math.min(a, Math.min(b, c));
		if (minVal == a) return path.L;
		if (minVal == b) return path.V;
		return path.R;
	}
}
