package com.axway.apim.lib.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Thanks to GeeksForGeeks sharing this library
// https://www.geeksforgeeks.org/image-processing-java-set-14-comparison-two-images/

public class ImageComparision 
{ 
	private static Logger LOG = LoggerFactory.getLogger(ImageComparision.class);
	
	public static boolean compare(byte[] image1, byte[] image2) { 
		BufferedImage firstImage = null; 
		BufferedImage secondImage = null; 
		try {
			firstImage = ImageIO.read(new ByteArrayInputStream(image1));
			secondImage = ImageIO.read(new ByteArrayInputStream(image2));
		} catch (IOException e) {
			LOG.error("Can't compare images. ", e);
			return false;
		}
		int width1 = firstImage.getWidth(); 
		int width2 = secondImage.getWidth(); 
		int height1 = firstImage.getHeight(); 
		int height2 = secondImage.getHeight(); 

		if ((width1 != width2) || (height1 != height2)) {
			LOG.debug("Images dimensations are different, not the same image. Return false");
			return false;
		} else { 
			long difference = 0; 
			for (int y = 0; y < height1; y++) 
			{ 
				for (int x = 0; x < width1; x++) 
				{ 
					int rgbA = firstImage.getRGB(x, y); 
					int rgbB = secondImage.getRGB(x, y); 
					int redA = (rgbA >> 16) & 0xff; 
					int greenA = (rgbA >> 8) & 0xff; 
					int blueA = (rgbA) & 0xff; 
					int redB = (rgbB >> 16) & 0xff; 
					int greenB = (rgbB >> 8) & 0xff; 
					int blueB = (rgbB) & 0xff; 
					difference += Math.abs(redA - redB); 
					difference += Math.abs(greenA - greenB); 
					difference += Math.abs(blueA - blueB); 
				} 
			} 

			// Total number of red pixels = width * height 
			// Total number of blue pixels = width * height 
			// Total number of green pixels = width * height 
			// So total number of pixels = width * height * 3 
			double total_pixels = width1 * height1 * 3; 

			// Normalizing the value of different pixels 
			// for accuracy(average pixels per color 
			// component) 
			double avg_different_pixels = difference / total_pixels; 

			// There are 255 values of pixels in total 
			double percentage = (avg_different_pixels / 255) * 100;
			
			if(percentage>3) {
				return false;
			} 
			return true; 
		} 
	} 
} 
