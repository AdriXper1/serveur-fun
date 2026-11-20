package src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class Client extends Thread 
{
	
	private Serveur serveur;
	private Socket  client;

	private String  adresse;
	private String  nom;

	private PrintWriter    out;
	private BufferedReader in;

	public Client (Serveur serveur, Socket client)
	{
		this.serveur = serveur;
		this.client = client;
	}
	
	public void run() //code executer automatiquement dans chaque thread
	{
		try
		{
			out = new PrintWriter   (client.getOutputStream(), true     ); // Envoyer
			in  = new BufferedReader(new InputStreamReader(client.getInputStream())); // Recevoir 

			adresse = client.getInetAddress().getHostName();

			out.println("Bienvenue sur le serveur " + serveur.getNom());

			/* Demande et Récupération du pseudo de l'utilisateur  */
			out.println("Entrez votre pseudo : ");
			nom = in.readLine();

			/* Vérification de longueur du pseudo */
			while (nom.length() > 20)
			{
				out.println("Votre pseudo dépasse la limite de 20 caractères ");
				nom = in.readLine();
			}

			/* On récupère l'historique du chat */
			out.println(serveur.getHistoriqueChat());
			serveur.getNewMessage(getAdresseNom() + " s'est connecté", null);

			String message;
			while ((message = in.readLine()) != null)
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
		this.out.println(message);
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

	/** renvoie la socket client */
	public Socket getSocket()
	{
		return this.client;
	}

	/** renvoie le BufferedReader du client */
	public BufferedReader getBufferedReader()
	{
		return this.in;
	}

	/** déconnecte le client correctement */
	public void deconnecte()
	{
		try
		{
			this.serveur.deconnecte(this);
			this.out.close();
			this.client.close();
			this.serveur.getNewMessage(getNom() + " s'est déconnecté", null);
		}
		catch (Exception e)
		{
			System.err.println(e);
		}
	}
}
