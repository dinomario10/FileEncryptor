package encryptor;

import java.awt.*;
import javax.swing.*;

/**
 * A collection of constants and methods generally used for visually
 * representing and communicating with the user.
 *
 * @author Mario Bobic
 */
public abstract class ConstantsAndMethods {

	/** Extension that will be added to encrypted files */
	public static final String FILE_EXTENSION = ".encrypted";
	
	/** Standard size for the loading byte buffer array */
	public static final int STD_LOADER_SIZE = 1024;
	
	/** Length of the password implanted into the encrypted files */
	public static final int PASSWORD_LENGTH = Integer.BYTES;
	
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

}
