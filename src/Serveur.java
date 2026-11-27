package src;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.net.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Scanner;

public class Serveur {


	//-----------------------//
	//fonctionnement generale//
	//-----------------------//


	private int    port;
	private String nom;
	private String adresse;
	private String psw;

	private boolean runing = true;

	private final Path   SAVEFILE   = Paths.get("saveFile.txt");
	private final String EMOJISPATH = "src/ascii/emojis";
	private String[][]   emojis;

	private ArrayList<Client> clients = new ArrayList<Client>();

	public Serveur (String nom, int port, String psw)	{
		this.nom  = nom;
		this.port = port;
		this.psw  = psw;

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
			System.err.println("can't start server\n" + e);
		}

		
		while (runing)
		{
			try {
				socket = serverSocket.accept();
				System.out.println(setColor("INFO: client " + socket.getInetAddress().getHostName() + " s'est connecté", 'w'));
			}
			catch (Exception e) {
				System.err.println("client can't connect to server\n" + e);
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

	public void recieveFromClient (String message, Client client) {
		//interdie les caratères spéciaux
		if (!message.chars().allMatch(c -> c > 31)) return;

		if (message.startsWith("/")) {
			message = lookupCommand(message, client);
		}
		else {
			message = lookupEmoji(message);
		}

		if (message.isEmpty()) return;

		message = setType(message, client);
		message = addTimeStamp(message);
		sendToAll(message);
	}

	private String setType (String message, Client client){
		//serveur
		if (client == null) {
			message = setColor("SERVEUR: " + message, 'g');
		}
		//localhost (admin)
		else if (client.getAdresse().equals("localhost")) {
			message = setColor(client.getAdresseNom() + ": " + message, 'r');
		}
		//client
		else {
			message = client.getAdresseNom() + ": " + message;
		}

		return message;
	}

	private String addTimeStamp (String message) {
		String timeStamp = new SimpleDateFormat("dd/MM/yy HH:mm ").format(Calendar.getInstance().getTime());
		return timeStamp + message;
	}

	private String setColor (String message, char color) {
        return switch (color) {
            case 'w' -> ("\u001B[97m" + message + "\u001B[0m");
            case 'g' -> ("\u001B[32m" + message + "\u001B[0m");
            case 'r' -> ("\u001B[31m" + message + "\u001B[0m");
            default -> message;
        };
	}

	private void sendToAll (String message) {
		sendToServeur(message);
		sendToAllClients(message);
	}

	private void sendToServeur(String message) {
		System.out.println(message);
		saveMessage(message + "\n");
	}

	private void sendToAllClients(String message) {
        for (Client client : clients) {
            client.toClient(message);
        }
	}

	private void saveMessage (String message) {
		try {
			Files.writeString(SAVEFILE, getHistoriqueChat() + message);
		}
		catch (Exception e) {
			System.out.println("can't save message to save file\n" + e);
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
		if (this.psw.equals(psw)) {
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

			emojis = new String [count/2][2];

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

	private String lookupEmoji (String message) {
		for (int i = 0; i < emojis.length; i++) {
			message = message.replace(emojis[i][0], emojis[i][1]);
		}
		return message;
	}


	//-----------------//
	//commandes de chat//
	//-----------------//


	//todo fair un fichier pour le /help
	private final String help =	"/c\t utilisateurs connectés\n" +
								"/q\t quitée le serveur\n" +
								"/db\t	Double la valeur\n" +
								"/bet\t let's go gambling!!!\n" +
								"/pet (animal)\t Affiche un animal\n";

	private long doubler = 1;

	private String lookupCommand (String message, Client client){
		//util
		if (message.equals("/help")) {
			message = "";
			client.toClient(help);
		}
		if (message.equals("/c")) {
			message = "";
			client.toClient(getClients());
		}
		if (message.equals("/emojis")) {
			message = "";
			client.toClient(stringEmojis());
		}

		//fun
		if (message.equals("/db"))
			message += "\n" + doubler();
		if (message.startsWith("/bet"))
			message += "\n" + (client.gamble(message));

		if (message.startsWith("/pet")) {
			if (message.length() > 5) {
			message = "\n" + pet(message.substring(5));
			}
			else {
				message += "\nje suis cense cherche quoi ?";
			}
		}

		return message;
	}

	private String getClients() {
		String toReturn = "Clients connectés :";

        for (Client client : this.clients) {
			toReturn += "\n" + client.getAdresseNom();
		}

		return toReturn;
	}

	private String pet(String pet) {
		try {
			return Files.readString(Paths.get("src/ascii/pet/" + pet));
        }catch (Exception e) {
			return "imposible de trouver " + pet;
		}
	}

	private String doubler() {
		return String.format("%d",this.doubler *= 2);
	}

	private String stringEmojis () {
		try {
			return Files.readString(Paths.get(EMOJISPATH));
        }
		catch (Exception e) {
			return ("imposible de trouver le dossier emoji");
		}
	}

	public void deconnecte (Client client) {
		clients.remove(client);
	}


	static void main(String[] args) {
		try {
			new Serveur(args[0], Integer.parseInt(args[1]), args[2]);
		}
		catch (Exception e) {
			System.err.println("Server nom port psw\n" + Arrays.toString(e.getStackTrace()));
		}
	}
}
