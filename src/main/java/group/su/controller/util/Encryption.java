package group.su.controller.util;

import com.sun.org.apache.bcel.internal.generic.ARETURN;

//简单的十六进制转换的方式加密数据，
public class Encryption {

    //字符串加密
    public String EncryptedString(String str){

//        String str = "true,0,localhost,1433,pxscj";//明文
        String[] a = str.split("");
        StringBuilder s1 = new StringBuilder();
        for(int i=0;i<a.length;i++)
        {
            char b = a[i].charAt(0);
            int c = b+1;//伪装，每一个字符+1位
            s1.append(Long.toHexString(c));//转换成十六进制
        }
//        System.out.println(s1);       输出加密后的密文
        return s1.toString();
    }


    //字符串解密
    public String  DecryptString(String str){
//密文
//        String str = "757376662d312d6d7064626d697074752d323534342d717974646b";
        String[] a = str.split("");
        StringBuilder s1 = new StringBuilder();
        int j = 0;
        for (int n = 0; n < a.length/2; n++) {
            StringBuilder s2 = new StringBuilder();
            for (int i = 0; i < 2; i++) {
                s2.append(a[j]);
                j++;
            }
            s1.append((char)(Integer.parseInt(String.valueOf(s2),16)-1));//转换成10进制数后-1
        }
//        System.out.println(s1);  输出原文
        return  s1.toString();
    }
}
