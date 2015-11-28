import java.io.*;
import java.net.*;

import java.util.Scanner;
import java.util.ArrayList;

/**
 * A chat server to allow people to talk to each other.
 *
 * @author Patrick Liu
 * @version November 26, 2015
 */
public class Server {
	ServerSocket serverSocket;
	boolean running = true;
	int noOfClients = 0;
	ArrayList<PrintWriter> outputs = new ArrayList<PrintWriter>();

	/**
	 * Constructs a new Server.
	 */
	public Server() {
	} // Server constructor

	/**
	 * Runs the chat server.
	 */
	public void go() {
		Scanner keyboard = new Scanner(System.in);

		System.out.print("Please enter a port: ");
		String port = keyboard.nextLine();

		while (!isValidPort(port)) {
			System.out.print("Please enter a valid port: ");
			port = keyboard.nextLine();
		}
		System.out.println("Server started.");
		System.out.println("Waiting for connections...");

		try {
			serverSocket = new ServerSocket(Integer.parseInt(port));

			while (running) {
				Socket client = serverSocket.accept();
				System.out.println("Client (" + client.getLocalAddress().getHostAddress() + ") is connecting...");
				noOfClients++;
				Thread t = new Thread(new ConnectionHandler(client));
				t.start();
			}
		} catch (Exception e) {
			System.out.println("Error accepting connection");
			e.printStackTrace();
		}
	} // go method

	class ConnectionHandler implements Runnable {
		BufferedReader input;
		PrintWriter output;
		Socket client;
		String username;

		ConnectionHandler(Socket s) {
			this.client = s;
		} // ConnectionHandler constructor

		public void run() {
			try {
				input = new BufferedReader(new InputStreamReader(client.getInputStream()));
				output = new PrintWriter(client.getOutputStream());
				outputs.add(output);
			} catch (IOException e) {
				System.out.println("Failed to set up input and output streams.");
			}

			try {
				username = input.readLine();
			} catch (IOException e) {
				System.out.println("Failed to receive username from client.");
			}

			System.out.println("\"" + username + "\" (" + client.getLocalAddress().getHostAddress() + ") has connected.");

			output.println("Hello " + username + "!");
			output.flush();
			output.println("There are currently " + noOfClients + " client(s) connected.");
			output.flush();

			String msg;

			while (true) {
				try {
					if (input.ready()) {
						msg = input.readLine();
						System.out.println(msg);


						if (msg.charAt(username.length() + 2) != '/') {
							for (int client = 0; client < outputs.size(); client++) {
								outputs.get(client).println(msg);
								outputs.get(client).flush();
							}
						} else {
							// Handle commands
						}
					}
				} catch
					(IOException e) {
					System.out.println("Failed to receive message from client.");
				}
			}


			// try {
			// 	input.close();
			// 	output.close();
			// 	client.close();
			// } catch (Exception e) {
			// 	System.out.println("Failed to close socket.");
			// }

		} // run method
	} // ConnectionHandler class

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

	public static void main(String[] args) {
		Server cs = new Server();
		cs.go();
	} // main method
} // Server