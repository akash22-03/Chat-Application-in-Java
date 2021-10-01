/*_____PROBLEM STATEMENT_____
It is an implementation of multi-threaded server. It accepts connections from an arbitrary number of
clients; any message sent from one client is broadcast to all clients. In addition to ServerSockets, it
demonstrates the use of threads. Because there are interactions among clients, this server needs to keep
track of all the clients it has at any one time. */

//Name:- AKASH DASH
//Regd. No:-19410121034
// CSE 'P'
//Date of editing:-09/02/2021

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

class TextArea extends OutputStream 
{
	JTextArea txtarea;
	StringBuilder sb = new StringBuilder();
	public TextArea(JTextArea txtarea) 
	{
		this.txtarea = txtarea;
	}
	public void write(int b) throws IOException 
	{
		if (b == '\r') 
		{
			return;
		}
		if (b == '\n') 
		{
			String text = sb.toString() + "\n";
			txtarea.append(text);
			sb.setLength(0);
		} 
		else 
		{
			sb.append((char) b);
		}
	}
}

public class Server extends JFrame 
{
	private static final long serialVersionUID = 1L;
	public static SimpleDateFormat sdf = new SimpleDateFormat("<hh:mm a->");
	static HashMap<String, PrintWriter> clientsmap = new HashMap<>();
	static int port = 5792;
	static ServerSocket serverSocket;
	static boolean exit = false;
	JPanel jPanel, jPanel1;
	JTextArea txtarea;
	JButton startbutton, responsebutton, onlinebutton;

	public Server() 
	{
		super("Messenger");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 400);
		jPanel = new JPanel();
		jPanel1 = new JPanel();
		setContentPane(jPanel);
		jPanel.setLayout(new BorderLayout(0, 0));
		JScrollPane scrollPane = new JScrollPane();
		jPanel.add(scrollPane);
		txtarea = new JTextArea();
		txtarea.setLineWrap(true);
		scrollPane.setViewportView(txtarea);
		jPanel1.setLayout(new FlowLayout());
		startbutton = new JButton("START");
		startbutton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
			{
				if (startbutton.getText().equals("START")) 
				{
					exit = false;
					start();
					startbutton.setText("STOP");
				}
				else 
				{
					System.out.printf("%s %s\n", sdf.format(new Date()), "Chat server stopped");
					exit = true;
					startbutton.setText("START");
				}
			}
		});
		responsebutton = new JButton("RESPONSE");
		responsebutton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				JOptionPane.showMessageDialog(null, "Client Response UI Started");
				Client.main(null);
			}
		});
		
		onlinebutton = new JButton("ONLINE");
		onlinebutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Clients Connected " + clientsmap.size());
			}
		});
		jPanel1.add(startbutton);
		jPanel1.add(responsebutton);
		jPanel1.add(onlinebutton);
		jPanel.add(jPanel1, BorderLayout.SOUTH);
	}

	public static void start() 
	{
		new Thread(new ServerHandler()).start();
	}

	public static void stop() throws IOException 
	{
		if (!serverSocket.isClosed())
			serverSocket.close();
	}

	private static void message(String message) 
	{
		for (PrintWriter p : clientsmap.values()) 
		{
			p.println(message);
		}
	}

	private static class ServerHandler implements Runnable {
		@Override
		public void run() {
			try {
				serverSocket = new ServerSocket(port);
				System.out.printf("%s %s\n", sdf.format(new Date()), "Server started on port: " + port);
				System.out.printf("%s %s\n", sdf.format(new Date()), "Listening for clients");
				while (!exit) {
					if (clientsmap.size() <= 100) {
						new Thread(new ClientHandler(serverSocket.accept())).start();
					}
				}
			} catch (Exception e) {
				System.out.printf("%s %s\n", sdf.format(new Date()), "\nError countered \n");
				System.out.printf("%s %s\n", sdf.format(new Date()), Arrays.toString(e.getStackTrace()));
				System.out.printf("%s %s\n", sdf.format(new Date()), "\nExiting");
			}
		}
	}

	private static class ClientHandler implements Runnable 
	{
		private Socket socket;
		private PrintWriter out;
		private BufferedReader br;
		private String name;

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			System.out.printf("%s %s\n", sdf.format(new Date()), "Client connected: " + socket.getInetAddress());
			try {
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				for (;;) {
					name = br.readLine();
					if (name == null) {
						return;
					}
					synchronized (clientsmap) {
						if (!name.isEmpty() && !clientsmap.keySet().contains(name))
							break;
						else
							out.println("INVALIDNAME");
					}
				}
				out.println("Welcome to the chat, " + name.toUpperCase());
				System.out.printf("%s %s\n", sdf.format(new Date()), name.toUpperCase() + " joined.");
				message("<Server-> " + name.toUpperCase() + " joined.");
				clientsmap.put(name, out);
				String message;
				out.println("Joined to chat");
				while ((message = br.readLine()) != null && !exit) {
					if (!message.isEmpty()) {
						if (message.toLowerCase().equals("/quit"))
							break;
						message(String.format("<%s-> %s", name, message));
					}
				}
			} catch (Exception e) {
				System.out.printf("%s %s\n", sdf.format(new Date()), e.getMessage());
			} finally {
				if (name != null) {
					clientsmap.remove(name);
					message(name + " left");
				}
			}
		}
	}
	public static void main(String[] args) 
	{
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					Server server = new Server();
					System.setOut(new PrintStream(new TextArea(server.txtarea)));
					server.setVisible(true);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}
}