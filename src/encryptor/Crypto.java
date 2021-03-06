package encryptor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import static encryptor.ConstantsAndMethods.STD_LOADER_SIZE;

/**
 * This class provides the functionality of a cryptographic cipher for
 * encryption and decryption.
 * <p>
 * The class offers a method for automatic encryption or decryption of a source
 * file into a destination file ({@link #execute(File, File)}.<br>
 * It also offers two methods to manually encrypt or decrypt bytes
 * ({@link #update(byte[], int, int)} and {@link #doFinal()}).
 *
 * @author Mario Bobic
 */
public class Crypto {

	/** Encryption mode. */
	public static final boolean ENCRYPT = true;
	/** Decryption mode. */
	public static final boolean DECRYPT = false;
	
	/** Minimal length of the hash. Hash is trimmed to this length. */
	private static final int HASH_LEN = 32;
	
	/** Hash to be used while encrypting or decrypting. */
	private String hash;
	
	/** Cipher used by this crypto. */
	Cipher cipher;
	
	/**
	 * Constructs an instance of {@code Crypto} with the specified arguments.
	 *
	 * @param hash hash to be used while encrypting or decrypting
	 * @param mode encryption or decryption mode, i.e. Crypto.ENCRYPT
	 * @throws IllegalArgumentException if <tt>hash</tt> is invalid
	 */
	public Crypto(String hash, boolean mode) {
		if (hash.length() < HASH_LEN) {
			throw new IllegalArgumentException("Hash length must be greater than " + HASH_LEN);
		}
		
		this.hash = hash.substring(0, HASH_LEN); // must be 16 bytes for SecretKeySpec
		initialize(mode);
	}
	
	/**
	 * Initializes this Crypto by instantiating a cipher in encryption or
	 * decryption mode.
	 * <p>
	 * To use the encryption mode, the <tt>encrypt</tt> value must be
	 * <tt>true</tt>.<br>
	 * To use the decryption mode, the <tt>encrypt</tt> value must be
	 * <tt>false</tt>.
	 * 
	 * @param encrypt specified encryption or decryption
	 */
	private void initialize(boolean encrypt) {
		try {
			SecretKeySpec keySpec = new SecretKeySpec(DatatypeConverter.parseHexBinary(hash), "AES");
			AlgorithmParameterSpec paramSpec = new IvParameterSpec(DatatypeConverter.parseHexBinary(hash));
			
			/* Create a cipher and start encrypting/decrypting. */
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, keySpec, paramSpec);
		} catch (GeneralSecurityException e) {
			throw new InternalError("Could not initialize.", e);
		}
	}
	
	/**
	 * <b>Encrypts</b> or <b>decrypts</b> the file specified by the
	 * <tt>sourcefile</tt> and generates a file specified by the
	 * <tt>destfile</tt>. The specified <tt>hash</tt> is used as a password. The
	 * encryption or decryption is specified by the fourth parameter in this
	 * method, an <tt>encrypt</tt> boolean. This method uses the AES
	 * cryptographic algorithm.
	 * 
	 * @param sourcefile file to be encrypted or decrypted
	 * @param destfile file to be created
	 * @throws IllegalArgumentException if the <tt>hash</tt> is invalid
	 * @throws FileNotFoundException if the file does not exist, is a directory
	 *         rather than a regular file, or for some other reason cannot be
	 *         opened for reading
	 * @throws IOException if any other I/O error occurs
	 */
	public void execute(File sourcefile, File destfile) throws IOException {
		try (
				InputStream in = new BufferedInputStream(new FileInputStream(sourcefile));
				OutputStream out = new BufferedOutputStream(new FileOutputStream(destfile));
		) {
			int len;
			byte[] bytes = new byte[STD_LOADER_SIZE];
			while ((len = in.read(bytes)) != -1) {
				// Update until the very end
				byte[] processedBytes = update(bytes, 0, len);
				out.write(processedBytes);
			}
			// Do the final touch
			byte[] processedBytes = doFinal();
			out.write(processedBytes);
		}
	}
	
	/**
	 * Continues a multiple-part encryption or decryption operation (depending
	 * on how this crypto was initialized), processing another data part.
	 * <p>
	 * The first <tt>len</tt> bytes in the <tt>input</tt> buffer, starting at
	 * <tt>offset</tt> inclusive, are processed, and the result is stored in a
	 * new buffer.
	 * <p>
	 * If <tt>len</tt> is zero, this method returns <tt>null</tt>.
	 * 
	 * @param bytes the input buffer
	 * @param offset the offset in input where the input starts
	 * @param len the input length
	 * @return the new buffer with the result
	 */
	public byte[] update(byte[] input, int offset, int len) {
		return cipher.update(input, 0, len);
	}
	
	/**
	 * Finishes a multiple-part encryption or decryption operation, depending on
	 * how this crypto was initialized. The result is stored in a new buffer.
	 * 
	 * @return the new buffer with the result
	 */
	public byte[] doFinal() {
		try {
			return cipher.doFinal();
		} catch (GeneralSecurityException e) {
			throw new InternalError(e);
		}
	}

}
