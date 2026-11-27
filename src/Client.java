package src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class Client extends Thread 
{


	//-----------------------//
	//fonctionnement generale//
	//-----------------------//

	
	private final Serveur serveur;
	private final Socket  client;

	private String  adresse;
	private String  nom;

	private PrintWriter    printer;
	private BufferedReader sender;

	public Client (Serveur serveur, Socket client) {
		this.serveur = serveur;
		this.client  = client;
	}

	public void run() {
		try {
			printer = new PrintWriter   (client.getOutputStream(), true); // Envoyer
			sender  = new BufferedReader(new InputStreamReader(client.getInputStream())); // Recevoir

			adresse = client.getInetAddress().getHostName();

			printer.println(
					"██████╗ ██╗███████╗███╗   ██╗██╗   ██╗███████╗███╗   ██╗██╗   ██╗███████╗\n" +
					"██╔══██╗██║██╔════╝████╗  ██║██║   ██║██╔════╝████╗  ██║██║   ██║██╔════╝\n" +
					"██████╔╝██║█████╗  ██╔██╗ ██║██║   ██║█████╗  ██╔██╗ ██║██║   ██║█████╗\n" +
					"██╔══██╗██║██╔══╝  ██║╚██╗██║╚██╗ ██╔╝██╔══╝  ██║╚██╗██║██║   ██║██╔══╝\n" +
					"██████╔╝██║███████╗██║ ╚████║ ╚████╔╝ ███████╗██║ ╚████║╚██████╔╝███████╗\n" +
					"╚═════╝ ╚═╝╚══════╝╚═╝  ╚═══╝  ╚═══╝  ╚══════╝╚═╝  ╚═══╝ ╚═════╝ ╚══════╝"
			);

			printer.println("Bienvenue sur le serveur " + serveur.getNom());

			if (adresse.equals("localhost")) {
				printer.println("Entrer le mot de passe localhost : ");
				String psw = sender.readLine();

				while (!serveur.checkPassworld(psw)) {
					printer.println("mauvais mot de passe");
					psw = sender.readLine();
				}
			}

			printer.println("Entrez votre pseudo : ");
			nom = sender.readLine();

			while (nom.length() > 20) {
				printer.println("pseudo trop long, max 20 caractères");
				nom = sender.readLine();
			}

			printer.println(serveur.getHistoriqueChat());
			serveur.recieveFromClient(getAdresseNom() + " s'est connecté", null);

			String message;
			while ((message = sender.readLine()) != null && !message.equals("/q"))
			{
				if (!message.isEmpty()) {
				serveur.recieveFromClient(message, this);
				}
			}

			deconnecte();
		}
		catch(Exception e) {
			System.err.println(e);
		}
	}

	//todo faire marcher cette merde
	private void deletInpute (String message) {
		printer.print((char)8);
		for (int i = message.length(); i > 0; i--) {
			printer.print((char)8);
		}
	}

	public void toClient(String message) {
		printer.println(message);
	}

	public String getAdresse() {
		return adresse;
	}

	public String getNom() {
		return nom;
	}

	public String getAdresseNom() {
		return ("(" + adresse.replace(".iut.univ-lehavre.fr", "") + ") " + nom);
	}

	public Socket getSocket() {
		return client;
	}

	public BufferedReader getBufferedReader() {
		return sender;
	}

	public void deconnecte() {
		try {
			printer.close();
			sender.close();
			client.close();
			serveur.deconnecte(this);
			serveur.recieveFromClient(getAdresseNom() + " s'est déconnecter", null);
		}
		catch (Exception e) {
			System.err.println(e);
		}
	}


	//-----------------//
	//commandes de chat//
	//-----------------//


	private double money = 10;

	public String gamble (String betS) {
		String toReturn = "";
		try {
			double bet = Double.parseDouble(betS.substring(5));

			if (bet <= money && bet > 0) {
				if (Math.random() > 0.5) {
					money += bet;
					toReturn += "vous avez gagnee " + bet;
				}
				else {
					money -= bet;
					toReturn +=  "vous avez perdue " + bet ;
				}
			}
			else {
				toReturn += "vous etes trop pauvre";
			}

			return toReturn + "\nTotal = " + money;
		}
		catch (Exception e) {
			return "tapez /bet (nombreIci)";
		}
	}
}