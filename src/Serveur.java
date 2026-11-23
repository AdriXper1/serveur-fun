package src;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.net.*;

public class Serveur {

	//-----------------------//
	//fonctionnement generale//
	//-----------------------//


	private int    port;
	private String nom;
	private String adresse;

	private boolean runing = true;

	private final String ADMINPSW = "psw";
	private final Path SAVEFILE = Paths.get("saveFile.txt");

	private ArrayList<Client> clients = new ArrayList<Client>();

	public Serveur (int port, String nom)
	{
		this.port = port;
		this.nom = nom;

		ServerSocket serverSocket = null;
		Socket       socket       = null;

		System.out.println("Démarrage du serveur " + nom);

		try {
			serverSocket = new ServerSocket(port);
			adresse = InetAddress.getLocalHost().getHostName();

			System.out.println("serveur démarré avec succès");
			System.out.printf("machine: %s\nport   : %d%n", adresse, port);
		}
		catch(Exception e) {
			System.err.println(e);
		}

		
		while (runing)
		{
			try {
				socket = serverSocket.accept();
				System.out.println("\u001B[97mINFO: client " + socket.getInetAddress().getHostName() + " s'est connecté\u001B[0m");
			}
			catch (Exception e) {System.err.println(e);}

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

	public void getNewMessage (String message, Client client) {
		//interdie les caratères spéciaux
		if (!message.chars().allMatch(c -> c > 31)) return;

		String cleanMessage = clean(message, client);

		sendNewMessage(cleanMessage);
	}

	public String clean (String message, Client client) {
		message = lookupCommand(message, client);

		if (client == null) message = "SERVEUR: " + message;
		else                message = (client.getAdresseNom() + ": " + message);

		message = message.replaceAll("^\\(localhost\\)", "\u001B[31m(localhost)")
		                 .replaceAll("^SERVEUR", "\u001B[32mSERVEUR");

		message = message += "\u001B[0m";

		return message;
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


	//-----------------//
	//commandes de chat//
	//-----------------//


	private final String help =	"/c\t	Utilisateurs connectés\n" +
								"/pig\t	Affiche un cochon\n" +
								"/db\t	Double la valeur\n" +
								"/help\t	Affiche cette aide\n\n" +
								"Rester simpat\n" +
								"Bonjour, merci, s'il vous plaît, et au revoir.\n" +
								"Restez respectueux envers les autres utilisateurs.";

	private long doubler = 1;

	public String lookupCommand (String message, Client client) {
		if (message.equals("/c"))
			message += "\u001B[32m\n" + getClients() + "\u001B[0m";
		if (message.equals("/pig"))
			message += "\u001B[32m\n" + pig() + "\u001B[0m";
		if (message.equals("/db"))
			message += "\u001B[32m\n" + doubler() + "\u001B[0m";
		if (message.equals("/help"))
			message += "\u001B[32m\n" + this.help + "\u001B[0m";

		return message;
	}

	public String getClients() {
		String toReturn = "Clients connectés :\n";

        for (Client client : this.clients) {
			toReturn += client.getAdresseNom() + "\n";
		}

		return toReturn;
	}

	public String pig() {
		return("pig\n    _____  \n^..^     \\9\n(oo)_____/\n    WW  WW");
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
