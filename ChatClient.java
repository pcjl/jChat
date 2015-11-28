import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.net.*;

import javax.swing.*;
import javax.swing.SwingConstants;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.text.StyledDocument;

/**
 * A chat client to talk with other people.
 *
 * @author Patrick Liu
 * @version November 25, 2015
 */
public class ChatClient {
	private JFrame frame;

	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem newConnection;
	private JMenuItem exitProgram;
	private JMenu helpMenu;
	private JMenuItem aboutProgram;

	private JScrollPane chatArea;
	private JTextPane chatTextArea;
	private JPanel messageBar;
	private JTextField messageField;
	private JButton sendButton;

	private StyledDocument doc;

	private String ipAddress = "127.0.0.1";
	private int port = 5000;

	private String username;
	private String msg;

	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;

	/**
	 * Constructs a new ChatClient.
	 */
	public ChatClient() {
	} // ChatClient constructor

	/**
	 * Runs the chat client.
	 */
	public void go() {
		// Create the frame of the main window
		frame = new JFrame("Chat");

		// Set the look and feel of the program to Windows
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
		}

		// Set basic properties of the frame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(560, 400);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);

		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		helpMenu = new JMenu ("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);

		ActionListener menuListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == newConnection) {
					changeServer(true);
				}

				if (e.getSource() == exitProgram) {
					try {
						input.close();
						output.close();
						socket.close();
					} catch (Exception exception) {
						System.out.println("Failed to close socket.");
					}
					System.exit(0);
				}

				if (e.getSource() == aboutProgram) {
					JOptionPane.showMessageDialog(frame, "Chat program designed and created by Patrick Liu.");
				}
			}
		};

		newConnection = new JMenuItem("New Connection", KeyEvent.VK_N);
		newConnection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));

		exitProgram = new JMenuItem("Exit Program", KeyEvent.VK_X);

		newConnection.addActionListener(menuListener);
		exitProgram.addActionListener(menuListener);

		fileMenu.add(newConnection);
		fileMenu.addSeparator();
		fileMenu.add(exitProgram);

		aboutProgram = new JMenuItem("About Program", KeyEvent.VK_B);

		aboutProgram.addActionListener(menuListener);

		helpMenu.add(aboutProgram);

		menuBar.add(fileMenu);
		menuBar.add(helpMenu);

		frame.setJMenuBar(menuBar);

		// Create a text area that will show the chat history
		chatTextArea = new JTextPane();
		doc = (StyledDocument) chatTextArea.getDocument();
		chatTextArea.setEditable(false);
		chatArea = new JScrollPane(chatTextArea);

		// Add the chat area to the center of the frame
		frame.add(chatArea, BorderLayout.CENTER);

		// Create a text field and button for sending messages
		messageField = new JTextField(30);
		sendButton = new JButton("Send");

		// Create an action listener that will allow sending messages to the server
		ActionListener messageListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Send a message whenever the send button is pressed
				// or when enter is pressed while on the text field
				if (e.getSource() == messageField || e.getSource() == sendButton) {
					// Remove the spaces from the ends of the string
					msg = messageField.getText().trim();

					// Make sure the message is not empty
					if (msg.length() != 0) {
						if (msg.charAt(0) != '/') {
							// Send the username and message to the server
							output.println(username + ": " + msg);
							output.flush();

							// Clear the text field for the message
							messageField.setText("");
						} else {
							// Handle commands
							output.println(username + ": " + msg);
							output.flush();

							// Clear the text field for the message
							messageField.setText("");
						}

						// Clear the text field for the message
						messageField.setText("");
					}
				}
			}
		};

		// Add the action listenre to the text field and button
		messageField.addActionListener(messageListener);
		sendButton.addActionListener(messageListener);

		// Put the text field and button a horizontal panel and add it to the frame
		messageBar = new JPanel(new FlowLayout());
		messageBar.add(messageField);
		messageBar.add(sendButton);
		frame.add(messageBar, BorderLayout.SOUTH);

		// Make the frame visible to the user
		frame.setVisible(true);

		// Make the user choose a server
		changeServer(false);

		try {
			doc.insertString(doc.getLength(), "Attempting to connect to the server...\n", null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		boolean connected = false;

		// While the client is not connected to a server, continue trying to connect to a new server
		if (!connected) {
			try {
				// Connect to the server and set up an input and output stream
				socket = new Socket(ipAddress, port);
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new PrintWriter(socket.getOutputStream());
				connected = true;
			} catch (Exception ex) {
				try {
					doc.insertString(doc.getLength(), "Connection to the server failed.\n", null);
				} catch (Exception e) {
					ex.printStackTrace();
				}
				changeServer(false);
				connected = false;
			}
		}

		try {
			doc.insertString(doc.getLength(), "Connected to the server.\n", null);
		} catch (Exception ex){
			ex.printStackTrace();
		}

		// Make the user choose a username
		chooseUsername();

		// Tell the server the username of the client
		output.println(username);
		output.flush();

		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

		// Keep trying to get a message from the server while the client is connected to the server
		while (connected) {
			try {
				if (input.ready()) {
					msg = input.readLine();
					Date date = new Date();
					try {
						doc.insertString(doc.getLength(), "[" + dateFormat.format(date) + "] " + msg + "\n", null);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			} catch (Exception ex) {
				try {
					doc.insertString(doc.getLength(), "Failed to receive message from the server.", null);
				} catch (Exception e) {
					ex.printStackTrace();
				}
			}
		}

		// try {
		// 	input.close();
		// 	output.close();
		// 	socket.close();
		// } catch (Exception e) {
		// 	System.out.println("Failed to close socket.");
		// }
	} // go method


	/**
	 * Check if a string is a valid port number.
	 *
	 * @param str
	 *            the string to check.
	 * @return whether or not the string is a valid port number.
	 */
	private boolean isValidPort(String str) {
		// Store the length of the string in order to check its validity
		int length = str.length();

		// Make sure the length is between 1 and 5
		if (length > 5 || length == 0) {
			return false;
		}

		// Make sure each character is a digit
		for (int ch = 0; ch < length; ch++) {
			if (!Character.isDigit(str.charAt(ch))) {
				return false;
			}
		}

		// If the string passed all of the tests, it is valid
		return true;
	} // isValidPort method

	/**
	 * Allows the user to choose a server.
	 */
	private void changeServer(boolean alreadyConnected) {
		// Keep track on whether a valid server has been chosen
		boolean serverBoolean = false;

		// Create the components of the window
		JTextField ipAddressField = new JTextField(ipAddress);
		JTextField portField = new JTextField(Integer.toString(port));
		Object[] connectObjects = { "IP Address:", ipAddressField, "Port:", portField };

		// Keep creating a window asking for a server
		// until a valid server is chosen
		while (!serverBoolean) {
			if (alreadyConnected) {
				JOptionPane.showConfirmDialog(frame, connectObjects, "Choose a server", JOptionPane.OK_CANCEL_OPTION);
			} else {
				JOptionPane.showConfirmDialog(frame, connectObjects, "Choose a server", JOptionPane.DEFAULT_OPTION);
			}
			// Get the information that the user entered
			String currentIpAddress = ipAddressField.getText();
			String currentPort = portField.getText();

			// Make sure an IP address was entered
			if (currentIpAddress.length() != 0) {
				// Make sure the port was valid
				if (isValidPort(currentPort)) {
					// Store the IP address and port and exit out of the while loop
					ipAddress = currentIpAddress;
					port = Integer.parseInt(currentPort);
					serverBoolean = true;
				} else {
					JOptionPane.showMessageDialog(frame, "Please enter a valid port.", "Invalid port", JOptionPane.WARNING_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(frame, "Please enter a valid IP address.", "Invalid IP address", JOptionPane.WARNING_MESSAGE);
			}
		}
	} // changeServer method

	/**
	 * Allows the user to choose a username.
	 */
	private void chooseUsername() {
		// Keep track on whether a valid name has been chosen
		boolean nameBoolean = false;

		// Create the components of the window
		JTextField nameField = new JTextField();
		Object[] nameObjects = { "Username", nameField };

		// Keep creating a window asking for a username
		// until a valid name is chosen
		while (!nameBoolean) {
			JOptionPane.showConfirmDialog(frame, nameObjects, "Choose a username", JOptionPane.DEFAULT_OPTION);

			// Check if the username is not blank
			if (!nameField.getText().trim().equals("")) {
				// Store the username and exit out of the while loop
				username = nameField.getText();
				nameBoolean = true;
			} else {
				JOptionPane.showMessageDialog(frame, "Please enter a username.", "Invalid username", JOptionPane.WARNING_MESSAGE);
			}
		}
	} // chooseUsername method

	public static void main(String[] args) {
		ChatClient cc = new ChatClient();
		cc.go();
	} // main method
} // ChatClient class