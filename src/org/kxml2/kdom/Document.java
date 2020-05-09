//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.kxml2.kdom;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class Document extends Node {
    protected int rootIndex = -1;
    String encoding;
    Boolean standalone;

    public Document() {
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(String var1) {
        this.encoding = var1;
    }

    public void setStandalone(Boolean var1) {
        this.standalone = var1;
    }

    public Boolean getStandalone() {
        return this.standalone;
    }

    public String getName() {
        return "#document";
    }

    public void addChild(int var1, int var2, Object var3) {
        if (var2 == 2) {
            this.rootIndex = var1;
        } else if (this.rootIndex >= var1) {
            ++this.rootIndex;
        }

        super.addChild(var1, var2, var3);
    }

    public void parse(XmlPullParser var1) throws IOException, XmlPullParserException {
        var1.require(0, (String)null, (String)null);
        var1.nextToken();
        this.encoding = var1.getInputEncoding();
        this.standalone = (Boolean)var1.getProperty("http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone");
        super.parse(var1);
        if (var1.getEventType() != 1) {
            throw new RuntimeException("Document end expected!");
        }
    }

    public void removeChild(int var1) {
        if (var1 == this.rootIndex) {
            this.rootIndex = -1;
        } else if (var1 < this.rootIndex) {
            --this.rootIndex;
        }

        super.removeChild(var1);
    }

    public Element getRootElement() {
        if (this.rootIndex == -1) {
            throw new RuntimeException("Document has no root element!");
        } else {
            return (Element)this.getChild(this.rootIndex);
        }
    }

    public void write(XmlSerializer var1) throws IOException {
        var1.startDocument(this.encoding, this.standalone);
        this.writeChildren(var1);
        var1.endDocument();
    }
}
