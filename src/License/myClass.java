package License;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class myClass extends ClassLoader {
    public static byte[] code;

    private static String md5(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(data);
        byte[] md5 = md.digest();
        return byte2HexStr(md5);
    }

    private static String byte2HexStr(byte[] b) {
        String hs = "";
        String stmp;
        for (byte aB : b) {
            stmp = (Integer.toHexString(aB & 0XFF));
            if (stmp.length() == 1)
                hs = hs + "0" + stmp;
            else
                hs = hs + stmp;
            // if (n<b.length-1) hs=hs+":";
        }
        return hs.toUpperCase();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class clazz;
        String res = name.replace(".", "/");

        res = "/" + res;
        int l = res.lastIndexOf("/");
        String className = res.substring(l + 1);
        try {
            res = res.substring(0, l + 1) + md5(className.getBytes()) + ".class";
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        InputStream is = this.getClass().getResourceAsStream(res);

        long len = 0;
        try {
            len = is.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] raw = new byte[(int) len];

        try {
            int r;
            int off = 0;
            while (true) {
                r = is.read(raw, off, (int) len);
                if (r != len) {
                    len -= r;
                    off += r;
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String ivStr = "****************";
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert md != null;
        md.update(code);
        byte[] key = md.digest();

        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        SecretKeySpec skey = new SecretKeySpec(key, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivStr.getBytes());
        try {
            assert cipher != null;
            cipher.init(Cipher.DECRYPT_MODE, skey, iv);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
//        cipher.update(raw);
        byte[] en = null;
        try {
            en = cipher.doFinal(raw);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        assert en != null;
        clazz = defineClass(name, en, 0, en.length);
        if (clazz == null) {
            throw new ClassNotFoundException(name);
        }
        return clazz;
    }
}
