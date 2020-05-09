package com.channel.v1;


import com.channel.AChannel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class V1360 extends AChannel {
    @Override
    protected File add(String channel) {
        File file = new File(directory, String.format(FORMAT, name, channel));
        copyFile(source, file);
        addZipSmallEndSequence(file, channel.getBytes());
        return file;
    }

    @Override
    public String getChannel(File file) {
        return new String(getZipSmallEndSequence(file));
    }

    /**
     * 用于Android添加渠道，360版，利用zip文件添加comment（摘要）
     */
    private boolean addZipSmallEndSequence(File path, byte[] content) {
        try {
            byte[] bytesContent = content;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(bytesContent); // 写入内容；
            baos.write(short2Stream((short) bytesContent.length)); // 写入内容长度；
            byte[] data = baos.toByteArray();
            baos.close();
            if (data.length > Short.MAX_VALUE) {
                throw new IllegalStateException("Zip comment length > 32767.");
            }
            // Zip文件末尾数据结构：{@see java.util.zip.ZipOutputStream.writeEND}
            RandomAccessFile raf = new RandomAccessFile(path, "rw");
            raf.seek(path.length() - 2); // comment长度是short类型
            raf.write(short2Stream((short) data.length)); // 重新写入comment长度，注意Android apk文件使用的是ByteOrder.LITTLE_ENDIAN（小端序）；
            raf.write(data);
            raf.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private byte[] getZipSmallEndSequence(File path) {
        byte[] bytesContent = null;
        try {
            byte[] bytes = new byte[2];
            RandomAccessFile raf = new RandomAccessFile(path, "r");
            long index = raf.length();
            index -= bytes.length;
            readFully(raf, index, bytes); // 读取内容长度；
            int lengthContent = stream2Short(bytes, 0);
            bytesContent = new byte[lengthContent];
            index -= lengthContent;
            readFully(raf, index, bytesContent); // 读取内容；
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytesContent;
    }

    /**
     * RandomAccessFile seek and readFully
     *
     * @param raf
     * @param index
     * @param buffer
     * @throws IOException
     */
    private void readFully(RandomAccessFile raf, long index, byte[] buffer) throws IOException {
        raf.seek(index);
        raf.readFully(buffer);
    }

    /**
     * short转换成字节数组（小端序）
     *
     * @param stream
     * @param offset
     * @return
     */
    private short stream2Short(byte[] stream, int offset) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(stream[offset]);
        buffer.put(stream[offset + 1]);
        return buffer.getShort(0);
    }

    /**
     * 字节数组转换成short（小端序）
     *
     * @param data
     * @return
     */
    private byte[] short2Stream(short data) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(data);
        buffer.flip();
        return buffer.array();
    }
}
