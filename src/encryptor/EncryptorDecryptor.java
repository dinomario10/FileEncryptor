package encryptor;

import java.util.Base64;

/**
 * This class contains useful methods for encrypting and decrypting.
 *
 * @author Mario Bobic
 */
public class EncryptorDecryptor {

	/** A constant used for byte encryption and decryption process */
	private static final int BYTE_ENCRYPTION_CONSTANT = 1;

	/**
	 * Encrypts the first {@code len} bytes of the given array of bytes using
	 * the given {@code hash}. Returns the encrypted array of bytes.
	 * 
	 * @param bytes bytes to be encrypted
	 * @param hash hash used for encryption
	 * @param len length considered for encrypting
	 * @return the encrypted array of bytes
	 */
	public static byte[] encryptBytes(byte[] bytes, int hash, int len) {
		byte[] encryptedBytes = new byte[len];
		
		for (int i = 0; i < len; i++) {
			byte b = (byte) (bytes[i] + hash + BYTE_ENCRYPTION_CONSTANT);
			encryptedBytes[i] = b;
		}
		
		return encryptedBytes;
	}
	
	/**
	 * Decrypts the first {@code len} bytes of the given array of bytes using
	 * the given {@code hash}. Returns the decrypted array of bytes.
	 * 
	 * @param bytes bytes to be decrypted
	 * @param hash hash used for decryption
	 * @param len length considered for decrypting
	 * @return the decrypted array of bytes
	 */
	public static byte[] decryptBytes(byte[] bytes, int hash, int len) {
		byte[] decryptedBytes = new byte[len];
		
		for (int i = 0; i < len; i++) {
			byte b = (byte) (bytes[i] - hash - BYTE_ENCRYPTION_CONSTANT);
			decryptedBytes[i] = b;
		}
		
		return decryptedBytes;
	}
	
	/**
	 * Encrypts the given {@code name} and returns the encrypted name. The name
	 * encryption method is done by the {@linkplain Base64} encoding scheme.
	 * 
	 * @param name name to be encrypted
	 * @return the encrypted name
	 */
	public static String encryptName(String name) {
		byte[] originalBytes = name.getBytes();
		byte[] encodedBytes = Base64.getEncoder().encode(originalBytes);
		return new String(encodedBytes);
	}
	
	
	/**
	 * Decrypts the given {@code encryptedName} and returns the decrypted name.
	 * The name decryption method is done by the {@linkplain Base64} decoding
	 * scheme.
	 * 
	 * @param encryptedName name to be decrypted
	 * @return the decrypted name
	 * @throws IllegalArgumentException if {@code encryptedName} is not in valid Base64 scheme
	 */
	public static String decryptName(String encryptedName) {
		byte[] encodedBytes = encryptedName.getBytes();
		byte[] originalBytes = Base64.getDecoder().decode(encodedBytes);
		return new String(originalBytes);
	}

}
