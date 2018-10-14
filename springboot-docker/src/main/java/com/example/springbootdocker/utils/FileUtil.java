package com.example.springbootdocker.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    public static void createFile(String filename) throws Exception{
        File file = new File("/data/" + filename + ".txt");
        if (!file.exists()) file.createNewFile();
        System.out.println("--------" + file.getAbsolutePath() + "--------");
        FileOutputStream os = new FileOutputStream(file);
        String b = "learn " + filename + "-v1";
        String c = "\r\n";
        String d = "learn " + filename + "-v2";
        try {
            os.write(b.getBytes());
            os.write(c.getBytes());
            os.write(d.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
