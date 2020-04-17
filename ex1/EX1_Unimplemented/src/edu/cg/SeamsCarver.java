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
  
  // Additional fields
  private long[][] energyMatrix;
  private int[][] greyscaledImage;
  private int[][] bestSeamsPaths;
  private int[][] xIndices;
  private boolean[][] shiftedMask;
  private int[][] seams;
  private int n;
  private boolean[][] maskAfterSeamCarving;
    
  static final long MAX_VALUE = 2147483647L;
  static final long BLACK = 0L;
  static final long WHITE = 255L;

  /**
   * Seam Carver Constructor. Previously partially provided.
   * 
   * @param logger - log to print messages on GUI module 
   * @param workingImage - working image
   * @param outWidth - width
   * @param rgbWeights - Color weights
   * @param imageMask - Image mask
   */
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
	    
	    // Initiating the additional fields and other requirements  
	    this.maskAfterSeamCarving = null;
	    if (this.numOfSeams > 0) {
	    	this.n = 0;
            this.energyMatrix = new long[this.inHeight][this.inWidth];
            this.bestSeamsPaths = new int[this.inHeight][this.inWidth];
            this.seams = new int[this.numOfSeams][this.inHeight];
            this.getGreyscale();
            this.initXIndices();
            this.shiftedMask = this.duplicateWorkingMask();
            this.findSeams();
         }
         this.logger.log("Preliminary calculations complete");
   }

   /**
    * Additional necessary function.
    * Duplicates the working mask by iterating over entire mask
    * creating a new one and returning it
    * @return duplicatedMask - the duplicated mask
    */
    private boolean[][] duplicateWorkingMask() {
    	this.logger.log("Duplicating working mask");
    	// New boolean mask object created
    	boolean[][] duplicatedMask = new boolean[this.inHeight][this.inWidth];
    	// Iterating over every pixel and copying it as the pixel of result
    	this.forEach((y, x) -> {
    		duplicatedMask[y][x] = this.imageMask[y][x];
    	});
    	return duplicatedMask;
    }

    /**
     * Additional necessary function.
     * This method finds the seams based on the cost matrix
     * then removes the seam in order to find the next one
     */
    private void findSeams() {
    	this.logger.log("Initiating findSeam alg");
    	do {
	        // In order to find seams we must calculate the cost matrix
	        this.calcEnergyMatrix();
	        this.logger.log("Seam #" + (this.n + 1));
	        // Now we remove it to find the next one
	        this.removeSeam();
    	} 
    	// We continue until we have the right number of seams
    	while (++this.n < this.numOfSeams);
    }

    /**
     * Additional necessary function.
     * This method calculates the cost/energy matrix of the working image
     */
    private void calcEnergyMatrix() {
      this.logger.log("Initiating energy (cost) matrix calculations.");
      
      this.pushForEachParameters();
      this.setForEachWidth(this.inWidth - this.n);
      
      // Iterating over all pixels in the image
      this.forEach((y, x) -> {
        // Calculating the optimal path by the cost value
        BestPath bestPath = this.getMinimalCost(y, x);
        this.bestSeamsPaths[y][x] = bestPath.pathMin;
        this.energyMatrix[y][x] = this.getPixelEnergy(y, x) + bestPath.pixelMin;
      });
      
      // Update parameters
      this.popForEachParameters();
    }
    
    /**
     * Additional necessary function. Getting minimum cost of seam/path based on top left,
     * top right and top pixels in dynamic programming manner 
     */
    private BestPath getMinimalCost(int y, int x) {
    	long currentPixelMin = BLACK;
	    int pathMin = x;
	    long costLeft, costUp, costRight, minLeft, minRight, minUp;
	
	    if (y > 0) {
	    	minUp = this.energyMatrix[y - 1][x];
	        if (x > 0 & x + 1 < this.inWidth - this.n) {
	        	// Getting the difference between the color value 
	        	costLeft = costUp = costRight = (long)Math.abs(this.greyscaledImage[y][x - 1] - this.greyscaledImage[y][x + 1]);
	        } else {
	        	// Assigning white color
	            costUp = costLeft = costRight = WHITE;
	        }
	         
	        // Top left pixel
	        if (x > 0) {
	           costLeft += (long)Math.abs(this.greyscaledImage[y - 1][x] - this.greyscaledImage[y][x - 1]);
	           minLeft = this.energyMatrix[y - 1][x - 1];
	        } else {
	           costLeft = BLACK;
	           minLeft = MAX_VALUE;
	        }
	         
	        // Top right pixel
	        if (x + 1 < this.inWidth - this.n) {
	           costRight += (long)Math.abs(this.greyscaledImage[y - 1][x] - this.greyscaledImage[y][x + 1]);
	           minRight = this.energyMatrix[y - 1][x + 1];
	        } else {
	           costRight = BLACK;
	           minRight = MAX_VALUE;
	        }
	         
	        // Updating sum of left, right and pixel above
	        long sumLeft = minLeft + costLeft;
	        long sumRight = minRight + costRight;
	        long sumUP = minUp + costUp;
	        
	        // Updating current pixel value to be the minimum of the three pixels above it
	        currentPixelMin = Math.min(Math.min(sumLeft, sumUP), sumRight);
	        if (currentPixelMin == sumRight & x + 1 < this.inWidth - this.n) {
	        	pathMin = x + 1;
	        } else if (currentPixelMin == sumLeft & x > 0) {
	        	pathMin = x - 1;
	        }
	    }
	    return new BestPath(currentPixelMin, pathMin);
    }

   /**
    * Additional necessary function. 
    * This method removes seams and stores them
    */
   private void removeSeam() {
	   this.logger.log("Initializing seam removal");
       int pathMin = 0;
       int y;
        
       // Acquiring bottom pixel with the best path value from the energy matrix
       for(y = 0; y < this.inWidth - this.n; ++y) {
    	   if (this.energyMatrix[this.inHeight - 1][y] < this.energyMatrix[this.inHeight - 1][pathMin]) {
    		   pathMin = y;
    	   }
       }
      
       // Traversing to fetch the whole path
       for(y = this.inHeight - 1; y > -1; --y) {
    	   this.seams[this.n][y] = this.xIndices[y][pathMin];
           int greyColor = this.greyscaledImage[y][pathMin];
           if (pathMin > 0) {
        	   this.greyscaledImage[y][pathMin - 1] = (this.greyscaledImage[y][pathMin - 1] + greyColor) / 2;
           } 
           if (pathMin + 1 < this.inWidth - this.n) {
        	   this.greyscaledImage[y][pathMin + 1] = (this.greyscaledImage[y][pathMin + 1] + greyColor) / 2;
           }
           this.shiftLeft(y, pathMin);
           
           this.logger.log("Storing path for pixel: " + pathMin);
           pathMin = this.bestSeamsPaths[y][pathMin];
       }
       this.logger.log("Seam removed");
   }
   
   /**
    * Additional necessary function.
    * This method updates the greyscale image, the mask and the x indices to the left of the working seam with 
    * the working seam value. This is applied to all the left seams of the working seam.
    * @param y
    * @param seamX
    */
   private void shiftLeft(int y, int seamX) {
	   for(int x = seamX + 1; x < this.inWidth - this.n; ++x) {
		   this.xIndices[y][x - 1] = this.xIndices[y][x];
		   this.greyscaledImage[y][x - 1] = this.greyscaledImage[y][x];
		   this.shiftedMask[y][x - 1] = this.shiftedMask[y][x];
	   }
   }

   /**
    * Additional necessary function.
    * 
    * @param y
    * @param x
    * @return
    */
   private long getPixelEnergy(int y, int x) {
      int nextX, nextY;
      if (y + 1 < this.inHeight) {
   	   nextY = y + 1;
      } else {
   	   nextY = y - 1;
      }
      if (x + 1 < this.inWidth - this.n ) {
    	   nextX = x + 1;
      } else {
    	   nextX = x - 1;
      }
     
      long forbidden = this.shiftedMask[y][x] ? -MAX_VALUE : BLACK;
      long pixelEnergy = (long) Math.abs(this.greyscaledImage[y][nextX] - this.greyscaledImage[y][x]) + Math.abs(this.greyscaledImage[nextY][x] - this.greyscaledImage[y][x]);
      
      return pixelEnergy + forbidden;
   }

   /**
    * Additional necessary function.
    * This method creates a 2D matrix of the original x indices
    */
   private void initXIndices() {
      this.xIndices = new int[this.inHeight][this.inWidth];
      // Iterating over all pixels, obtaining x value for all
      this.forEach((y, x) -> {
    	  this.xIndices[y][x] = x;
      });
   }
   
   /**
    * Additional necessary function.
    * Get greyscale image by iterating over every pixel
    */
   private void getGreyscale() {
	   BufferedImage grey = this.greyscale();
       this.greyscaledImage = new int[this.inHeight][this.inWidth];
       // Iterating over all pixels
       this.forEach((y, x) -> {
    	   // Acquiring pixel color range based on one color
    	   // we choose blue but it doesn't matter
    	   this.greyscaledImage[y][x] = (new Color(grey.getRGB(x, y))).getBlue();
       });
   }
   
   /**
    * Provided resize function
    * @return
    */
   public BufferedImage resize() {
      return this.resizeOp.resize();
   }

   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   public BufferedImage showSeams(int seamColorRGB) {
      BufferedImage ans = this.duplicateWorkingImage();
      if (this.numOfSeams > 0) {
         int[][] var6;
         int var5 = (var6 = this.seams).length;

         for(int var4 = 0; var4 < var5; ++var4) {
            int[] seam = var6[var4];
            this.forEachHeight((y) -> {
               int x = seam[y];
               ans.setRGB(x, y, seamColorRGB);
            });
         }
      }

      return ans;
   }

   private BufferedImage reduceImageWidth() {
      this.logger.log("reduces image width by " + this.numOfSeams + " pixels.");
      int[][] image = this.duplicateWorkingImageAs2DArray();
      int[][] var5;
      int var4 = (var5 = this.seams).length;

      for(int var3 = 0; var3 < var4; ++var3) {
         int[] seam = var5[var3];
         this.forEachHeight((y) -> {
            int x = seam[y];
            int rgbMid = image[y][x];
            int rgbRight;
            int rgbMix;
            if (x > 0) {
               rgbRight = image[y][x - 1];
               rgbMix = mixRGB(rgbMid, rgbRight);
               image[y][x - 1] = rgbMix;
            }

            if (x + 1 < this.inWidth) {
               rgbRight = image[y][x + 1];
               rgbMix = mixRGB(rgbMid, rgbRight);
               image[y][x + 1] = rgbMix;
            }

         });
      }

      BufferedImage ans = this.newEmptyOutputSizedImage();
      this.pushForEachParameters();
      this.setForEachWidth(this.outWidth);
      this.forEach((y, x) -> {
         int originalX = this.xIndices[y][x];
         int rgb = image[y][originalX];
         Color c = new Color(getRed(rgb), getGreen(rgb), getBlue(rgb));
         ans.setRGB(x, y, c.getRGB());
      });
      this.popForEachParameters();
      this.setMaskAfterWidthReduce();
      return ans;
   }

   private int[][] duplicateWorkingImageAs2DArray() {
      int[][] image = new int[this.inHeight][this.inWidth];
      this.forEach((y, x) -> {
         Color c = new Color(this.workingImage.getRGB(x, y));
         int r = c.getRed();
         int g = c.getGreen();
         int b = c.getBlue();
         image[y][x] = getRGB(r, g, b);
      });
      return image;
   }

   private static int getRGB(int r, int g, int b) {
      return r << 16 | g << 8 | b;
   }

   private static int mixRGB(int rgb1, int rgb2) {
      int r = (getRed(rgb1) + getRed(rgb2)) / 2;
      int g = (getGreen(rgb1) + getGreen(rgb2)) / 2;
      int b = (getBlue(rgb1) + getBlue(rgb2)) / 2;
      return getRGB(r, g, b);
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
      this.forEachHeight((y) -> {
         image[y] = this.merge(image[y], rotatedSeams[y], y);
      });
      BufferedImage ans = this.newEmptyOutputSizedImage();
      this.pushForEachParameters();
      this.setForEachWidth(this.outWidth);
      this.forEach((y, x) -> {
         int rgb = image[y][x];
         Color c = new Color(getRed(rgb), getGreen(rgb), getBlue(rgb));
         ans.setRGB(x, y, c.getRGB());
      });
      this.popForEachParameters();
      return ans;
   }

   private int[] merge(int[] imageLine, int[] seamsLine, int y) {
      int[] line = new int[this.outWidth];
      int x1 = 0;
      int i2 = 0;

      int i;
      int x2;
      for(i = 0; x1 < imageLine.length & i2 < seamsLine.length; line[i++] = x1 <= x2 ? imageLine[x1++] : this.seamsRGB(imageLine, y, seamsLine, i2++)) {
         x2 = seamsLine[i2];
         this.maskAfterSeamCarving[y][i] = x1 <= x2 ? this.imageMask[y][x1] : this.imageMask[y][x2] || x2 + 1 < this.inWidth && this.imageMask[y][x2 + 1];
      }

      while(i2 < seamsLine.length) {
         x2 = seamsLine[i2];
         this.maskAfterSeamCarving[y][i] = this.imageMask[y][x2] || x2 + 1 < this.inWidth && this.imageMask[y][x2 + 1];
         line[i++] = this.seamsRGB(imageLine, y, seamsLine, i2++);
      }

      while(x1 < imageLine.length) {
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
         int rgb2 = getRGB(r, g, b);
         rgb = mixRGB(rgb, rgb2);
      }

      return rgb;
   }

   private int[][] rotateSeams() {
      int[][] rotatedSeams = new int[this.inHeight][this.numOfSeams];
      this.pushForEachParameters();
      this.setForEachWidth(this.numOfSeams);
      this.forEach((y, i) -> {
         rotatedSeams[y][i] = this.seams[i][y];
      });
      this.popForEachParameters();
      int[][] var5 = rotatedSeams;
      int var4 = rotatedSeams.length;

      for(int var3 = 0; var3 < var4; ++var3) {
         int[] arr = var5[var3];
         Arrays.sort(arr);
      }

      return rotatedSeams;
   }

   private void setMaskAfterWidthReduce() {
      this.maskAfterSeamCarving = new boolean[this.inHeight][this.outWidth];
      this.pushForEachParameters();
      this.setForEachWidth(this.outWidth);
      this.forEach((y, x) -> {
         this.maskAfterSeamCarving[y][x] = this.shiftedMask[y][x];
      });
      this.popForEachParameters();
   }

   public boolean[][] getMaskAfterSeamCarving() {
      return this.maskAfterSeamCarving != null ? this.maskAfterSeamCarving : this.duplicateWorkingMask();
   
}
}