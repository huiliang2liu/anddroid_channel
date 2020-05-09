//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.kxml2.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Hashtable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class KXmlParser implements XmlPullParser {
    private Object location;
    private static final String UNEXPECTED_EOF = "Unexpected EOF";
    private static final String ILLEGAL_TYPE = "Wrong event type";
    private static final int LEGACY = 999;
    private static final int XML_DECL = 998;
    private String version;
    private Boolean standalone;
    private boolean processNsp;
    private boolean relaxed;
    private Hashtable entityMap;
    private int depth;
    private String[] elementStack = new String[16];
    private String[] nspStack = new String[8];
    private int[] nspCounts = new int[4];
    private Reader reader;
    private String encoding;
    private char[] srcBuf = new char[Runtime.getRuntime().freeMemory() >= 1048576L ? 8192 : 128];
    private int srcPos;
    private int srcCount;
    private int line;
    private int column;
    private char[] txtBuf = new char[128];
    private int txtPos;
    private int type;
    private boolean isWhitespace;
    private String namespace;
    private String prefix;
    private String name;
    private boolean degenerated;
    private int attributeCount;
    private String[] attributes = new String[16];
    private int stackMismatch = 0;
    private String error;
    private int[] peek = new int[2];
    private int peekCount;
    private boolean wasCR;
    private boolean unresolved;
    private boolean token;

    public KXmlParser() {
    }

    private final boolean isProp(String var1, boolean var2, String var3) {
        if (!var1.startsWith("http://xmlpull.org/v1/doc/")) {
            return false;
        } else {
            return var2 ? var1.substring(42).equals(var3) : var1.substring(40).equals(var3);
        }
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
                    this.error("illegal empty namespace");
                }

                System.arraycopy(this.attributes, var2 + 4, this.attributes, var2, (--this.attributeCount << 2) - var2);
                var2 -= 4;
            }
        }

        if (var1) {
            for(var2 = (this.attributeCount << 2) - 4; var2 >= 0; var2 -= 4) {
                var3 = this.attributes[var2 + 2];
                var4 = var3.indexOf(58);
                if (var4 == 0 && !this.relaxed) {
                    throw new RuntimeException("illegal attribute name: " + var3 + " at " + this);
                }

                if (var4 != -1) {
                    var5 = var3.substring(0, var4);
                    var3 = var3.substring(var4 + 1);
                    String var7 = this.getNamespace(var5);
                    if (var7 == null && !this.relaxed) {
                        throw new RuntimeException("Undefined Prefix: " + var5 + " in " + this);
                    }

                    this.attributes[var2] = var7;
                    this.attributes[var2 + 1] = var5;
                    this.attributes[var2 + 2] = var3;
                }
            }
        }

        var2 = this.name.indexOf(58);
        if (var2 == 0) {
            this.error("illegal tag name: " + this.name);
        }

        if (var2 != -1) {
            this.prefix = this.name.substring(0, var2);
            this.name = this.name.substring(var2 + 1);
        }

        this.namespace = this.getNamespace(this.prefix);
        if (this.namespace == null) {
            if (this.prefix != null) {
                this.error("undefined prefix: " + this.prefix);
            }

            this.namespace = "";
        }

        return var1;
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

    private final void error(String var1) throws XmlPullParserException {
        if (this.relaxed) {
            if (this.error == null) {
                this.error = "ERR: " + var1;
            }
        } else {
            this.exception(var1);
        }

    }

    private final void exception(String var1) throws XmlPullParserException {
        throw new XmlPullParserException(var1.length() < 100 ? var1 : var1.substring(0, 100) + "\n", this, (Throwable)null);
    }

    private final void nextImpl() throws IOException, XmlPullParserException {
        if (this.reader == null) {
            this.exception("No Input specified");
        }

        if (this.type == 3) {
            --this.depth;
        }

        while(true) {
            this.attributeCount = -1;
            if (this.degenerated) {
                this.degenerated = false;
                this.type = 3;
                return;
            }

            int var1;
            if (this.error != null) {
                for(var1 = 0; var1 < this.error.length(); ++var1) {
                    this.push(this.error.charAt(var1));
                }

                this.error = null;
                this.type = 9;
                return;
            }

            if (this.relaxed && (this.stackMismatch > 0 || this.peek(0) == -1 && this.depth > 0)) {
                var1 = this.depth - 1 << 2;
                this.type = 3;
                this.namespace = this.elementStack[var1];
                this.prefix = this.elementStack[var1 + 1];
                this.name = this.elementStack[var1 + 2];
                if (this.stackMismatch != 1) {
                    this.error = "missing end tag /" + this.name + " inserted";
                }

                if (this.stackMismatch > 0) {
                    --this.stackMismatch;
                }

                return;
            }

            this.prefix = null;
            this.name = null;
            this.namespace = null;
            this.type = this.peekType();
            switch(this.type) {
                case 1:
                    return;
                case 2:
                    this.parseStartTag(false);
                    return;
                case 3:
                    this.parseEndTag();
                    return;
                case 4:
                    this.pushText(60, !this.token);
                    if (this.depth == 0 && this.isWhitespace) {
                        this.type = 7;
                    }

                    return;
                case 5:
                default:
                    this.type = this.parseLegacy(this.token);
                    if (this.type != 998) {
                        return;
                    }
                    break;
                case 6:
                    this.pushEntity();
                    return;
            }
        }
    }

    private final int parseLegacy(boolean var1) throws IOException, XmlPullParserException {
        String var2 = "";
        int var5 = 0;
        this.read();
        int var6 = this.read();
        byte var3;
        byte var4;
        int var7;
        if (var6 == 63) {
            if ((this.peek(0) == 120 || this.peek(0) == 88) && (this.peek(1) == 109 || this.peek(1) == 77)) {
                if (var1) {
                    this.push(this.peek(0));
                    this.push(this.peek(1));
                }

                this.read();
                this.read();
                if ((this.peek(0) == 108 || this.peek(0) == 76) && this.peek(1) <= 32) {
                    if (this.line != 1 || this.column > 4) {
                        this.error("PI must not start with xml");
                    }

                    this.parseStartTag(true);
                    if (this.attributeCount < 1 || !"version".equals(this.attributes[2])) {
                        this.error("version expected");
                    }

                    this.version = this.attributes[3];
                    var7 = 1;
                    if (var7 < this.attributeCount && "encoding".equals(this.attributes[6])) {
                        this.encoding = this.attributes[7];
                        ++var7;
                    }

                    if (var7 < this.attributeCount && "standalone".equals(this.attributes[4 * var7 + 2])) {
                        String var8 = this.attributes[3 + 4 * var7];
                        if ("yes".equals(var8)) {
                            this.standalone = new Boolean(true);
                        } else if ("no".equals(var8)) {
                            this.standalone = new Boolean(false);
                        } else {
                            this.error("illegal standalone value: " + var8);
                        }

                        ++var7;
                    }

                    if (var7 != this.attributeCount) {
                        this.error("illegal xmldecl");
                    }

                    this.isWhitespace = true;
                    this.txtPos = 0;
                    return 998;
                }
            }

            var3 = 63;
            var4 = 8;
        } else {
            if (var6 != 33) {
                this.error("illegal: <" + var6);
                return 9;
            }

            if (this.peek(0) == 45) {
                var4 = 9;
                var2 = "--";
                var3 = 45;
            } else if (this.peek(0) == 91) {
                var4 = 5;
                var2 = "[CDATA[";
                var3 = 93;
                var1 = true;
            } else {
                var4 = 10;
                var2 = "DOCTYPE";
                var3 = -1;
            }
        }

        for(var7 = 0; var7 < var2.length(); ++var7) {
            this.read(var2.charAt(var7));
        }

        if (var4 == 10) {
            this.parseDoctype(var1);
        } else {
            while(true) {
                var6 = this.read();
                if (var6 == -1) {
                    this.error("Unexpected EOF");
                    return 9;
                }

                if (var1) {
                    this.push(var6);
                }

                if ((var3 == 63 || var6 == var3) && this.peek(0) == var3 && this.peek(1) == 62) {
                    if (var3 == 45 && var5 == 45) {
                        this.error("illegal comment delimiter: --->");
                    }

                    this.read();
                    this.read();
                    if (var1 && var3 != 63) {
                        --this.txtPos;
                    }
                    break;
                }

                var5 = var6;
            }
        }

        return var4;
    }

    private final void parseDoctype(boolean var1) throws IOException, XmlPullParserException {
        int var2 = 1;
        boolean var3 = false;

        while(true) {
            int var4 = this.read();
            switch(var4) {
                case -1:
                    this.error("Unexpected EOF");
                    return;
                case 39:
                    var3 = !var3;
                    break;
                case 60:
                    if (!var3) {
                        ++var2;
                    }
                    break;
                case 62:
                    if (!var3) {
                        --var2;
                        if (var2 == 0) {
                            return;
                        }
                    }
            }

            if (var1) {
                this.push(var4);
            }
        }
    }

    private final void parseEndTag() throws IOException, XmlPullParserException {
        this.read();
        this.read();
        this.name = this.readName();
        this.skip();
        this.read('>');
        int var1 = this.depth - 1 << 2;
        if (this.depth == 0) {
            this.error("element stack empty");
            this.type = 9;
        } else {
            if (!this.name.equals(this.elementStack[var1 + 3])) {
                this.error("expected: /" + this.elementStack[var1 + 3] + " read: " + this.name);

                int var2;
                for(var2 = var1; var2 >= 0 && !this.name.toLowerCase().equals(this.elementStack[var2 + 3].toLowerCase()); var2 -= 4) {
                    ++this.stackMismatch;
                }

                if (var2 < 0) {
                    this.stackMismatch = 0;
                    this.type = 9;
                    return;
                }
            }

            this.namespace = this.elementStack[var1];
            this.prefix = this.elementStack[var1 + 1];
            this.name = this.elementStack[var1 + 2];
        }
    }

    private final int peekType() throws IOException {
        switch(this.peek(0)) {
            case -1:
                return 1;
            case 38:
                return 6;
            case 60:
                switch(this.peek(1)) {
                    case 33:
                    case 63:
                        return 999;
                    case 47:
                        return 3;
                    default:
                        return 2;
                }
            default:
                return 4;
        }
    }

    private final String get(int var1) {
        return new String(this.txtBuf, var1, this.txtPos - var1);
    }

    private final void push(int var1) {
        this.isWhitespace &= var1 <= 32;
        if (this.txtPos == this.txtBuf.length) {
            char[] var2 = new char[this.txtPos * 4 / 3 + 4];
            System.arraycopy(this.txtBuf, 0, var2, 0, this.txtPos);
            this.txtBuf = var2;
        }

        this.txtBuf[this.txtPos++] = (char)var1;
    }

    private final void parseStartTag(boolean var1) throws IOException, XmlPullParserException {
        if (!var1) {
            this.read();
        }

        this.name = this.readName();
        this.attributeCount = 0;

        int var2;
        while(true) {
            this.skip();
            var2 = this.peek(0);
            if (var1) {
                if (var2 == 63) {
                    this.read();
                    this.read('>');
                    return;
                }
            } else {
                if (var2 == 47) {
                    this.degenerated = true;
                    this.read();
                    this.skip();
                    this.read('>');
                    break;
                }

                if (var2 == 62 && !var1) {
                    this.read();
                    break;
                }
            }

            if (var2 == -1) {
                this.error("Unexpected EOF");
                return;
            }

            String var3 = this.readName();
            if (var3.length() == 0) {
                this.error("attr name expected");
                break;
            }

            int var4 = this.attributeCount++ << 2;
            this.attributes = this.ensureCapacity(this.attributes, var4 + 4);
            this.attributes[var4++] = "";
            this.attributes[var4++] = null;
            this.attributes[var4++] = var3;
            this.skip();
            if (this.peek(0) != 61) {
                this.error("Attr.value missing f. " + var3);
                this.attributes[var4] = "1";
            } else {
                this.read('=');
                this.skip();
                int var5 = this.peek(0);
                if (var5 != 39 && var5 != 34) {
                    this.error("attr value delimiter missing!");
                    var5 = 32;
                } else {
                    this.read();
                }

                int var6 = this.txtPos;
                this.pushText(var5, true);
                this.attributes[var4] = this.get(var6);
                this.txtPos = var6;
                if (var5 != 32) {
                    this.read();
                }
            }
        }

        var2 = this.depth++ << 2;
        this.elementStack = this.ensureCapacity(this.elementStack, var2 + 4);
        this.elementStack[var2 + 3] = this.name;
        if (this.depth >= this.nspCounts.length) {
            int[] var7 = new int[this.depth + 4];
            System.arraycopy(this.nspCounts, 0, var7, 0, this.nspCounts.length);
            this.nspCounts = var7;
        }

        this.nspCounts[this.depth] = this.nspCounts[this.depth - 1];
        if (this.processNsp) {
            this.adjustNsp();
        } else {
            this.namespace = "";
        }

        this.elementStack[var2] = this.namespace;
        this.elementStack[var2 + 1] = this.prefix;
        this.elementStack[var2 + 2] = this.name;
    }

    private final void pushEntity() throws IOException, XmlPullParserException {
        this.push(this.read());
        int var1 = this.txtPos;

        while(true) {
            int var2 = this.read();
            if (var2 == 59) {
                String var5 = this.get(var1);
                this.txtPos = var1 - 1;
                if (this.token && this.type == 6) {
                    this.name = var5;
                }

                if (var5.charAt(0) == '#') {
                    int var6 = var5.charAt(1) == 'x' ? Integer.parseInt(var5.substring(2), 16) : Integer.parseInt(var5.substring(1));
                    this.push(var6);
                    return;
                }

                String var3 = (String)this.entityMap.get(var5);
                this.unresolved = var3 == null;
                if (this.unresolved) {
                    if (!this.token) {
                        this.error("unresolved: &" + var5 + ";");
                    }
                } else {
                    for(int var4 = 0; var4 < var3.length(); ++var4) {
                        this.push(var3.charAt(var4));
                    }
                }

                return;
            }

            if (var2 < 128 && (var2 < 48 || var2 > 57) && (var2 < 97 || var2 > 122) && (var2 < 65 || var2 > 90) && var2 != 95 && var2 != 45 && var2 != 35) {
                if (!this.relaxed) {
                    this.error("unterminated entity ref");
                }

                if (var2 != -1) {
                    this.push(var2);
                }

                return;
            }

            this.push(var2);
        }
    }

    private final void pushText(int var1, boolean var2) throws IOException, XmlPullParserException {
        int var3 = this.peek(0);

        for(int var4 = 0; var3 != -1 && var3 != var1 && (var1 != 32 || var3 > 32 && var3 != 62); var3 = this.peek(0)) {
            if (var3 == 38) {
                if (!var2) {
                    break;
                }

                this.pushEntity();
            } else if (var3 == 10 && this.type == 2) {
                this.read();
                this.push(32);
            } else {
                this.push(this.read());
            }

            if (var3 == 62 && var4 >= 2 && var1 != 93) {
                this.error("Illegal: ]]>");
            }

            if (var3 == 93) {
                ++var4;
            } else {
                var4 = 0;
            }
        }

    }

    private final void read(char var1) throws IOException, XmlPullParserException {
        int var2 = this.read();
        if (var2 != var1) {
            this.error("expected: '" + var1 + "' actual: '" + (char)var2 + "'");
        }

    }

    private final int read() throws IOException {
        int var1;
        if (this.peekCount == 0) {
            var1 = this.peek(0);
        } else {
            var1 = this.peek[0];
            this.peek[0] = this.peek[1];
        }

        --this.peekCount;
        ++this.column;
        if (var1 == 10) {
            ++this.line;
            this.column = 1;
        }

        return var1;
    }

    private final int peek(int var1) throws IOException {
        while(var1 >= this.peekCount) {
            int var2;
            if (this.srcBuf.length <= 1) {
                var2 = this.reader.read();
            } else if (this.srcPos < this.srcCount) {
                var2 = this.srcBuf[this.srcPos++];
            } else {
                this.srcCount = this.reader.read(this.srcBuf, 0, this.srcBuf.length);
                if (this.srcCount <= 0) {
                    var2 = -1;
                } else {
                    var2 = this.srcBuf[0];
                }

                this.srcPos = 1;
            }

            if (var2 == 13) {
                this.wasCR = true;
                this.peek[this.peekCount++] = 10;
            } else {
                if (var2 == 10) {
                    if (!this.wasCR) {
                        this.peek[this.peekCount++] = 10;
                    }
                } else {
                    this.peek[this.peekCount++] = var2;
                }

                this.wasCR = false;
            }
        }

        return this.peek[var1];
    }

    private final String readName() throws IOException, XmlPullParserException {
        int var1 = this.txtPos;
        int var2 = this.peek(0);
        if ((var2 < 97 || var2 > 122) && (var2 < 65 || var2 > 90) && var2 != 95 && var2 != 58 && var2 < 192 && !this.relaxed) {
            this.error("name expected");
        }

        do {
            do {
                do {
                    do {
                        this.push(this.read());
                        var2 = this.peek(0);
                    } while(var2 >= 97 && var2 <= 122);
                } while(var2 >= 65 && var2 <= 90);
            } while(var2 >= 48 && var2 <= 57);
        } while(var2 == 95 || var2 == 45 || var2 == 58 || var2 == 46 || var2 >= 183);

        String var3 = this.get(var1);
        this.txtPos = var1;
        return var3;
    }

    private final void skip() throws IOException {
        while(true) {
            int var1 = this.peek(0);
            if (var1 > 32 || var1 == -1) {
                return;
            }

            this.read();
        }
    }

    public void setInput(Reader var1) throws XmlPullParserException {
        this.reader = var1;
        this.line = 1;
        this.column = 0;
        this.type = 0;
        this.name = null;
        this.namespace = null;
        this.degenerated = false;
        this.attributeCount = -1;
        this.encoding = null;
        this.version = null;
        this.standalone = null;
        if (var1 != null) {
            this.srcPos = 0;
            this.srcCount = 0;
            this.peekCount = 0;
            this.depth = 0;
            this.entityMap = new Hashtable();
            this.entityMap.put("amp", "&");
            this.entityMap.put("apos", "'");
            this.entityMap.put("gt", ">");
            this.entityMap.put("lt", "<");
            this.entityMap.put("quot", "\"");
        }
    }

    public void setInput(InputStream var1, String var2) throws XmlPullParserException {
        this.srcPos = 0;
        this.srcCount = 0;
        String var3 = var2;
        if (var1 == null) {
            throw new IllegalArgumentException();
        } else {
            try {
                int var4;
                if (var3 == null) {
                    int var5;
                    for(var4 = 0; this.srcCount < 4; this.srcBuf[this.srcCount++] = (char)var5) {
                        var5 = var1.read();
                        if (var5 == -1) {
                            break;
                        }

                        var4 = var4 << 8 | var5;
                    }

                    if (this.srcCount == 4) {
                        label113: {
                            switch(var4) {
                                case -131072:
                                    var3 = "UTF-32LE";
                                    this.srcCount = 0;
                                    break label113;
                                case 60:
                                    var3 = "UTF-32BE";
                                    this.srcBuf[0] = '<';
                                    this.srcCount = 1;
                                    break label113;
                                case 65279:
                                    var3 = "UTF-32BE";
                                    this.srcCount = 0;
                                    break label113;
                                case 3932223:
                                    var3 = "UTF-16BE";
                                    this.srcBuf[0] = '<';
                                    this.srcBuf[1] = '?';
                                    this.srcCount = 2;
                                    break label113;
                                case 1006632960:
                                    var3 = "UTF-32LE";
                                    this.srcBuf[0] = '<';
                                    this.srcCount = 1;
                                    break label113;
                                case 1006649088:
                                    var3 = "UTF-16LE";
                                    this.srcBuf[0] = '<';
                                    this.srcBuf[1] = '?';
                                    this.srcCount = 2;
                                    break label113;
                                case 1010792557:
                                    while(true) {
                                        var5 = var1.read();
                                        if (var5 == -1) {
                                            break;
                                        }

                                        this.srcBuf[this.srcCount++] = (char)var5;
                                        if (var5 == 62) {
                                            String var6 = new String(this.srcBuf, 0, this.srcCount);
                                            int var7 = var6.indexOf("encoding");
                                            if (var7 != -1) {
                                                while(var6.charAt(var7) != '"' && var6.charAt(var7) != '\'') {
                                                    ++var7;
                                                }

                                                char var8 = var6.charAt(var7++);
                                                int var9 = var6.indexOf(var8, var7);
                                                var3 = var6.substring(var7, var9);
                                            }
                                            break;
                                        }
                                    }
                            }

                            if ((var4 & -65536) == -16842752) {
                                var3 = "UTF-16BE";
                                this.srcBuf[0] = (char)(this.srcBuf[2] << 8 | this.srcBuf[3]);
                                this.srcCount = 1;
                            } else if ((var4 & -65536) == -131072) {
                                var3 = "UTF-16LE";
                                this.srcBuf[0] = (char)(this.srcBuf[3] << 8 | this.srcBuf[2]);
                                this.srcCount = 1;
                            } else if ((var4 & -256) == -272908544) {
                                var3 = "UTF-8";
                                this.srcBuf[0] = this.srcBuf[3];
                                this.srcCount = 1;
                            }
                        }
                    }
                }

                if (var3 == null) {
                    var3 = "UTF-8";
                }

                var4 = this.srcCount;
                this.setInput(new InputStreamReader(var1, var3));
                this.encoding = var2;
                this.srcCount = var4;
            } catch (Exception var10) {
                throw new XmlPullParserException("Invalid stream or encoding: " + var10.toString(), this, var10);
            }
        }
    }

    public boolean getFeature(String var1) {
        if ("http://xmlpull.org/v1/doc/features.html#process-namespaces".equals(var1)) {
            return this.processNsp;
        } else {
            return this.isProp(var1, false, "relaxed") ? this.relaxed : false;
        }
    }

    public String getInputEncoding() {
        return this.encoding;
    }

    public void defineEntityReplacementText(String var1, String var2) throws XmlPullParserException {
        if (this.entityMap == null) {
            throw new RuntimeException("entity replacement text must be defined after setInput!");
        } else {
            this.entityMap.put(var1, var2);
        }
    }

    public Object getProperty(String var1) {
        if (this.isProp(var1, true, "xmldecl-version")) {
            return this.version;
        } else if (this.isProp(var1, true, "xmldecl-standalone")) {
            return this.standalone;
        } else if (this.isProp(var1, true, "location")) {
            return this.location != null ? this.location : this.reader.toString();
        } else {
            return null;
        }
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

        var1.append("@" + this.line + ":" + this.column);
        if (this.location != null) {
            var1.append(" in ");
            var1.append(this.location);
        } else if (this.reader != null) {
            var1.append(" in ");
            var1.append(this.reader.toString());
        }

        return var1.toString();
    }

    public int getLineNumber() {
        return this.line;
    }

    public int getColumnNumber() {
        return this.column;
    }

    public boolean isWhitespace() throws XmlPullParserException {
        if (this.type != 4 && this.type != 7 && this.type != 5) {
            this.exception("Wrong event type");
        }

        return this.isWhitespace;
    }

    public String getText() {
        return this.type >= 4 && (this.type != 6 || !this.unresolved) ? this.get(0) : null;
    }

    public char[] getTextCharacters(int[] var1) {
        if (this.type >= 4) {
            if (this.type == 6) {
                var1[0] = 0;
                var1[1] = this.name.length();
                return this.name.toCharArray();
            } else {
                var1[0] = 0;
                var1[1] = this.txtPos;
                return this.txtBuf;
            }
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
        this.txtPos = 0;
        this.isWhitespace = true;
        int var1 = 9999;
        this.token = false;

        do {
            do {
                this.nextImpl();
                if (this.type < var1) {
                    var1 = this.type;
                }
            } while(var1 > 6);
        } while(var1 >= 4 && this.peekType() >= 4);

        this.type = var1;
        if (this.type > 4) {
            this.type = 4;
        }

        return this.type;
    }

    public int nextToken() throws XmlPullParserException, IOException {
        this.isWhitespace = true;
        this.txtPos = 0;
        this.token = true;
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

    public void require(int var1, String var2, String var3) throws XmlPullParserException, IOException {
        if (var1 != this.type || var2 != null && !var2.equals(this.getNamespace()) || var3 != null && !var3.equals(this.getName())) {
            this.exception("expected: " + XmlPullParser.TYPES[var1] + " {" + var2 + "}" + var3);
        }

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

    public void setFeature(String var1, boolean var2) throws XmlPullParserException {
        if ("http://xmlpull.org/v1/doc/features.html#process-namespaces".equals(var1)) {
            this.processNsp = var2;
        } else if (this.isProp(var1, false, "relaxed")) {
            this.relaxed = var2;
        } else {
            this.exception("unsupported feature: " + var1);
        }

    }

    public void setProperty(String var1, Object var2) throws XmlPullParserException {
        if (this.isProp(var1, true, "location")) {
            this.location = var2;
        } else {
            throw new XmlPullParserException("unsupported property: " + var1);
        }
    }

    public void skipSubTree() throws XmlPullParserException, IOException {
        this.require(2, (String)null, (String)null);
        int var1 = 1;

        while(var1 > 0) {
            int var2 = this.next();
            if (var2 == 3) {
                --var1;
            } else if (var2 == 2) {
                ++var1;
            }
        }

    }
}
