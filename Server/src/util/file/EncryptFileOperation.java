package util.file;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;

public class EncryptFileOperation {
    /**
     * 采用DES算法加密文件
     *
     * @param filePath 文件路径
     * @return 文件是否加密成功
     */
    public static boolean encryptFile(String filePath, String destFilePath, String keyStr)
            throws IOException, NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        if (!FileOperation.judeFileExists(filePath)) {
            return false;
        }

        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        AlgorithmParameterSpec paramSpec = new IvParameterSpec("12345678".getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, getKey(keyStr), paramSpec);

        InputStream is = new FileInputStream(filePath);
        OutputStream out = new FileOutputStream(destFilePath);
        CipherInputStream cis = new CipherInputStream(is, cipher);
        byte[] buffer = new byte[1024];
        int r;
        while ((r = cis.read(buffer)) > 0) {
            out.write(buffer, 0, r);
        }
        cis.close();
        is.close();
        out.close();
        return true;
    }

    /**
     * 文件采用DES算法解密文件
     *
     * @param filePath     已加密的文件的文件路径
     * @param destFilePath 解密文件路径
     */
    public static boolean decryptFile(String filePath, String destFilePath, String keyStr)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, IOException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        if (!FileOperation.judeFileExists(filePath)) {
            return false;
        }
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        AlgorithmParameterSpec paramSpec = new IvParameterSpec("12345678".getBytes());
        cipher.init(Cipher.DECRYPT_MODE, getKey(keyStr), paramSpec);

        InputStream is = new FileInputStream(filePath);
        OutputStream out = new FileOutputStream(destFilePath);
        CipherOutputStream cos = new CipherOutputStream(out, cipher);
        byte[] buffer = new byte[1024];
        int r;
        while ((r = is.read(buffer)) >= 0) {
            cos.write(buffer, 0, r);
        }
        cos.close();
        out.close();
        is.close();
        return true;
    }

    /**
     * 根据参数生成密钥
     *
     * @param keyStr 密钥参数
     * @return 密钥
     */
    private static Key getKey(String keyStr) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        // 判断Key是否正确
        if (keyStr == null) {
            System.out.print("Key为空null");
            return null;
        }
        // 判断Key是否为16位
        if (keyStr.length() != 16) {
            System.out.print("Key长度不是16位");
            return null;
        }
        DESKeySpec dks = new DESKeySpec(keyStr.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        // key的长度不能够小于8位字节
        return keyFactory.generateSecret(dks);
    }
}
