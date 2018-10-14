package com.example.springbootdocker;

import java.util.Map;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        Properties p = System.getProperties();
        Map<String, String> map = System.getenv();
        //p.list(System.out);
        for (String key : map.keySet()) {
            System.out.println(key + "=" + map.get(key));
        }
        System.out.println("JAVA_HOME:" + System.getProperty("JAVA_HOME"));
        System.out.println("JAVA_HOME:" + System.getenv("JAVA_HOME"));
    }
}
