package com.channel.v2;


import com.channel.AChannel;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class V2meituan extends AChannel {
    @Override
    protected File add(String channel) {
        File file = new File(directory, String.format(FORMAT, name, channel));
        copyFile(source, file);
        try {
            ChannelWriter.put(file, channel);
        } catch (Exception e) {
            e.printStackTrace();
            file.delete();
        }
        return file;
    }

    @Override
    public String getChannel(File file) {
        return ChannelReader.get(file).getChannel();
    }

    public static boolean isV2(File apkFile) {
        RandomAccessFile fIn = null;
        FileChannel fileChannel = null;
        try {
            fIn = new RandomAccessFile(apkFile, "rw");
            fileChannel = fIn.getChannel();
            long commentLength = ApkUtil.getCommentLength(fileChannel);
            long centralDirStartOffset = ApkUtil.findCentralDirStartOffset(fileChannel, commentLength);
            ApkUtil.findApkSigningBlock(fileChannel, centralDirStartOffset);
            return true;
        } catch (Exception e) {
        } finally {
            close(fileChannel);
            close(fIn);
        }
        return false;
    }
}
