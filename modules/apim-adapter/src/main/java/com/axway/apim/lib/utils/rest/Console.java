package com.axway.apim.lib.utils.rest;

public class Console {

    public static void println(String message){
        System.out.println(message);
    }

    public static void print(String message){
        System.out.print(message);
    }

    public static void println(){
        System.out.println();
    }

    public static void printf(String format, Object ... args){
        System.out.printf(format, args);
    }
}
