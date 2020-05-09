package com.channel;

import java.io.*;

public class FileUtils {
    public static byte[] readFileToByteArray(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[1024 * 1024];
        int len = 0;
        while ((len = fis.read(buff)) > 0) {
            baos.write(buff, 0, len);
        }
        fis.close();
        return baos.toByteArray();
    }

    public static void writeByteArrayToFile(File file,byte[] buff)throws IOException{
        FileOutputStream fileOutputStream=new FileOutputStream(file);
        fileOutputStream.write(buff);
        fileOutputStream.close();
    }
}
