package ca.ets.tch055.laboratoire4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;


/**
 * Classe principale du laboratoire 4
 *
 * @author el hachemi Alikacem
 * @author Pamella Kissok
 * Version 2
 */
public class Laboratoire4 {

    public static Statement statmnt = null;
    public static Connection connexion = null;

    /**
     * Bloc statique pour le chargement du pilote Oracle
     */
    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

        } catch (ClassNotFoundException e) {

            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------------

    /**
     * Question 0 : Ouverture de la connection
     *
     * @param login
     * @param password
     * @param uri
     * @return
     */
    public static Connection connexionBDD(String login, String password, String uri) {

        Connection une_connexion = null;
        // System.err.println("Il faut implémenter la méthode connexionBDD()") ;

        try {
            une_connexion = DriverManager.getConnection(uri, login, password);
            System.out.println("vous êtes connecté!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return une_connexion;
    }

    // ------------------------------------------------------------------------------

    /**
     * Question déconnexion - fermeture de la connexion
     *
     * @return
     */
    public static boolean fermetureConnexion() {
        boolean resultat = false;

        if (connexion != null) {
            try {
                connexion.close();
                resultat = true;
                System.out.println("déconnexion réussie!");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        return resultat;
    }

    // ------------------------------------------------------------------------------

    /**
     * Question 1 - liste les cours d'une session
     *
     * @param codeSession
     */
    public static void listeCoursSession(int codeSession) {
        CREATE OR REPLACE FUNCTION listeCoursSession(code_session INT)
        RETURN TABLE(sigle TEXT) AS
        BEGIN

        IF NOT EXISTS (SELECT 1 FROM SessionETS WHERE code_session = code_session) THEN
        RAISE EXCEPTION 'Code de session pas trouve';
        END IF;
        
        RETURN QUERY 
        SELECT DISTINCT GroupeCours.sigle
        FROM GroupCours
        INNER JOIN SessionETS ON GroupeCours.code_session = SessionETS.code_session
        WHERE SessionETS.code_session = code_session;
        END;
        /

    } // listeCoursSession()


    // ------------------------------------------------------------------------------

    /**
     * Question 2 -  calcule et affiche le cout d'une session pour un étudiant
     *
     * @param codeSession
     * @param nom
     * @param prenom
     */
    public static void coutSession(int codeSession, String nom, String prenom) {
        // TODO : compléter ici
        System.err.println("Il faut implémenter la méthode coutSession()");

    } // méthode coutSession()

    // ------------------------------------------------------------------------------

    /**
     * Question 3 -  Ajoute un cours dans la base de données
     *
     * @param newSigle
     * @param titreCours
     * @param nbreCredit
     * @param listPrealable
     */
    public static void AjoutCours(String newSigle, String titreCours,
                                  int nbreCredit, ArrayList<String> listPrealable) {

        // TODO : compléter ici
        System.err.println("Il faut implémenter la méthode AjoutCours()");

    } // methode AjoutCours()

    // ------------------------------------------------------------------------------

    /**
     * Question 4 - calcul de la cote
     */
    public static void calculCote() {

        // TODO : compléter ici
        System.err.println("Il faut implémenter la méthode calculCote()");

    } // méthode calculCote()

    // ------------------------------------------------------------------------------

    /**
     * Question 5 - statistique des cours
     */
    public static void statistiqueCours() {

        // TODO : compléter ici
        System.err.println("Il faut implémenter la méthode statistiqueCours()");

    }


    // ------------------------------------------------------------------------------

    /**
     * Extrait les éléments séparés par une virgule dans d'une liste
     * et retourne les éléments dans un objet ArrayList
     *
     * @param listPrealable
     * @return
     */
    private static ArrayList<String> toArrayList(String listPrealable) {
        ArrayList<String> res = new ArrayList<String>();
        StringTokenizer st1 = new StringTokenizer(listPrealable, ",");
        while (st1.hasMoreTokens()) {
            String un_token = st1.nextToken().trim();
            res.add(un_token);
        }

        return res;
    } //  toArrayList()


    // ------------------------------------------------------------------------------

    /**
     * Affiche un menu pour le choix des opérations
     */
    public static void afficheMenu() {
        System.out.println();
        System.out.println("\t0. Quitter le programme");
        System.out.println("\t1. Afficher la liste des cours d'une session");
        System.out.println("\t2. Afficher le coût d'une session pour un étudiant");
        System.out.println("\t3. Ajouter un nouveau cours dans la base de données");
        System.out.println("\t4. Caluler les cotes");
        System.out.println("\t5. Afficher les statistiques des cours");
        System.out.print("\n\t\tVotre choix...");
    }

    /**
     * Méthode principale du TP
     *
     * @param args
     */
    public static void main(String args[]) {

        // Mettre les informations de votre compte sur SGBD Oracle
        String username = "equipe412";
        String motDePasse = "pScP3sHm";

        String uri = "jdbc:oracle:thin:@tch054ora12c.logti.etsmtl.ca:1521:TCH054";

        // Appel de le méthode pour établir la connexion avec le SGBD
        connexion = connexionBDD(username, motDePasse, uri);

        if (connexion != null) {

            System.out.println("Connection reussie...\n");

            // Affichage du menu pour le choix des opérations
            afficheMenu();

            Scanner scChoix = new Scanner(System.in);
            String choix = scChoix.nextLine().trim();

            while (!choix.equals("0")) {

                switch (choix) {
                    case "1":
                        System.out.print("Veuillez saisir le code de la session : ");
                        Scanner scCS = new Scanner(System.in);
                        Integer codeSession = scCS.nextInt();
                        listeCoursSession(codeSession.intValue());
                        break;

                    case "2":
                        System.out.print("Veuillez saisir le nom de l'etudiant(e): ");
                        Scanner scQ2 = new Scanner(System.in);
                        String nom = scQ2.nextLine().trim();

                        System.out.print("Veuillez saisir le prenom de l'etudiant(e): ");
                        scQ2 = new Scanner(System.in);
                        String prenom = scQ2.nextLine().trim();

                        scQ2 = new Scanner(System.in);
                        System.out.print("Veuillez saisir le code de la session : ");
                        int codeSessionQ2 = scQ2.nextInt();
                        coutSession(codeSessionQ2, nom, prenom);

                        break;

                    case "3":
                        Scanner scQ3 = new Scanner(System.in);
                        System.out.print("Veuillez introduire le sigle du nouveau cours : ");
                        String newSigle = scQ3.nextLine().trim();

                        scQ3 = new Scanner(System.in);
                        System.out.print("Veuillez introduire le titre du nouveau cours : ");
                        String newTitre = scQ3.nextLine().trim();

                        scQ3 = new Scanner(System.in);
                        System.out.print("Veuillez introduire le nombre de crédit du nouveau cours : ");
                        int nbreCredit = scQ3.nextInt();

                        scQ3 = new Scanner(System.in);
                        System.out.print("Veuillez introduire la liste des préalables séparés par des virgules : ");
                        String listePrealables = scQ3.nextLine().trim();

                        // Transfert des préalables de la liste vers une ArrayList
                        ArrayList<String> prealableArrayList = toArrayList(listePrealables);

                        // Appel de la méthode d'ajout du cours
                        AjoutCours(newSigle, newTitre, nbreCredit, prealableArrayList);

                        break;
                    case "4":
                        calculCote();
                        break;

                    case "5":
                        statistiqueCours();
                        break;

                    default:
                        System.out.print("Choix invalide. Veuillez recommencer !");
                }

                afficheMenu();
                scChoix = new Scanner(System.in);
                choix = scChoix.nextLine();

            } // while

            // FIn de la boucle While - Fermeture de la connexion
            if (fermetureConnexion()) {
                System.out.println("Deconnection reussie...");
            } else {
                System.out.println("Échec ou Erreur lors de le déconnexion...");
            }

        } else {
            System.out.println("Echec de la Connection. Au revoir ! ");
        }

    } // main()
}

}
