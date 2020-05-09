package com.channel;

import sun.misc.BASE64Decoder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        Channel channel = new Channel.ChannelBuild(Channel.ChannelType.MANIFEST).build();
        File file = new File("/Users/liuhuiliang/test/xiaowei_2.3.9.5.apk");
        System.err.println(file.getAbsolutePath());
        List<String> channels = new ArrayList<>();
        for (int i=0;i<10;i++){
            channels.add("meituan"+i);
        }
        channel.addChannel(file, channels);

        File[] files=new File("/Users/liuhuiliang/test/xiaowei_2.3.9.5").listFiles();
        if(files!=null&&files.length>0)
            for (File f:files)
                System.out.println(channel.getChannel(f));

    }
}
