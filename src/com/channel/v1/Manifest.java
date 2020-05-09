package com.channel.v1;

import android.content.Context;
import com.bigzhao.xml2axml.Encoder;
import com.bigzhao.xml2axml.test.AXMLPrinter;
import com.channel.AChannel;
import com.channel.xml.NodeTree;
import com.channel.xml.Paras;
import com.channel.xml.ParasFactory;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Manifest extends AChannel {
    private static final String CHANNEL_META_NAME = "app_channel";

    @Override
    protected File add(String channel) {
        File file = new File(directory, String.format(FORMAT, name, channel));
        ZOS zipOutputStream = null;
        ZipFile zipFile = null;
        try {
            zipOutputStream = new ZOS(file, channel);
            ZipEntry ze;
            zipFile = new ZipFile(source);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                if (e.getName().equals("META-INF/MANIFEST.MF"))
                    continue;
                if (e.getName().contains("META-INF/CERT"))
                    continue;
                ze = new ZipEntry(e.getName());
                ze.setSize(e.getSize());
                ze.setCompressedSize(e.getCompressedSize());
                zipOutputStream.putNextEntry(ze);
                if (!e.isDirectory()) {
                    InputStream is = zipFile.getInputStream(e);
                    if (e.getName().equals("AndroidManifest.xml")) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        AXMLPrinter.out = new PrintStream(baos);
                        AXMLPrinter.decode(is);
                        byte[] buff = baos.toByteArray();
                        Encoder encoder = new Encoder();
                        byte[] bs = encoder.encodeString(new Context(), new String(addChannel(buff, channel)));
                        zipOutputStream.write(bs);
                        baos.close();
                    } else {
                        int len = 0;
                        byte[] buff = new byte[1024];
                        while ((len = is.read(buff)) > 0) {
                            zipOutputStream.write(buff, 0, len);
                        }
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
        File sign = new File(zipOutputStream.file.getParent(), "sign.apk");
        sign(zipOutputStream.file, sign);
        sign.renameTo(zipOutputStream.file);
        return file;
    }

    private static final boolean debug = true;

    @Override
    public String getChannel(File file) {
        ZipFile zipFile = null;
        InputStream is = null;
        String channel="test";
        try {
            zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                if (e.getName().equals("AndroidManifest.xml")) {
                    is = zipFile.getInputStream(e);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    AXMLPrinter.out = new PrintStream(baos);
                    AXMLPrinter.decode(is);
                    Paras paras = ParasFactory.sax();
                    byte[] buff = baos.toByteArray();
                    baos.close();
                    NodeTree nodeTree = paras.is2nodeTree(new ByteArrayInputStream(buff));
                    List<NodeTree> nodeTrees = nodeTree.getChildNodeTrees();
                    for (NodeTree tree : nodeTrees) {
                        if ("application".equals(tree.getName())) {
                            nodeTrees = tree.getChildNodeTrees();
                            break;
                        }
                    }
                    for (NodeTree tree : nodeTrees) {
                        if ("meta-data".equals(tree.getName())) {
                            String value = tree.getAttributes().get("android:name");
                            if (CHANNEL_META_NAME.equals(value)) {
                                channel=tree.getAttributes().get("android:value");
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(is);
            close(zipFile);
        }
        return channel;
    }

    public byte[] addChannel(byte[] buff, String channel) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buff)));
        String line;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));
        boolean add = false;
        while ((line = reader.readLine()) != null) {
//            if (debug&&line.contains("<application")) {
//                while (true) {
//                    if (line.contains(">")) {
//                        writer.write("\t\tandroid:debuggable=\"true\"");
//                        writer.newLine();
//                        writer.write(line);
//                        writer.newLine();
//                        break;
//                    }
//                    writer.write(line);
//                    writer.newLine();
//                    line = reader.readLine();
//                }
//                continue;
//            }
            if (channel != null && !channel.isEmpty()) {
                if (line.contains(String.format("android:name=\"%s\"", CHANNEL_META_NAME))) {
                    writer.write(line);
                    writer.newLine();
                    reader.readLine();
                    writer.write(String.format("\t\t\tandroid:value=\"%s\"\n", channel));
                    writer.newLine();
                    add = true;
                    continue;
                }
                if (line.contains("</application>") && !add) {
                    writer.write(String.format("\t\t<meta-data\n" +
                            "\t\t\tandroid:name=\"%s\"\n" +
                            "\t\t\tandroid:value=\"%s\"\n" +
                            "\t\t\t>\n" +
                            "\t\t</meta-data>", CHANNEL_META_NAME, channel));
                    writer.newLine();
                }
            }

            writer.write(line);
            writer.newLine();
        }
        writer.close();
        reader.close();
        byte[] bs = baos.toByteArray();
//        System.out.println(new String(bs));
        return bs;
    }


    private static class ZOS extends ZipOutputStream {
        private String channel;
        private File file;

        public ZOS(File out, String channel) throws IOException {
            super(new FileOutputStream(out));
            this.channel = channel;
            file = out;
        }

    }

}
