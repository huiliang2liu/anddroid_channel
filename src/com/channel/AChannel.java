package com.channel;

import com.android.signapk.Sign;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.List;

public abstract class AChannel implements Channel {
    protected static final String FORMAT = "%s_%s.apk";
    private static final String PRINT_FORMAT = "compile channel %s end,use time %d";
    private static final String PRINT_FAILURE_FORMAT = "compile channel %s failure";
    protected File directory;
    protected String name;
    protected File source;
    protected List<String> channels;
    protected File parent;

    {
        System.out.println(String.format("use %s", this.getClass().getName()));
    }

    @Override
    public final void addChannel(File file, List<String> channels) {
        parent = file.getParentFile();
        this.source = file;
        name = file.getName();
        name = name.substring(0, name.length() - 4);
        directory = new File(parent, name);
        if (!directory.exists())
            directory.mkdirs();
        this.channels = channels;
        long startTime = System.currentTimeMillis();
        for (String channel : channels) {
            long startChannel = System.currentTimeMillis();
            File file1 = add(channel);
            String ch = getChannel(file1);
            if (channel.equals(ch))
                System.out.println(String.format(PRINT_FORMAT, channel, System.currentTimeMillis() - startChannel));
            else {
                if (file1.exists())
                    file1.delete();
                System.err.println(String.format(PRINT_FAILURE_FORMAT, channel));
            }

        }

        System.out.println(String.format("compile end,channel size is %d,use time %d", channels.size(), System.currentTimeMillis() - startTime));

    }

    protected abstract File add(String channel);

    protected final void copyFile(File source, File target) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            fis = new FileInputStream(source);
            fos = new FileOutputStream(target);
            input = fis.getChannel();
            output = fos.getChannel();
            output.transferFrom(input, 0, source.length());
            fos.flush();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            close(output);
            close(fos);
            close(input);
            close(fis);
        }
    }

    protected void sign(File file, File sign) {
        File pem = new File(parent, "52itvKey.x509.pem");
        File pk8 = new File(parent, "52itvKey.pk8");
        Sign.sign(new String[]{pem.getAbsolutePath(), pk8.getAbsolutePath(), file.getAbsolutePath(), sign.getAbsolutePath()});
    }


    protected static void close(Closeable closeable) {
        if (closeable == null)
            return;
        try {
            closeable.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * instruction:将输入流转化为string 2018-6-7 上午11:29:39
     *
     * @param is
     * @param charset 编码
     * @return
     */
    protected String stream2string(InputStream is, String charset) {
        byte[] buff = stream2bytes(is);
        if (buff == null || buff.length <= 0)
            return null;
        if (charset == null || charset.isEmpty())
            charset = CHARSET;
        try {
            return new String(buff, charset);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    private final static String CHARSET = "utf-8";
    private final static int LENGTH = 1024 * 1024;

    /**
     * instruction:将输入流转化为字节数组 2018-6-7 上午11:26:12
     *
     * @param is
     * @return
     */
    protected byte[] stream2bytes(InputStream is) {
        if (is == null)
            return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[LENGTH];
        byte[] arr = null;
        try {
            int len = is.read(buff);
            while (len > 0) {
                baos.write(buff, 0, len);
                len = is.read(buff);
            }
            arr = baos.toByteArray();
            baos.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            close(baos);
            close(is);
        }
        return arr;
    }
}
