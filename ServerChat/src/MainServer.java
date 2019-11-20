import add.*;
import add.MessageType;
import add.Session;
import table.UsersTable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {
    private static int port;
    private static Socket socket = null;
    private static ServerSocket server = null;
    private static ExecutorService executor = null;

    private static ArrayList<Session> sessions = null;
    private static int numSessions = 0;

    private static ArrayList<Client> clients;
    private static int numClients = 0;
    private static boolean alreadyOnline;

    MainServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws IOException {
        MainServer mainServer = new MainServer(7575);
        mainServer.startServer();
    }

    private void startServer() throws IOException {
        clients = new ArrayList<>();
        sessions = new ArrayList<>();
        sessions.add(new Session(0, "MainSession"));

        executor = Executors.newCachedThreadPool();

        server = new ServerSocket(port);
        System.out.println("SERVER DZIALA");


        /** nasluchiwanie na zaakceptowanie przez socket i uruchamianie watku */
        while(true) {
            socket = server.accept();

            executor.submit(() -> {
                ObjectInputStream ois = null;
                ObjectOutputStream oos = null;

                Client client = null;
                try {
                    client = new Client(socket);
                    ois = client.getIn();
                    oos = client.getOut();
                    clients.add(client);
                    numClients++;

                } catch (IOException e) {
                    e.printStackTrace();
                }

                /** Sprawdzanie typ wiadomosci*/
                try {
                    Message msg = (Message) ois.readObject();
                    /** Rejestracja nowego uzytkownika */
                    if(msg.getTyp() == MessageType.NEW_USER)
                        databaseMode(ois, oos, msg);
                    /** Logowanie sie na stronie glownej */
                    else if(msg.getTyp() == MessageType.LOGIN)
                        databaseMode(ois, oos, msg);
                    /** Logowanie sie jako anonim */
                    else if(msg.getTyp() == MessageType.CHAT) {
                        chatMode(oos, ois);
                    }
                    /** Odpowiada za zalogowanie sie do chatu uzywajac uzytkownika z bazy */
                    else if(msg.getTyp() == MessageType.LOGIN_CHAT) {
                        databaseMode(ois, oos, msg);
                    }
                } catch (IOException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private int getSessionId(ArrayList<Session> sessions, String msg) {
        for(int i=0; i<sessions.size(); i++) {
            if(sessions.get(i).getName().equals(msg))
                return sessions.get(i).getId();
        }
        return -1;
    }

    private void databaseMode(ObjectInputStream ois, ObjectOutputStream oos, Message message) throws IOException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        UsersTable ut = (UsersTable) ois.readObject();
        MySql mysql = new MySql();

        if(message.getTyp() == MessageType.NEW_USER) {
            if(mysql.addUser(ut)) {
                message.setMsg("uzytkownik dodany");
            }
            else {
                message.setMsg("blad przy dodawaniu");
            }
            oos.writeObject(message);
        }
        else if(message.getTyp() == MessageType.LOGIN) {
            if(mysql.verification(ut.getLogin(), ut.getPassword())) {
                message = new Message();
                message.setTyp(MessageType.LOGIN);
                oos.writeObject(message);
            }
        }
        else if(message.getTyp() == MessageType.LOGIN_CHAT) {
            if(mysql.verification(ut.getLogin(), ut.getPassword())) {
                message.setMsg("logged");
                mysql.updateUser(ut.getLogin(), "login");
                oos.writeObject(message);
                chatMode(oos, ois);
            }
            else {
                message.setMsg("not logged");
                oos.writeObject(message);
            }
        }
        else if(message.getTyp() == MessageType.CHANGE_PASSWORD) {
            System.out.println("siema");
            ut = (UsersTable) ois.readObject();
            mysql.changePassword(ut.getLogin(), ut.getPassword(), ut.getQuery());
        }
    }

    private void chatMode(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        final Client[] whoiam = new Client[1];
        final boolean[] logged = {false};
        int sessionId = 0;

        try {
            int tmp = 0;
            /** Watek danego klienta po accept(). Dopoki status nie bedzie odpowiedni odczytuje nicki
             * od klienta. Jezeli nick jest wolny to dodaje na serwer i przechodzi dalej*/
            while (!logged[0]) {
                ClientData clientData = (ClientData) ois.readObject();

                System.out.println("Odebrano obiekt Client. Sprawdzam dostepnosc nicku.");
                alreadyOnline = false;

                for (int i = 0; i < clients.size(); i++) {

                    if ((clients.get(i).getLogin() == null) || (clientData.getLogin() == null))
                        continue;

                    if (clients.get(i).getLogin().equals(clientData.getLogin())) {
                        alreadyOnline = true;
                        tmp++;
                    }
                }

                if (alreadyOnline && tmp==1) {
                    System.out.println("Nick juz zalogowany.");
                    Message msg = new Message();
                    msg.setTyp(MessageType.ALREADY_ONLINE);
                    oos.writeObject(msg);
                    oos.flush();
                    numClients--;
                    clients.remove(numClients);
                } else if (!alreadyOnline){
                    clients.get(numClients - 1).setLogin(clientData.getLogin());
                    clients.get(numClients - 1).setId(numClients - 1);
                    clients.get(numClients - 1).setStatus(Status.ONLINE);

                    Message msg = new Message();
                    msg.setTyp(MessageType.NOT_ONLINE);
                    oos.writeObject(msg);
                    oos.flush();
                    System.out.println("Login wolny. Dodaje klienta na serwer.\nWyslano odpowiedz do klienta");
                    whoiam[0] = clients.get(numClients - 1);
                    logged[0] = true;
                    tmp = 0;
                }
            }

            oos = whoiam[0].getOut();
            oos.writeObject(sessions);
            oos.flush();

            Message msg = (Message) ois.readObject();
            if(msg.getTyp() == MessageType.SELECTED_SESSION) {
                System.out.println("Uzytkownik: " + msg.login + " dolacza do sesji: " + msg.msg);
                sessionId = getSessionId(sessions, msg.msg);
                sessions.get(sessionId).addNewUser(msg.login);

            }
            else if(msg.getTyp() == MessageType.NEW_SESSION) {
                numSessions++;
                sessionId = numSessions;
                Session s = new Session(numSessions, msg.msg);
                s.addNewUser(msg.login);
                sessions.add(s);
                System.out.println("Uzytkownik "+msg.login+" tworzy nowa sesje "+msg.msg);
            }

            msg.setMsg(sessions.get(numSessions).getName());
            oos.writeObject(msg);
            oos.flush();

            /** Rozsylanie do klientow informacji o nowym uzytkowniku */
            msg = new Message(msg.login, "Nowy uzytkownik: "+whoiam[0].getLogin()+"\n");
            msg.setTyp(MessageType.NEW_USER);

            ArrayList<String> usersInSession = sessions.get(sessionId).getActiveUsers();
            for(int i=0; i<numClients; i++) {
                    if(usersInSession.contains(clients.get(i).getLogin())) {
                        oos = clients.get(i).getOut();
                        oos.writeObject(msg);
                        oos.flush();
                    }
            }

            System.out.println("\nZalogowani klienci:\n"+clients.toString());
            /** Nasluchiwanie */
            String line = "";
            while (true) {

                msg = (Message) ois.readObject();
                line = msg.msg;
                System.out.println("Otrzymano: " + line + " od " + msg.login + Thread.currentThread().getName());

                usersInSession = sessions.get(sessionId).getActiveUsers();

                /** Przechodzi po calej liscie klientow i odsyla to co odebral */
                for (int i = 0; i < clients.size(); i++) {
                    if(usersInSession.contains(clients.get(i).getLogin())) {
                        oos = clients.get(i).getOut();
                        oos.writeObject(msg);
                        oos.flush();
                        System.out.println("Odeslano do klienta: " + i + " Sesja: " + sessions.get(sessionId).getName());
                    }
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            /** Jezeli watek zlapie wyjatek to obsluguje go tak, jakby uzytkownik sie wylogowal */
            //e.printStackTrace();
            MySql mysql = new MySql();
            mysql.updateUser(whoiam[0].getLogin(), "offline");
            System.out.println("Zamykam polaczenie z klientem: " + Thread.currentThread().getName()+" "+whoiam[0].getLogin());
            Message msg = new Message("", "Uzytkownik '"+whoiam[0].getLogin()+"' opuscil chat!\n");
            msg.setTyp(MessageType.USER_LEFT);

            ArrayList<String> userInSession= sessions.get(sessionId).getActiveUsers();
            for(int i=0; i<numClients; i++) {
                if(clients.get(i).getId() != whoiam[0].getId()) {
                    if(userInSession.contains(clients.get(i).getLogin())) {
                        oos = clients.get(i).getOut();
                        oos.writeObject(msg);
                        oos.flush();
                    }
                }
            }
            userInSession.remove(whoiam[0].getLogin()); //usuwa z listy zalogowanych na sesji login
            sessions.get(sessionId).setActiveUsers(userInSession); //aktualizuje liste userow w sesji
            if((userInSession.size() == 0) && (sessions.get(sessionId).getName() != "MainSession")) {
                System.out.println("Wszyscy wylogowani z sesji "+sessions.get(sessionId).getName()+". Zamykam");
                sessions.get(sessionId).setId(-1);
                numSessions--;

            }
            int id = whoiam[0].getId();
            clients.remove(id); //usuwa z obecnie zalogowanych z serwera
            numClients--;
        }
    }
}
