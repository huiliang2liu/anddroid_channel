package com.channel.v1;


import com.channel.AChannel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class V1meituan1 extends AChannel {

    private static final String KEY = "META-INF/channel";

    @Override
    protected File add(String channel) {
        File file = new File(directory, String.format(FORMAT, name, channel));
        ZipOutputStream zipOutputStream = null;
        ZipFile zipFile = null;
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry zipEntry = new ZipEntry(KEY);
            byte[] text = channel.getBytes();
            zipEntry.setSize(text.length);
            zipEntry.setCompressedSize(text.length);
            zipEntry.setExtra(text);
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(text);
            zipOutputStream.closeEntry();
            zipFile = new ZipFile(source);
            ZipEntry ze;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                ze = new ZipEntry(e.getName());
                ze.setSize(e.getSize());
                ze.setCompressedSize(e.getCompressedSize());
                zipOutputStream.putNextEntry(ze);
                if (!e.isDirectory()) {
                    InputStream is = zipFile.getInputStream(e);
                    int len = 0;
                    byte[] buff = new byte[1024];
                    while ((len = is.read(buff)) > 0) {
                        zipOutputStream.write(buff, 0, len);
                    }
                    is.close();
                }
                zipOutputStream.closeEntry();
            }
        } catch (Exception e) {
            e.printStackTrace();
            file.delete();
        } finally {
            close(zipFile);
            close(zipOutputStream);
        }
        return file;
    }

    @Override
    public String getChannel(File file) {
        ZipFile zipFile = null;
        String ret = "";
        try {
            zipFile = new ZipFile(file);
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.equals(KEY)) {
                    ret = stream2string(zipFile.getInputStream(entry), null);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(zipFile);
        }
        return ret;
    }
}
