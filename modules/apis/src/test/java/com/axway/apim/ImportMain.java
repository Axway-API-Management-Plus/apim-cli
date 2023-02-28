package com.axway.apim;

public class ImportMain {
    public static void main(String[] args) {
        int rc = APIImportApp.importAPI(args);
        System.out.println(rc);
    }
}
