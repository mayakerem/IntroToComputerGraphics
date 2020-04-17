//package edu.cg;
//
//import java.awt.Color;
//import java.awt.image.BufferedImage;
//import java.util.Arrays;
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
//		
//		public static void printMask(int mat[][]) { 
//	        for (int[] row : mat) 
//	            // converting each row as string 
//	            // and then printing in a separate line 
//	            System.out.println(Arrays.toString(row)); 
//	    } 
//		public static void printMask(boolean mat[][]) { 
//	        for (boolean[] row : mat) 
//	            // converting each row as string 
//	            // and then printing in a separate line 
//	            System.out.println(Arrays.toString(row)); 
//	    } 
//}
//
//
/*
 * Decompiled with CFR 0.145.
 */
package edu.cg;

import edu.cg.ImageProcessor;
import edu.cg.Logger;
import edu.cg.RGBWeights;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SeamsCarver
extends ImageProcessor {
    private int numOfSeams;
    private ResizeOperation resizeOp;
    private boolean[][] imageMask;
    private int[][] greyScaleImage;
    private long[][] m;
    private int[][] minPaths;
    private int[][] xIndices;
    private boolean[][] shiftedMask;
    private int[][] seams;
    private int k;
    private boolean[][] maskAfterSeamCarving;

    public SeamsCarver(Logger logger, BufferedImage workingImage, int outWidth, RGBWeights rgbWeights, boolean[][] imageMask) {
        super(s -> logger.log("Seam carving: " + s), workingImage, rgbWeights, outWidth, workingImage.getHeight());
        this.numOfSeams = Math.abs(outWidth - this.inWidth);
        this.imageMask = imageMask;
        if (this.inWidth < 2 | this.inHeight < 2) {
            throw new RuntimeException("Can not apply seam carving: workingImage is too small");
        }
        if (this.numOfSeams > this.inWidth / 2) {
            throw new RuntimeException("Can not apply seam carving: too many seams...");
        }
        this.resizeOp = outWidth > this.inWidth ? this::increaseImageWidth : (outWidth < this.inWidth ? this::reduceImageWidth : this::duplicateWorkingImage);
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

    private boolean[][] duplicateWorkingMask() {
        boolean[][] res = new boolean[this.inHeight][this.inWidth];
        this.forEach((y, x) -> {
            res[y.intValue()][x.intValue()] = this.imageMask[y][x];
        });
        return res;
        
    }

    private void findSeams() {
        this.logger.log("finds the " + this.numOfSeams + " minimal seams.");
        do {
            this.calculateCostsMatrix();
            this.logger.log("finds seam no: " + (this.k + 1) + ".");
            this.removeSeam();
        } while (++this.k < this.numOfSeams);
    }

    private void calculateCostsMatrix() {
        this.logger.log("calculates the costs matrix \"m\".");
        this.pushForEachParameters();
        this.setForEachWidth(this.inWidth - this.k);
        this.forEach((y, x) -> {
            MinCost minCost = this.getMinCost((int)y, (int)x);
            this.minPaths[y.intValue()][x.intValue()] = minCost.minX;
            this.m[y.intValue()][x.intValue()] = this.energyAt((int)y, (int)x) + minCost.min;
        });
        this.popForEachParameters();
    }

    private MinCost getMinCost(int y, int x) {
        long min = 0L;
        int minX = x;
        if (y > 0) {
            long mr;
            long cv;
            long ml;
            long cr;
            long cl;
            long mv = this.m[y - 1][x];
            if (x > 0 & x + 1 < this.inWidth - this.k) {
                cv = cr = (long)Math.abs(this.greyScaleImage[y][x - 1] - this.greyScaleImage[y][x + 1]);
                cl = cr;
            } else {
                cr = 255L;
                cv = 255L;
                cl = 255L;
            }
            if (x > 0) {
                cl += (long)Math.abs(this.greyScaleImage[y - 1][x] - this.greyScaleImage[y][x - 1]);
                ml = this.m[y - 1][x - 1];
            } else {
                cl = 0L;
                ml = Integer.MAX_VALUE;
            }
            if (x + 1 < this.inWidth - this.k) {
                cr += (long)Math.abs(this.greyScaleImage[y - 1][x] - this.greyScaleImage[y][x + 1]);
                mr = this.m[y - 1][x + 1];
            } else {
                cr = 0L;
                mr = Integer.MAX_VALUE;
            }
            long sumL = ml + cl;
            long sumV = mv + cv;
            long sumR = mr + cr;
            min = Math.min(Math.min(sumL, sumV), sumR);
            if (min == sumR & x + 1 < this.inWidth - this.k) {
                minX = x + 1;
            } else if (min == sumL & x > 0) {
                minX = x - 1;
            }
        }
        return new MinCost(min, minX);
    }

    private void removeSeam() {
        this.logger.log("looking for the \"x\" index of the bottom row that holds the minimal cost.");
        int minX = 0;
        for (int x = 0; x < this.inWidth - this.k; ++x) {
            if (this.m[this.inHeight - 1][x] >= this.m[this.inHeight - 1][minX]) continue;
            minX = x;
        }
        this.logger.log("minX = " + minX + ".");
        this.logger.log("constructs the path of the minimal seam.");
        this.logger.log("stores the path.");
        for (int y = this.inHeight - 1; y > -1; --y) {
            this.seams[this.k][y] = this.xIndices[y][minX];
            int greyColor = this.greyScaleImage[y][minX];
            if (minX > 0) {
                this.greyScaleImage[y][minX - 1] = (this.greyScaleImage[y][minX - 1] + greyColor) / 2;
            }
            if (minX + 1 < this.inWidth - this.k) {
                this.greyScaleImage[y][minX + 1] = (this.greyScaleImage[y][minX + 1] + greyColor) / 2;
            }
            this.shiftLeft(y, minX);
            minX = this.minPaths[y][minX];
        }
        this.logger.log("removes the seam.");
    }

    private void shiftLeft(int y, int seamX) {
        for (int x = seamX + 1; x < this.inWidth - this.k; ++x) {
            this.xIndices[y][x - 1] = this.xIndices[y][x];
            this.greyScaleImage[y][x - 1] = this.greyScaleImage[y][x];
            this.shiftedMask[y][x - 1] = this.shiftedMask[y][x];
        }
    }

    private long energyAt(int y, int x) {
        int nextX = x + 1 < this.inWidth - this.k ? x + 1 : x - 1;
        int nextY = y + 1 < this.inHeight ? y + 1 : y - 1;
        long forbidden = this.shiftedMask[y][x] ? Integer.MIN_VALUE : 0L;
        return (long)(Math.abs(this.greyScaleImage[y][nextX] - this.greyScaleImage[y][x]) + Math.abs(this.greyScaleImage[nextY][x] - this.greyScaleImage[y][x])) + forbidden;
    }

    private void initXIndices() {
        this.logger.log("creates a 2D matrix of original \"x\" indices.");
        this.xIndices = new int[this.inHeight][this.inWidth];
        this.forEach((y, x) -> {
            int n = this.xIndices[y.intValue()][x.intValue()] = x.intValue();
        });
    }

    private void initGreyScaleImage() {
        BufferedImage grey = this.greyscale();
        this.greyScaleImage = new int[this.inHeight][this.inWidth];
        this.forEach((y, x) -> {
            int n = this.greyScaleImage[y.intValue()][x.intValue()] = new Color(grey.getRGB((int)x, (int)y)).getRed();
        });
    }

    public BufferedImage resize() {
        return this.resizeOp.resize();
    }

    public BufferedImage showSeams(int seamColorRGB) {
        BufferedImage ans = this.duplicateWorkingImage();
        if (this.numOfSeams > 0) {
            for (int[] seam : this.seams) {
                this.forEachHeight(y -> {
                    int x = seam[y];
                    ans.setRGB(x, (int)y, seamColorRGB);
                });
            }
        }
        return ans;
    }

    private BufferedImage reduceImageWidth() {
        this.logger.log("reduces image width by " + this.numOfSeams + " pixels.");
        int[][] image = this.duplicateWorkingImageAs2DArray();
        for (int[] seam : this.seams) {
            this.forEachHeight(y -> {
                int rgbMix;
                int x = seam[y];
                int rgbMid = image[y][x];
                if (x > 0) {
                    int rgbLeft = image[y][x - 1];
                    arrn2[y.intValue()][x - 1] = rgbMix = SeamsCarver.mixRGB(rgbMid, rgbLeft);
                }
                if (x + 1 < this.inWidth) {
                    int rgbRight = image[y][x + 1];
                    arrn2[y.intValue()][x + 1] = rgbMix = SeamsCarver.mixRGB(rgbMid, rgbRight);
                }
            });
        }
        BufferedImage ans = this.newEmptyOutputSizedImage();
        this.pushForEachParameters();
        this.setForEachWidth(this.outWidth);
        this.forEach((y, x) -> {
            int originalX = this.xIndices[y][x];
            int rgb = image[y][originalX];
            Color c = new Color(SeamsCarver.getRed(rgb), SeamsCarver.getGreen(rgb), SeamsCarver.getBlue(rgb));
            ans.setRGB((int)x, (int)y, c.getRGB());
        });
        this.popForEachParameters();
        this.setMaskAfterWidthReduce();
        return ans;
    }

    private int[][] duplicateWorkingImageAs2DArray() {
        int[][] image = new int[this.inHeight][this.inWidth];
        this.forEach((y, x) -> {
            Color c = new Color(this.workingImage.getRGB((int)x, (int)y));
            int r = c.getRed();
            int g = c.getGreen();
            int b = c.getBlue();
            image[y.intValue()][x.intValue()] = SeamsCarver.getRGB(r, g, b);
        });
        return image;
    }

    private static int getRGB(int r, int g, int b) {
        return r << 16 | g << 8 | b;
    }

    private static int mixRGB(int rgb1, int rgb2) {
        int r = (SeamsCarver.getRed(rgb1) + SeamsCarver.getRed(rgb2)) / 2;
        int g = (SeamsCarver.getGreen(rgb1) + SeamsCarver.getGreen(rgb2)) / 2;
        int b = (SeamsCarver.getBlue(rgb1) + SeamsCarver.getBlue(rgb2)) / 2;
        return SeamsCarver.getRGB(r, g, b);
    }

    private static int getRed(int rgb) {
        return rgb >> 16;
    }

    private static int getGreen(int rgb) {
        return rgb >> 8 & 255;
    }

    private static int getBlue(int rgb) {
        return rgb & 255;
    }

    private BufferedImage increaseImageWidth() {
        this.logger.log("increases image width by + " + this.numOfSeams + " pixels.");
        this.maskAfterSeamCarving = new boolean[this.inHeight][this.outWidth];
        int[][] image = this.duplicateWorkingImageAs2DArray();
        int[][] rotatedSeams = this.rotateSeams();
        this.forEachHeight(y -> {
            arrn[y.intValue()] = this.merge(image[y], rotatedSeams[y], (int)y);
            int[] arrn3 = arrn[y.intValue()];
        });
        BufferedImage ans = this.newEmptyOutputSizedImage();
        this.pushForEachParameters();
        this.setForEachWidth(this.outWidth);
        this.forEach((y, x) -> {
            int rgb = image[y][x];
            Color c = new Color(SeamsCarver.getRed(rgb), SeamsCarver.getGreen(rgb), SeamsCarver.getBlue(rgb));
            ans.setRGB((int)x, (int)y, c.getRGB());
        });
        this.popForEachParameters();
        return ans;
    }

    private int[] merge(int[] imageLine, int[] seamsLine, int y) {
        int x2;
        int[] line = new int[this.outWidth];
        int x1 = 0;
        int i2 = 0;
        int i = 0;
        while (x1 < imageLine.length & i2 < seamsLine.length) {
            x2 = seamsLine[i2];
            this.maskAfterSeamCarving[y][i] = x1 <= x2 ? this.imageMask[y][x1] : this.imageMask[y][x2] || x2 + 1 < this.inWidth && this.imageMask[y][x2 + 1];
            line[i++] = x1 <= x2 ? imageLine[x1++] : this.seamsRGB(imageLine, y, seamsLine, i2++);
        }
        while (i2 < seamsLine.length) {
            x2 = seamsLine[i2];
            this.maskAfterSeamCarving[y][i] = this.imageMask[y][x2] || x2 + 1 < this.inWidth && this.imageMask[y][x2 + 1];
            line[i++] = this.seamsRGB(imageLine, y, seamsLine, i2++);
        }
        while (x1 < imageLine.length) {
            this.maskAfterSeamCarving[y][i] = this.imageMask[y][x1];
            line[i++] = imageLine[x1++];
        }
        return line;
    }

    private int seamsRGB(int[] imageLine, int y, int[] seamsLine, int i) {
        int x = seamsLine[i];
        int rgb = imageLine[x];
        if (x + 1 < this.inWidth) {
            Color c = new Color(this.workingImage.getRGB(x + 1, y));
            int r = c.getRed();
            int g = c.getGreen();
            int b = c.getBlue();
            int rgb2 = SeamsCarver.getRGB(r, g, b);
            rgb = SeamsCarver.mixRGB(rgb, rgb2);
        }
        return rgb;
    }

    private int[][] rotateSeams() {
        int[][] rotatedSeams = new int[this.inHeight][this.numOfSeams];
        this.pushForEachParameters();
        this.setForEachWidth(this.numOfSeams);
        this.forEach((y, i) -> {
            int n = arrn[y.intValue()][i.intValue()] = this.seams[i][y];
        });
        this.popForEachParameters();
        for (int[] arr : rotatedSeams) {
            Arrays.sort(arr);
        }
        return rotatedSeams;
    }

    private void setMaskAfterWidthReduce() {
        this.maskAfterSeamCarving = new boolean[this.inHeight][this.outWidth];
        this.pushForEachParameters();
        this.setForEachWidth(this.outWidth);
        this.forEach((y, x) -> {
            this.maskAfterSeamCarving[y.intValue()][x.intValue()] = this.shiftedMask[y][x];
        });
        this.popForEachParameters();
    }

    public boolean[][] getMaskAfterSeamCarving() {
        return this.maskAfterSeamCarving != null ? this.maskAfterSeamCarving : this.duplicateWorkingMask();
    }

    private static class MinCost {
        final long min;
        final int minX;

        MinCost(long min, int minX) {
            this.min = min;
            this.minX = minX;
        }
    }

    @FunctionalInterface
    static interface ResizeOperation {
        public BufferedImage resize();
    }

}

