package com.axway.apim.api.model;

import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationProfileTest {

    @Test
    public void compareTwoSslAuthProfiles(){

        AuthenticationProfile authenticationProfile = new AuthenticationProfile();
        authenticationProfile.setName("_default");
        authenticationProfile.setType(AuthType.ssl);
        authenticationProfile.setIsDefault(true);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("source","file");
        parameters.put("password", Utils.getEncryptedPassword());
        parameters.put("pfx","data:application/x-pkcs12;base64,MIIJ0QIBAzCCCZcGCSqGSIb3DQEHAaCCCYgEggmEMIIJgDCCBDcGCSqGSIb3DQEHBqCCBCgwggQk\n" +
                "AgEAMIIEHQYJKoZIhvcNAQcBMBwGCiqGSIb3DQEMAQYwDgQIKOFh7OGREFQCAggAgIID8FuShYz4\n" +
                "BnyOGx//v1NYgA2Z8Yg4sAJonwZsCAcbY3jbWU6wZjvjfMVTK88ReCcYatqO/6cDSNn0HbMCT+wq\n" +
                "sDdX+u/vA3JB3norF7idkHKnkpCX13iZIlbgaI4cZrDZyNYsSugdVXGUv7ohNeglEun9s+qzW1yu\n" +
                "xJnM9LL25fYlXS4Ca1AlIBv2QFCLDci/W/QodbXO6Fun4EqopfIBKEn94wKQhu4z0f0X/JVmNv4k\n" +
                "Va4+tngt8nEzCMI3Hchl1rwFK9ujOZCrJwLmdEkmuJASUixmq8WNXf0J4dMupswKn/0H3fTdaS/Z\n" +
                "99hrT1nWmYNUiwYwqzNNqwgQw4VatJ1S/IC3bT5tG4PqbV6fasJXNxwG5/pofHP4Z4zcMSASuOMf\n" +
                "4bEKnTMbiA6zamzgHzD/OZxiIBP3lhp1EkOd+F36G4b7GCdMrQJSNNeWjgDybJmm89ue5wevNSP3\n" +
                "SVWSI1JpBAB64mC9RrInbh4mhD0UWpoM0sffDuqVwBQq2QzyvxrRmacrWoctMynQrSFusicdVkz0\n" +
                "ugtR4BzfDb03ObHAwWljH7RLjTIZuAqL8ezshsh6VlfRKJHTw2sHJhsy3DrSIJlP7DDJaDXncWuz\n" +
                "Oo0dtymAnvEc9wWJjtxA+6CxjeZAG4h85S0a3IfGxdhWkjsDap5s0Yr9lCujt29kRPVoTi7TbI/l\n" +
                "brnhA5jUWm0M46oq1Aajpf0952PxwaJ1Z3hb/Pk+BKqSvgLXGbnz1rXZjhRkZ1iOYWXrUhJ9LMhJ\n" +
                "S932wF2UUuQVZ7XcgMpN3Udd7vogEkrjLclUAksWTpHz6uQiFT+JVSmNMqeyaVOBf2UzzCGWnSoW\n" +
                "KovConNl1/zgrgK7II5dECI600fTIvirI67icSwxq4ehv+nqOgaPqtwhbhgQLBn1efDYiO8zgKyP\n" +
                "yWW8fBgsfAu03W5XrJ0QUgjP8CCOjkBOqFCDx/bMkJmgL4AejAlB7O33HHJ9y8NLstERmOUBP5eZ\n" +
                "TdkOTGSg0yM/2qp+SyFf1mfxRmBbcOPnTrmZLlv8nhPXHJBl41qHx9BDEACSpA25MqcsDyL4pIJu\n" +
                "DFElV9iCmUg6Mf6dOSCq/xXl9OdtBKuRvh5aE6J/OzHHk2mvwlO0KNNnsZyYBmpLCIhg+9xC1NCt\n" +
                "hhyq3bYEzu2M9KAYdQZTV3QTntaYBOMzC5YQJYTpiVSQUky8YHCIRrJXUm12i7j9UVAkarlYxQzE\n" +
                "wLpmZ1yEdZgtNxqGUqjjk0W3rQZcbcdQTjgrufmAZk0VtauXS8BHnjEERP7uN+BYEXqDo8Syz9c6\n" +
                "BrZZ2KqXj+6tv3aRI2ZIsH4BnkyYYPRTj9c7ou8/yLLaojCCBUEGCSqGSIb3DQEHAaCCBTIEggUu\n" +
                "MIIFKjCCBSYGCyqGSIb3DQEMCgECoIIE7jCCBOowHAYKKoZIhvcNAQwBAzAOBAg9rL96KTeVTQIC\n" +
                "CAAEggTIMI0Bxf3ztqeb1XVLdGLL20MT8CzK9wxQBCY4fLaCIFQLkNgDWAuvNw243PJ1vfTRlDcO\n" +
                "2fZ7CXPtRESmRplLgbEDW2ScnQRUzeq8EYeOiwC1mtNodpyMgaDkpVUvqpjtXaDe4UIW2WXll94L\n" +
                "j64Tddd8DkOcpZtNrVHnmvIZjw7i1MS6jyIfAJkHxlJ42YDg9NNAltw/OIc7tG9GPxc+H9EFUW1q\n" +
                "6S/PLwzlsY1CBGLZjKiTb+p6RuZlPvYcTyaZIMvk5IBvhqtVQhZiGsSKfpEa73OhUUvOK2luizQK\n" +
                "31nrrM7d9Jm9wh/hvsVImRCk5lnCODOLncHZTX06CsivAlvFKw5kAxXOK6SZyRLQOKxsO4Utx23t\n" +
                "qaMZ+aO8Hra1gQepg2viHVq9rggLvBuBA5Lq9MGQCkKe8kPGDpayh/i/BCzMK6Jb/jfgVhR3CB3c\n" +
                "zILfe3M8fC9C1+ceHMHKQjTdAQt2DkcaqnAL9t4TF+/77JMJBMqTsiOHj0szhf84SOWtWeP2fO3m\n" +
                "nRsmBdlf/iPJmEgsoMUripVykciyXq1SLOBnZ4I5wHD6AiKKlbQ2ZhosUmaA1zWDBQA2gtOdYdVD\n" +
                "+gGMo57Ii2OX6MPQDax+BkPPqOsM7rDeNga66JGn2e3BvouIpsQUy5MZDzTAHU6rXVEJFr6BT56U\n" +
                "Y1fY1aWHtMOqsk9rbCl7cplM53JUWcwIdzDW6O3e0NXUBnYpL3BzjFyQg7NQhe5Jm+d/ZND0fQHy\n" +
                "WsP0gACNHl96BghcODUxPxTQxWB+UYq5/W88j81ijQF1/VJBqPFlsR26+O1eZA1sTgehbtBw2hW2\n" +
                "9Rzl7yMWqnoyaHnIH8yTYCTfwggca3IfPjW0m9pqhb6N/EX9AHCAwr1F5NWPwPSzjcrhHl+b+EN3\n" +
                "NYT4XtFYauOXD99415vozrHVa+MD/oYDBUL5sFoaK+G1laHdrEYqQQeZWEoNoOF1i7zyLa/An//u\n" +
                "9WUSVfE+HlOx3X0O6sbRzqeRDxle0fqr1regZtmiECiEEv443NM9PnvZBqEAKYcu6QKY9UN1ZNcd\n" +
                "RsCDZhi4kobuuyvbo5T83gs09bMfgPKAzhyfXGvyaZEunfnb4Egwg0sxGKSLg4nsadsaXNnlo1d/\n" +
                "fum/mYOeAVlzJPm7vw3SNuPechOdVRK+Dqwk2Hmxl1CDKOZs3+htlHpFgZveIKn5j6FzO/Sfy50t\n" +
                "WEmJyq6HQvlWp13uKrrFle5Yo1/zpjYSZkUY85/jmTjaUR33B1V4kmek+rYbTHOtHX5gyI3nHONK\n" +
                "VgPwUuWRxEeVWWFooKrY7LckCiOqFPFlApm64IKaWUn0AOt4FOsE4jcUfcwuu0TvQruxIHe76T8/\n" +
                "JpRnm/qRDu9g9xnCzl8/uC/HM9QPOeaWTqEkwx37lmQGnEU5xmfmxuRNytdfpQMfB+8y4ehNMhGW\n" +
                "BH41BlU+IJkejAdQMpTHS1v0QImy0fmeUm1On+wUux0p//K/k7a94sI8PEM4aW/oQsOzx/Q8AnUo\n" +
                "IT4NqLjr7XmHNJg0LGBQ+xICnRoT/QPcxLb15gs80AeRjIyzD0f5KjIo9sZ+F1FMjoh4r524Jon+\n" +
                "k1MMHA2rItNJfQ41v3fjqqYJmYXuW4kkheEsyOCfFM2+MSUwIwYJKoZIhvcNAQkVMRYEFJFOoqGe\n" +
                "mIoMh0hWjBRVbMzm5AT4MDEwITAJBgUrDgMCGgUABBSDTrC6vHHhfv4XlItsYxYIoHGi2gQINKfm\n" +
                "zpVz38ACAggA");
        authenticationProfile.setParameters(parameters);
        AuthenticationProfile authenticationProfileFromGateway = new AuthenticationProfile();
        authenticationProfileFromGateway.setName("_default");
        authenticationProfileFromGateway.setIsDefault(true);
        authenticationProfileFromGateway.setType(AuthType.ssl);

        parameters = new HashMap<>();
        parameters.put("source","file");
        parameters.put("pfx","data:application/x-pkcs12;base64,MIIJ0QIBAzCCCZcGCSqGSIb3DQEHAaCCCYgEggmEMIIJgDCCBDcGCSqGSIb3DQEHBqCCBCgwggQk\n" +
                "AgEAMIIEHQYJKoZIhvcNAQcBMBwGCiqGSIb3DQEMAQYwDgQIKOFh7OGREFQCAggAgIID8FuShYz4\n" +
                "BnyOGx//v1NYgA2Z8Yg4sAJonwZsCAcbY3jbWU6wZjvjfMVTK88ReCcYatqO/6cDSNn0HbMCT+wq\n" +
                "sDdX+u/vA3JB3norF7idkHKnkpCX13iZIlbgaI4cZrDZyNYsSugdVXGUv7ohNeglEun9s+qzW1yu\n" +
                "xJnM9LL25fYlXS4Ca1AlIBv2QFCLDci/W/QodbXO6Fun4EqopfIBKEn94wKQhu4z0f0X/JVmNv4k\n" +
                "Va4+tngt8nEzCMI3Hchl1rwFK9ujOZCrJwLmdEkmuJASUixmq8WNXf0J4dMupswKn/0H3fTdaS/Z\n" +
                "99hrT1nWmYNUiwYwqzNNqwgQw4VatJ1S/IC3bT5tG4PqbV6fasJXNxwG5/pofHP4Z4zcMSASuOMf\n" +
                "4bEKnTMbiA6zamzgHzD/OZxiIBP3lhp1EkOd+F36G4b7GCdMrQJSNNeWjgDybJmm89ue5wevNSP3\n" +
                "SVWSI1JpBAB64mC9RrInbh4mhD0UWpoM0sffDuqVwBQq2QzyvxrRmacrWoctMynQrSFusicdVkz0\n" +
                "ugtR4BzfDb03ObHAwWljH7RLjTIZuAqL8ezshsh6VlfRKJHTw2sHJhsy3DrSIJlP7DDJaDXncWuz\n" +
                "Oo0dtymAnvEc9wWJjtxA+6CxjeZAG4h85S0a3IfGxdhWkjsDap5s0Yr9lCujt29kRPVoTi7TbI/l\n" +
                "brnhA5jUWm0M46oq1Aajpf0952PxwaJ1Z3hb/Pk+BKqSvgLXGbnz1rXZjhRkZ1iOYWXrUhJ9LMhJ\n" +
                "S932wF2UUuQVZ7XcgMpN3Udd7vogEkrjLclUAksWTpHz6uQiFT+JVSmNMqeyaVOBf2UzzCGWnSoW\n" +
                "KovConNl1/zgrgK7II5dECI600fTIvirI67icSwxq4ehv+nqOgaPqtwhbhgQLBn1efDYiO8zgKyP\n" +
                "yWW8fBgsfAu03W5XrJ0QUgjP8CCOjkBOqFCDx/bMkJmgL4AejAlB7O33HHJ9y8NLstERmOUBP5eZ\n" +
                "TdkOTGSg0yM/2qp+SyFf1mfxRmBbcOPnTrmZLlv8nhPXHJBl41qHx9BDEACSpA25MqcsDyL4pIJu\n" +
                "DFElV9iCmUg6Mf6dOSCq/xXl9OdtBKuRvh5aE6J/OzHHk2mvwlO0KNNnsZyYBmpLCIhg+9xC1NCt\n" +
                "hhyq3bYEzu2M9KAYdQZTV3QTntaYBOMzC5YQJYTpiVSQUky8YHCIRrJXUm12i7j9UVAkarlYxQzE\n" +
                "wLpmZ1yEdZgtNxqGUqjjk0W3rQZcbcdQTjgrufmAZk0VtauXS8BHnjEERP7uN+BYEXqDo8Syz9c6\n" +
                "BrZZ2KqXj+6tv3aRI2ZIsH4BnkyYYPRTj9c7ou8/yLLaojCCBUEGCSqGSIb3DQEHAaCCBTIEggUu\n" +
                "MIIFKjCCBSYGCyqGSIb3DQEMCgECoIIE7jCCBOowHAYKKoZIhvcNAQwBAzAOBAg9rL96KTeVTQIC\n" +
                "CAAEggTIMI0Bxf3ztqeb1XVLdGLL20MT8CzK9wxQBCY4fLaCIFQLkNgDWAuvNw243PJ1vfTRlDcO\n" +
                "2fZ7CXPtRESmRplLgbEDW2ScnQRUzeq8EYeOiwC1mtNodpyMgaDkpVUvqpjtXaDe4UIW2WXll94L\n" +
                "j64Tddd8DkOcpZtNrVHnmvIZjw7i1MS6jyIfAJkHxlJ42YDg9NNAltw/OIc7tG9GPxc+H9EFUW1q\n" +
                "6S/PLwzlsY1CBGLZjKiTb+p6RuZlPvYcTyaZIMvk5IBvhqtVQhZiGsSKfpEa73OhUUvOK2luizQK\n" +
                "31nrrM7d9Jm9wh/hvsVImRCk5lnCODOLncHZTX06CsivAlvFKw5kAxXOK6SZyRLQOKxsO4Utx23t\n" +
                "qaMZ+aO8Hra1gQepg2viHVq9rggLvBuBA5Lq9MGQCkKe8kPGDpayh/i/BCzMK6Jb/jfgVhR3CB3c\n" +
                "zILfe3M8fC9C1+ceHMHKQjTdAQt2DkcaqnAL9t4TF+/77JMJBMqTsiOHj0szhf84SOWtWeP2fO3m\n" +
                "nRsmBdlf/iPJmEgsoMUripVykciyXq1SLOBnZ4I5wHD6AiKKlbQ2ZhosUmaA1zWDBQA2gtOdYdVD\n" +
                "+gGMo57Ii2OX6MPQDax+BkPPqOsM7rDeNga66JGn2e3BvouIpsQUy5MZDzTAHU6rXVEJFr6BT56U\n" +
                "Y1fY1aWHtMOqsk9rbCl7cplM53JUWcwIdzDW6O3e0NXUBnYpL3BzjFyQg7NQhe5Jm+d/ZND0fQHy\n" +
                "WsP0gACNHl96BghcODUxPxTQxWB+UYq5/W88j81ijQF1/VJBqPFlsR26+O1eZA1sTgehbtBw2hW2\n" +
                "9Rzl7yMWqnoyaHnIH8yTYCTfwggca3IfPjW0m9pqhb6N/EX9AHCAwr1F5NWPwPSzjcrhHl+b+EN3\n" +
                "NYT4XtFYauOXD99415vozrHVa+MD/oYDBUL5sFoaK+G1laHdrEYqQQeZWEoNoOF1i7zyLa/An//u\n" +
                "9WUSVfE+HlOx3X0O6sbRzqeRDxle0fqr1regZtmiECiEEv443NM9PnvZBqEAKYcu6QKY9UN1ZNcd\n" +
                "RsCDZhi4kobuuyvbo5T83gs09bMfgPKAzhyfXGvyaZEunfnb4Egwg0sxGKSLg4nsadsaXNnlo1d/\n" +
                "fum/mYOeAVlzJPm7vw3SNuPechOdVRK+Dqwk2Hmxl1CDKOZs3+htlHpFgZveIKn5j6FzO/Sfy50t\n" +
                "WEmJyq6HQvlWp13uKrrFle5Yo1/zpjYSZkUY85/jmTjaUR33B1V4kmek+rYbTHOtHX5gyI3nHONK\n" +
                "VgPwUuWRxEeVWWFooKrY7LckCiOqFPFlApm64IKaWUn0AOt4FOsE4jcUfcwuu0TvQruxIHe76T8/\n" +
                "JpRnm/qRDu9g9xnCzl8/uC/HM9QPOeaWTqEkwx37lmQGnEU5xmfmxuRNytdfpQMfB+8y4ehNMhGW\n" +
                "BH41BlU+IJkejAdQMpTHS1v0QImy0fmeUm1On+wUux0p//K/k7a94sI8PEM4aW/oQsOzx/Q8AnUo\n" +
                "IT4NqLjr7XmHNJg0LGBQ+xICnRoT/QPcxLb15gs80AeRjIyzD0f5KjIo9sZ+F1FMjoh4r524Jon+\n" +
                "k1MMHA2rItNJfQ41v3fjqqYJmYXuW4kkheEsyOCfFM2+MSUwIwYJKoZIhvcNAQkVMRYEFJFOoqGe\n" +
                "mIoMh0hWjBRVbMzm5AT4MDEwITAJBgUrDgMCGgUABBSDTrC6vHHhfv4XlItsYxYIoHGi2gQINKfm\n" +
                "zpVz38ACAggA");
        authenticationProfileFromGateway.setParameters(parameters);
        Assert.assertTrue(authenticationProfile.equals(authenticationProfileFromGateway));
    }

    @Test
    public void compareBasicAuthProfiles(){
        AuthenticationProfile authenticationProfile = new AuthenticationProfile();
        authenticationProfile.setName("_default");
        authenticationProfile.setType(AuthType.http_basic);
        authenticationProfile.setIsDefault(true);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username","admin");
        parameters.put("password", Utils.getEncryptedPassword());
        parameters.put("_id_", 0);

        authenticationProfile.setParameters(parameters);
        AuthenticationProfile authenticationProfileFromGateway = new AuthenticationProfile();
        authenticationProfileFromGateway.setName("_default");
        authenticationProfileFromGateway.setIsDefault(true);
        authenticationProfileFromGateway.setType(AuthType.http_basic);

        parameters = new HashMap<>();
        parameters.put("username","admin");
        parameters.put("_id_", 0);

        authenticationProfileFromGateway.setParameters(parameters);
        Assert.assertTrue(authenticationProfile.equals(authenticationProfileFromGateway));
    }

    @Test
    public void compareApikeyAuthProfiles(){
        AuthenticationProfile authenticationProfile = new AuthenticationProfile();
        authenticationProfile.setName("_default");
        authenticationProfile.setType(AuthType.apiKey);
        authenticationProfile.setIsDefault(true);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("apiKey","abcdedg");
        parameters.put("apiKeyField","QUERYSTRING_PARAMETER");
        parameters.put("_id_", 0);

        authenticationProfile.setParameters(parameters);
        AuthenticationProfile authenticationProfileFromGateway = new AuthenticationProfile();
        authenticationProfileFromGateway.setName("_default");
        authenticationProfileFromGateway.setIsDefault(true);
        authenticationProfileFromGateway.setType(AuthType.apiKey);

        parameters = new HashMap<>();
        parameters.put("apiKey","abcdedg");
        parameters.put("apiKeyField","QUERYSTRING_PARAMETER");
        parameters.put("_id_", 0);

        authenticationProfileFromGateway.setParameters(parameters);
        Assert.assertTrue(authenticationProfile.equals(authenticationProfileFromGateway));
    }

    @Test
    public void compareApikeyAuthProfilesWithDifferentKeys(){
        AuthenticationProfile authenticationProfile = new AuthenticationProfile();
        authenticationProfile.setName("_default");
        authenticationProfile.setType(AuthType.apiKey);
        authenticationProfile.setIsDefault(true);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("apiKey","abcdedg1");
        parameters.put("apiKeyField","QUERYSTRING_PARAMETER");
        parameters.put("_id_", 0);

        authenticationProfile.setParameters(parameters);
        AuthenticationProfile authenticationProfileFromGateway = new AuthenticationProfile();
        authenticationProfileFromGateway.setName("_default");
        authenticationProfileFromGateway.setIsDefault(true);
        authenticationProfileFromGateway.setType(AuthType.apiKey);

        parameters = new HashMap<>();
        parameters.put("apiKey","abcdedg");
        parameters.put("apiKeyField","QUERYSTRING_PARAMETER");
        parameters.put("_id_", 0);

        authenticationProfileFromGateway.setParameters(parameters);
        Assert.assertFalse(authenticationProfile.equals(authenticationProfileFromGateway));
        Assert.assertNotNull(authenticationProfile.toString());
    }
}
