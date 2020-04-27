package com.axway.apim.test.cacerts;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.api.model.CaCert;

public class CertFilenameTest {
	@Test
	public void testAliasOne() {
		String alias = "EMAILADDRESS=barry.dolton@nov.com, CN=srvgdywbjob02.nov.com, OU=Wellbore Technologies Information Systems, O=National Oilwell Varco, L=Houston, ST=TX, C=US, subject=EMAILADDRESS=barry.dolton@nov.com, CN=srvgdywbjob02.nov.com, OU=Wellbore Technologies Information Systems, O=National Oilwell Varco, L=Houston, ST=TX, C=US";
		CaCert cert = new CaCert();
		cert.setAlias(alias);
		
		String certFilename = cert.getCertFile();
		
		Assert.assertEquals(certFilename, "srvgdywbjob02.nov.com.crt");
	}

	@Test
	public void testAliasUnknown() {
		String alias = "UNKNOWN";
		CaCert cert = new CaCert();
		cert.setAlias(alias);

		String certFilename = cert.getCertFile();

		Assert.assertTrue(certFilename.startsWith("UnknownCertificate_"));
	}

}
