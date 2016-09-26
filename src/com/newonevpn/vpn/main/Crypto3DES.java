package com.newonevpn.vpn.main;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;

public class Crypto3DES
{

   

    public static String Key()
    {
        return "A2U7vzy9";
    }

    private static Cipher ecipher;
    private static Cipher dcipher;
    private static final int iterationCount = 10;
    private static String decryptedData;
     private static byte[] salt = {
      'A','2','U','7','v','z','y','9'
        };

        public Crypto3DES() {
        	
  
        }
     
      public String getDecryptedData(){
    	  return decryptedData;
      }
     
      public static String Decrypte(String message){
    	  try {
				DESKeySpec keySpec = new DESKeySpec(Key().getBytes("UTF8"));
	        	SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
	        	SecretKey key = keyFactory.generateSecret(keySpec);
	        	

	        	BASE64Encoder base64encoder = new BASE64Encoder();
	        	BASE64Decoder base64decoder = new BASE64Decoder();
	        	
	         	//......

	        	// DECODE encryptedPwd String
	        	byte[] encrypedPwdBytes = base64decoder.decodeBuffer(message);

	        	Cipher cipher1 = Cipher.getInstance("DES/ECB/NoPadding");// cipher is not thread safe
	        	cipher1.init(Cipher.DECRYPT_MODE, key);
	        	byte[] plainTextPwdBytes = (cipher1.doFinal(encrypedPwdBytes));
	        	decryptedData = new String(plainTextPwdBytes, "UTF8");
	        	return decryptedData;
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchPaddingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvalidKeyException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalBlockSizeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (BadPaddingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvalidKeySpecException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} //catch (InvalidAlgorithmParameterException e1) {
				// TODO Auto-generated catch block
			//	e1.printStackTrace();
		//	}      
    	  return null;
      }
}