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

public class Element extends Node {
    protected String namespace;
    protected String name;
    protected Vector attributes;
    protected Node parent;
    protected Vector prefixes;

    public Element() {
    }

    public void init() {
    }

    public void clear() {
        this.attributes = null;
        this.children = null;
    }

    public Element createElement(String var1, String var2) {
        return this.parent == null ? super.createElement(var1, var2) : this.parent.createElement(var1, var2);
    }

    public int getAttributeCount() {
        return this.attributes == null ? 0 : this.attributes.size();
    }

    public String getAttributeNamespace(int var1) {
        return ((String[])((String[])this.attributes.elementAt(var1)))[0];
    }

    public String getAttributeName(int var1) {
        return ((String[])((String[])this.attributes.elementAt(var1)))[1];
    }

    public String getAttributeValue(int var1) {
        return ((String[])((String[])this.attributes.elementAt(var1)))[2];
    }

    public String getAttributeValue(String var1, String var2) {
        for(int var3 = 0; var3 < this.getAttributeCount(); ++var3) {
            if (var2.equals(this.getAttributeName(var3)) && (var1 == null || var1.equals(this.getAttributeNamespace(var3)))) {
                return this.getAttributeValue(var3);
            }
        }

        return null;
    }

    public Node getRoot() {
        Element var1;
        for(var1 = this; var1.parent != null; var1 = (Element)var1.parent) {
            if (!(var1.parent instanceof Element)) {
                return var1.parent;
            }
        }

        return var1;
    }

    public String getName() {
        return this.name;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getNamespaceUri(String var1) {
        int var2 = this.getNamespaceCount();

        for(int var3 = 0; var3 < var2; ++var3) {
            if (var1 == this.getNamespacePrefix(var3) || var1 != null && var1.equals(this.getNamespacePrefix(var3))) {
                return this.getNamespaceUri(var3);
            }
        }

        return this.parent instanceof Element ? ((Element)this.parent).getNamespaceUri(var1) : null;
    }

    public int getNamespaceCount() {
        return this.prefixes == null ? 0 : this.prefixes.size();
    }

    public String getNamespacePrefix(int var1) {
        return ((String[])((String[])this.prefixes.elementAt(var1)))[0];
    }

    public String getNamespaceUri(int var1) {
        return ((String[])((String[])this.prefixes.elementAt(var1)))[1];
    }

    public Node getParent() {
        return this.parent;
    }

    public void parse(XmlPullParser var1) throws IOException, XmlPullParserException {
        int var2;
        for(var2 = var1.getNamespaceCount(var1.getDepth() - 1); var2 < var1.getNamespaceCount(var1.getDepth()); ++var2) {
            this.setPrefix(var1.getNamespacePrefix(var2), var1.getNamespaceUri(var2));
        }

        for(var2 = 0; var2 < var1.getAttributeCount(); ++var2) {
            this.setAttribute(var1.getAttributeNamespace(var2), var1.getAttributeName(var2), var1.getAttributeValue(var2));
        }

        this.init();
        if (var1.isEmptyElementTag()) {
            var1.nextToken();
        } else {
            var1.nextToken();
            super.parse(var1);
            if (this.getChildCount() == 0) {
                this.addChild(7, "");
            }
        }

        var1.require(3, this.getNamespace(), this.getName());
        var1.nextToken();
    }

    public void setAttribute(String var1, String var2, String var3) {
        if (this.attributes == null) {
            this.attributes = new Vector();
        }

        if (var1 == null) {
            var1 = "";
        }

        for(int var4 = this.attributes.size() - 1; var4 >= 0; --var4) {
            String[] var5 = (String[])((String[])this.attributes.elementAt(var4));
            if (var5[0].equals(var1) && var5[1].equals(var2)) {
                if (var3 == null) {
                    this.attributes.removeElementAt(var4);
                } else {
                    var5[2] = var3;
                }

                return;
            }
        }

        this.attributes.addElement(new String[]{var1, var2, var3});
    }

    public void setPrefix(String var1, String var2) {
        if (this.prefixes == null) {
            this.prefixes = new Vector();
        }

        this.prefixes.addElement(new String[]{var1, var2});
    }

    public void setName(String var1) {
        this.name = var1;
    }

    public void setNamespace(String var1) {
        if (var1 == null) {
            throw new NullPointerException("Use \"\" for empty namespace");
        } else {
            this.namespace = var1;
        }
    }

    protected void setParent(Node var1) {
        this.parent = var1;
    }

    public void write(XmlSerializer var1) throws IOException {
        int var2;
        if (this.prefixes != null) {
            for(var2 = 0; var2 < this.prefixes.size(); ++var2) {
                var1.setPrefix(this.getNamespacePrefix(var2), this.getNamespaceUri(var2));
            }
        }

        var1.startTag(this.getNamespace(), this.getName());
        var2 = this.getAttributeCount();

        for(int var3 = 0; var3 < var2; ++var3) {
            var1.attribute(this.getAttributeNamespace(var3), this.getAttributeName(var3), this.getAttributeValue(var3));
        }

        this.writeChildren(var1);
        var1.endTag(this.getNamespace(), this.getName());
    }
}
