package controller;

import add.ClientData;
import add.Message;
import add.MessageType;
import add.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import table.UsersTable;
import thread.ClientThread;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class ControllerChat implements Initializable {

    private static ArrayList<String> usersInSession = null;
    private static Optional<String> dialogLogin;
    private static String login;
    private static String sName = null;
    private static Socket socket = null;
    private static ObjectOutputStream oos = null;
    private static ObjectInputStream ois = null;
    private static Thread threadChat = null;
    private static Thread threadDatabase = null;
    /**
     * Glowne okno chatu
     */
    public Button sendButton;
    public TextArea textArea;
    public TextField textField;
    public Button loginButton;
    public Button logoutButton;
    public Label returnButton;
    /**
     * Panel z wyborem sesji
     */
    public Button confirmSessionButton;
    public ChoiceBox<Object> choiceBoxSession;
    public Pane sessionPane;
    public Rectangle sessionPaneBackground;
    public TextField sessionNameTextField;
    public Label Label2;
    public Label Label1;
    public GridPane onlineGridPane;
    public Pane onlinePane;
    public PasswordField passwordField;
    public Button anonimButton;
    public TextField loginField;
    public Pane loginPane;

    /** Zmienne odpowiadajace za aktualizacje listy ONLINE */
    private static boolean afterLogin = false;
    private static boolean update = false;

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        /** Uruchamiam watek, ktory odpowiada za komunikacje z serwerem w celu pracy z BD */
        threadDatabase = new Thread(new ClientThread());
        //threadDatabase.start();

        /** Ta czesc odpowiada za funkcje chatu */
        textField.setEditable(false);
        textArea.setText("Aby dolaczyc do chatu kliknij: 'LOGIN'");
    }

    public void setAnonimButton(ActionEvent actionEvent) throws IOException, ClassNotFoundException {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Anonim");
        dialog.setHeaderText("Dolaczanie do chatu");
        dialog.setContentText("Podaj swoj nick:");
        dialogLogin = dialog.showAndWait();
        login =  "Annon"+dialogLogin.orElse("Annon"); //dodac jakas losowosc nickow

        Runnable runnable = () -> {
            try {
                socket = new Socket("localhost", 7575);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Wystapil nieoczekiwany problem z laczeniem sie..");
            }

            try {
                Message msg = new Message();
                msg.setTyp(MessageType.CHAT);
                oos.writeObject(msg);
                oos.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

            checkLogin(oos);
            confirmSession(oos);
            listenForMessages(ois);
        };

        /** Wystartowanie wyzej utworzonego watku */
        threadChat = new Thread(runnable);
        threadChat.start();
    }


    public void setSendButton(javafx.event.ActionEvent actionEvent) throws IOException {
        String text = textField.getText();
        Message msg = new Message(login, text);
        msg.setTyp(MessageType.NORMAL);
        oos.writeObject(msg);
        oos.flush();
        textField.setText("");
        System.out.println("Wiadomosc wyslana.");
    }

    public void setLogoutButton(ActionEvent actionEvent) throws IOException {
        Stage root = (Stage) logoutButton.getScene().getWindow();
        if (threadChat != null)
            threadChat.stop();
        if (socket != null)
            socket.close();
        root.close();

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/page/MainPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("Projekt Java - K.Sieg");
        stage.setScene(scene);
        stage.show();
    }

    public void returnToMainPage() throws IOException {
        Stage root = (Stage) returnButton.getScene().getWindow();
        root.close();

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/page/MainPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("Oskasieg Chat v.1.0");
        stage.setScene(scene);
        stage.show();
    }

    public void setLoginButton(ActionEvent actionEvent) {

        Runnable runnable2 = () -> {
            try {
                socket = new Socket("localhost", 7575);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                login = loginField.getText();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Wystapil nieoczekiwany problem z laczeniem sie..");
            }

            Message message = new Message();
            UsersTable ut = new UsersTable();
            ut.setLogin(login);
            ut.setPassword(passwordField.getText());
            message.setTyp(MessageType.LOGIN_CHAT);
            try {
                oos.writeObject(message);
                oos.writeObject(ut);
                message = (Message) ois.readObject();
                if(message.msg.equals("logged")) {
                    System.out.println("dasdas");
                    message.setTyp(MessageType.CHAT);
                    checkLogin(oos);
                    confirmSession(oos);
                    listenForMessages(ois);
                    loginPane.setVisible(false);
                }
                else {
                    JOptionPane.showMessageDialog(null, "Błędne dane!", "Komunikat", JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        };

        Thread thread2 = new Thread(runnable2);
        thread2.start();
    }

    private void checkLogin(ObjectOutputStream oos) {
        try {
            boolean logged = false;
            Message msg;

            /** Proby dolaczenia do chatu, wysylanie podanych nickow */
            while (!logged) {
                ClientData clientData = new ClientData();
                clientData.setLogin(login);
                oos.writeObject(clientData);
                oos.flush();
                System.out.println("Wyslano zapytanie z loginem.");

                /** Odbiera objekt typu Message z odpowiedzia o dostepnosci nicku */
                msg = (Message) ois.readObject();
                System.out.println("Odebrano odpowiedz o dostepnosci nicku.");
                if (msg.getTyp() == MessageType.NOT_ONLINE) {
                    textField.setEditable(true);
                    logged = true;

                    textArea.clear();
                }
                textArea.setText("Podany login jest zajety!");
            }
        }
        catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void confirmSession(ObjectOutputStream oos) {
        try {
            /** Pojawienie sie panelu i wybranie sesji */
            ArrayList<Session> sessions = (ArrayList<Session>) ois.readObject();
            ArrayList<String> sessionsName = new ArrayList<String>();
            for (int i = 0; i < sessions.size(); i++) {
                if (sessions.get(i).getId() != -1)
                    sessionsName.add(sessions.get(i).getName());
            }
            sessionsName.add("Utwórz sesję");
            ObservableList<Object> options = FXCollections.observableArrayList(sessionsName);
            choiceBoxSession.setItems(options);

            sessionPane.setVisible(true);
            sessionPaneBackground.setVisible(true);

            /** Listener do przycisku tutaj, bo pracuje na strumieniach tego watku */
            confirmSessionButton.setOnAction(event -> {
                String value;
                String sessionName;
                Message session;

                if ((sessionNameTextField.isVisible()) && (!sessionNameTextField.getText().equals(""))) {
                    sessionName = sessionNameTextField.getText();
                    session = new Message(login, sessionName);
                    session.setTyp(MessageType.NEW_SESSION);
                    sessionPaneBackground.setVisible(false);
                    sessionPane.setVisible(false);
                    try {
                        oos.writeObject(session);
                        oos.flush();
                        sName = sessionName;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                value = (String) choiceBoxSession.getValue();

                /** Przegladanie wszystkich sesji, znajdowanie ID do ktorej chcemy dolaczyc i dodanie siebie do listy online */
                int sessionId = 0;
                for(int i=0; i<sessions.size(); i++) {
                    if(sessions.get(i).getName().equals(value))
                        sessionId = sessions.get(i).getId();
                }
                usersInSession = sessions.get(sessionId).getActiveUsers();
                usersInSession.add(login);


                if (value.equals("Utwórz sesję")) {
                    Label1.setText("Podaj nazwę sesji:");
                    choiceBoxSession.setVisible(false);
                    sessionNameTextField.setVisible(true);
                    value = ".";
                } else if (!value.equals("")) {
                    session = new Message(login, value);
                    session.setTyp(MessageType.SELECTED_SESSION);
                    sessionPane.setVisible(false);
                    sessionPaneBackground.setVisible(false);
                    try {
                        oos.writeObject(session);
                        oos.flush();
                        sName = value;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //TimeUnit.SECONDS.sleep(3);

                loginPane.setVisible(false);
                onlinePane.setVisible(true);

                afterLogin = true;
            });

            /** zawiesza watek dopoki nie otrzyma odpowiedzi */
            Message tmp = (Message) ois.readObject();
            if (tmp.getTyp() == MessageType.NEW_SESSION) {
                textArea.setText("INFO:\nUtworzyłeś nową sesje o nazwie " + tmp.msg + "\n\n" +
                        "Witaj na chacie: " + login + "!\n");
                System.out.println("Utworzono sesje: " + sName);
            } else if (tmp.getTyp() == MessageType.SELECTED_SESSION) {
                textArea.setText("INFO:\nDołączyłeś do sesji o nazwie " + tmp.msg + "\n\n" +
                        "Witaj na chacie: " + login + "!\n");
                System.out.println("Dolaczono do sesji: " + tmp.msg);
            }
        }
        catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addUsersToOnlinePane(ArrayList<String> usersInSession) {
        //onlineGridPane.getChildren().removeAll();


        Platform.runLater(() -> {
            while(onlineGridPane.getRowConstraints().size() > 0){
             onlineGridPane.getRowConstraints().remove(0);
             }

             while(onlineGridPane.getColumnConstraints().size() > 0){
             onlineGridPane.getColumnConstraints().remove(0);
             }
            System.out.print("Zalogowani: ");

            for (int i = 0; i < usersInSession.size(); i++) {
                Label userLabel = new Label(usersInSession.get(i));
                onlineGridPane.addColumn(0, userLabel);
            }
        });

        System.out.println();
    }

    private void listenForMessages(ObjectInputStream ois) {
        /** Nasluchiwanie na wiadomosci z serwera */
        while (true) {
            try {
                String line = "";
                String text = "";
                String loginMsg = "";
                Message msg = (Message) ois.readObject();
                line = msg.msg;
                if (msg.getTyp() == MessageType.NEW_USER) {
                    if(msg.login.equals(login))
                        text = textArea.getText();
                    else
                        text = textArea.getText() + "\n" + line;

                    update = true;
                    afterLogin = false;
                    System.out.print("Zalogowani: ");
                    for(int i=0; i<usersInSession.size(); i++) {
                        System.out.print(usersInSession.get(i)+"\t");
                    }
                    System.out.println();
                    //usersInSession = (ArrayList<String>) ois.readObject();
                    //addUsersToOnlinePane(usersInSession);
                }
                else if (msg.getTyp() == MessageType.NORMAL) {
                    loginMsg = msg.login;
                    text = textArea.getText() + "\n" + loginMsg + ": " + line + "\n";
                }
                else if (msg.getTyp() == MessageType.USER_LEFT) {
                    loginMsg = msg.login;
                    text = textArea.getText() + "\n" + line;
                    //addUsersToOnlinePane(usersInSession);
                }
                else if(msg.getTyp() == MessageType.ONLINE_LIST) {

                }

                textArea.setText(text);
            } catch (IOException | ClassNotFoundException e) {
                //e.printStackTrace();
            }
        }
    }
}
