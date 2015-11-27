import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.SwingConstants;

import java.io.*;

import java.net.*;

/**
 * A chat client to talk with other people.
 *
 * @author Patrick Liu
 * @version November 25, 2015
 */
public class ChatClient {
	private JFrame frame;
	private JScrollPane chatArea;
	private JTextArea chatTextArea;
	private JPanel messageBar;
	private JTextField messageField;
	private JButton sendButton;

	private String ipAddress = "10.242.181.61";
	private int port = 5000;
	private JTextField ipAddressField;
	private JTextField portField;

	private JTextField nameField;

	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;

	private String username = "Patrick";
	private String msg;

	public ChatClient() {
	} // ChatClient constructor

	public void go() {
		frame = new JFrame("Chat");

		try {
			UIManager
			.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
		}


		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 800);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);

		chatTextArea = new JTextArea();
		chatTextArea.setEditable(false);
		chatTextArea.setLineWrap(true);
		chatTextArea.setWrapStyleWord(true);
		chatArea = new JScrollPane(chatTextArea);

		frame.add(chatArea, BorderLayout.CENTER);

		messageField = new JTextField(30);
		sendButton = new JButton("Send");

		ActionListener messageListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == messageField || e.getSource() == sendButton) {
					msg = messageField.getText().trim();

					if (msg.length() != 0) {
						if (msg.charAt(0) != '/') {
							output.println(username + ": " + msg + "\n");
							output.flush();
							// chatTextArea.append(username + ": " + msg + "\n");
							messageField.setText("");
						} else {
							// handle commands
						}
						// don't send anything
						messageField.setText("");
					}
				}
			}
		};

		messageField.addActionListener(messageListener);
		sendButton.addActionListener(messageListener);

		messageBar = new JPanel(new FlowLayout());
		messageBar.add(messageField);
		messageBar.add(sendButton);

		frame.add(messageBar, BorderLayout.SOUTH);

		frame.setVisible(true);

		changeServer();

		chatTextArea.append("Attempting to connect to the server...\n");

		boolean connected = false;

		if (!connected) {
			try {
				socket = new Socket(ipAddress, port);
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new PrintWriter(new PrintWriter(socket.getOutputStream()));
				connected = true;
			} catch (Exception e) {
				chatTextArea.append("Connection to the server failed.\n");
				// e.printStackTrace();
				changeServer();
			}
		}

		chatTextArea.append("Connected to the server.\n");

		chooseUsername();

		output.println("\\username " + username);

		while (connected) {
			try {
				msg = input.readLine();
				chatTextArea.append(msg);
			} catch (IOException e) {
				chatTextArea.append("Failed to receive message from the server.");
				// e.printStackTrace();
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

	private boolean isValidPort(String str) {
		int length = str.length();

		if (length > 5 || length == 0) {
			return false;
		}

		for (int ch = 0; ch < length; ch++) {
			if (!Character.isDigit(str.charAt(ch))) {
				return false;
			}
		}

		return true;
	} // isValidPort method

	private void changeServer() {
		boolean portBoolean = false;
		ipAddressField = new JTextField(ipAddress);
		portField = new JTextField(Integer.toString(port));

		Object[] connectObjects = { "IP Address:", ipAddressField, "Port:", portField };

		while (!portBoolean) {
			JOptionPane.showConfirmDialog(frame, connectObjects, "Choose a server", JOptionPane.OK_CANCEL_OPTION);

			String currentIpAddress = ipAddressField.getText();
			String currentPort = portField.getText();

			if (currentIpAddress.length() != 0) {
				if (isValidPort(currentPort)) {
					ipAddress = currentIpAddress;
					port = Integer.parseInt(currentPort);
					portBoolean = true;
				} else {
					JOptionPane.showMessageDialog(frame, "Please enter a valid port.", "Invalid port", JOptionPane.WARNING_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(frame, "Please enter a valid IP address.", "Invalid IP address", JOptionPane.WARNING_MESSAGE);
			}
		}
	} // changeServer method

	private void chooseUsername() {
		boolean nameBoolean = false;

		nameField = new JTextField();

		Object[] nameObjects = { "Username", nameField };

		while (!nameBoolean) {
			JOptionPane.showConfirmDialog(frame, nameObjects, "Choose a username", JOptionPane.DEFAULT_OPTION);

			if (!nameField.getText().trim().equals("")) {
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