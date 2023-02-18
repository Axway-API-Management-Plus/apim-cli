package com.axway.apim;

public class ImportMain {
    public static void main(String[] args) {

        String[] override = new String[]{"api", "import", "-h", "127.0.0.1", "-c", "test.json"};
        int rc = APIImportApp.importAPI(override);
       System.out.println(rc);
    }
}
