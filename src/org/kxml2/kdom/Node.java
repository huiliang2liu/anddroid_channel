//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.kxml2.kdom;

import java.io.IOException;
import java.util.Vector;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class Node {
    public static final int DOCUMENT = 0;
    public static final int ELEMENT = 2;
    public static final int TEXT = 4;
    public static final int CDSECT = 5;
    public static final int ENTITY_REF = 6;
    public static final int IGNORABLE_WHITESPACE = 7;
    public static final int PROCESSING_INSTRUCTION = 8;
    public static final int COMMENT = 9;
    public static final int DOCDECL = 10;
    protected Vector children;
    protected StringBuffer types;

    public Node() {
    }

    public void addChild(int var1, int var2, Object var3) {
        if (var3 == null) {
            throw new NullPointerException();
        } else {
            if (this.children == null) {
                this.children = new Vector();
                this.types = new StringBuffer();
            }

            if (var2 == 2) {
                if (!(var3 instanceof Element)) {
                    throw new RuntimeException("Element obj expected)");
                }

                ((Element)var3).setParent(this);
            } else if (!(var3 instanceof String)) {
                throw new RuntimeException("String expected");
            }

            this.children.insertElementAt(var3, var1);
            this.types.insert(var1, (char)var2);
        }
    }

    public void addChild(int var1, Object var2) {
        this.addChild(this.getChildCount(), var1, var2);
    }

    public Element createElement(String var1, String var2) {
        Element var3 = new Element();
        var3.namespace = var1 == null ? "" : var1;
        var3.name = var2;
        return var3;
    }

    public Object getChild(int var1) {
        return this.children.elementAt(var1);
    }

    public int getChildCount() {
        return this.children == null ? 0 : this.children.size();
    }

    public Element getElement(int var1) {
        Object var2 = this.getChild(var1);
        return var2 instanceof Element ? (Element)var2 : null;
    }

    public Element getElement(String var1, String var2) {
        int var3 = this.indexOf(var1, var2, 0);
        int var4 = this.indexOf(var1, var2, var3 + 1);
        if (var3 != -1 && var4 == -1) {
            return this.getElement(var3);
        } else {
            throw new RuntimeException("Element {" + var1 + "}" + var2 + (var3 == -1 ? " not found in " : " more than once in ") + this);
        }
    }

    public String getText(int var1) {
        return this.isText(var1) ? (String)this.getChild(var1) : null;
    }

    public int getType(int var1) {
        return this.types.charAt(var1);
    }

    public int indexOf(String var1, String var2, int var3) {
        int var4 = this.getChildCount();

        for(int var5 = var3; var5 < var4; ++var5) {
            Element var6 = this.getElement(var5);
            if (var6 != null && var2.equals(var6.getName()) && (var1 == null || var1.equals(var6.getNamespace()))) {
                return var5;
            }
        }

        return -1;
    }

    public boolean isText(int var1) {
        int var2 = this.getType(var1);
        return var2 == 4 || var2 == 7 || var2 == 5;
    }

    public void parse(XmlPullParser var1) throws IOException, XmlPullParserException {
        boolean var2 = false;

        do {
            int var3 = var1.getEventType();
            switch(var3) {
                case 1:
                case 3:
                    var2 = true;
                    break;
                case 2:
                    Element var4 = this.createElement(var1.getNamespace(), var1.getName());
                    this.addChild(2, var4);
                    var4.parse(var1);
                    break;
                default:
                    if (var1.getText() != null) {
                        this.addChild(var3 == 6 ? 4 : var3, var1.getText());
                    } else if (var3 == 6 && var1.getName() != null) {
                        this.addChild(6, var1.getName());
                    }

                    var1.nextToken();
            }
        } while(!var2);

    }

    public void removeChild(int var1) {
        this.children.removeElementAt(var1);
        int var2 = this.types.length() - 1;

        for(int var3 = var1; var3 < var2; ++var3) {
            this.types.setCharAt(var3, this.types.charAt(var3 + 1));
        }

        this.types.setLength(var2);
    }

    public void write(XmlSerializer var1) throws IOException {
        this.writeChildren(var1);
        var1.flush();
    }

    public void writeChildren(XmlSerializer var1) throws IOException {
        if (this.children != null) {
            int var2 = this.children.size();

            for(int var3 = 0; var3 < var2; ++var3) {
                int var4 = this.getType(var3);
                Object var5 = this.children.elementAt(var3);
                switch(var4) {
                    case 2:
                        ((Element)var5).write(var1);
                        break;
                    case 3:
                    default:
                        throw new RuntimeException("Illegal type: " + var4);
                    case 4:
                        var1.text((String)var5);
                        break;
                    case 5:
                        var1.cdsect((String)var5);
                        break;
                    case 6:
                        var1.entityRef((String)var5);
                        break;
                    case 7:
                        var1.ignorableWhitespace((String)var5);
                        break;
                    case 8:
                        var1.processingInstruction((String)var5);
                        break;
                    case 9:
                        var1.comment((String)var5);
                        break;
                    case 10:
                        var1.docdecl((String)var5);
                }
            }

        }
    }
}
