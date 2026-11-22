package src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class Client extends Thread 
{
	
	private final Serveur serveur;
	private final Socket  client;

	private String  adresse;
	private String  nom;

	private PrintWriter    printer;
	private BufferedReader sender;

	public Client (Serveur serveur, Socket client)
	{
		this.serveur = serveur;
		this.client  = client;
	}
	
	public void run() //code executer automatiquement dans chaque thread
	{
		try
		{
			printer = new PrintWriter   (client.getOutputStream(), true); // Envoyer
			sender  = new BufferedReader(new InputStreamReader(client.getInputStream())); // Recevoir

			adresse = client.getInetAddress().getHostName();

			printer.println("Bienvenue sur le serveur " + serveur.getNom());

			/* Demande et Récupération du pseudo de l'utilisateur  */
			printer.println("Entrez votre pseudo : ");
			nom = sender.readLine();

			/* Vérification de longueur du pseudo */
			while (nom.length() > 20)
			{
				printer.println("Pseudo trop long, max 20 caractères");
				nom = sender.readLine();
			}

			/* On récupère l'historique du chat */
			printer.println(serveur.getHistoriqueChat());
			serveur.getNewMessage(getAdresseNom() + " s'est connecté", null);

			String message;
			while ((message = sender.readLine()) != null)
			{
				serveur.getNewMessage(message, this);
			}

			deconnecte();
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
	}

	/** envoie un message au client */
	public void toClient(String message)
	{
		this.printer.println(message);
	}

	/** renvoie l'adresse du client */
	public String getAdresse()
	{
		return adresse;
	}

	/** renvoie le nom du client */
	public String getNom()
	{
		return nom;
	}

	/** renvoie l'adresse et le nom de façon propre */
	public String getAdresseNom()
	{
		return ("(" + adresse.replace(".iut.univ-lehavre.fr", "") + ") " + nom);
	}

	/** renvoie le socket client */
	public Socket getSocket()
	{
		return this.client;
	}

	/** renvoie le BufferedReader du client */
	public BufferedReader getBufferedReader()
	{
		return this.sender;
	}

	/** déconnecte le client correctement */
	public void deconnecte()
	{
		try
		{
			this.serveur.deconnecte(this);
			this.printer.close();
			this.client.close();
			this.serveur.getNewMessage(getNom() + " s'est déconnecté", null);
		}
		catch (Exception e)
		{
			System.err.println(e);
		}
	}
}
