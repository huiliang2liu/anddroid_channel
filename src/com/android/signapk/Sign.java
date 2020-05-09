package com.android.signapk;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.security.DigestOutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.encoders.Base64;

public class Sign {
    private static final String CERT_SF_NAME = "META-INF/CERT.SF";
    private static final String CERT_SIG_NAME = "META-INF/CERT.%s";
    private static final String CERT_SF_MULTI_NAME = "META-INF/CERT%d.SF";
    private static final String CERT_SIG_MULTI_NAME = "META-INF/CERT%d.%s";
    private static final String OTACERT_NAME = "META-INF/com/android/otacert";
    private static Provider sBouncyCastleProvider;
    private static final int USE_SHA1 = 1;
    private static final int USE_SHA256 = 2;
    private static Pattern stripPattern = Pattern.compile("^(META-INF/((.*)[.](SF|RSA|DSA|EC)|com/android/otacert))|(" + Pattern.quote("META-INF/MANIFEST.MF") + ")$");


    public static int getDigestAlgorithm(X509Certificate var0) {
        String var1 = var0.getSigAlgName().toUpperCase(Locale.US);
        if (!"SHA1WITHRSA".equals(var1) && !"MD5WITHRSA".equals(var1)) {
            if (var1.startsWith("SHA256WITH")) {
                return 2;
            } else {
                throw new IllegalArgumentException("unsupported signature algorithm \"" + var1 + "\" in cert [" + var0.getSubjectDN());
            }
        } else {
            return 1;
        }
    }

    private static String getSignatureAlgorithm(X509Certificate var0) {
        String var1 = var0.getSigAlgName().toUpperCase(Locale.US);
        String var2 = var0.getPublicKey().getAlgorithm().toUpperCase(Locale.US);
        if ("RSA".equalsIgnoreCase(var2)) {
            return getDigestAlgorithm(var0) == 2 ? "SHA256withRSA" : "SHA1withRSA";
        } else if ("EC".equalsIgnoreCase(var2)) {
            return "SHA256withECDSA";
        } else {
            throw new IllegalArgumentException("unsupported key type: " + var2);
        }
    }

    public static X509Certificate readPublicKey(File var0) throws IOException, GeneralSecurityException {
        FileInputStream var1 = new FileInputStream(var0);

        X509Certificate var3;
        try {
            CertificateFactory var2 = CertificateFactory.getInstance("X.509");
            var3 = (X509Certificate)var2.generateCertificate(var1);
        } finally {
            var1.close();
        }

        return var3;
    }

    private static String readPassword(File var0) {
        System.out.print("Enter password for " + var0 + " (password will not be hidden): ");
        System.out.flush();
        BufferedReader var1 = new BufferedReader(new InputStreamReader(System.in));

        try {
            return var1.readLine();
        } catch (IOException var3) {
            return null;
        }
    }

    private static PKCS8EncodedKeySpec decryptPrivateKey(byte[] var0, File var1) throws GeneralSecurityException {
        EncryptedPrivateKeyInfo var2;
        try {
            var2 = new EncryptedPrivateKeyInfo(var0);
        } catch (IOException var9) {
            return null;
        }

        char[] var3 = readPassword(var1).toCharArray();
        SecretKeyFactory var4 = SecretKeyFactory.getInstance(var2.getAlgName());
        SecretKey var5 = var4.generateSecret(new PBEKeySpec(var3));
        Cipher var6 = Cipher.getInstance(var2.getAlgName());
        var6.init(2, var5, var2.getAlgParameters());

        try {
            return var2.getKeySpec(var6);
        } catch (InvalidKeySpecException var8) {
            System.err.println("signapk: Password for " + var1 + " may be bad.");
            throw var8;
        }
    }

    public static PrivateKey readPrivateKey(File var0) throws IOException, GeneralSecurityException {
        DataInputStream var1 = new DataInputStream(new FileInputStream(var0));

        PrivateKey var7;
        try {
            byte[] var2 = new byte[(int)var0.length()];
            var1.read(var2);
            PKCS8EncodedKeySpec var3 = decryptPrivateKey(var2, var0);
            if (var3 == null) {
                var3 = new PKCS8EncodedKeySpec(var2);
            }

            ASN1InputStream var4 = new ASN1InputStream(new ByteArrayInputStream(var3.getEncoded()));
            PrivateKeyInfo var5 = PrivateKeyInfo.getInstance(var4.readObject());
            String var6 = var5.getPrivateKeyAlgorithm().getAlgorithm().getId();
            var7 = KeyFactory.getInstance(var6).generatePrivate(var3);
        } finally {
            var1.close();
        }

        return var7;
    }

    public static Manifest addDigestsToManifest(JarFile var0, int var1) throws IOException, GeneralSecurityException {
        Manifest var2 = var0.getManifest();
        Manifest var3 = new Manifest();
        Attributes var4 = var3.getMainAttributes();
        if (var2 != null) {
            var4.putAll(var2.getMainAttributes());
        } else {
            var4.putValue("Manifest-Version", "1.0");
            var4.putValue("Created-By", "1.0 (Android SignApk)");
        }

        MessageDigest var5 = null;
        MessageDigest var6 = null;
        if ((var1 & 1) != 0) {
            var5 = MessageDigest.getInstance("SHA1");
        }

        if ((var1 & 2) != 0) {
            var6 = MessageDigest.getInstance("SHA256");
        }

        byte[] var7 = new byte[4096];
        TreeMap var9 = new TreeMap();
        Enumeration var10 = var0.entries();

        JarEntry var11;
        while(var10.hasMoreElements()) {
            var11 = (JarEntry)var10.nextElement();
            var9.put(var11.getName(), var11);
        }

        Iterator var15 = var9.values().iterator();

        while(true) {
            String var12;
            do {
                do {
                    if (!var15.hasNext()) {
                        return var3;
                    }

                    var11 = (JarEntry)var15.next();
                    var12 = var11.getName();
                } while(var11.isDirectory());
            } while(stripPattern != null && stripPattern.matcher(var12).matches());

            InputStream var13 = var0.getInputStream(var11);

            int var8;
            while((var8 = var13.read(var7)) > 0) {
                if (var5 != null) {
                    var5.update(var7, 0, var8);
                }

                if (var6 != null) {
                    var6.update(var7, 0, var8);
                }
            }

            Attributes var14 = null;
            if (var2 != null) {
                var14 = var2.getAttributes(var12);
            }

            var14 = var14 != null ? new Attributes(var14) : new Attributes();
            if (var5 != null) {
                var14.putValue("SHA1-Digest", new String(Base64.encode(var5.digest()), "ASCII"));
            }

            if (var6 != null) {
                var14.putValue("SHA-256-Digest", new String(Base64.encode(var6.digest()), "ASCII"));
            }

            var3.getEntries().put(var12, var14);
        }
    }

    private static void addOtacert(JarOutputStream var0, File var1, long var2, Manifest var4, int var5) throws IOException, GeneralSecurityException {
        MessageDigest var6 = MessageDigest.getInstance(var5 == 1 ? "SHA1" : "SHA256");
        JarEntry var7 = new JarEntry("META-INF/com/android/otacert");
        var7.setTime(var2);
        var0.putNextEntry(var7);
        FileInputStream var8 = new FileInputStream(var1);
        byte[] var9 = new byte[4096];

        int var10;
        while((var10 = var8.read(var9)) != -1) {
            var0.write(var9, 0, var10);
            var6.update(var9, 0, var10);
        }

        var8.close();
        Attributes var11 = new Attributes();
        var11.putValue(var5 == 1 ? "SHA1-Digest" : "SHA-256-Digest", new String(Base64.encode(var6.digest()), "ASCII"));
        var4.getEntries().put("META-INF/com/android/otacert", var11);
    }

    private static void writeSignatureFile(Manifest var0, OutputStream var1, int var2) throws IOException, GeneralSecurityException {
        Manifest var3 = new Manifest();
        Attributes var4 = var3.getMainAttributes();
        var4.putValue("Signature-Version", "1.0");
        var4.putValue("Created-By", "1.0 (Android SignApk)");
        MessageDigest var5 = MessageDigest.getInstance(var2 == 2 ? "SHA256" : "SHA1");
        PrintStream var6 = new PrintStream(new DigestOutputStream(new ByteArrayOutputStream(), var5), true, "UTF-8");
        var0.write(var6);
        var6.flush();
        var4.putValue(var2 == 2 ? "SHA-256-Digest-Manifest" : "SHA1-Digest-Manifest", new String(Base64.encode(var5.digest()), "ASCII"));
        Map var7 = var0.getEntries();
        Iterator var8 = var7.entrySet().iterator();

        while(var8.hasNext()) {
            Map.Entry<String,Attributes> var9 = (Map.Entry)var8.next();
            var6.print("Name: " + (String)var9.getKey() + "\r\n");
            Iterator var10 = ((Attributes)var9.getValue()).entrySet().iterator();

            while(var10.hasNext()) {
                Map.Entry var11 = (Map.Entry)var10.next();
                var6.print(var11.getKey() + ": " + var11.getValue() + "\r\n");
            }

            var6.print("\r\n");
            var6.flush();
            Attributes var12 = new Attributes();
            var12.putValue(var2 == 2 ? "SHA-256-Digest" : "SHA1-Digest-Manifest", new String(Base64.encode(var5.digest()), "ASCII"));
            var3.getEntries().put(var9.getKey(), var12);
        }

        Sign.CountOutputStream var13 = new Sign.CountOutputStream(var1);
        var3.write(var13);
        if (var13.size() % 1024 == 0) {
            var13.write(13);
            var13.write(10);
        }

    }

    private static void writeSignatureBlock(CMSTypedData var0, X509Certificate var1, PrivateKey var2, OutputStream var3) throws IOException, CertificateEncodingException, OperatorCreationException, CMSException {
        ArrayList var4 = new ArrayList(1);
        var4.add(var1);
        JcaCertStore var5 = new JcaCertStore(var4);
        CMSSignedDataGenerator var6 = new CMSSignedDataGenerator();
        ContentSigner var7 = (new JcaContentSignerBuilder(getSignatureAlgorithm(var1))).setProvider(sBouncyCastleProvider).build(var2);
        var6.addSignerInfoGenerator((new JcaSignerInfoGeneratorBuilder((new JcaDigestCalculatorProviderBuilder()).setProvider(sBouncyCastleProvider).build())).setDirectSignature(true).build(var7, var1));
        var6.addCertificates(var5);
        CMSSignedData var8 = var6.generate(var0, false);
        ASN1InputStream var9 = new ASN1InputStream(var8.getEncoded());
        DEROutputStream var10 = new DEROutputStream(var3);
        var10.writeObject(var9.readObject());
    }

    private static void copyFiles(Manifest var0, JarFile var1, JarOutputStream var2, long var3) throws IOException {
        byte[] var5 = new byte[4096];
        Map var7 = var0.getEntries();
        ArrayList var8 = new ArrayList(var7.keySet());
        Collections.sort(var8);
        Iterator var9 = var8.iterator();

        while(var9.hasNext()) {
            String var10 = (String)var9.next();
            JarEntry var11 = var1.getJarEntry(var10);
            JarEntry var12 = null;
            if (var11.getMethod() == 0) {
                var12 = new JarEntry(var11);
            } else {
                var12 = new JarEntry(var10);
            }

            var12.setTime(var3);
            var2.putNextEntry(var12);
            InputStream var13 = var1.getInputStream(var11);

            int var6;
            while((var6 = var13.read(var5)) > 0) {
                var2.write(var5, 0, var6);
            }

            var2.flush();
        }

    }

    private static void signWholeFile(JarFile var0, File var1, X509Certificate var2, PrivateKey var3, OutputStream var4) throws Exception {
        Sign.CMSSigner var5 = new Sign.CMSSigner(var0, var1, var2, var3, var4);
        ByteArrayOutputStream var6 = new ByteArrayOutputStream();
        byte[] var7 = "signed by SignApk".getBytes("UTF-8");
        var6.write(var7);
        var6.write(0);
        var5.writeSignatureBlock(var6);
        byte[] var8 = var5.getSigner().getTail();
        if (var8[var8.length - 22] == 80 && var8[var8.length - 21] == 75 && var8[var8.length - 20] == 5 && var8[var8.length - 19] == 6) {
            int var9 = var6.size() + 6;
            if (var9 > 65535) {
                throw new IllegalArgumentException("signature is too big for ZIP file comment");
            } else {
                int var10 = var9 - var7.length - 1;
                var6.write(var10 & 255);
                var6.write(var10 >> 8 & 255);
                var6.write(255);
                var6.write(255);
                var6.write(var9 & 255);
                var6.write(var9 >> 8 & 255);
                var6.flush();
                byte[] var11 = var6.toByteArray();

                for(int var12 = 0; var12 < var11.length - 3; ++var12) {
                    if (var11[var12] == 80 && var11[var12 + 1] == 75 && var11[var12 + 2] == 5 && var11[var12 + 3] == 6) {
                        throw new IllegalArgumentException("found spurious EOCD header at " + var12);
                    }
                }

                var4.write(var9 & 255);
                var4.write(var9 >> 8 & 255);
                var6.writeTo(var4);
            }
        } else {
            throw new IllegalArgumentException("zip data already has an archive comment");
        }
    }

    private static void signFile(Manifest var0, JarFile var1, X509Certificate[] var2, PrivateKey[] var3, JarOutputStream var4) throws Exception {
        long var5 = var2[0].getNotBefore().getTime() + 3600000L;
        JarEntry var7 = new JarEntry("META-INF/MANIFEST.MF");
        var7.setTime(var5);
        var4.putNextEntry(var7);
        var0.write(var4);
        int var8 = var2.length;
        for(int var9 = 0; var9 < var8; ++var9) {
            var7 = new JarEntry(var8 == 1 ? "META-INF/CERT.SF" : String.format("META-INF/CERT%d.SF", var9));
            var7.setTime(var5);
            var4.putNextEntry(var7);
            ByteArrayOutputStream var10 = new ByteArrayOutputStream();
            writeSignatureFile(var0, var10, getDigestAlgorithm(var2[var9]));
            byte[] var11 = var10.toByteArray();
            var4.write(var11);
            String var12 = var2[var9].getPublicKey().getAlgorithm();
            var7 = new JarEntry(var8 == 1 ? String.format("META-INF/CERT.%s", var12) : String.format("META-INF/CERT%d.%s", var9, var12));
            var7.setTime(var5);
            var4.putNextEntry(var7);
            writeSignatureBlock(new CMSProcessableByteArray(var11), var2[var9], var3[var9], var4);
        }

    }
    public static void signFile(Manifest var0, ZipOutputStream zos, X509Certificate var2, PrivateKey var3) throws Exception {
        long var5 = var2.getNotBefore().getTime() + 3600000L;
        ZipEntry var7 = new ZipEntry("META-INF/MANIFEST.MF");
        var7.setTime(var5);
        zos.putNextEntry(var7);
        var0.write(zos);
        zos.closeEntry();
        var7 = new ZipEntry("META-INF/CERT.SF");
        var7.setTime(var5);
        zos.putNextEntry(var7);
        ByteArrayOutputStream var10 = new ByteArrayOutputStream();
        writeSignatureFile(var0, var10, getDigestAlgorithm(var2));
        byte[] var11 = var10.toByteArray();
        zos.write(var11);
        zos.closeEntry();
        String var12 = var2.getPublicKey().getAlgorithm();
        var7 = new ZipEntry(String.format("META-INF/CERT.%s", var12));
        var7.setTime(var5);
        zos.putNextEntry(var7);
        writeSignatureBlock(new CMSProcessableByteArray(var11), var2, var3, zos);
        zos.closeEntry();

    }

    private static void loadProviderIfNecessary(String var0) {
        if (var0 != null) {
            Class var1;
            try {
                ClassLoader var2 = ClassLoader.getSystemClassLoader();
                if (var2 != null) {
                    var1 = var2.loadClass(var0);
                } else {
                    var1 = Class.forName(var0);
                }
            } catch (ClassNotFoundException var8) {
                var8.printStackTrace();
                System.exit(1);
                return;
            }

            Constructor var9 = null;
            Constructor[] var3 = var1.getConstructors();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Constructor var6 = var3[var5];
                if (var6.getParameterTypes().length == 0) {
                    var9 = var6;
                    break;
                }
            }

            if (var9 == null) {
                System.err.println("No zero-arg constructor found for " + var0);
                System.exit(1);
            } else {
                Object var10;
                try {
                    var10 = var9.newInstance();
                } catch (Exception var7) {
                    var7.printStackTrace();
                    System.exit(1);
                    return;
                }

                if (!(var10 instanceof Provider)) {
                    System.err.println("Not a Provider class: " + var0);
                    System.exit(1);
                }

                Security.insertProviderAt((Provider)var10, 1);
            }
        }
    }

    private static void usage() {
        System.err.println("Usage: signapk [-w] [-providerClass <className>] publickey.x509[.pem] privatekey.pk8 [publickey2.x509[.pem] privatekey2.pk8 ...] input.jar output.jar");
        System.exit(2);
    }

    public static void sign(String[] var0) {
        if (var0.length < 4) {
            usage();
        }

        sBouncyCastleProvider = new BouncyCastleProvider();
        Security.addProvider(sBouncyCastleProvider);
        boolean var1 = false;
        String var2 = null;
        Object var3 = null;
        int var4 = 0;

        while(var4 < var0.length && var0[var4].startsWith("-")) {
            if ("-w".equals(var0[var4])) {
                var1 = true;
                ++var4;
            } else if ("-providerClass".equals(var0[var4])) {
                if (var4 + 1 >= var0.length) {
                    usage();
                }

                ++var4;
                var2 = var0[var4];
                ++var4;
            } else {
                usage();
            }
        }

        if ((var0.length - var4) % 2 == 1) {
            usage();
        }

        int var5 = (var0.length - var4) / 2 - 1;
        if (var1 && var5 > 1) {
            System.err.println("Only one key may be used with -w.");
            System.exit(2);
        }

        loadProviderIfNecessary(var2);
        String var6 = var0[var0.length - 2];
        String var7 = var0[var0.length - 1];
        JarFile var8 = null;
        FileOutputStream var9 = null;
        int var10 = 0;

        try {
            File var11 = new File(var0[var4 + 0]);
            X509Certificate[] var12 = new X509Certificate[var5];

            try {
                for(int var13 = 0; var13 < var5; ++var13) {
                    int var14 = var4 + var13 * 2;
                    var12[var13] = readPublicKey(new File(var0[var14]));
                    var10 |= getDigestAlgorithm(var12[var13]);
                }
            } catch (IllegalArgumentException var27) {
                System.err.println(var27);
                System.exit(1);
            }

            long var30 = var12[0].getNotBefore().getTime() + 3600000L;
            PrivateKey[] var15 = new PrivateKey[var5];

            for(int var16 = 0; var16 < var5; ++var16) {
                int var17 = var4 + var16 * 2 + 1;
                var15[var16] = readPrivateKey(new File(var0[var17]));
            }

            var8 = new JarFile(new File(var6), false);
            var9 = new FileOutputStream(var7);
            if (var1) {
                signWholeFile(var8, var11, var12[0], var15[0], var9);
            } else {
                JarOutputStream var31 = new JarOutputStream(var9);
                var31.setLevel(9);
                Manifest var32 = addDigestsToManifest(var8, var10);
                copyFiles(var32, var8, var31, var30);
                signFile(var32, var8, var12, var15, var31);
                var31.close();
            }
        } catch (Exception var28) {
            var28.printStackTrace();
            System.exit(1);
        } finally {
            try {
                if (var8 != null) {
                    var8.close();
                }

                if (var9 != null) {
                    var9.close();
                }
            } catch (IOException var26) {
                var26.printStackTrace();
                System.exit(1);
            }

        }

    }

    private static class CMSSigner implements CMSTypedData {
        private JarFile inputJar;
        private File publicKeyFile;
        private X509Certificate publicKey;
        private PrivateKey privateKey;
        private String outputFile;
        private OutputStream outputStream;
        private final ASN1ObjectIdentifier type;
        private Sign.WholeFileSignerOutputStream signer;

        public CMSSigner(JarFile var1, File var2, X509Certificate var3, PrivateKey var4, OutputStream var5) {
            this.inputJar = var1;
            this.publicKeyFile = var2;
            this.publicKey = var3;
            this.privateKey = var4;
            this.outputStream = var5;
            this.type = new ASN1ObjectIdentifier(CMSObjectIdentifiers.data.getId());
        }

        public Object getContent() {
            throw new UnsupportedOperationException();
        }

        public ASN1ObjectIdentifier getContentType() {
            return this.type;
        }

        public void write(OutputStream var1) throws IOException {
            try {
                this.signer = new Sign.WholeFileSignerOutputStream(var1, this.outputStream);
                JarOutputStream var2 = new JarOutputStream(this.signer);
                int var3 = Sign.getDigestAlgorithm(this.publicKey);
                long var4 = this.publicKey.getNotBefore().getTime() + 3600000L;
                Manifest var6 = Sign.addDigestsToManifest(this.inputJar, var3);
                Sign.copyFiles(var6, this.inputJar, var2, var4);
                Sign.addOtacert(var2, this.publicKeyFile, var4, var6, var3);
                Sign.signFile(var6, this.inputJar, new X509Certificate[]{this.publicKey}, new PrivateKey[]{this.privateKey}, var2);
                this.signer.notifyClosing();
                var2.close();
                this.signer.finish();
            } catch (Exception var7) {
                throw new IOException(var7);
            }
        }

        public void writeSignatureBlock(ByteArrayOutputStream var1) throws IOException, CertificateEncodingException, OperatorCreationException, CMSException {
            Sign.writeSignatureBlock(this, this.publicKey, this.privateKey, var1);
        }

        public Sign.WholeFileSignerOutputStream getSigner() {
            return this.signer;
        }
    }

    private static class WholeFileSignerOutputStream extends FilterOutputStream {
        private boolean closing = false;
        private ByteArrayOutputStream footer = new ByteArrayOutputStream();
        private OutputStream tee;

        public WholeFileSignerOutputStream(OutputStream var1, OutputStream var2) {
            super(var1);
            this.tee = var2;
        }

        public void notifyClosing() {
            this.closing = true;
        }

        public void finish() throws IOException {
            this.closing = false;
            byte[] var1 = this.footer.toByteArray();
            if (var1.length < 2) {
                throw new IOException("Less than two bytes written to footer");
            } else {
                this.write(var1, 0, var1.length - 2);
            }
        }

        public byte[] getTail() {
            return this.footer.toByteArray();
        }

        public void write(byte[] var1) throws IOException {
            this.write(var1, 0, var1.length);
        }

        public void write(byte[] var1, int var2, int var3) throws IOException {
            if (this.closing) {
                this.footer.write(var1, var2, var3);
            } else {
                this.out.write(var1, var2, var3);
                this.tee.write(var1, var2, var3);
            }

        }

        public void write(int var1) throws IOException {
            if (this.closing) {
                this.footer.write(var1);
            } else {
                this.out.write(var1);
                this.tee.write(var1);
            }

        }
    }

    private static class CountOutputStream extends FilterOutputStream {
        private int mCount = 0;

        public CountOutputStream(OutputStream var1) {
            super(var1);
        }

        public void write(int var1) throws IOException {
            super.write(var1);
            ++this.mCount;
        }

        public void write(byte[] var1, int var2, int var3) throws IOException {
            super.write(var1, var2, var3);
            this.mCount += var3;
        }

        public int size() {
            return this.mCount;
        }
    }
}
