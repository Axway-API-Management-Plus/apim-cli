package com.axway.apim.adapter;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.api.model.User;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class APIManagerAdapterTest extends WiremockWrapper {

    private APIManagerAdapter apiManagerAdapter;

    @BeforeClass
    public void init() {
        try {
            initWiremock();

            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("apiadmin");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apiManagerAdapter = APIManagerAdapter.getInstance();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void close() {
        apiManagerAdapter.deleteInstance();
        super.close();
    }


    @Test
    public void testGetHigherRoleAdmin() {
        User user = new User();
        user.setRole("admin");
        Assert.assertEquals("admin", apiManagerAdapter.getHigherRole(user));
    }

    @Test
    public void testGetHigherRoleOadmin() {
        User user = new User();
        user.setRole("oadmin");
        Assert.assertEquals("oadmin", apiManagerAdapter.getHigherRole(user));
    }

    @Test
    public void testGetHigherRoleUserOAdmin() {
        User user = new User();
        user.setRole("user");
        Map<String, String> orgs2Role = new HashMap<>();
        orgs2Role.put("1038f4db-7453-4d47-9f29-121a057a6e1f", "oadmin");
        user.setOrgs2Role(orgs2Role);
        Assert.assertEquals("oadmin", apiManagerAdapter.getHigherRole(user));
    }

    @Test
    public void testGetHigherRoleUserAdmin() {
        User user = new User();
        user.setRole("user");
        Map<String, String> orgs2Role = new HashMap<>();
        orgs2Role.put("1038f4db-7453-4d47-9f29-121a057a6e1f", "oadmin");
        orgs2Role.put("2038f4db-6453-3d47-8f29-221a057a6e1f", "admin");
        user.setOrgs2Role(orgs2Role);
        Assert.assertEquals("admin", apiManagerAdapter.getHigherRole(user));
    }


    @Test
    public void loginToAPIManager() {
        try {
            apiManagerAdapter.loginToAPIManager();
        } catch (AppException appException) {
            Assert.fail("unable to login", appException);
        }
    }

    @Test
    public void logoutFromAPIManager() {
        try {
            apiManagerAdapter.logoutFromAPIManager();
        } catch (AppException appException) {
            Assert.fail("unable to login", appException);
        }
    }

    @Test
    public void getCurrentUser() throws AppException {
        User user = apiManagerAdapter.getCurrentUser();
        Assert.assertNotNull(user);
    }

    @Test
    public void getAppIdForCredential() throws AppException {
        ClientApplication clientApplication = apiManagerAdapter.getAppIdForCredential("extclientid", APIManagerAdapter.CREDENTIAL_TYPE_EXT_CLIENTID);
        Assert.assertNotNull(clientApplication);

    }

    @Test
    public void getAppIdForCredentialUnknown() throws AppException {
        ClientApplication clientApplication = apiManagerAdapter.getAppIdForCredential("extclientid-unknown", APIManagerAdapter.CREDENTIAL_TYPE_EXT_CLIENTID);
        Assert.assertNull(clientApplication);

    }

    @Test
    public void getCertInfo() {
        String certChain = "-----BEGIN CERTIFICATE-----\n" +
                "MIIPCDCCDfCgAwIBAgIQJOjqKNF0xwAK8Wmtv6h6RzANBgkqhkiG9w0BAQsFADBG\n" +
                "MQswCQYDVQQGEwJVUzEiMCAGA1UEChMZR29vZ2xlIFRydXN0IFNlcnZpY2VzIExM\n" +
                "QzETMBEGA1UEAxMKR1RTIENBIDFDMzAeFw0yMzAyMDgwNDM0MzBaFw0yMzA1MDMw\n" +
                "NDM0MjlaMBcxFTATBgNVBAMMDCouZ29vZ2xlLmNvbTCCASIwDQYJKoZIhvcNAQEB\n" +
                "BQADggEPADCCAQoCggEBAMqEv2x+sfBXqgXUIwBPFBwlfInVT8LKDfVvplHpEr+c\n" +
                "Htpso2jZHnvBCNXQYQoBMMttPZQchGJGjEX5E/XQVJ6st4Xrnbs+ydYYittSK7Of\n" +
                "RI3F0QGuGhdrdYb88nbPYR1z0MK/iP09aYHQrPkHfo4NbF4ze+m0VVK0wxogUFxm\n" +
                "jyt8H8Ujjq68lTjDxpCQk5HREI7QKa5HVYqMjRKrY0/vATysZvSaFiqVhvFfNOdL\n" +
                "wQJfLDYPWHQsXrvtsvY8uIfk01ptJUQ49ZpzxlgdgL8PudWjmtqV1/JbJMkko3R0\n" +
                "UhCpeaRka1vUe5GxrIJi12hIAygtcQPmI8mG9HLBxL0CAwEAAaOCDB8wggwbMA4G\n" +
                "A1UdDwEB/wQEAwIFoDATBgNVHSUEDDAKBggrBgEFBQcDATAMBgNVHRMBAf8EAjAA\n" +
                "MB0GA1UdDgQWBBTV9myNOVTKEPF1p9Jsg609Pc/BWDAfBgNVHSMEGDAWgBSKdH+v\n" +
                "hc3ulc09nNDiRhTzcTUdJzBqBggrBgEFBQcBAQReMFwwJwYIKwYBBQUHMAGGG2h0\n" +
                "dHA6Ly9vY3NwLnBraS5nb29nL2d0czFjMzAxBggrBgEFBQcwAoYlaHR0cDovL3Br\n" +
                "aS5nb29nL3JlcG8vY2VydHMvZ3RzMWMzLmRlcjCCCc0GA1UdEQSCCcQwggnAggwq\n" +
                "Lmdvb2dsZS5jb22CFiouYXBwZW5naW5lLmdvb2dsZS5jb22CCSouYmRuLmRldoIV\n" +
                "Ki5vcmlnaW4tdGVzdC5iZG4uZGV2ghIqLmNsb3VkLmdvb2dsZS5jb22CGCouY3Jv\n" +
                "d2Rzb3VyY2UuZ29vZ2xlLmNvbYIYKi5kYXRhY29tcHV0ZS5nb29nbGUuY29tggsq\n" +
                "Lmdvb2dsZS5jYYILKi5nb29nbGUuY2yCDiouZ29vZ2xlLmNvLmlugg4qLmdvb2ds\n" +
                "ZS5jby5qcIIOKi5nb29nbGUuY28udWuCDyouZ29vZ2xlLmNvbS5hcoIPKi5nb29n\n" +
                "bGUuY29tLmF1gg8qLmdvb2dsZS5jb20uYnKCDyouZ29vZ2xlLmNvbS5jb4IPKi5n\n" +
                "b29nbGUuY29tLm14gg8qLmdvb2dsZS5jb20udHKCDyouZ29vZ2xlLmNvbS52boIL\n" +
                "Ki5nb29nbGUuZGWCCyouZ29vZ2xlLmVzggsqLmdvb2dsZS5mcoILKi5nb29nbGUu\n" +
                "aHWCCyouZ29vZ2xlLml0ggsqLmdvb2dsZS5ubIILKi5nb29nbGUucGyCCyouZ29v\n" +
                "Z2xlLnB0ghIqLmdvb2dsZWFkYXBpcy5jb22CDyouZ29vZ2xlYXBpcy5jboIRKi5n\n" +
                "b29nbGV2aWRlby5jb22CDCouZ3N0YXRpYy5jboIQKi5nc3RhdGljLWNuLmNvbYIP\n" +
                "Z29vZ2xlY25hcHBzLmNughEqLmdvb2dsZWNuYXBwcy5jboIRZ29vZ2xlYXBwcy1j\n" +
                "bi5jb22CEyouZ29vZ2xlYXBwcy1jbi5jb22CDGdrZWNuYXBwcy5jboIOKi5na2Vj\n" +
                "bmFwcHMuY26CEmdvb2dsZWRvd25sb2Fkcy5jboIUKi5nb29nbGVkb3dubG9hZHMu\n" +
                "Y26CEHJlY2FwdGNoYS5uZXQuY26CEioucmVjYXB0Y2hhLm5ldC5jboIQcmVjYXB0\n" +
                "Y2hhLWNuLm5ldIISKi5yZWNhcHRjaGEtY24ubmV0ggt3aWRldmluZS5jboINKi53\n" +
                "aWRldmluZS5jboIRYW1wcHJvamVjdC5vcmcuY26CEyouYW1wcHJvamVjdC5vcmcu\n" +
                "Y26CEWFtcHByb2plY3QubmV0LmNughMqLmFtcHByb2plY3QubmV0LmNughdnb29n\n" +
                "bGUtYW5hbHl0aWNzLWNuLmNvbYIZKi5nb29nbGUtYW5hbHl0aWNzLWNuLmNvbYIX\n" +
                "Z29vZ2xlYWRzZXJ2aWNlcy1jbi5jb22CGSouZ29vZ2xlYWRzZXJ2aWNlcy1jbi5j\n" +
                "b22CEWdvb2dsZXZhZHMtY24uY29tghMqLmdvb2dsZXZhZHMtY24uY29tghFnb29n\n" +
                "bGVhcGlzLWNuLmNvbYITKi5nb29nbGVhcGlzLWNuLmNvbYIVZ29vZ2xlb3B0aW1p\n" +
                "emUtY24uY29tghcqLmdvb2dsZW9wdGltaXplLWNuLmNvbYISZG91YmxlY2xpY2st\n" +
                "Y24ubmV0ghQqLmRvdWJsZWNsaWNrLWNuLm5ldIIYKi5mbHMuZG91YmxlY2xpY2st\n" +
                "Y24ubmV0ghYqLmcuZG91YmxlY2xpY2stY24ubmV0gg5kb3VibGVjbGljay5jboIQ\n" +
                "Ki5kb3VibGVjbGljay5jboIUKi5mbHMuZG91YmxlY2xpY2suY26CEiouZy5kb3Vi\n" +
                "bGVjbGljay5jboIRZGFydHNlYXJjaC1jbi5uZXSCEyouZGFydHNlYXJjaC1jbi5u\n" +
                "ZXSCHWdvb2dsZXRyYXZlbGFkc2VydmljZXMtY24uY29tgh8qLmdvb2dsZXRyYXZl\n" +
                "bGFkc2VydmljZXMtY24uY29tghhnb29nbGV0YWdzZXJ2aWNlcy1jbi5jb22CGiou\n" +
                "Z29vZ2xldGFnc2VydmljZXMtY24uY29tghdnb29nbGV0YWdtYW5hZ2VyLWNuLmNv\n" +
                "bYIZKi5nb29nbGV0YWdtYW5hZ2VyLWNuLmNvbYIYZ29vZ2xlc3luZGljYXRpb24t\n" +
                "Y24uY29tghoqLmdvb2dsZXN5bmRpY2F0aW9uLWNuLmNvbYIkKi5zYWZlZnJhbWUu\n" +
                "Z29vZ2xlc3luZGljYXRpb24tY24uY29tghZhcHAtbWVhc3VyZW1lbnQtY24uY29t\n" +
                "ghgqLmFwcC1tZWFzdXJlbWVudC1jbi5jb22CC2d2dDEtY24uY29tgg0qLmd2dDEt\n" +
                "Y24uY29tggtndnQyLWNuLmNvbYINKi5ndnQyLWNuLmNvbYILMm1kbi1jbi5uZXSC\n" +
                "DSouMm1kbi1jbi5uZXSCFGdvb2dsZWZsaWdodHMtY24ubmV0ghYqLmdvb2dsZWZs\n" +
                "aWdodHMtY24ubmV0ggxhZG1vYi1jbi5jb22CDiouYWRtb2ItY24uY29tghRnb29n\n" +
                "bGVzYW5kYm94LWNuLmNvbYIWKi5nb29nbGVzYW5kYm94LWNuLmNvbYIeKi5zYWZl\n" +
                "bnVwLmdvb2dsZXNhbmRib3gtY24uY29tgg0qLmdzdGF0aWMuY29tghQqLm1ldHJp\n" +
                "Yy5nc3RhdGljLmNvbYIKKi5ndnQxLmNvbYIRKi5nY3BjZG4uZ3Z0MS5jb22CCiou\n" +
                "Z3Z0Mi5jb22CDiouZ2NwLmd2dDIuY29tghAqLnVybC5nb29nbGUuY29tghYqLnlv\n" +
                "dXR1YmUtbm9jb29raWUuY29tggsqLnl0aW1nLmNvbYILYW5kcm9pZC5jb22CDSou\n" +
                "YW5kcm9pZC5jb22CEyouZmxhc2guYW5kcm9pZC5jb22CBGcuY26CBiouZy5jboIE\n" +
                "Zy5jb4IGKi5nLmNvggZnb28uZ2yCCnd3dy5nb28uZ2yCFGdvb2dsZS1hbmFseXRp\n" +
                "Y3MuY29tghYqLmdvb2dsZS1hbmFseXRpY3MuY29tggpnb29nbGUuY29tghJnb29n\n" +
                "bGVjb21tZXJjZS5jb22CFCouZ29vZ2xlY29tbWVyY2UuY29tgghnZ3BodC5jboIK\n" +
                "Ki5nZ3BodC5jboIKdXJjaGluLmNvbYIMKi51cmNoaW4uY29tggh5b3V0dS5iZYIL\n" +
                "eW91dHViZS5jb22CDSoueW91dHViZS5jb22CFHlvdXR1YmVlZHVjYXRpb24uY29t\n" +
                "ghYqLnlvdXR1YmVlZHVjYXRpb24uY29tgg95b3V0dWJla2lkcy5jb22CESoueW91\n" +
                "dHViZWtpZHMuY29tggV5dC5iZYIHKi55dC5iZYIaYW5kcm9pZC5jbGllbnRzLmdv\n" +
                "b2dsZS5jb22CG2RldmVsb3Blci5hbmRyb2lkLmdvb2dsZS5jboIcZGV2ZWxvcGVy\n" +
                "cy5hbmRyb2lkLmdvb2dsZS5jboIYc291cmNlLmFuZHJvaWQuZ29vZ2xlLmNuMCEG\n" +
                "A1UdIAQaMBgwCAYGZ4EMAQIBMAwGCisGAQQB1nkCBQMwPAYDVR0fBDUwMzAxoC+g\n" +
                "LYYraHR0cDovL2NybHMucGtpLmdvb2cvZ3RzMWMzL2ZWSnhiVi1LdG1rLmNybDCC\n" +
                "AQYGCisGAQQB1nkCBAIEgfcEgfQA8gB3ALNzdwfhhFD4Y4bWBancEQlKeS2xZwwL\n" +
                "h9zwAw55NqWaAAABhi+EvPIAAAQDAEgwRgIhAKZMmfkRWWRDPDk0xq04XCFElMhX\n" +
                "lH/vlBpb5f/KDdpwAiEAyfZD4UPqu8hynqhPrWcPDc/UPUcPYogX1HbkHhXdk9IA\n" +
                "dwCt9776fP8QyIudPZwePhhqtGcpXc+xDCTKhYY069yCigAAAYYvhL0SAAAEAwBI\n" +
                "MEYCIQC6lliwq089YDDOBB28amDQ1IjkbazQRT23IY+mg848owIhANbeiebRvjLy\n" +
                "28rDKvpO2MVHkVWWAEwDenrg3on0ZVfTMA0GCSqGSIb3DQEBCwUAA4IBAQCQclRL\n" +
                "b4mvgb/5VZdU/xrZkOXL4yxunAw0YAxQ9et0b0g61FTy0kui+On8s3bco/xtwB7B\n" +
                "ZcV+HXW3Al2sgaIcCCBjrLyJG71slwgxYq54jQofzUh8pZ+4Jqk76lXL7Q1/88dc\n" +
                "5VcY8mz+4vgdWKpxV9rQqHR3v6YsyfY1iQ08ZKTMaxCIC3n7ISzgDzhTwbhN5caz\n" +
                "3BU+aNC09JW2Utuw9xfODGH85gvtzvNPBz3L1N7r9VSUr8Zgo4p1ibbfsnWIdY9K\n" +
                "RxVmsjX1YDWILmr9xxKgyKSlyQrWKL++jFyWSY3jLA+oPL8uURJ+JiVfngF+l81y\n" +
                "KWSC3MeV+X3rnLYK\n" +
                "-----END CERTIFICATE-----\n" +
                "-----BEGIN CERTIFICATE-----\n" +
                "MIIFljCCA36gAwIBAgINAgO8U1lrNMcY9QFQZjANBgkqhkiG9w0BAQsFADBHMQsw\n" +
                "CQYDVQQGEwJVUzEiMCAGA1UEChMZR29vZ2xlIFRydXN0IFNlcnZpY2VzIExMQzEU\n" +
                "MBIGA1UEAxMLR1RTIFJvb3QgUjEwHhcNMjAwODEzMDAwMDQyWhcNMjcwOTMwMDAw\n" +
                "MDQyWjBGMQswCQYDVQQGEwJVUzEiMCAGA1UEChMZR29vZ2xlIFRydXN0IFNlcnZp\n" +
                "Y2VzIExMQzETMBEGA1UEAxMKR1RTIENBIDFDMzCCASIwDQYJKoZIhvcNAQEBBQAD\n" +
                "ggEPADCCAQoCggEBAPWI3+dijB43+DdCkH9sh9D7ZYIl/ejLa6T/belaI+KZ9hzp\n" +
                "kgOZE3wJCor6QtZeViSqejOEH9Hpabu5dOxXTGZok3c3VVP+ORBNtzS7XyV3NzsX\n" +
                "lOo85Z3VvMO0Q+sup0fvsEQRY9i0QYXdQTBIkxu/t/bgRQIh4JZCF8/ZK2VWNAcm\n" +
                "BA2o/X3KLu/qSHw3TT8An4Pf73WELnlXXPxXbhqW//yMmqaZviXZf5YsBvcRKgKA\n" +
                "gOtjGDxQSYflispfGStZloEAoPtR28p3CwvJlk/vcEnHXG0g/Zm0tOLKLnf9LdwL\n" +
                "tmsTDIwZKxeWmLnwi/agJ7u2441Rj72ux5uxiZ0CAwEAAaOCAYAwggF8MA4GA1Ud\n" +
                "DwEB/wQEAwIBhjAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwEgYDVR0T\n" +
                "AQH/BAgwBgEB/wIBADAdBgNVHQ4EFgQUinR/r4XN7pXNPZzQ4kYU83E1HScwHwYD\n" +
                "VR0jBBgwFoAU5K8rJnEaK0gnhS9SZizv8IkTcT4waAYIKwYBBQUHAQEEXDBaMCYG\n" +
                "CCsGAQUFBzABhhpodHRwOi8vb2NzcC5wa2kuZ29vZy9ndHNyMTAwBggrBgEFBQcw\n" +
                "AoYkaHR0cDovL3BraS5nb29nL3JlcG8vY2VydHMvZ3RzcjEuZGVyMDQGA1UdHwQt\n" +
                "MCswKaAnoCWGI2h0dHA6Ly9jcmwucGtpLmdvb2cvZ3RzcjEvZ3RzcjEuY3JsMFcG\n" +
                "A1UdIARQME4wOAYKKwYBBAHWeQIFAzAqMCgGCCsGAQUFBwIBFhxodHRwczovL3Br\n" +
                "aS5nb29nL3JlcG9zaXRvcnkvMAgGBmeBDAECATAIBgZngQwBAgIwDQYJKoZIhvcN\n" +
                "AQELBQADggIBAIl9rCBcDDy+mqhXlRu0rvqrpXJxtDaV/d9AEQNMwkYUuxQkq/BQ\n" +
                "cSLbrcRuf8/xam/IgxvYzolfh2yHuKkMo5uhYpSTld9brmYZCwKWnvy15xBpPnrL\n" +
                "RklfRuFBsdeYTWU0AIAaP0+fbH9JAIFTQaSSIYKCGvGjRFsqUBITTcFTNvNCCK9U\n" +
                "+o53UxtkOCcXCb1YyRt8OS1b887U7ZfbFAO/CVMkH8IMBHmYJvJh8VNS/UKMG2Yr\n" +
                "PxWhu//2m+OBmgEGcYk1KCTd4b3rGS3hSMs9WYNRtHTGnXzGsYZbr8w0xNPM1IER\n" +
                "lQCh9BIiAfq0g3GvjLeMcySsN1PCAJA/Ef5c7TaUEDu9Ka7ixzpiO2xj2YC/WXGs\n" +
                "Yye5TBeg2vZzFb8q3o/zpWwygTMD0IZRcZk0upONXbVRWPeyk+gB9lm+cZv9TSjO\n" +
                "z23HFtz30dZGm6fKa+l3D/2gthsjgx0QGtkJAITgRNOidSOzNIb2ILCkXhAd4FJG\n" +
                "AJ2xDx8hcFH1mt0G/FX0Kw4zd8NLQsLxdxP8c4CU6x+7Nz/OAipmsHMdMqUybDKw\n" +
                "juDEI/9bfU1lcKwrmz3O2+BtjjKAvpafkmO8l7tdufThcV4q5O8DIrGKZTqPwJNl\n" +
                "1IXNDw9bg1kWRxYtnCQ6yICmJhSFm/Y3m6xv+cXDBlHz4n/FsRC6UfTd\n" +
                "-----END CERTIFICATE-----\n" +
                "-----BEGIN CERTIFICATE-----\n" +
                "MIIFYjCCBEqgAwIBAgIQd70NbNs2+RrqIQ/E8FjTDTANBgkqhkiG9w0BAQsFADBX\n" +
                "MQswCQYDVQQGEwJCRTEZMBcGA1UEChMQR2xvYmFsU2lnbiBudi1zYTEQMA4GA1UE\n" +
                "CxMHUm9vdCBDQTEbMBkGA1UEAxMSR2xvYmFsU2lnbiBSb290IENBMB4XDTIwMDYx\n" +
                "OTAwMDA0MloXDTI4MDEyODAwMDA0MlowRzELMAkGA1UEBhMCVVMxIjAgBgNVBAoT\n" +
                "GUdvb2dsZSBUcnVzdCBTZXJ2aWNlcyBMTEMxFDASBgNVBAMTC0dUUyBSb290IFIx\n" +
                "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAthECix7joXebO9y/lD63\n" +
                "ladAPKH9gvl9MgaCcfb2jH/76Nu8ai6Xl6OMS/kr9rH5zoQdsfnFl97vufKj6bwS\n" +
                "iV6nqlKr+CMny6SxnGPb15l+8Ape62im9MZaRw1NEDPjTrETo8gYbEvs/AmQ351k\n" +
                "KSUjB6G00j0uYODP0gmHu81I8E3CwnqIiru6z1kZ1q+PsAewnjHxgsHA3y6mbWwZ\n" +
                "DrXYfiYaRQM9sHmklCitD38m5agI/pboPGiUU+6DOogrFZYJsuB6jC511pzrp1Zk\n" +
                "j5ZPaK49l8KEj8C8QMALXL32h7M1bKwYUH+E4EzNktMg6TO8UpmvMrUpsyUqtEj5\n" +
                "cuHKZPfmghCN6J3Cioj6OGaK/GP5Afl4/Xtcd/p2h/rs37EOeZVXtL0m79YB0esW\n" +
                "CruOC7XFxYpVq9Os6pFLKcwZpDIlTirxZUTQAs6qzkm06p98g7BAe+dDq6dso499\n" +
                "iYH6TKX/1Y7DzkvgtdizjkXPdsDtQCv9Uw+wp9U7DbGKogPeMa3Md+pvez7W35Ei\n" +
                "Eua++tgy/BBjFFFy3l3WFpO9KWgz7zpm7AeKJt8T11dleCfeXkkUAKIAf5qoIbap\n" +
                "sZWwpbkNFhHax2xIPEDgfg1azVY80ZcFuctL7TlLnMQ/0lUTbiSw1nH69MG6zO0b\n" +
                "9f6BQdgAmD06yK56mDcYBZUCAwEAAaOCATgwggE0MA4GA1UdDwEB/wQEAwIBhjAP\n" +
                "BgNVHRMBAf8EBTADAQH/MB0GA1UdDgQWBBTkrysmcRorSCeFL1JmLO/wiRNxPjAf\n" +
                "BgNVHSMEGDAWgBRge2YaRQ2XyolQL30EzTSo//z9SzBgBggrBgEFBQcBAQRUMFIw\n" +
                "JQYIKwYBBQUHMAGGGWh0dHA6Ly9vY3NwLnBraS5nb29nL2dzcjEwKQYIKwYBBQUH\n" +
                "MAKGHWh0dHA6Ly9wa2kuZ29vZy9nc3IxL2dzcjEuY3J0MDIGA1UdHwQrMCkwJ6Al\n" +
                "oCOGIWh0dHA6Ly9jcmwucGtpLmdvb2cvZ3NyMS9nc3IxLmNybDA7BgNVHSAENDAy\n" +
                "MAgGBmeBDAECATAIBgZngQwBAgIwDQYLKwYBBAHWeQIFAwIwDQYLKwYBBAHWeQIF\n" +
                "AwMwDQYJKoZIhvcNAQELBQADggEBADSkHrEoo9C0dhemMXoh6dFSPsjbdBZBiLg9\n" +
                "NR3t5P+T4Vxfq7vqfM/b5A3Ri1fyJm9bvhdGaJQ3b2t6yMAYN/olUazsaL+yyEn9\n" +
                "WprKASOshIArAoyZl+tJaox118fessmXn1hIVw41oeQa1v1vg4Fv74zPl6/AhSrw\n" +
                "9U5pCZEt4Wi4wStz6dTZ/CLANx8LZh1J7QJVj2fhMtfTJr9w4z30Z209fOU0iOMy\n" +
                "+qduBmpvvYuR7hZL6Dupszfnw0Skfths18dG9ZKb59UhvmaSGZRVbNQpsg3BZlvi\n" +
                "d0lIKO2d1xozclOzgjXPYovJJIultzkMu34qQb9Sz/yilrbCgj8=\n" +
                "-----END CERTIFICATE-----\n";
        try (InputStream inputStream = new ByteArrayInputStream(certChain.getBytes())) {
            CaCert caCert = new CaCert();
            caCert.setAlias("google");
            caCert.setInbound("false");
            caCert.setOutbound("true");
            String jsonResponse = APIManagerAdapter.getCertInfo(inputStream, "", caCert);
            Assert.assertNotNull(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("fail to process certificate");
        }
    }
}
