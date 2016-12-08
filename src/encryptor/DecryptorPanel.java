package encryptor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static encryptor.EncryptorDecryptor.*;
import static encryptor.ConstantsAndMethods.*;

/**
 * All the necessary GUI components and functions for decrypting are in this
 * panel. This is one of the tabs from the main window, where the other one is
 * {@linkplain EncryptorPanel}.
 *
 * @author Mario Bobic
 */
public class DecryptorPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/** List model where chosen files are stored */
	private DefaultListModel<File> listModel = new DefaultListModel<>();
	/** Visual representation of a list model, showing chosen files */
	private JList<File> filesList = new JList<>(listModel);
	
	/** A simple file chooser */
	private JFileChooser chooser = FileEncryptorFrame.chooser;
	
	/** A checkbox for optional encrypted files deletion. */
	private JCheckBox deleteFilesBox;
	/** A checkbox for optional file name decryption */
	private JCheckBox decryptNamesBox;
	
	/** A button used for adding files to the list */
	private JButton addBtn;
	/** A button used for removing files from the list */
	private JButton removeBtn;
	/** A button that starts the decryption process */
	private JButton decryptBtn;
	/** A button that cancels the encryption process */
	private JButton cancelBtn;
	
	/** Total number of chosen files */
	private int totalFiles = 0;
	/** Total size of chosen files */
	private long totalSize = 0L;
	/** A text field that shows the total number of chosen files */
	private JTextField totalFilesTf;
	/** A text field that shows the total size of chosen files */
	private JTextField totalSizeTf;

	/** A progress bar that shows the progress status of decryption */
	private JProgressBar progressBar = new JProgressBar();
	/** The initial progress bar foreground color */
	private Color initialProgressBarColor = progressBar.getForeground();
	
	/** Hash code of entered password, used for decrypting files */
	private int passwordHash;
	
	/** A listener which calls the function to delete the selected files */
	private ActionListener removeListener = (e) -> {
		List<File> selected = filesList.getSelectedValuesList();
		for (File file : selected) {
			listModel.removeElement(file);
		}
		
		List<File> remaining = Collections.list(listModel.elements());
		if (remaining.size() == 0) {
			removeBtn.setEnabled(false);
			totalFiles = 0;
			totalSize = 0L;
			totalFilesTf.setText("0");
			totalSizeTf.setText("0 B");
		} else {
			totalFiles = remaining.size();
			totalSize = 0L;
			for (File file : remaining) {
				totalSize += file.length();
			}
			
			totalFilesTf.setText(Integer.toString(totalFiles));
			totalSizeTf.setText(humanReadableByteCount(totalSize));
		}
	};
	
	/**
	 * Constructs and initializes this panel with GUI components.
	 */
	public DecryptorPanel() {
		/* Set this panel's style. */
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		/* Create and add physically independent panels. */
		JPanel center = new JPanel(new BorderLayout());
		JPanel lower = new JPanel(new GridLayout(0, 1, 0, 10));
		
		add(center, BorderLayout.CENTER);
		add(lower, BorderLayout.PAGE_END);
		
		/* Style the central panel and add the files list to it. */
		center.setBorder(BorderFactory.createTitledBorder("Encrypted files"));
		center.add(new JScrollPane(filesList));

		/* Add a check box panel and add check boxes to it. */
		JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lower.add(checkBoxPanel);
		
		deleteFilesBox = new JCheckBox("Delete encrypted files after decryption");
		decryptNamesBox = new JCheckBox("Decrypt file names");
		
		checkBoxPanel.add(deleteFilesBox);
		checkBoxPanel.add(decryptNamesBox);
		
		/* Create a specially designed panel to fit the progress and the buttons. */
		JPanel progressPanel = new JPanel(new BorderLayout(10, 0));
		lower.add(progressPanel);
		
		/* Create and add a progress bar to this panel. */
		JLabel progressLabel = new JLabel("Progress:");
		progressPanel.add(progressLabel, BorderLayout.LINE_START);

		progressPanel.add(progressBar, BorderLayout.CENTER);
		progressBar.setStringPainted(true);

		/* Create a panel exclusively for the add and remove buttons. */
		JPanel addRemovePanel = new JPanel(new GridLayout(1, 2, 2, 0));
		progressPanel.add(addRemovePanel, BorderLayout.LINE_END);
		
		/* Add an "Add files" and "Remove selected" buttons to this panel. */
		addBtn = new JButton("Add files");
		addBtn.addActionListener((e) -> {
			loadFiles();
		});
		
		removeBtn = new JButton("Remove selected");
		removeBtn.setEnabled(false);
		removeBtn.addActionListener(removeListener);
		
		addRemovePanel.add(addBtn);
		addRemovePanel.add(removeBtn);
		
		/* Create and add the info and buttons panel. */
		JPanel lowerInfo = new JPanel(new GridLayout(1, 0, 5, 0));
		JPanel lowerBtns = new JPanel(new BorderLayout(2, 0));
		
		lower.add(lowerInfo);
		lower.add(lowerBtns);
		
		/* Create special info panels. */
		JPanel info1 = new JPanel(new BorderLayout(5, 0));
		JPanel info2 = new JPanel(new BorderLayout(5, 0));
		
		lowerInfo.add(info1);
		lowerInfo.add(info2);
		
		/* Add total files info. */
		JLabel totalFilesLbl = new JLabel("Number of files:", SwingConstants.RIGHT);
		totalFilesTf = new JTextField("0");
		totalFilesTf.setEditable(false);
		info1.add(totalFilesLbl, BorderLayout.LINE_START);
		info1.add(totalFilesTf, BorderLayout.CENTER);
		
		/* Add total size info. */
		JLabel totalSizeLbl = new JLabel("Total size:", SwingConstants.RIGHT);
		totalSizeTf = new JTextField("0 B");
		totalSizeTf.setEditable(false);
		info2.add(totalSizeLbl, BorderLayout.LINE_START);
		info2.add(totalSizeTf, BorderLayout.CENTER);

		/* Create and add a decrypt button. */
		decryptBtn = new JButton("Decrypt");
		lowerBtns.add(decryptBtn, BorderLayout.CENTER);
		
		decryptBtn.addActionListener((e) -> {
			decrypt();
		});
		
		cancelBtn = new JButton("Cancel");
		cancelBtn.setEnabled(false);
		lowerBtns.add(cancelBtn, BorderLayout.LINE_END);
		
		cancelBtn.addActionListener((e) -> {
			
		});
	}
	
	/**
	 * Prepare the decrypting by disabling GUI components, and creating and
	 * executing the {@linkplain SwingWorker} task.
	 */
	private void decrypt() {
		/* Reset the progress. */
		progressBar.setValue(0);
		progressBar.setForeground(initialProgressBarColor);
		
		/* Check if there are any files in the list. */
		if (listModel.getSize() == 0) {
			showError(DecryptorPanel.this, "Please select at least one file.");
			return;
		}
		
		/* Disable GUI components. */
		decryptBtn.setEnabled(false);
		addBtn.setEnabled(false);
		removeBtn.setEnabled(false);
		
		/* Enable the cancel button. */
		cancelBtn.setEnabled(true);
		
		/* Execute the task on a working thread and listen for progress change. */
		DecryptWorker worker = new DecryptWorker();
		
		cancelBtn.addActionListener((e) -> {
			worker.cancel();
		});

		worker.addPropertyChangeListener((evt) -> {
			if ("progress".equals(evt.getPropertyName())) {
				Integer progress = (Integer) evt.getNewValue();
				progressBar.setValue(progress);
				progressBar.setString(progress + "%");
			}
		});
		worker.execute();
	}

	/**
	 * Creates a dialog for the user to select files to be composed.
	 */
	private void loadFiles() {
		/* File chooser settings */
		chooser.setDialogTitle("Load files for decryption");
		chooser.setMultiSelectionEnabled(true);
		
		/* After the window has closed, get the selected file
		 * and store this file's info to class variables. */
		int retVal = chooser.showOpenDialog(this);

		if (retVal == JFileChooser.APPROVE_OPTION) {
			/* Get selected files and clear the previously selected. */
			File[] files = chooser.getSelectedFiles();
			listModel.clear();

			/* Set the total size initially to 0 */
			totalSize = 0L;

			/* Add newly selected files to the list model. */
			for (File file : files) {
				listModel.addElement(file);
				totalSize += file.length();
			}
			totalFiles = files.length;
			
			/* Enable the remove button. */
			removeBtn.setEnabled(true);

			/* Set the total number of files text field. */
			totalFilesTf.setText(Integer.toString(totalFiles));
			/* Set the total size text field to a human readable size. */
			totalSizeTf.setText(humanReadableByteCount(totalSize));
		}
	}
	
	/**
	 * A working thread for decrypting a list of files chosen by the user.
	 *
	 * @author Mario Bobic
	 */
	private class DecryptWorker extends SwingWorker<Void, Void> {
		
		/** Variable used for tracking the progress */
		private long totalDecryptedSize;
		
		/** Indicates if the user must be prompted to overwrite existing files */
		private boolean overwritePrompt = true;

		/**
		 * Prepares the chosen files and decrypts them in background.
		 */
		@Override
		protected Void doInBackground() throws Exception {
			/* Load all files to a list. */
			List<File> files = Collections.list(listModel.elements());

			/* Prompt the user to enter the password.
			 * If the user hit cancel, cancel the decryption. */
			boolean passwordEntered = showEnterPassword();
			if (!passwordEntered) {
				return null;
			}
			
			/* Prepare the progress bar variable */
			totalDecryptedSize = 0L;

			/* Decrypt all selected files. Make sure they exist. */
			for (File file : files) {
				if (!file.exists()) {
					showInformation(DecryptorPanel.this, "File " + file + " no longer exists.\nContinuing...");
					continue;
				}

				decryptFile(file, passwordHash, deleteFilesBox.isSelected(), decryptNamesBox.isSelected());
			}

			return null;
		}

		/**
		 * Decrypts the given {@code file} using the given {@code hash}.
		 * 
		 * @param file file to be decrypted
		 * @param hash hash code to be used when decrypting this file
		 * @param deleteFile deletes the encrypted file after decrypting if true
		 * @param decryptName decrypts the file name upon decrypting the file
		 */
		private void decryptFile(File file, int hash, boolean deleteFile, boolean decryptName) {
			/* Prepare the input stream. */
			try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
				/* If the password is incorrect, there's no point in going further. */
				boolean passwordCorrect = checkPassword(in, hash);
				if (!passwordCorrect) {
					showError(DecryptorPanel.this, "Incorrect password for file " + file);
					return;
				}
				
				/* Remove the extension of the encrypted file and create the output file. */
				String fileName = file.getName().replace(FILE_EXTENSION, "");
				String newFileName;
				if (decryptName) {
					try {
						newFileName = decryptName(fileName);
					} catch (IllegalArgumentException e) {
						showInformation(DecryptorPanel.this,
								"Decrypted name of file " + file + " has been tampered with.\nOnly the file will be decrypted.");
						newFileName = fileName;
					}
				} else {
					newFileName = fileName;
				}
				File outputFile = new File(file.getParentFile(), newFileName);
				
				/* Ask the user if he wants to overwrite the file. */
				if (overwritePrompt && outputFile.exists()) {
					boolean overwrite = showQuestion(DecryptorPanel.this,
							"File " + outputFile + " already exists.\nOverwrite this file" + (totalFiles==1?"?":"and future files?"));
					if (overwrite == true) {
						overwritePrompt = false;
					} else {
						return;
					}
				}

				/* Create the output stream and write decrypted bytes to it. */
				try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
					int len;
					byte[] bytes = new byte[STD_LOADER_SIZE];
					while ((len = in.read(bytes)) > 0  && !isCancelled()) {
						byte[] decryptedBytes = decryptBytes(bytes, hash, len);
						out.write(decryptedBytes);
						
						/* Update the progress bar. */
						totalDecryptedSize += len;
						setProgress((int) (100 * totalDecryptedSize / totalSize));
					}
				}
				
			} catch (Exception e) {
				showError(DecryptorPanel.this, "An error occured while processing file " + file);
				return;
			}
			
			System.out.println("Total decrypted: " + totalDecryptedSize);
			System.out.println("Total size: " + totalSize);
			
			if (deleteFile) {
				file.delete();
			}
		}
		
		private boolean checkPassword(BufferedInputStream in, int password) throws IOException {
			ByteBuffer b = ByteBuffer.allocate(PASSWORD_LENGTH);
			b.putInt(password);
			byte[] result = b.array();
			
			byte[] readings = new byte[result.length];
			in.read(readings);

			/* If arrays are not of the same contents,
			 * the user has entered an incorrect password */
			if (!Arrays.equals(result, readings)) {
				return false;
			} else {
				totalDecryptedSize += readings.length;
				return true;
			}
		}

		/**
		 * Simply re-enables every disabled GUI component.
		 */
		@Override
		protected void done() {
			/* Re-enable GUI components. */
			decryptBtn.setEnabled(true);
			addBtn.setEnabled(true);
			removeBtn.setEnabled(true);
			/* Disable the cancel button. */
			cancelBtn.setEnabled(false);
		}
		
		/**
		 * Attempts to cancel execution of this task. This attempt will fail if
		 * the task has already completed, has already been cancelled, or could
		 * not be cancelled for some other reason.
		 */
		protected void cancel() {
			if (this.cancel(true)) {
				progressBar.setForeground(Color.RED);
			}
		}

	}
	
	/**
	 * Shows a password input dialog and returns true if the password has been
	 * entered. False otherwise.
	 * 
	 * @return true if the password has been entered, false otherwise
	 */
	protected boolean showEnterPassword() {

		/**
		 * A panel with just the password field.
		 *
		 * @author Mario Bobic
		 */
		class PasswordPanel extends JPanel {
			private static final long serialVersionUID = 1L;
			
			/** A password field */
			private JPasswordField passwordField = new JPasswordField(20);

			/**
			 * Constructs and initializes this panel with GUI components. 
			 */
			public PasswordPanel() {
				setLayout(new GridLayout(0, 1, 0, 3));
				setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

				JPanel passPanel = new JPanel(new GridLayout(1, 0));
				add(passPanel);

				passPanel.add(new JLabel("Password:", SwingConstants.LEADING));
				passPanel.add(passwordField);
			}
			
			/**
			 * Returns the hash code of the entered password. If no characters
			 * are entered, or more formally if {@code password.length == 0}, a
			 * {@linkplain NullPointerException} is thrown.
			 * 
			 * @return hash code of the entered password
			 * @throws NullPointerException if password.length == 0
			 */
			private int getPasswordHash() {
				if (passwordField.getPassword().length == 0) {
					throw new NullPointerException("Password not entered.");
				}
				
				return Arrays.hashCode(passwordField.getPassword());
			}
		}
		
		PasswordPanel passwordPanel = new PasswordPanel();

		while (true) {
			try {
				int retVal = JOptionPane.showConfirmDialog(
						DecryptorPanel.this,
						passwordPanel,
						"Password input",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE
				);
				if (retVal == JOptionPane.OK_OPTION) {
					passwordHash = passwordPanel.getPasswordHash();
					return true;
				} else {
					return false;
				}
			} catch (NullPointerException e) {
				showError(DecryptorPanel.this, "Please enter a password.");
				continue;
			}
		}
	}
	
}
