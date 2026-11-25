package src;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.net.*;
import java.util.Calendar;
import java.util.Scanner;

public class Serveur {

	//-----------------------//
	//fonctionnement generale//
	//-----------------------//


	private int    port;
	private String nom;
	private String adresse;

	private boolean runing = true;

	private final String ADMINPSW   = "psw";
	private final String EMOJISPATH = "src/ascii/emojis";
	private final Path   SAVEFILE   = Paths.get("saveFile.txt");

	private String[][] emojis;

	private ArrayList<Client> clients = new ArrayList<Client>();

	public Serveur (int port, String nom)
	{
		this.port = port;
		this.nom = nom;

		ServerSocket serverSocket = null;
		Socket       socket       = null;

		System.out.println("Démarrage du serveur " + nom);
		setupEmojis();

		try {
			serverSocket = new ServerSocket(port);
			adresse = InetAddress.getLocalHost().getHostName();

			System.out.println("serveur démarré avec succès");
			System.out.printf("machine: %s\nport   : %d%n", adresse, port);
		}
		catch(Exception e) {
			System.err.println("can't start serveur\n" + e);
		}

		
		while (runing)
		{
			try {
				socket = serverSocket.accept();
				System.out.println(setColor("INFO: client " + socket.getInetAddress().getHostName() + " s'est connecté", 'w'));
			}
			catch (Exception e) {
				System.err.println("client can't connect to serveur error\n" + e);
			}

			Client newClient = new Client(this, socket);

			newClient.start();
			clients.add(newClient);
		}

		try {
			serverSocket.close();
        }
		catch (Exception e) {
			System.err.println(e);
		}
	}

	public void getNewMessage (String message, Client client){
		//interdie les caratères spéciaux
		if (!message.chars().allMatch(c -> c > 31) || message.isEmpty()) return;

		String cleanMessage = clean(message, client);

		sendNewMessage(cleanMessage);
	}

	public String clean (String message, Client client){
		if (message.startsWith("/")) {
			message = lookupCommand(message, client);
		}
		else {
			message = lookupEmoji(message);
		}

		if (client == null) {
			message = setColor("SERVEUR: " + message, 'g');
		}
		else if (client.getAdresse().equals("localhost")) {
			message = setColor(client.getAdresseNom() + ": " + message, 'r');
		}
		else {
			message = client.getAdresseNom() + ": " + message;
		}

		message = addTimeStamp(message);

		return message;
	}

	private String addTimeStamp (String message) {
		String timeStamp = new SimpleDateFormat("dd/MM/yy HH:mm ").format(Calendar.getInstance().getTime());
		return timeStamp + message;
	}

	public String setColor(String message, char color) {
		switch (color) {
			case 'w' : return ("\u001B[97m" + message + "\u001B[0m");
			case 'g' : return ("\u001B[32m" + message + "\u001B[0m");
			case 'r' : return ("\u001B[31m" + message + "\u001B[0m");
            default  : return message;
		}
	}

	public void sendNewMessage (String message) {
		System.out.println(message);
		saveMessage("\n" + message);
		toAllClient(message);
	}

	public void toServeur (String message) {
		System.out.println(setColor(message, 'w'));
	}

	public void toAllClient (String message) {
        for (Client client : clients) {
            client.toClient(message);
        }
	}

	public void saveMessage (String message) {
		try {
			Files.writeString(SAVEFILE, getHistoriqueChat() + message);
		}
		catch (IOException exception) {
			System.out.println("can't save message to save file");
		}
	}

	public int getPort() {
		return this.port;
	}

	public String getNom() {
		return this.nom;
	}

	public String getHistoriqueChat() {
		try {
			return Files.readString(SAVEFILE);
        }
		catch (IOException exception) {
			return "Imposible de charger les messages precedants";
		}
	}

	public boolean checkPassworld (String psw) {
		if (this.ADMINPSW.equals(psw)) {
			return true;
		}
		return false;
	}

	private void setupEmojis () {
		try {
			Scanner lineCounter = new Scanner(new File(EMOJISPATH));

			int count = 0;
			while (lineCounter.hasNextLine()) {
				count++;
				lineCounter.nextLine();
			}

			emojis = new String [count][2];

			Scanner scanner = new Scanner(new File(EMOJISPATH));

			int i = 0;
			while (scanner.hasNextLine()) {
				emojis[i][0] = scanner.nextLine();
				emojis[i][1] = scanner.nextLine();
				i++;
			}
		}
		catch (Exception e) {
			System.err.println("can load emojis file\n" + e);
		}
	}

	public String lookupEmoji (String message) {
		for (int i = 0; i < emojis.length; i++) {
			message.replace(emojis[i][0], emojis[i][1]);
		}
		return message;
	}


	//-----------------//
	//commandes de chat//
	//-----------------//

	//todo fair un fichier pour le /help
	private final String help =	"/c\t	Utilisateurs connectés\n" +
								"/pig\t	Affiche un cochon\n" +
								"/db\t	Double la valeur\n" +
								"/help\t	Affiche cette aide\n" +
								"/pet (animal)\t	Affiche un animal\n\n" +
								"Rester simpat\n" +
								"Bonjour, merci, s'il vous plaît, et au revoir.\n" +
								"Restez respectueux envers les autres utilisateurs.";

	private long doubler = 1;

	public String lookupCommand (String message, Client client){
		if (message.equals("/c"))
			message += "\n" + getClients();
		if (message.equals("/db"))
			message += "\n" + doubler();
		if (message.equals("/help"))
			message += "\n" + help;

		if (message.startsWith("/pet")) {
			if (message.length() > 5) {
			message = "\n" + pet(message.substring(5));
			}
			else {
				message += "\nje suis cense cherche quoi ?";
			}
		}

		setColor(message, 'g');

		return message;
	}

	public String getClients() {
		String toReturn = "Clients connectés :\n";

        for (Client client : this.clients) {
			toReturn += client.getAdresseNom() + "\n";
		}

		return toReturn;
	}

	public String pet(String pet) {
		try {
			return Files.readString(Paths.get("src/ascii/pet/" + pet));
        }catch (Exception e) {
			return "imposible de trouver " + pet;
		}
	}

	public String doubler() {
		return String.format("%d",this.doubler *= 2);
	}

	public void deconnecte (Client client) {
		clients.remove(client);
	}


	static void main(String[] args) {
		new Serveur(9002, "chat IUT info");
	}
}
