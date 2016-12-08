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
 * All the necessary GUI components and functions for encrypting are in this
 * panel. This is one of the tabs from the main window, where the other one is
 * {@linkplain DecryptorPanel}.
 *
 * @author Mario Bobic
 */
public class EncryptorPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	/** List model where chosen files are stored */
	private DefaultListModel<File> listModel = new DefaultListModel<>();
	/** Visual representation of a list model, showing chosen files */
	private JList<File> filesList = new JList<>(listModel);
	
	/** A simple file chooser */
	private JFileChooser chooser = FileEncryptorFrame.chooser;
	
	/** A checkbox for optional original files deletion. */
	private JCheckBox deleteFilesBox;
	/** A checkbox for optional file name encryption */
	private JCheckBox encryptNamesBox;
	
	/** A button used for adding files to the list */
	private JButton addBtn;
	/** A button used for removing files from the list */
	private JButton removeBtn;
	/** A button that starts the encryption process */
	private JButton encryptBtn;
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

	/** A progress bar that shows the progress status of encryption */
	private JProgressBar progressBar = new JProgressBar();
	/** The initial progress bar foreground color */
	private Color initialProgressBarColor = progressBar.getForeground();
	
	/** Hash code of entered password, used for encrypting files */
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
			totalSize += PASSWORD_LENGTH*totalFiles;
			
			totalFilesTf.setText(Integer.toString(totalFiles));
			totalSizeTf.setText(humanReadableByteCount(totalSize));
		}
	};

	/**
	 * Constructs and initializes this panel with GUI components.
	 */
	public EncryptorPanel() {
		/* Set this panel's style. */
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		/* Create and add physically independent panels. */
		JPanel center = new JPanel(new BorderLayout());
		JPanel lower = new JPanel(new GridLayout(0, 1, 0, 10));
		
		add(center, BorderLayout.CENTER);
		add(lower, BorderLayout.PAGE_END);
		
		/* Style the central panel and add the files list to it. */
		center.setBorder(BorderFactory.createTitledBorder("Files"));
		center.add(new JScrollPane(filesList));

		/* Add a check box panel and add check boxes to it. */
		JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lower.add(checkBoxPanel);
		
		deleteFilesBox = new JCheckBox("Delete original files after encryption");
		encryptNamesBox = new JCheckBox("Encrypt file names");
		
		checkBoxPanel.add(deleteFilesBox);
		checkBoxPanel.add(encryptNamesBox);
		
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

		/* Create and add a encrypt button. */
		encryptBtn = new JButton("Encrypt");
		lowerBtns.add(encryptBtn, BorderLayout.CENTER);
		
		encryptBtn.addActionListener((e) -> {
			encrypt();
		});
		
		cancelBtn = new JButton("Cancel");
		cancelBtn.setEnabled(false);
		lowerBtns.add(cancelBtn, BorderLayout.LINE_END);
		
		cancelBtn.addActionListener((e) -> {
			
		});
	}
	
	/**
	 * Prepare the encrypting by disabling GUI components, and creating and
	 * executing the {@linkplain SwingWorker} task.
	 */
	private void encrypt() {
		/* Reset the progress. */
		progressBar.setValue(0);
		progressBar.setForeground(initialProgressBarColor);

		/* Check if there are any files in the list. */
		if (listModel.getSize() == 0) {
			showError(EncryptorPanel.this, "Please select at least one file.");
			return;
		}
		
		/* Disable GUI components. */
		encryptBtn.setEnabled(false);
		addBtn.setEnabled(false);
		removeBtn.setEnabled(false);
		
		/* Enable the cancel button. */
		cancelBtn.setEnabled(true);
		
		/* Execute the task on a working thread and listen for progress change. */
		EncryptWorker worker = new EncryptWorker();
		
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
		chooser.setDialogTitle("Load files for encryption");
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
			totalSize += PASSWORD_LENGTH*totalFiles;
			
			/* Enable the remove button. */
			removeBtn.setEnabled(true);

			/* Set the total number of files text field. */
			totalFilesTf.setText(Integer.toString(totalFiles));
			/* Set the total size text field to a human readable size. */
			totalSizeTf.setText(humanReadableByteCount(totalSize));
		}
	}
	
	/**
	 * A working thread for encrypting a list of files chosen by the user.
	 *
	 * @author Mario Bobic
	 */
	private class EncryptWorker extends SwingWorker<Void, Void> {
		
		/** Variable used for tracking the progress */
		private long totalEncryptedSize;
		
		/** Indicates if the user must be prompted to overwrite existing files */
		private boolean overwritePrompt = true;

		/**
		 * Prepares the chosen files and encrypts them in background.
		 */
		@Override
		protected Void doInBackground() throws Exception {
			/* Load all files to a list. */
			List<File> files = Collections.list(listModel.elements());

			/* Prompt the user to enter the password.
			 * If the user hit cancel, cancel the encryption. */
			boolean passwordEntered = showEnterPassword();
			if (!passwordEntered) {
				return null;
			}
			
			/* Prepare the progress bar variable */
			totalEncryptedSize = 0L;

			/* Encrypt all selected files. Make sure they exist. */
			for (File file : files) {
				if (!file.exists()) {
					showInformation(EncryptorPanel.this, "File " + file + " no longer exists.\nContinuing...");
					continue;
				}

				encryptFile(file, passwordHash, deleteFilesBox.isSelected(), encryptNamesBox.isSelected());
			}

			return null;
		}

		/**
		 * Encrypts the given {@code file} using the given {@code hash}.
		 * 
		 * @param file file to be encrypted
		 * @param hash hash code to be used when encrypting this file
		 * @param deleteFile deletes the original file after encrypting if true
		 * @param encryptName encrypts the file name upon encrypting the file
		 */
		private void encryptFile(File file, int hash, boolean deleteFile, boolean encryptName) {
			String newFileName = (encryptName ? encryptName(file.getName()) : file.getName()) + FILE_EXTENSION;
			File outputFile = new File(file.getParentFile(), newFileName);
			
			/* Ask the user if he wants to overwrite the file. */
			if (overwritePrompt && outputFile.exists()) {
				boolean overwrite = showQuestion(EncryptorPanel.this,
						"File " + outputFile + " already exists.\nOverwrite this file" + (totalFiles==1?"?":"and future files?"));
				if (overwrite == true) {
					overwritePrompt = false;
				} else {
					return;
				}
			}
			
			/* Prepare the streams. */
			try (
					BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
					BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
			) {
				storePassword(out, hash);

				int len;
				byte[] bytes = new byte[STD_LOADER_SIZE];
				while ((len = in.read(bytes)) > 0 && !isCancelled()) {
					byte[] encryptedBytes = encryptBytes(bytes, hash, len);
					out.write(encryptedBytes);
					
					/* Update the progress bar. */
					totalEncryptedSize += len;
					setProgress((int) (100 * totalEncryptedSize / totalSize));
				}
				
			} catch (Exception e) {
				showError(EncryptorPanel.this, "An error occured while processing file " + file);
				return;
			}

			System.out.println("Total encrypted: " + totalEncryptedSize);
			System.out.println("Total size: " + totalSize);
			
			if (deleteFile) {
				file.delete();
			}
		}
		
		private void storePassword(BufferedOutputStream out, int password) throws IOException {
			ByteBuffer b = ByteBuffer.allocate(PASSWORD_LENGTH);
			b.putInt(password);
			byte[] result = b.array();
			out.write(result);
			
			totalEncryptedSize += result.length;
		}

		/**
		 * Simply re-enables every disabled GUI component.
		 */
		@Override
		protected void done() {
			/* Re-enable GUI components. */
			encryptBtn.setEnabled(true);
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
	private boolean showEnterPassword() {

		/**
		 * A panel with just the password field.
		 *
		 * @author Mario Bobic
		 */
		class PasswordPanel extends JPanel {
			private static final long serialVersionUID = 1L;
			
			/** A password field */
			private JPasswordField passwordField1 = new JPasswordField(20);
			/** A confirmation password field */
			private JPasswordField passwordField2 = new JPasswordField(20);

			/**
			 * Constructs and initializes this panel with GUI components. 
			 */
			public PasswordPanel() {
				setLayout(new GridLayout(0, 1, 0, 3));
				setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

				JPanel passPanel1 = new JPanel(new GridLayout(1, 0));
				JPanel passPanel2 = new JPanel(new GridLayout(1, 0));

				add(passPanel1);
				add(passPanel2);

				passPanel1.add(new JLabel("Password:", SwingConstants.LEADING));
				passPanel2.add(new JLabel("Confirm password:", SwingConstants.LEADING));

				passPanel1.add(passwordField1);
				passPanel2.add(passwordField2);
			}
			
			/**
			 * Returns the hash code of the entered password. If no characters
			 * are entered, or more formally if {@code password.length == 0}, a
			 * {@linkplain NullPointerException} is thrown. If the passwords in
			 * both fields do not match, an {@linkplain IllegalArgumentException}
			 * is thrown.
			 * 
			 * @return hash code of the entered password
			 * @throws NullPointerException if password.length == 0
			 * @throws IllegalArgumentException if the passwords do not match
			 */
			private int getPasswordHash() {
				if (passwordField1.getPassword().length == 0 || passwordField2.getPassword().length == 0) {
					throw new NullPointerException("Password not entered.");
				}
				
				int passHash1 = Arrays.hashCode(passwordField1.getPassword());
				int passHash2 = Arrays.hashCode(passwordField2.getPassword());
				if (passHash1 != passHash2) {
					throw new IllegalArgumentException("Passwords do not match.");
				}
				
				return passHash1;
			}
		}
		
		PasswordPanel passwordPanel = new PasswordPanel();

		while (true) {
			try {
				int retVal = JOptionPane.showConfirmDialog(
						EncryptorPanel.this,
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
				showError(EncryptorPanel.this, "Please enter a password.");
				continue;
			} catch (IllegalArgumentException e) {
				showError(EncryptorPanel.this, "The entered passwords do not match.");
				continue;
			}
		}
	}

}
