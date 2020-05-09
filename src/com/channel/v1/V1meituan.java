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

public class V1meituan extends AChannel {
    private static final String ZIP_FORMAT = "META-INF/channel_%s";
    private static final String KEY = "META-INF/channel_";

    @Override
    protected File add(String channel) {
        File file = new File(directory, String.format(FORMAT, name, channel));
        ZipOutputStream zipOutputStream = null;
        ZipFile zipFile = null;
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry zipEntry = new ZipEntry(String.format(ZIP_FORMAT, channel));
            zipEntry.setSize(0);
            zipEntry.setCompressedSize(0);
            zipEntry.setExtra(null);
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.closeEntry();
            ZipEntry ze;
            zipFile = new ZipFile(source);
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
                if (entryName.startsWith(KEY)) {
                    ret = entryName;
                    ret = ret.substring(KEY.length());
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
