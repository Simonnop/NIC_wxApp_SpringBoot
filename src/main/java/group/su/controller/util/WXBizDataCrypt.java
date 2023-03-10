package group.su.controller.util;

import com.sun.xml.internal.messaging.saaj.util.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.web.bind.annotation.RequestParam;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.Security;


//微信解密算法，
public class WXBizDataCrypt {

    public String decrypt(String session_key, String iv, String encryptData) {

        String decryptString = "";
        //解码经过 base64 编码的字符串
        byte[] sessionKeyByte = Base64.base64Decode(session_key).getBytes();
        byte[] ivByte = Base64.base64Decode(iv).getBytes();
        byte[] encryptDataByte = Base64.base64Decode(encryptData).getBytes();

        try {
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            //得到密钥
            Key key = new SecretKeySpec(sessionKeyByte, "AES");
            //AES 加密算法
            AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance("AES");
            algorithmParameters.init(new IvParameterSpec(ivByte));
            cipher.init(Cipher.DECRYPT_MODE, key, algorithmParameters);
            byte[] bytes = cipher.doFinal(encryptDataByte);
            decryptString = new String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptString;
    }
}
