package com.axway.apim.test.image;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.ImageComparision;

public class ImageComparisionTest {

	@Test
	public void testSameImage() throws AppException, IOException {
		// Load the API-Image as it defined by the user
		InputStream is = this.getClass().getResourceAsStream("/com/axway/apim/test/files/basic/API-Logo.jpg");
		InputStream is2 = this.getClass().getResourceAsStream("/com/axway/apim/test/files/basic/API-Logo.jpg");

		boolean rc = ImageComparision.compare(IOUtils.toByteArray(is), IOUtils.toByteArray(is2));

		Assert.assertEquals(rc, true, "Images are the same must match.");
	}

	@Test
	public void testOrigVsProcessedImageSmall() throws AppException, IOException {
		// Load the API-Image as it defined by the user
		InputStream is = this.getClass().getResourceAsStream("/com/axway/apim/test/files/basic/API-Logo.jpg");
		InputStream is2 = this.getClass().getResourceAsStream("/com/axway/apim/test/files/basic/API-Logo-Processed.jpg");

		boolean rc = ImageComparision.compare(IOUtils.toByteArray(is), IOUtils.toByteArray(is2));

		Assert.assertEquals(rc, true, "Images are almost the same, but processed once by the API-Manager. However, they must match.");
	}

	@Test
	public void testSlightlyChangedImage() throws AppException, IOException {
		// Load the API-Image as it defined by the user
		InputStream is = this.getClass().getResourceAsStream("/com/axway/apim/test/files/basic/API-Logo.jpg");
		InputStream is2 = this.getClass().getResourceAsStream("/com/axway/apim/test/files/basic/API-Logo-Changed.jpg");

		boolean rc = ImageComparision.compare(IOUtils.toByteArray(is), IOUtils.toByteArray(is2));

		Assert.assertEquals(rc, false, "Word API has been removed from the image, hence images don't match anymore.");
	}

	@Test
	public void testMediumImage() throws AppException, IOException {
		// Load the API-Image as it defined by the user
		InputStream is = this.getClass().getResourceAsStream("/com/axway/apim/test/files/basic/MediumOriginal.jpg");
		InputStream is2 = this.getClass().getResourceAsStream("/com/axway/apim/test/files/basic/MediumProcessed.jpg");

		boolean rc = ImageComparision.compare(IOUtils.toByteArray(is), IOUtils.toByteArray(is2));

		Assert.assertEquals(rc, true, "Must be realized as to be the same image.");
	}

	@Test
	public void testLargeImage() throws AppException, IOException {
		// Load the API-Image as it defined by the user
		InputStream is = this.getClass().getResourceAsStream("/com/axway/apim/test/files/basic/LargeImage.jpg");
		InputStream is2 = this.getClass().getResourceAsStream("/com/axway/apim/test/files/basic/MediumProcessed.jpg");

		boolean rc = ImageComparision.compare(IOUtils.toByteArray(is), IOUtils.toByteArray(is2));

		Assert.assertEquals(rc, true, "Must be realized as to be the same image.");
	}
}
