package server.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encryption{
	public Encryption(){
		// Constructor
	}

	public String getHash(int hash, String var){
		// 0 - SHA1, 1 - MD5, 2 - SHA256, 3 - SHA512
		switch(hash){
			case 0:
				return getSha1(var);
			case 1:
				return getMd5(var);
			case 2:
				return getSha256(var);
			case 3:
				return getSha512(var);
			default:
				return getSha1(var);
		}
	}

	public String getSha1(String var){
		MessageDigest md = null;
		try{
			md = MessageDigest.getInstance("SHA1");
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		}
		md.reset();
		byte buffer[] = var.getBytes();
		md.update(buffer);
		byte digest[] = md.digest();
		String hash = "";
		for(int i = 0; i < digest.length; i++)
			hash += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
		return hash;
	}

	public String getMd5(String var){
		byte[] defaultBytes = var.getBytes();
		MessageDigest md = null;
		try{
			md = MessageDigest.getInstance("MD5");
		}catch(Exception e){
			e.printStackTrace();
		}
		md.reset();
		md.update(defaultBytes);
		byte messageDigest[] = md.digest();
		StringBuffer hexString = new StringBuffer();
		for(int i = 0; i < messageDigest.length; i++)
			hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
		return hexString.toString();
	}

	public String getSha256(String var){
		MessageDigest md = null;
		try{
			md = MessageDigest.getInstance("SHA-256");
		}catch(Exception e){
			e.printStackTrace();
		}
		byte hash[] = md.digest(var.getBytes());
		StringBuffer hexString = new StringBuffer();
		for(byte b : hash)
			hexString.append(String.format("%02x", b));
		return hexString.toString();
	}

	public String getSha512(String var){
		MessageDigest md = null;
		try{
			md = MessageDigest.getInstance("SHA-256");
		}catch(Exception e){
			e.printStackTrace();
		}
		byte hash[] = md.digest(var.getBytes());
		StringBuffer hexString = new StringBuffer();
		for(byte b : hash)
			hexString.append(String.format("%02x", b));
		return hexString.toString();
	}
}