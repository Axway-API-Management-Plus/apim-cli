package com.axway.lib.utils;

import com.axway.apim.lib.utils.ImageComparision;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

public class ImageComparisionTest {

	@Test
	public void testSameImage() throws IOException {
		// Load the API-Image as it defined by the user
		InputStream is = this.getClass().getResourceAsStream("/com/axway/apim/images/API-Logo.jpg");
		InputStream is2 = this.getClass().getResourceAsStream("/com/axway/apim/images/API-Logo.jpg");

		boolean rc = ImageComparision.compare(IOUtils.toByteArray(is), IOUtils.toByteArray(is2));

        Assert.assertTrue(rc, "Images are the same must match.");
	}

	@Test
	public void testOrigVsProcessedImageSmall() throws IOException {
		// Load the API-Image as it defined by the user
		InputStream is = this.getClass().getResourceAsStream("/com/axway/apim/images/API-Logo.jpg");
		InputStream is2 = this.getClass().getResourceAsStream("/com/axway/apim/images/API-Logo-Processed.jpg");

		boolean rc = ImageComparision.compare(IOUtils.toByteArray(is), IOUtils.toByteArray(is2));

        Assert.assertTrue(rc, "Images are almost the same, but processed once by the API-Manager. However, they must match.");
	}

	@Test
	public void testSlightlyChangedImage() throws IOException {
		// Load the API-Image as it defined by the user
		InputStream is = this.getClass().getResourceAsStream("/com/axway/apim/images/API-Logo.jpg");
		InputStream is2 = this.getClass().getResourceAsStream("/com/axway/apim/images/API-Logo-Changed.jpg");

		boolean rc = ImageComparision.compare(IOUtils.toByteArray(is), IOUtils.toByteArray(is2));

        Assert.assertFalse(rc, "Word API has been removed from the image, hence images don't match anymore.");
	}

	@Test
	public void testMediumImage() throws IOException {
		// Load the API-Image as it defined by the user
		InputStream is = this.getClass().getResourceAsStream("/com/axway/apim/images/MediumOriginal.jpg");
		InputStream is2 = this.getClass().getResourceAsStream("/com/axway/apim/images/MediumProcessed.jpg");

		boolean rc = ImageComparision.compare(IOUtils.toByteArray(is), IOUtils.toByteArray(is2));

        Assert.assertTrue(rc, "Must be realized as to be the same image.");
	}

	@Test
	public void testLargeImage() throws IOException {
		// Load the API-Image as it defined by the user
		InputStream is = this.getClass().getResourceAsStream("/com/axway/apim/images/LargeImage.jpg");
		InputStream is2 = this.getClass().getResourceAsStream("/com/axway/apim/images/MediumProcessed.jpg");

		boolean rc = ImageComparision.compare(IOUtils.toByteArray(is), IOUtils.toByteArray(is2));

        Assert.assertTrue(rc, "Must be realized as to be the same image.");
	}
}
