package com.hayvn.hayvnapp.ProdDev;

public class Modes {
    private static String submission = "disabled";
    private static String dashboard = "disabled";
    private static String faq = "disabled";
    private static String medical = "medical";

    public static String getSubmission(){return submission;}
    public static String getDashboard(){return dashboard;}
    public static String getFaq(){return faq;}
    public static String getMedical(){return medical;}

    public static boolean IS_ADMIN = false;
}
