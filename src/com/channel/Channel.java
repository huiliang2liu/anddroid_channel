package com.channel;


import com.channel.v1.Manifest;
import com.channel.v1.V1360;
import com.channel.v1.V1meituan;
import com.channel.v1.V1meituan1;
import com.channel.v2.V2meituan;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public interface Channel {
    void addChannel(File file, List<String> channels);

    String getChannel(File file);

    public static enum ChannelType {
        V1360, V1MEI_TUAN, V1MEI_TUAN1, V2_MEITUAN,MANIFEST
    }

    class ChannelBuild {
        private ChannelType type;

        public ChannelBuild(ChannelType type) {
            this.type = type;
            if (type == null)
                this.type = ChannelType.V1360;
        }

        public Channel build() {
            return (Channel) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Channel.class}, new InvocationHandler() {
                Channel channel;

                {
                    switch (type) {
                        case V1360:
                            channel = new V1360();
                            break;
                        case V1MEI_TUAN:
                            channel = new V1meituan();
                            break;
                        case V1MEI_TUAN1:
                            channel = new V1meituan1();
                            break;
                        case V2_MEITUAN:
                            channel=new V2meituan();
                            break;
                        case MANIFEST:
                            channel=new Manifest();
                            break;
                        default:
                            channel = new V1360();
                            break;
                    }
                }

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    File file = (File) args[0];
                    if (!file.exists()){
                        System.out.println(String.format("文件不存在:%s",file.getAbsolutePath()));
                        return null;
                    }
                    return method.invoke(channel, args);
                }
            });
        }
    }

}
