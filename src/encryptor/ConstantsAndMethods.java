package encryptor;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.*;
import javax.xml.bind.DatatypeConverter;

/**
 * A collection of constants and methods generally used for visually
 * representing and communicating with the user.
 *
 * @author Mario Bobic
 */
public abstract class ConstantsAndMethods {

	/** Extension that will be added to encrypted files */
	public static final String FILE_EXTENSION = ".crypt";
	
	/** Standard size for the loading byte buffer array */
	public static final int STD_LOADER_SIZE = 4096;
	
	/**
	 * Converts the number of bytes to a human readable byte count with binary
	 * prefixes.
	 * 
	 * @param bytes number of bytes
	 * @return human readable byte count with binary prefixes
	 */
	public static String humanReadableByteCount(long bytes) {
		/* Use the natural 1024 units and binary prefixes. */
		int unit = 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = "kMGTPE".charAt(exp - 1) + "i";
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	/**
	 * Shows an error message with the desired text.
	 * 
	 * @param message desired text
	 */
	protected static void showError(Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Shows an information message with the desired text.
	 * 
	 * @param message desired text
	 */
	protected static void showInformation(Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Shows a warning that one or more piece files have been tampered with
	 * and prompts the user to continue with composing or to cancel.
	 * This dialog is shown only once, no matter how much pieces are tampered with.
	 * 
	 * @return the user's decision to continue or not
	 */
	protected static boolean showQuestion(Component parent, String message) {
		int retVal = JOptionPane.showConfirmDialog(parent, message, "Question", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		return retVal == JOptionPane.YES_OPTION ? true : false;
	}
	
	/**
	 * Generates the 40 character long SHA-1 password hash of the user's
	 * password by converting the specified <tt>password</tt> to an array of
	 * bytes decoded with the {@link StandardCharsets#UTF_8 UTF-8} charset and
	 * digested with the hash-algorithm.
	 * 
	 * @param password password to be hashed
	 * @return the hash of the specified <tt>password</tt>
	 */
	public static String generatePasswordHash(String password) {
		String pass = password.concat("peaches.*"); // add salt
		byte[] passwordBytes = pass.getBytes(StandardCharsets.UTF_8);
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new InternalError("Algorithm unavailable (SHA-1)", e);
		}
		
		return DatatypeConverter.printHexBinary(md.digest(passwordBytes));
	}

}
