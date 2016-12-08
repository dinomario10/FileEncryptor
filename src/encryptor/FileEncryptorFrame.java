package encryptor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class FileEncryptorFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private EncryptorPanel encryptorPanel = new EncryptorPanel();
	private DecryptorPanel decryptorPanel = new DecryptorPanel();
	
	/** A simple file chooser */
	public static JFileChooser chooser = new JFileChooser();
	
	/**
	 * Constructs and initializes this window with GUI components.
	 */
	public FileEncryptorFrame() {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("File Encryptor");
		setSize(640, 480);
		setMinimumSize(new Dimension(405, 320));
		
		initGUI();
		
		setLocationRelativeTo(null);
	}

	/**
	 * A helper GUI initializing method.
	 */
	private void initGUI() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		
		JPanel top = new JPanel(new GridLayout(0, 1));
		cp.add(top, BorderLayout.PAGE_START);
		
		top.add(createMenuBar());

		JTabbedPane tabs = new JTabbedPane();
		tabs.add("Encryptor", encryptorPanel);
		tabs.add("Decryptor", decryptorPanel);
		tabs.setSelectedComponent(encryptorPanel);
		
		cp.add(tabs);
	}
	
	/**
	 * Creates and returns the menu bar to be added to the GUI.
	 * 
	 * @return the menu bar to be added to the GUI
	 */
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		JMenu helpMenu = new JMenu("Help");
		
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		
		fileMenu.setMnemonic(KeyEvent.VK_F);
		helpMenu.setMnemonic(KeyEvent.VK_H);

		JMenuItem exitItem =
				createMenuItem(fileMenu, "Exit", KeyEvent.VK_X, "ctrl X", "Exits the program");
		exitItem.addActionListener((e) -> {
			this.dispose();
			System.exit(0);
		});
		
		JMenuItem aboutItem =
				createMenuItem(helpMenu, "About File Encryptor", -1, null, "Shows information about this program");
		aboutItem.addActionListener((e) -> {
			showHelp();
		});
		
		return menuBar;
	}
	
	/**
	 * Creates a returns menu item based on the given parameters. The parent
	 * parameter and text must exist, while other parameters may be null or -1.
	 * 
	 * @param parent the parent to which this item will be added
	 * @param text text for this menu item
	 * @param mnemonic mnemonic for this menu item
	 * @param keyStrokeText shortcut key combination for this menu item
	 * @param tooltip tooltip message to be displayed for this menu item
	 * @return menu item based on the given parameters
	 */
	private JMenuItem createMenuItem(JMenu parent, String text, int mnemonic, String keyStrokeText, String tooltip) {
		JMenuItem item = new JMenuItem();
		
		item.setText(text);
		if (mnemonic != -1) {
			item.setMnemonic(mnemonic);
		}
		if (keyStrokeText != null) {
			KeyStroke accelerator = KeyStroke.getKeyStroke(keyStrokeText);
			item.setAccelerator(accelerator);
		}
		if (tooltip != null) {
			item.setToolTipText(tooltip);
		}
		
		parent.add(item);
		return item;
	}
	
	/**
	 * Shows the help dialog when the user clicks on the help menu item.
	 */
	private void showHelp() {
		JOptionPane.showMessageDialog(this, "Created by Mario Bobić", "About File Encryptor", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void main(String[] args) {
		String syslaf = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(syslaf);
		} catch (Exception e) {
			// Leave the standard look and feel
			System.err.println("Can not set look and feel to " + syslaf);
			System.err.println("Falling back to the standard look and feel.");
		}

		SwingUtilities.invokeLater(() -> {
			new FileEncryptorFrame().setVisible(true);
		});
	}

}
