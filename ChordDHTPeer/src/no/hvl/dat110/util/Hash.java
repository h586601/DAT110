package no.hvl.dat110.util;

/**
 * exercise/demo purpose in dat110
 * @author tdoy
 *
 */

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash { 
	
	private static BigInteger hashint = BigInteger.ZERO; 
	private static MessageDigest md = null;
	
	/**
	 * Hash a given string using MD5 and return the result as a BigInteger.
	 * 
	 * @param entity
	 * @return BigInteger hashint
	 * @throws NoSuchAlgorithmException
	 */
	public static BigInteger hashOf(String entity)  {		
		
		// Use MD5 with 128 bits digest
		
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		// compute the hash of the input 'entity'
		byte[] stringToBytes = entity.getBytes(StandardCharsets.UTF_8);
		md.update(stringToBytes);
		byte[] hash = md.digest();
		
		// convert the hash into hex format
		String str = toHex(hash);
		
		// convert the hex into BigInteger
		hashint = new BigInteger(str, 16);
		
		
		return hashint;
	}
	
	/**
	 * Compute the address size of MD5
	 * 
	 * @return BigInteger
	 */
	public static BigInteger addressSize() {
		
		// get the digest length
		// and compute the number of bits = digest length * 8
		int numberOfBits = md.getDigestLength()*8;		
		
		// compute the address size = 2 ^ number of bits
		BigInteger two = new BigInteger("2");
		BigInteger addressSize = two.pow(numberOfBits);
		
		return addressSize;
	}
	
	public static String toHex(byte[] digest) {
		StringBuilder strbuilder = new StringBuilder();
		for(byte b : digest) {
			strbuilder.append(String.format("%02x", b&0xff));
		}
		return strbuilder.toString();
	}
	
	public static void main(String[] args) {
		
		System.out.println(Hash.hashOf("process1"));
		System.out.println(Hash.addressSize());
		BigInteger diff = Hash.addressSize().subtract(Hash.hashOf("process1"));
		System.out.println(diff);
	}

}
