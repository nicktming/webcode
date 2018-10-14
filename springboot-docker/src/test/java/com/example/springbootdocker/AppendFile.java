package com.example.springbootdocker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AppendFile {
    public static void main(String[] args) throws FileNotFoundException {
        File file = new File("test.txt");
        FileOutputStream os = new FileOutputStream(file);
        String b = "learn java";
        String c = "\r\n";
        String d = "learn php";
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
