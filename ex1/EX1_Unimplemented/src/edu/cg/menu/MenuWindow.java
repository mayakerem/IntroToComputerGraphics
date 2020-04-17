package edu.cg.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import edu.cg.RGBWeights;
import edu.cg.ImageProcessor;
import edu.cg.Logger;
import edu.cg.SeamsCarver;
import edu.cg.UnimplementedMethodException;
import edu.cg.menu.components.ActionsController;
import edu.cg.menu.components.ColorMixer;
import edu.cg.menu.components.ImagePicker;
import edu.cg.menu.components.LogField;
import edu.cg.menu.components.ScaleSelector;
import edu.cg.menu.components.ScaleSelector.ResizingOperation;

@SuppressWarnings("serial")
public class MenuWindow extends JFrame implements Logger {
	// MARK: fields
	private BufferedImage workingImage;
	private boolean[][] imageMask;
	private String imageTitle;

	// MARK: GUI fields
	private ImagePicker imagePicker;
	private ColorMixer colorMixer;
	private ScaleSelector scaleSelector;
	private ActionsController actionsController;
	private LogField logField;

	public MenuWindow() {
		super();

		setTitle("Maya/Maia - Ex1: Image Processing Application");
		// The following line makes sure that all application threads are terminated
		// when this window is closed.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		imagePicker = new ImagePicker(this);
		colorMixer = new ColorMixer();
		scaleSelector = new ScaleSelector();
		actionsController = new ActionsController(this);
		logField = new LogField();

		contentPane.add(imagePicker, BorderLayout.NORTH);

		JPanel panel1 = new JPanel();
		contentPane.add(panel1, BorderLayout.CENTER);
		panel1.setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panel2 = new JPanel();
		panel1.add(panel2, BorderLayout.CENTER);
		panel2.setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panel3 = new JPanel();
		panel2.add(panel3, BorderLayout.CENTER);
		panel3.setLayout(new GridLayout(0, 1, 0, 0));

		panel3.add(colorMixer);
		panel3.add(scaleSelector);
		panel2.add(actionsController);
		panel1.add(logField);

		workingImage = null;
		imageMask = null;
		imageTitle = null;

		pack();
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		log("Application started.");
	}

	public void changeHue() {
		int outWidth = scaleSelector.width();
		int outHeight = scaleSelector.height();
		RGBWeights rgbWeights = colorMixer.getRGBWeights();
		BufferedImage img = new ImageProcessor(this, duplicateImage(), rgbWeights, outWidth, outHeight).changeHue();
		present(img, "Change hue");
	}

	public void greyscale() {
		BufferedImage img = new ImageProcessor(this, duplicateImage(), colorMixer.getRGBWeights()).greyscale();
		present(img, "Grey scale");
	}

	public void resize() {
		int outWidth = scaleSelector.width();
		int outHeight = scaleSelector.height();
		ResizingOperation op = scaleSelector.resizingOperation();
		RGBWeights rgbWeights = colorMixer.getRGBWeights();
		BufferedImage img;
		switch (op) {
		case NEAREST_NEIGHBOR:
			img = new ImageProcessor(this, duplicateImage(), rgbWeights, outWidth, outHeight).nearestNeighbor();
			break;

		default: // seam carving
			SeamsCarver sc = new SeamsCarver(this, duplicateImage(), outWidth, rgbWeights, duplicateMask());
			img = sc.resize();
			boolean[][] new_mask = sc.getMaskAfterSeamCarving();
			img = new SeamsCarver(this, rotateClockwise(img), outHeight, rgbWeights, rotateMaskClockwise(new_mask))
					.resize();
			img = rotateCounterclockwise(img);
			break;
		}

		present(img, "Resize: " + op.title + " [" + outWidth + "][" + outHeight + "]");
	}

	public void showSeamsVertical() {
		int outWidth = scaleSelector.width();
		RGBWeights rgbWeights = colorMixer.getRGBWeights();
		BufferedImage vertical = new SeamsCarver(this, duplicateImage(), outWidth, rgbWeights, duplicateMask())
				.showSeams(Color.RED.getRGB());
		present(vertical, "Show seams vertical");
	}

	public void showSeamsHorizontal() {
		int outHeight = scaleSelector.height();
		RGBWeights rgbWeights = colorMixer.getRGBWeights();

		BufferedImage horizontal = new SeamsCarver(this, rotateClockwise(workingImage), outHeight, rgbWeights,
				rotateMaskClockwise(imageMask)).showSeams(Color.GREEN.getRGB());

		horizontal = rotateCounterclockwise(horizontal);

		present(horizontal, "Show seams horizontal");
	}

	private void present(BufferedImage img, String title) {
		if (img == null)
			throw new NullPointerException("Can not present a null image.");

		new ImageWindow(img, imageTitle + "; " + title, this).setVisible(true);
	}

	private static BufferedImage rotateClockwise(BufferedImage img) {
		int imgWidth = img.getWidth();
		int imgHeight = img.getHeight();
		BufferedImage ans = new BufferedImage(imgHeight, imgWidth, img.getType());
		for (int y = 0; y < imgWidth; ++y)
			for (int x = 0; x < imgHeight; ++x) {
				int imgX = y;
				int imgY = imgHeight - 1 - x;
				ans.setRGB(x, y, img.getRGB(imgX, imgY));
			}

		return ans;
	}

	private static boolean[][] rotateMaskClockwise(boolean[][] mask) {
		int height = mask.length;
		int width = mask[0].length;
		boolean[][] ans = new boolean[width][height];
		for (int y = 0; y < height; ++y)
			for (int x = 0; x < width; ++x) {
				int X = y;
				int Y = width - 1 - x;
				ans[Y][X] = mask[y][x];
			}

		return ans;
	}

	private static BufferedImage rotateCounterclockwise(BufferedImage img) {
		int imgWidth = img.getWidth();
		int imgHeight = img.getHeight();
		BufferedImage ans = new BufferedImage(imgHeight, imgWidth, img.getType());
		for (int y = 0; y < imgWidth; ++y)
			for (int x = 0; x < imgHeight; ++x) {
				int imgX = imgWidth - 1 - y;
				int imgY = x;
				ans.setRGB(x, y, img.getRGB(imgX, imgY));
			}

		return ans;
	}

	private static BufferedImage duplicateImage(BufferedImage img) {
		BufferedImage dup = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		for (int y = 0; y < dup.getHeight(); ++y)
			for (int x = 0; x < dup.getWidth(); ++x)
				dup.setRGB(x, y, img.getRGB(x, y));

		return dup;
	}

	private static boolean[][] duplicateMask(boolean[][] mask) {
		boolean[][] cpyMask = new boolean[mask.length][];
		for (int i = 0; i < mask.length; i++) {
			cpyMask[i] = Arrays.copyOf(mask[i], mask[i].length);
		}
		return cpyMask;
	}

	private BufferedImage duplicateImage() {
		return duplicateImage(workingImage);
	}

	private boolean[][] duplicateMask() {
		return duplicateMask(imageMask);
	}

	public void setWorkingImage(BufferedImage workingImage, String imageTitle) {
		this.imageTitle = imageTitle;
		this.workingImage = workingImage;
		log("Image: " + imageTitle + " has been selected as working image.");
		scaleSelector.setWidth(workingImage.getWidth());
		scaleSelector.setHeight(workingImage.getHeight());
		actionsController.activateButtons();
		imageMask = new boolean[workingImage.getHeight()][workingImage.getWidth()];
	}

	public void present() {
		new ImageWindow(workingImage, imageTitle, this).setVisible(true);
	}

	// MARK: Logger
	@Override
	public void log(String s) {
		logField.log(s);
	}

	public void setImageMask(boolean[][] srcMask) {
		imageMask = duplicateMask(srcMask);
	}

	/**
	 * The function we added that removes a provided object, specified by the mask,
	 * from the image
	 * 
	 * @param srcMask
	 */	
	public void removeObjectFromImage(boolean[][] srcMask) {
		//First we are duplicating the working image
		BufferedImage result = duplicateImage(workingImage);
		// Then we are getting the mask
		boolean[][] tempMask = duplicateMask(srcMask);
		
		
		int width = workingImage.getWidth();
		RGBWeights rgbWeights = colorMixer.getRGBWeights();

		// Find the maximum number of true values in a row
		// in the mask.
		int maxCount = getMaxTrueValuesInMask(tempMask);
		System.out.println("Initial Max Count " + maxCount);
		int i = 0;
		while (maxCount > 0) {
			// Bound the number of seams to reduce in each use of
			// the seam carver
			int numOfSeamsToReduce = Math.min(maxCount, (width / 3) - 1);
			int outWidth = width - numOfSeamsToReduce;
			SeamsCarver sc = new SeamsCarver(this, result, outWidth,
					rgbWeights, tempMask);

			// Reduce the image and get the updated mask.
			result = sc.resize();
			i ++;
			if (i%1000==0) {
				present(result, "After seam iteration " + i);
			}
			tempMask = sc.getMaskAfterSeamCarving();
				//printMask(tempMask);
			maxCount = getMaxTrueValuesInMask(tempMask);
		}
		//System.out.println("finished while loop");
		// Increase the image back to it's original size.
		SeamsCarver sc = new SeamsCarver(this, result, width, rgbWeights, duplicateMask());
		result = sc.resize();
		present(result, "Image After Object Removal");
	}
	
	/**
	 * Find the maximum number of true values per row in a given mask.
	 * @param mask - matrix of booleans.
	 * @return - the maximum number.
	 */
	private int getMaxTrueValuesInMask(boolean[][] mask) {
		//System.out.println("getting max true values---------------");
		int maxCount = 0;
		for (boolean[] row : mask) {
			int currentRowCounter = 0;
			for (boolean col : row) {
				if (col) 
					currentRowCounter++;
			}
			if (currentRowCounter > maxCount) maxCount = currentRowCounter;
		}
		//System.out.println("finished going over entire mask - " + maxCount);
		return maxCount;
	}
	
	public void maskImage() {
		new MaskPainterWindow(duplicateImage(), "Mask Painter", this).setVisible(true);
	}
	public static void printMask(int mat[][]) { 
        for (int[] row : mat) 
            // converting each row as string 
            // and then printing in a separate line 
            System.out.println(Arrays.toString(row)); 
    } 
	public static void printMask(boolean mat[][]) { 
        for (boolean[] row : mat) 
            // converting each row as string 
            // and then printing in a separate line 
            System.out.println(Arrays.toString(row)); 
    } 
}