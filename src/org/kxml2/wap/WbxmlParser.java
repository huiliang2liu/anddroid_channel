//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.kxml2.wap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Vector;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class WbxmlParser implements XmlPullParser {
    static final String HEX_DIGITS = "0123456789abcdef";
    public static final int WAP_EXTENSION = 64;
    private static final String UNEXPECTED_EOF = "Unexpected EOF";
    private static final String ILLEGAL_TYPE = "Wrong event type";
    private InputStream in;
    private int TAG_TABLE = 0;
    private int ATTR_START_TABLE = 1;
    private int ATTR_VALUE_TABLE = 2;
    private String[] attrStartTable;
    private String[] attrValueTable;
    private String[] tagTable;
    private byte[] stringTable;
    private Hashtable cacheStringTable = null;
    private boolean processNsp;
    private int depth;
    private String[] elementStack = new String[16];
    private String[] nspStack = new String[8];
    private int[] nspCounts = new int[4];
    private int attributeCount;
    private String[] attributes = new String[16];
    private int nextId = -2;
    private Vector tables = new Vector();
    private int version;
    private int publicIdentifierId;
    private String prefix;
    private String namespace;
    private String name;
    private String text;
    private Object wapExtensionData;
    private int wapCode;
    private int type;
    private boolean degenerated;
    private boolean isWhitespace;
    private String encoding;

    public WbxmlParser() {
    }

    public boolean getFeature(String var1) {
        return "http://xmlpull.org/v1/doc/features.html#process-namespaces".equals(var1) ? this.processNsp : false;
    }

    public String getInputEncoding() {
        return this.encoding;
    }

    public void defineEntityReplacementText(String var1, String var2) throws XmlPullParserException {
    }

    public Object getProperty(String var1) {
        return null;
    }

    public int getNamespaceCount(int var1) {
        if (var1 > this.depth) {
            throw new IndexOutOfBoundsException();
        } else {
            return this.nspCounts[var1];
        }
    }

    public String getNamespacePrefix(int var1) {
        return this.nspStack[var1 << 1];
    }

    public String getNamespaceUri(int var1) {
        return this.nspStack[(var1 << 1) + 1];
    }

    public String getNamespace(String var1) {
        if ("xml".equals(var1)) {
            return "http://www.w3.org/XML/1998/namespace";
        } else if ("xmlns".equals(var1)) {
            return "http://www.w3.org/2000/xmlns/";
        } else {
            for(int var2 = (this.getNamespaceCount(this.depth) << 1) - 2; var2 >= 0; var2 -= 2) {
                if (var1 == null) {
                    if (this.nspStack[var2] == null) {
                        return this.nspStack[var2 + 1];
                    }
                } else if (var1.equals(this.nspStack[var2])) {
                    return this.nspStack[var2 + 1];
                }
            }

            return null;
        }
    }

    public int getDepth() {
        return this.depth;
    }

    public String getPositionDescription() {
        StringBuffer var1 = new StringBuffer(this.type < XmlPullParser.TYPES.length ? XmlPullParser.TYPES[this.type] : "unknown");
        var1.append(' ');
        if (this.type != 2 && this.type != 3) {
            if (this.type != 7) {
                if (this.type != 4) {
                    var1.append(this.getText());
                } else if (this.isWhitespace) {
                    var1.append("(whitespace)");
                } else {
                    String var4 = this.getText();
                    if (var4.length() > 16) {
                        var4 = var4.substring(0, 16) + "...";
                    }

                    var1.append(var4);
                }
            }
        } else {
            if (this.degenerated) {
                var1.append("(empty) ");
            }

            var1.append('<');
            if (this.type == 3) {
                var1.append('/');
            }

            if (this.prefix != null) {
                var1.append("{" + this.namespace + "}" + this.prefix + ":");
            }

            var1.append(this.name);
            int var2 = this.attributeCount << 2;

            for(int var3 = 0; var3 < var2; var3 += 4) {
                var1.append(' ');
                if (this.attributes[var3 + 1] != null) {
                    var1.append("{" + this.attributes[var3] + "}" + this.attributes[var3 + 1] + ":");
                }

                var1.append(this.attributes[var3 + 2] + "='" + this.attributes[var3 + 3] + "'");
            }

            var1.append('>');
        }

        return var1.toString();
    }

    public int getLineNumber() {
        return -1;
    }

    public int getColumnNumber() {
        return -1;
    }

    public boolean isWhitespace() throws XmlPullParserException {
        if (this.type != 4 && this.type != 7 && this.type != 5) {
            this.exception("Wrong event type");
        }

        return this.isWhitespace;
    }

    public String getText() {
        return this.text;
    }

    public char[] getTextCharacters(int[] var1) {
        if (this.type >= 4) {
            var1[0] = 0;
            var1[1] = this.text.length();
            char[] var2 = new char[this.text.length()];
            this.text.getChars(0, this.text.length(), var2, 0);
            return var2;
        } else {
            var1[0] = -1;
            var1[1] = -1;
            return null;
        }
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getName() {
        return this.name;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public boolean isEmptyElementTag() throws XmlPullParserException {
        if (this.type != 2) {
            this.exception("Wrong event type");
        }

        return this.degenerated;
    }

    public int getAttributeCount() {
        return this.attributeCount;
    }

    public String getAttributeType(int var1) {
        return "CDATA";
    }

    public boolean isAttributeDefault(int var1) {
        return false;
    }

    public String getAttributeNamespace(int var1) {
        if (var1 >= this.attributeCount) {
            throw new IndexOutOfBoundsException();
        } else {
            return this.attributes[var1 << 2];
        }
    }

    public String getAttributeName(int var1) {
        if (var1 >= this.attributeCount) {
            throw new IndexOutOfBoundsException();
        } else {
            return this.attributes[(var1 << 2) + 2];
        }
    }

    public String getAttributePrefix(int var1) {
        if (var1 >= this.attributeCount) {
            throw new IndexOutOfBoundsException();
        } else {
            return this.attributes[(var1 << 2) + 1];
        }
    }

    public String getAttributeValue(int var1) {
        if (var1 >= this.attributeCount) {
            throw new IndexOutOfBoundsException();
        } else {
            return this.attributes[(var1 << 2) + 3];
        }
    }

    public String getAttributeValue(String var1, String var2) {
        for(int var3 = (this.attributeCount << 2) - 4; var3 >= 0; var3 -= 4) {
            if (this.attributes[var3 + 2].equals(var2) && (var1 == null || this.attributes[var3].equals(var1))) {
                return this.attributes[var3 + 3];
            }
        }

        return null;
    }

    public int getEventType() throws XmlPullParserException {
        return this.type;
    }

    public int next() throws XmlPullParserException, IOException {
        this.isWhitespace = true;
        int var1 = 9999;

        label41:
        while(true) {
            String var2;
            do {
                var2 = this.text;
                this.nextImpl();
                if (this.type < var1) {
                    var1 = this.type;
                }
            } while(var1 > 5);

            if (var1 < 4) {
                break;
            }

            if (var2 != null) {
                this.text = this.text == null ? var2 : var2 + this.text;
            }

            switch(this.peekId()) {
                case 2:
                case 3:
                case 4:
                case 68:
                case 131:
                case 132:
                case 196:
                    break;
                default:
                    break label41;
            }
        }

        this.type = var1;
        if (this.type > 4) {
            this.type = 4;
        }

        return this.type;
    }

    public int nextToken() throws XmlPullParserException, IOException {
        this.isWhitespace = true;
        this.nextImpl();
        return this.type;
    }

    public int nextTag() throws XmlPullParserException, IOException {
        this.next();
        if (this.type == 4 && this.isWhitespace) {
            this.next();
        }

        if (this.type != 3 && this.type != 2) {
            this.exception("unexpected type");
        }

        return this.type;
    }

    public String nextText() throws XmlPullParserException, IOException {
        if (this.type != 2) {
            this.exception("precondition: START_TAG");
        }

        this.next();
        String var1;
        if (this.type == 4) {
            var1 = this.getText();
            this.next();
        } else {
            var1 = "";
        }

        if (this.type != 3) {
            this.exception("END_TAG expected");
        }

        return var1;
    }

    public void require(int var1, String var2, String var3) throws XmlPullParserException, IOException {
        if (var1 != this.type || var2 != null && !var2.equals(this.getNamespace()) || var3 != null && !var3.equals(this.getName())) {
            this.exception("expected: " + (var1 == 64 ? "WAP Ext." : XmlPullParser.TYPES[var1] + " {" + var2 + "}" + var3));
        }

    }

    public void setInput(Reader var1) throws XmlPullParserException {
        this.exception("InputStream required");
    }

    public void setInput(InputStream var1, String var2) throws XmlPullParserException {
        this.in = var1;

        try {
            this.version = this.readByte();
            this.publicIdentifierId = this.readInt();
            if (this.publicIdentifierId == 0) {
                this.readInt();
            }

            int var3 = this.readInt();
            if (null == var2) {
                switch(var3) {
                    case 4:
                        this.encoding = "ISO-8859-1";
                        break;
                    case 106:
                        this.encoding = "UTF-8";
                        break;
                    default:
                        throw new UnsupportedEncodingException("" + var3);
                }
            } else {
                this.encoding = var2;
            }

            int var4 = this.readInt();
            this.stringTable = new byte[var4];

            int var6;
            for(int var5 = 0; var5 < var4; var5 += var6) {
                var6 = var1.read(this.stringTable, var5, var4 - var5);
                if (var6 <= 0) {
                    break;
                }
            }

            this.selectPage(0, true);
            this.selectPage(0, false);
        } catch (IOException var7) {
            this.exception("Illegal input format");
        }

    }

    public void setFeature(String var1, boolean var2) throws XmlPullParserException {
        if ("http://xmlpull.org/v1/doc/features.html#process-namespaces".equals(var1)) {
            this.processNsp = var2;
        } else {
            this.exception("unsupported feature: " + var1);
        }

    }

    public void setProperty(String var1, Object var2) throws XmlPullParserException {
        throw new XmlPullParserException("unsupported property: " + var1);
    }

    private final boolean adjustNsp() throws XmlPullParserException {
        boolean var1 = false;

        int var2;
        String var3;
        int var4;
        String var5;
        for(var2 = 0; var2 < this.attributeCount << 2; var2 += 4) {
            var3 = this.attributes[var2 + 2];
            var4 = var3.indexOf(58);
            if (var4 != -1) {
                var5 = var3.substring(0, var4);
                var3 = var3.substring(var4 + 1);
            } else {
                if (!var3.equals("xmlns")) {
                    continue;
                }

                var5 = var3;
                var3 = null;
            }

            if (!var5.equals("xmlns")) {
                var1 = true;
            } else {
                int var6 = this.nspCounts[this.depth]++ << 1;
                this.nspStack = this.ensureCapacity(this.nspStack, var6 + 2);
                this.nspStack[var6] = var3;
                this.nspStack[var6 + 1] = this.attributes[var2 + 3];
                if (var3 != null && this.attributes[var2 + 3].equals("")) {
                    this.exception("illegal empty namespace");
                }

                System.arraycopy(this.attributes, var2 + 4, this.attributes, var2, (--this.attributeCount << 2) - var2);
                var2 -= 4;
            }
        }

        if (var1) {
            for(var2 = (this.attributeCount << 2) - 4; var2 >= 0; var2 -= 4) {
                var3 = this.attributes[var2 + 2];
                var4 = var3.indexOf(58);
                if (var4 == 0) {
                    throw new RuntimeException("illegal attribute name: " + var3 + " at " + this);
                }

                if (var4 != -1) {
                    var5 = var3.substring(0, var4);
                    var3 = var3.substring(var4 + 1);
                    String var8 = this.getNamespace(var5);
                    if (var8 == null) {
                        throw new RuntimeException("Undefined Prefix: " + var5 + " in " + this);
                    }

                    this.attributes[var2] = var8;
                    this.attributes[var2 + 1] = var5;
                    this.attributes[var2 + 2] = var3;

                    for(int var7 = (this.attributeCount << 2) - 4; var7 > var2; var7 -= 4) {
                        if (var3.equals(this.attributes[var7 + 2]) && var8.equals(this.attributes[var7])) {
                            this.exception("Duplicate Attribute: {" + var8 + "}" + var3);
                        }
                    }
                }
            }
        }

        var2 = this.name.indexOf(58);
        if (var2 == 0) {
            this.exception("illegal tag name: " + this.name);
        } else if (var2 != -1) {
            this.prefix = this.name.substring(0, var2);
            this.name = this.name.substring(var2 + 1);
        }

        this.namespace = this.getNamespace(this.prefix);
        if (this.namespace == null) {
            if (this.prefix != null) {
                this.exception("undefined prefix: " + this.prefix);
            }

            this.namespace = "";
        }

        return var1;
    }

    private final void setTable(int var1, int var2, String[] var3) {
        if (this.stringTable != null) {
            throw new RuntimeException("setXxxTable must be called before setInput!");
        } else {
            while(this.tables.size() < 3 * var1 + 3) {
                this.tables.addElement((Object)null);
            }

            this.tables.setElementAt(var3, var1 * 3 + var2);
        }
    }

    private final void exception(String var1) throws XmlPullParserException {
        throw new XmlPullParserException(var1, this, (Throwable)null);
    }

    private void selectPage(int var1, boolean var2) throws XmlPullParserException {
        if (this.tables.size() != 0 || var1 != 0) {
            if (var1 * 3 > this.tables.size()) {
                this.exception("Code Page " + var1 + " undefined!");
            }

            if (var2) {
                this.tagTable = (String[])((String[])this.tables.elementAt(var1 * 3 + this.TAG_TABLE));
            } else {
                this.attrStartTable = (String[])((String[])this.tables.elementAt(var1 * 3 + this.ATTR_START_TABLE));
                this.attrValueTable = (String[])((String[])this.tables.elementAt(var1 * 3 + this.ATTR_VALUE_TABLE));
            }

        }
    }

    private final void nextImpl() throws IOException, XmlPullParserException {
        if (this.type == 3) {
            --this.depth;
        }

        if (this.degenerated) {
            this.type = 3;
            this.degenerated = false;
        } else {
            this.text = null;
            this.prefix = null;
            this.name = null;

            int var2;
            for(var2 = this.peekId(); var2 == 0; var2 = this.peekId()) {
                this.nextId = -2;
                this.selectPage(this.readByte(), true);
            }

            this.nextId = -2;
            switch(var2) {
                case -1:
                    this.type = 1;
                    break;
                case 1:
                    int var4 = this.depth - 1 << 2;
                    this.type = 3;
                    this.namespace = this.elementStack[var4];
                    this.prefix = this.elementStack[var4 + 1];
                    this.name = this.elementStack[var4 + 2];
                    break;
                case 2:
                    this.type = 6;
                    char var3 = (char)this.readInt();
                    this.text = "" + var3;
                    this.name = "#" + var3;
                    break;
                case 3:
                    this.type = 4;
                    this.text = this.readStrI();
                    break;
                case 64:
                case 65:
                case 66:
                case 128:
                case 129:
                case 130:
                case 192:
                case 193:
                case 194:
                case 195:
                    this.type = 64;
                    this.wapCode = var2;
                    this.wapExtensionData = this.parseWapExtension(var2);
                    break;
                case 67:
                    throw new RuntimeException("PI curr. not supp.");
                case 131:
                    this.type = 4;
                    this.text = this.readStrT();
                    break;
                default:
                    this.parseElement(var2);
            }

        }
    }

    public Object parseWapExtension(int var1) throws IOException, XmlPullParserException {
        switch(var1) {
            case 64:
            case 65:
            case 66:
                return this.readStrI();
            case 128:
            case 129:
            case 130:
                return new Integer(this.readInt());
            case 192:
            case 193:
            case 194:
                return null;
            case 195:
                int var2 = this.readInt();

                byte[] var3;
                for(var3 = new byte[var2]; var2 > 0; var2 -= this.in.read(var3, var3.length - var2, var2)) {
                }

                return var3;
            default:
                this.exception("illegal id: " + var1);
                return null;
        }
    }

    public void readAttr() throws IOException, XmlPullParserException {
        int var1 = this.readByte();

        for(int var2 = 0; var1 != 1; ++this.attributeCount) {
            while(var1 == 0) {
                this.selectPage(this.readByte(), false);
                var1 = this.readByte();
            }

            String var3 = this.resolveId(this.attrStartTable, var1);
            int var5 = var3.indexOf(61);
            StringBuffer var4;
            if (var5 == -1) {
                var4 = new StringBuffer();
            } else {
                var4 = new StringBuffer(var3.substring(var5 + 1));
                var3 = var3.substring(0, var5);
            }

            for(var1 = this.readByte(); var1 > 128 || var1 == 0 || var1 == 2 || var1 == 3 || var1 == 131 || var1 >= 64 && var1 <= 66 || var1 >= 128 && var1 <= 130; var1 = this.readByte()) {
                switch(var1) {
                    case 0:
                        this.selectPage(this.readByte(), false);
                        break;
                    case 2:
                        var4.append((char)this.readInt());
                        break;
                    case 3:
                        var4.append(this.readStrI());
                        break;
                    case 64:
                    case 65:
                    case 66:
                    case 128:
                    case 129:
                    case 130:
                    case 192:
                    case 193:
                    case 194:
                    case 195:
                        var4.append(this.resolveWapExtension(var1, this.parseWapExtension(var1)));
                        break;
                    case 131:
                        var4.append(this.readStrT());
                        break;
                    default:
                        var4.append(this.resolveId(this.attrValueTable, var1));
                }
            }

            this.attributes = this.ensureCapacity(this.attributes, var2 + 4);
            this.attributes[var2++] = "";
            this.attributes[var2++] = null;
            this.attributes[var2++] = var3;
            this.attributes[var2++] = var4.toString();
        }

    }

    private int peekId() throws IOException {
        if (this.nextId == -2) {
            this.nextId = this.in.read();
        }

        return this.nextId;
    }

    protected String resolveWapExtension(int var1, Object var2) {
        if (!(var2 instanceof byte[])) {
            return "$(" + var2 + ")";
        } else {
            StringBuffer var3 = new StringBuffer();
            byte[] var4 = (byte[])((byte[])var2);

            for(int var5 = 0; var5 < var4.length; ++var5) {
                var3.append("0123456789abcdef".charAt(var4[var5] >> 4 & 15));
                var3.append("0123456789abcdef".charAt(var4[var5] & 15));
            }

            return var3.toString();
        }
    }

    String resolveId(String[] var1, int var2) throws IOException {
        int var3 = (var2 & 127) - 5;
        if (var3 == -1) {
            this.wapCode = -1;
            return this.readStrT();
        } else if (var3 >= 0 && var1 != null && var3 < var1.length && var1[var3] != null) {
            this.wapCode = var3 + 5;
            return var1[var3];
        } else {
            throw new IOException("id " + var2 + " undef.");
        }
    }

    void parseElement(int var1) throws IOException, XmlPullParserException {
        this.type = 2;
        this.name = this.resolveId(this.tagTable, var1 & 63);
        this.attributeCount = 0;
        if ((var1 & 128) != 0) {
            this.readAttr();
        }

        this.degenerated = (var1 & 64) == 0;
        int var2 = this.depth++ << 2;
        this.elementStack = this.ensureCapacity(this.elementStack, var2 + 4);
        this.elementStack[var2 + 3] = this.name;
        if (this.depth >= this.nspCounts.length) {
            int[] var3 = new int[this.depth + 4];
            System.arraycopy(this.nspCounts, 0, var3, 0, this.nspCounts.length);
            this.nspCounts = var3;
        }

        this.nspCounts[this.depth] = this.nspCounts[this.depth - 1];

        for(int var5 = this.attributeCount - 1; var5 > 0; --var5) {
            for(int var4 = 0; var4 < var5; ++var4) {
                if (this.getAttributeName(var5).equals(this.getAttributeName(var4))) {
                    this.exception("Duplicate Attribute: " + this.getAttributeName(var5));
                }
            }
        }

        if (this.processNsp) {
            this.adjustNsp();
        } else {
            this.namespace = "";
        }

        this.elementStack[var2] = this.namespace;
        this.elementStack[var2 + 1] = this.prefix;
        this.elementStack[var2 + 2] = this.name;
    }

    private final String[] ensureCapacity(String[] var1, int var2) {
        if (var1.length >= var2) {
            return var1;
        } else {
            String[] var3 = new String[var2 + 16];
            System.arraycopy(var1, 0, var3, 0, var1.length);
            return var3;
        }
    }

    int readByte() throws IOException {
        int var1 = this.in.read();
        if (var1 == -1) {
            throw new IOException("Unexpected EOF");
        } else {
            return var1;
        }
    }

    int readInt() throws IOException {
        int var1 = 0;

        int var2;
        do {
            var2 = this.readByte();
            var1 = var1 << 7 | var2 & 127;
        } while((var2 & 128) != 0);

        return var1;
    }

    String readStrI() throws IOException {
        ByteArrayOutputStream var1 = new ByteArrayOutputStream();
        boolean var2 = true;

        while(true) {
            int var3 = this.in.read();
            if (var3 == 0) {
                this.isWhitespace = var2;
                String var4 = new String(var1.toByteArray(), this.encoding);
                var1.close();
                return var4;
            }

            if (var3 == -1) {
                throw new IOException("Unexpected EOF");
            }

            if (var3 > 32) {
                var2 = false;
            }

            var1.write(var3);
        }
    }

    String readStrT() throws IOException {
        int var1 = this.readInt();
        if (this.cacheStringTable == null) {
            this.cacheStringTable = new Hashtable();
        }

        String var2 = (String)this.cacheStringTable.get(new Integer(var1));
        if (var2 == null) {
            int var3;
            for(var3 = var1; var3 < this.stringTable.length && this.stringTable[var3] != 0; ++var3) {
            }

            var2 = new String(this.stringTable, var1, var3 - var1, this.encoding);
            this.cacheStringTable.put(new Integer(var1), var2);
        }

        return var2;
    }

    public void setTagTable(int var1, String[] var2) {
        this.setTable(var1, this.TAG_TABLE, var2);
    }

    public void setAttrStartTable(int var1, String[] var2) {
        this.setTable(var1, this.ATTR_START_TABLE, var2);
    }

    public void setAttrValueTable(int var1, String[] var2) {
        this.setTable(var1, this.ATTR_VALUE_TABLE, var2);
    }

    public int getWapCode() {
        return this.wapCode;
    }

    public Object getWapExtensionData() {
        return this.wapExtensionData;
    }
}
