//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.kxml2.wap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Vector;
import org.xmlpull.v1.XmlSerializer;

public class WbxmlSerializer implements XmlSerializer {
    Hashtable stringTable = new Hashtable();
    OutputStream out;
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    ByteArrayOutputStream stringTableBuf = new ByteArrayOutputStream();
    String pending;
    int depth;
    String name;
    String namespace;
    Vector attributes = new Vector();
    Hashtable attrStartTable = new Hashtable();
    Hashtable attrValueTable = new Hashtable();
    Hashtable tagTable = new Hashtable();
    private int attrPage;
    private int tagPage;
    private String encoding;

    public WbxmlSerializer() {
    }

    public XmlSerializer attribute(String var1, String var2, String var3) {
        this.attributes.addElement(var2);
        this.attributes.addElement(var3);
        return this;
    }

    public void cdsect(String var1) throws IOException {
        this.text(var1);
    }

    public void comment(String var1) {
    }

    public void docdecl(String var1) {
        throw new RuntimeException("Cannot write docdecl for WBXML");
    }

    public void entityRef(String var1) {
        throw new RuntimeException("EntityReference not supported for WBXML");
    }

    public int getDepth() {
        return this.depth;
    }

    public boolean getFeature(String var1) {
        return false;
    }

    public String getNamespace() {
        throw new RuntimeException("NYI");
    }

    public String getName() {
        throw new RuntimeException("NYI");
    }

    public String getPrefix(String var1, boolean var2) {
        throw new RuntimeException("NYI");
    }

    public Object getProperty(String var1) {
        return null;
    }

    public void ignorableWhitespace(String var1) {
    }

    public void endDocument() throws IOException {
        writeInt(this.out, this.stringTableBuf.size());
        this.out.write(this.stringTableBuf.toByteArray());
        this.out.write(this.buf.toByteArray());
        this.out.flush();
    }

    public void flush() {
    }

    public void checkPending(boolean var1) throws IOException {
        if (this.pending != null) {
            int var2 = this.attributes.size();
            int[] var3 = (int[])((int[])this.tagTable.get(this.pending));
            if (var3 == null) {
                this.buf.write(var2 == 0 ? (var1 ? 4 : 68) : (var1 ? 132 : 196));
                this.writeStrT(this.pending, false);
            } else {
                if (var3[0] != this.tagPage) {
                    this.tagPage = var3[0];
                    this.buf.write(0);
                    this.buf.write(this.tagPage);
                }

                this.buf.write(var2 == 0 ? (var1 ? var3[1] : var3[1] | 64) : (var1 ? var3[1] | 128 : var3[1] | 192));
            }

            for(int var4 = 0; var4 < var2; ++var4) {
                var3 = (int[])((int[])this.attrStartTable.get(this.attributes.elementAt(var4)));
                if (var3 == null) {
                    this.buf.write(4);
                    this.writeStrT((String)this.attributes.elementAt(var4), false);
                } else {
                    if (var3[0] != this.attrPage) {
                        this.attrPage = var3[0];
                        this.buf.write(0);
                        this.buf.write(this.attrPage);
                    }

                    this.buf.write(var3[1]);
                }

                ++var4;
                var3 = (int[])((int[])this.attrValueTable.get(this.attributes.elementAt(var4)));
                if (var3 == null) {
                    this.writeStr((String)this.attributes.elementAt(var4));
                } else {
                    if (var3[0] != this.attrPage) {
                        this.attrPage = var3[0];
                        this.buf.write(0);
                        this.buf.write(this.attrPage);
                    }

                    this.buf.write(var3[1]);
                }
            }

            if (var2 > 0) {
                this.buf.write(1);
            }

            this.pending = null;
            this.attributes.removeAllElements();
        }
    }

    public void processingInstruction(String var1) {
        throw new RuntimeException("PI NYI");
    }

    public void setFeature(String var1, boolean var2) {
        throw new IllegalArgumentException("unknown feature " + var1);
    }

    public void setOutput(Writer var1) {
        throw new RuntimeException("Wbxml requires an OutputStream!");
    }

    public void setOutput(OutputStream var1, String var2) throws IOException {
        this.encoding = var2 == null ? "UTF-8" : var2;
        this.out = var1;
        this.buf = new ByteArrayOutputStream();
        this.stringTableBuf = new ByteArrayOutputStream();
    }

    public void setPrefix(String var1, String var2) {
        throw new RuntimeException("NYI");
    }

    public void setProperty(String var1, Object var2) {
        throw new IllegalArgumentException("unknown property " + var1);
    }

    public void startDocument(String var1, Boolean var2) throws IOException {
        this.out.write(3);
        this.out.write(1);
        if (var1 != null) {
            this.encoding = var1;
        }

        if (this.encoding.toUpperCase().equals("UTF-8")) {
            this.out.write(106);
        } else {
            if (!this.encoding.toUpperCase().equals("ISO-8859-1")) {
                throw new UnsupportedEncodingException(var1);
            }

            this.out.write(4);
        }

    }

    public XmlSerializer startTag(String var1, String var2) throws IOException {
        if (var1 != null && !"".equals(var1)) {
            throw new RuntimeException("NSP NYI");
        } else {
            this.checkPending(false);
            this.pending = var2;
            ++this.depth;
            return this;
        }
    }

    public XmlSerializer text(char[] var1, int var2, int var3) throws IOException {
        this.checkPending(false);
        this.writeStr(new String(var1, var2, var3));
        return this;
    }

    public XmlSerializer text(String var1) throws IOException {
        this.checkPending(false);
        this.writeStr(var1);
        return this;
    }

    private void writeStr(String var1) throws IOException {
        int var2 = 0;
        int var3 = 0;

        int var4;
        int var5;
        for(var4 = var1.length(); var2 < var4; var2 = var5) {
            while(var2 < var4 && var1.charAt(var2) < 'A') {
                ++var2;
            }

            for(var5 = var2; var5 < var4 && var1.charAt(var5) >= 'A'; ++var5) {
            }

            if (var5 - var2 > 10) {
                if (var2 > var3 && var1.charAt(var2 - 1) == ' ' && this.stringTable.get(var1.substring(var2, var5)) == null) {
                    this.buf.write(131);
                    this.writeStrT(var1.substring(var3, var5), false);
                } else {
                    if (var2 > var3 && var1.charAt(var2 - 1) == ' ') {
                        --var2;
                    }

                    if (var2 > var3) {
                        this.buf.write(131);
                        this.writeStrT(var1.substring(var3, var2), false);
                    }

                    this.buf.write(131);
                    this.writeStrT(var1.substring(var2, var5), true);
                }

                var3 = var5;
            }
        }

        if (var3 < var4) {
            this.buf.write(131);
            this.writeStrT(var1.substring(var3, var4), false);
        }

    }

    public XmlSerializer endTag(String var1, String var2) throws IOException {
        if (this.pending != null) {
            this.checkPending(true);
        } else {
            this.buf.write(1);
        }

        --this.depth;
        return this;
    }

    public void writeWapExtension(int var1, Object var2) throws IOException {
        this.checkPending(false);
        this.buf.write(var1);
        switch(var1) {
            case 64:
            case 65:
            case 66:
                this.writeStrI(this.buf, (String)var2);
                break;
            case 128:
            case 129:
            case 130:
                this.writeStrT((String)var2, false);
            case 192:
            case 193:
            case 194:
                break;
            case 195:
                byte[] var3 = (byte[])((byte[])var2);
                writeInt(this.buf, var3.length);
                this.buf.write(var3);
                break;
            default:
                throw new IllegalArgumentException();
        }

    }

    static void writeInt(OutputStream var0, int var1) throws IOException {
        byte[] var2 = new byte[5];
        int var3 = 0;

        do {
            var2[var3++] = (byte)(var1 & 127);
            var1 >>= 7;
        } while(var1 != 0);

        while(var3 > 1) {
            --var3;
            var0.write(var2[var3] | 128);
        }

        var0.write(var2[0]);
    }

    void writeStrI(OutputStream var1, String var2) throws IOException {
        byte[] var3 = var2.getBytes(this.encoding);
        var1.write(var3);
        var1.write(0);
    }

    private final void writeStrT(String var1, boolean var2) throws IOException {
        Integer var3 = (Integer)this.stringTable.get(var1);
        if (var3 != null) {
            writeInt(this.buf, var3);
        } else {
            int var4 = this.stringTableBuf.size();
            if (var1.charAt(0) >= '0' && var2) {
                var1 = ' ' + var1;
                writeInt(this.buf, var4 + 1);
            } else {
                writeInt(this.buf, var4);
            }

            this.stringTable.put(var1, new Integer(var4));
            if (var1.charAt(0) == ' ') {
                this.stringTable.put(var1.substring(1), new Integer(var4 + 1));
            }

            int var5 = var1.lastIndexOf(32);
            if (var5 > 1) {
                this.stringTable.put(var1.substring(var5), new Integer(var4 + var5));
                this.stringTable.put(var1.substring(var5 + 1), new Integer(var4 + var5 + 1));
            }

            this.writeStrI(this.stringTableBuf, var1);
            this.stringTableBuf.flush();
        }

    }

    public void setTagTable(int var1, String[] var2) {
        for(int var3 = 0; var3 < var2.length; ++var3) {
            if (var2[var3] != null) {
                int[] var4 = new int[]{var1, var3 + 5};
                this.tagTable.put(var2[var3], var4);
            }
        }

    }

    public void setAttrStartTable(int var1, String[] var2) {
        for(int var3 = 0; var3 < var2.length; ++var3) {
            if (var2[var3] != null) {
                int[] var4 = new int[]{var1, var3 + 5};
                this.attrStartTable.put(var2[var3], var4);
            }
        }

    }

    public void setAttrValueTable(int var1, String[] var2) {
        for(int var3 = 0; var3 < var2.length; ++var3) {
            if (var2[var3] != null) {
                int[] var4 = new int[]{var1, var3 + 133};
                this.attrValueTable.put(var2[var3], var4);
            }
        }

    }
}
