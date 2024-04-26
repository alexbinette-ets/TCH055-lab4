package ca.ets.tch055.laboratoire4;

import java.sql.*;
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
        try {
            Statement requete = connexion.createStatement();
            ResultSet resultats = requete.executeQuery("SELECT DISTINCT GroupeCours.sigle FROM GroupeCours INNER JOIN SessionETS ON GroupeCours.code_session = SessionETS.code_session WHERE SessionETS.code_session = " + codeSession);
            if(!resultats.next()){
                System.out.println("Pas de cours offerts pour cette session");
            }
            else{
                do{
                    System.out.println(resultats.getString("sigle"));
                }while(resultats.next());
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
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

        String request = "SELECT SUM(C.nb_credits) FROM Etudiant E JOIN Inscription I ON E.code_permanent = I.code_permanent JOIN GroupeCours GC ON GC.sigle = I.sigle AND GC.no_groupe=I.no_groupe AND GC.code_session=I.code_session JOIN Cours C on C.sigle = GC.sigle WHERE I.code_session = ? AND E.nom = ? AND E.prenom = ?";

        try {
            connexion.setAutoCommit(false);
            PreparedStatement statement = connexion.prepareStatement(request);
            statement.setString(1, String.valueOf(codeSession));
            statement.setString(2, nom);
            statement.setString(3, prenom);
            ResultSet resultSet = statement.executeQuery();
            connexion.commit();

            if (resultSet.next()) {
                double sum = resultSet.getInt(1) * 155.77 ;
                System.out.println(String.format("Étudiant : %s %s Session : %d Cout Session : %.2f$",prenom,nom,codeSession,sum));
            } else {
                System.out.println("Étudiant introuvable!");
            }


        } catch (SQLException e) {
            try {
                System.err.println("erreur dans la trans : " + e.getMessage());
                connexion.rollback();
                System.out.println("Rollback de transaction, erreur dans le calcul.");
            } catch (SQLException ex) {
                System.err.println("Problème dans le rollback de la transaction" + ex.getMessage());
            }
        }

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

        String queryCours = String.format("insert into Cours (sigle, titre, nb_credits) values ('%s','%s',%d)",
                newSigle,titreCours,nbreCredit);

        try {
            connexion.setAutoCommit(false);
            Statement statement = connexion.createStatement();
            statement.executeUpdate(queryCours);

            for(String prealable : listPrealable){
                String queryPrealables = String.format("insert into Prealable (sigle, sigle_prealable) values ('%s','%s')",newSigle,prealable);
                Statement statement2 = connexion.createStatement();
                statement2.executeUpdate(queryPrealables);
            };

            connexion.commit();
            System.out.println("Ajout complété avec succès");

        } catch (SQLException e) {
            try {
                connexion.rollback();
                System.out.println("Rollback de transaction, erreur dans l'ajout.");
            } catch (SQLException ex) {
                System.err.println("Problème dans le rollback de la transaction" + ex.getMessage());
            }
        }


    } // methode AjoutCours()

    // ------------------------------------------------------------------------------

    /**
     * Question 4 - calcul de la cote
     */
    public static void calculCote() {

        ArrayList<Integer> notes = new ArrayList<>();
        ArrayList<Character> cotes = new ArrayList<>();

        try {
            //Preparation Requête

            //GET LES ID POUR PAS OVERWRITE LA BD EVERYTIME!
            ArrayList<String> codesPermanents = new ArrayList<>();
            ArrayList<String> sigles = new ArrayList<>();

            String selectQuery = "SELECT DISTINCT code_permanent, sigle, note FROM Inscription";
            PreparedStatement ps = connexion.prepareStatement(selectQuery);
            ResultSet resultats = ps.executeQuery();

            while (resultats.next()) {
                String codePermanent = resultats.getString("code_permanent");
                String sigle = resultats.getString("sigle");
                Integer note = resultats.getInt("note");

                if (resultats.wasNull()) {
                    notes.add(null);
                } else {
                    notes.add(note);
                }
                codesPermanents.add(codePermanent);
                sigles.add(sigle);
            }

            // Fermer requete de fetch
            resultats.close();
            ps.close();

            for (Integer noteCheck : notes) {
                char cote;
                if (noteCheck != null) {
                    int noteValue = noteCheck.intValue();
                    if (noteValue >= 90 && noteValue <= 100) {
                        cote = 'A';
                    } else if (noteValue >= 80 && noteValue < 90) {
                        cote = 'B';
                    } else if (noteValue >= 70 && noteValue < 80) {
                        cote = 'C';
                    } else if (noteValue >= 60 && noteValue < 70) {
                        cote = 'D';
                    } else {
                        cote = 'E';
                    }
                } else {
                    cote = 'N';
                }
                cotes.add(cote);
            }

            //Maintenant les cles correspondent avec les cotes, on peut les inserer dans l'ordre
            String update = "UPDATE Inscription SET cote = ? WHERE code_permanent = ? AND sigle = ?";
            PreparedStatement insertionStatement = connexion.prepareStatement(update);
            int rowCount = 0;
            //inserer dans la base de données
            for (int i = 0; i < cotes.size(); i++) {
                char coteAInserer = cotes.get(i);
                String codePermanent = codesPermanents.get(i);
                String sigle = sigles.get(i);

                //System.out.println("cote insere : " + coteAInserer);
                insertionStatement.setString(1, String.valueOf(coteAInserer));
                insertionStatement.setString(2, codePermanent);
                insertionStatement.setString(3, sigle);
                insertionStatement.executeUpdate();
                rowCount++;
            }


            System.out.println("Nombre de cotes ajouté : " + rowCount);

            insertionStatement.close();
        } catch (SQLException e) {
            System.out.println("Erreur lors du calcul de cote" + e.getMessage());
            e.printStackTrace();
        }

    } // méthode calculCote()

    // ------------------------------------------------------------------------------

    /**
     * Question 5 - statistique des cours
     */
    public static void statistiqueCours() {


        try {
            //Preparation RequÃªte
            ArrayList<String> sigles = new ArrayList<>();
            ArrayList<Integer> nbInscriptions = new ArrayList<>();
            ArrayList<Integer> nbEtudiants80Pl= new ArrayList<>();
            ArrayList<Integer> notesMinimales= new ArrayList<>();
            ArrayList<Integer> notesMaximales= new ArrayList<>();
            ArrayList<Double> moyenneNotes= new ArrayList<>();


            String selectQuery = "SELECT sigle FROM Inscription";
            PreparedStatement ps = connexion.prepareStatement(selectQuery);
            ResultSet resultats = ps.executeQuery();

            while (resultats.next()) {
                String sigle = resultats.getString("sigle");
                sigles.add(sigle);
            }

            resultats.close();
            ps.close();

            for (String sigleCheck : sigles) {

                //--1--
                String nbInscri = "COUNT(note) FROM Inscription WHERE sigle =" + sigleCheck + "";
                PreparedStatement ps1 = connexion.prepareStatement(nbInscri);
                ResultSet resultatsnbInscription = ps1.executeQuery();
                int totalInscription = resultatsnbInscription.getInt(1);
                nbInscriptions.add(totalInscription);
                System.out.println("totalInscription" + totalInscription);
                resultatsnbInscription.close();
                ps1.close();

                //--2--
                String nbEtudiant80 = "SELECT COUNT(*) FROM Inscription WHERE note >= 80 WHERE sigle=" + sigleCheck + "";
                PreparedStatement ps2 = connexion.prepareStatement(nbEtudiant80);
                ResultSet resultatsnbEtudiants80 = ps2.executeQuery();
                int totalnbEtudiants80 = resultatsnbEtudiants80.getInt(1);
                nbEtudiants80Pl.add(totalnbEtudiants80);
                System.out.println("totaletudaint80 : " + totalnbEtudiants80);
                resultatsnbEtudiants80.close();
                ps2.close();

                //--3--
                String noteMinimaleMaximale = "SELECT MIN(note), MAX(note) FROM Inscription WHERE sigle=" + sigleCheck + "";
                PreparedStatement ps3 = connexion.prepareStatement(noteMinimaleMaximale);
                ResultSet resultatNotesMinMax = ps3.executeQuery();
                int noteMinimale = resultatNotesMinMax.getInt(1);
                notesMinimales.add(noteMinimale);
                int noteMaximale = resultatNotesMinMax.getInt(2);
                notesMaximales.add(noteMaximale);
                System.out.println("Note Min : " + noteMinimale + "\n NOTE MAX : " + noteMaximale);
                resultatNotesMinMax.close();
                ps3.close();


                //--4--
                String moyenneNotesString = "SELECT AVG(note) FROM Inscription WHERE sigle=" + sigleCheck + "";
                PreparedStatement ps4 = connexion.prepareStatement(moyenneNotesString);
                ResultSet moyenneresults = ps4.executeQuery();
                double moyenne = moyenneresults.getInt(1);
                moyenneNotes.add(moyenne);
                System.out.println("MOYENNE  " + moyenne);
                moyenneresults.close();
                ps4.close();
            }
        }



        catch (SQLException e) {
            System.out.println("Erreur lors du calcul de cote" + e.getMessage());
            e.printStackTrace();
        }

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


