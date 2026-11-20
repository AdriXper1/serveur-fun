import java.util.ArrayList;
import java.net.*;

public class Serveur {

	/** le port auquel le client va se connecter */
	private int port;
	/** le nom du serveur */
	private String nom;
	/** l'adresse du serveur */
	private String adresse;

	/** tous les messages envoyés depuis le démarrage */
	private String chat = "";

	/** tableau contenant les clients connectés */
	private ArrayList<Client>clients = new ArrayList<Client>();

	/** double pour doubler */
	private long doubler = 1;

	/** commande /help */
	private String help =	"/c\t	Utilisateurs connectés\n" +
							"/pig\t	Affiche un cochon\n" +
							"/db\t	Double la valeur\n" +
							"/help\t	Affiche cette aide\n\n" +
							"Veuillez respecter les règles de politesse :\n" +
							"Bonjour, merci, s'il vous plaît, et au revoir.\n" +
							"Restez respectueux envers les autres utilisateurs.";

	/** Démarrage du serveur avec le port et le nom */
	public Serveur (int port, String nom)
	{
		this.port = port;
		this.nom = nom;

		ServerSocket serverSocket = null;
		Socket       socket       = null;

		System.out.println("Démarrage du serveur " + nom);

		try
		{
			serverSocket = new ServerSocket(port);
			adresse = InetAddress.getLocalHost().getHostName();

			System.out.println("serveur démarré avec succès");
			System.out.println(String.format("machine: %s\nport   : %d", adresse, port));
		}
		catch(Exception e) {System.err.println(e);}

		
		while (true)
		{
			try
			{
				socket = serverSocket.accept();
				System.out.println("\u001B[97mINFO: client " + socket.getInetAddress().getHostName() + " s'est connecté\u001B[0m");
			}
			catch (Exception e) {System.err.println(e);}

			Client newClient = new Client(this, socket);

			newClient.start();
			clients.add(newClient);
		}
	}

	/** Reçoit un message pour le renvoyer à tous les clients proprement */
	public void getNewMessage(String message, Client client)
	{
		//interdie les caratères spéciaux
		if (!message.chars().allMatch(c -> c > 31)) return;

		String cleanMessage = clean(message, client);

		sendNewMessage(cleanMessage);
	}

	/** Envoie directement un message à tous les clients et au serveur */
	public void sendNewMessage(String message)
	{
		System.out.println(message);
		chat += "\n" + message;
		toAllClient(message);
	}

	/** change les couleurs et ajout un surnom*/
	public String clean(String message, Client client)
	{
		message = lookupCommand(message, client);

		if (client == null) message = "SERVEUR: " + message;
		else                message = (client.getAdresseNom() + ": " + message);

		message = message.replaceAll("^\\(localhost\\)", "\u001B[31m(localhost)")
		                 .replaceAll("^SERVEUR", "\u001B[32mSERVEUR");
		
		message = message += "\u001B[0m";
		
		return message;
	}
	
	/** Envoie un message au serveur */
	public void toServeur(String message)
	{
		System.out.println("\u001B[97mINFO: " + message + "\u001B[0m\"");
	}

	/** Envoie un message à tous les clients connectés */
	public void toAllClient(String message)
	{
		for (int cpt = 0; cpt < clients.size(); cpt++)
		{
			clients.get(cpt).toClient(message);
		}
	}
	
	/** Donne le port du serveur */
	public int getPort()
	{
		return this.port;
	}

	/** Donne le nom du serveur */
	public String getNom()
	{
		return this.nom;
	}

	/** Renvoie l'historique du chat complet */
	public String getHistoriqueChat()
	{
		return this.chat;
	}

	/** Envoie le message aux classes extérieures et vérifie si le message est une commande. Si oui, l'exécute */
	public String lookupCommand(String message, Client client)
	{
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

	/** Renvoie les clients connectés sous forme de String */
	public String getClients()
	{
		String toReturn = "Clients connectés :\n";

		for (int nbClients = 0; nbClients < this.clients.size(); nbClients++)
			toReturn += clients.get(nbClients).getAdresseNom() + "\n";

		return toReturn;
	}

	/** pig
	 * Retourne un cochon
	*/
	public String pig()
	{
		return("pig\n    _____  \n^..^     \\9\n(oo)_____/\n    WW  WW");
	}

	/** doubler
	 * ça double
	*/
	public String doubler()
	{
		return String.format("%d",this.doubler *= 2);
	}


	/** Supprime le client de la liste des clients connectés */
	public void deconnecte(Client client)
	{
		this.clients.remove(client);
	}

	public static void main(String[] args) {
		new Serveur(9002, "Serveur SAE");
	}
}
