package controller;

import add.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import add.Message;
import table.UsersTable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ControllerMain {
    public Label registerLabel;
    public Label enterChat;
    public Button loginButton;
    public Button logoutButton;
    public PasswordField password;
    public TextField login;
    public Pane loggedPane;
    public Pane loginPane;
    public Label nick;
    Socket socket = null;
    ObjectOutputStream oos = null;
    ObjectInputStream ois = null;

    @FXML
    public void initialize() {

    }

    @FXML
    private void enterChatPressed() throws IOException {
        Stage root = (Stage) enterChat.getScene().getWindow();
        root.close();

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/page/Chat.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("Oskasieg Chat v.1.0");
        stage.setScene(scene);
        stage.show();
    }

    public void loginMain(MouseEvent mouseEvent) {

        Message message = new Message();
        message.setTyp(MessageType.LOGIN);

        Runnable runnable = () -> {
            try {
                socket = new Socket("localhost", 7575);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Wystapil nieoczekiwany problem z laczeniem sie..");
            }

            UsersTable ut = new UsersTable();
            ut.setLogin(login.getText());
            ut.setPassword(password.getText());
            try {
                oos.writeObject(message);
                oos.writeObject(ut);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Message message2 = (Message) ois.readObject();
                if(message2.getTyp() == MessageType.LOGIN) {
                    loginPane.setVisible(false);
                    loggedPane.setVisible(true);
                    logoutButton.setVisible(true);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            nick.setText(ut.getLogin());
                        }
                    });
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void logout(MouseEvent mouseEvent) {
        loginPane.setVisible(true);
        loggedPane.setVisible(false);
        logoutButton.setVisible(false);
        login.setText("");
        password.setText("");
    }

    public void registerLabelPressed(MouseEvent mouseEvent) throws IOException {
        Stage root = (Stage) registerLabel.getScene().getWindow();
        root.close();

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/page/Register.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("Oskasieg Chat v.1.0");
        stage.setScene(scene);
        stage.show();
    }

    public void changePasswordPage(MouseEvent mouseEvent) throws IOException {
        Stage root = (Stage) registerLabel.getScene().getWindow();
        root.close();

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/page/ChangePassword.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("Oskasieg Chat v.1.0");
        stage.setScene(scene);
        stage.show();
    }
}
