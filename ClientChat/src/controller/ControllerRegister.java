package controller;

import add.Message;
import add.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import table.UsersTable;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ControllerRegister {
    public PasswordField password, password2;
    public TextField login;
    public Text returnButton;
    public Button confirmButton;

    private static Socket socket = null;
    private static ObjectOutputStream oos = null;
    private static ObjectInputStream ois = null;
    private static Thread threadRegister = null;
    public TextField query;

    public void addUser() {

        boolean noEmptyFields = true;

        if((login.getText().equals("")) || (password.getText().equals("")) || (password2.getText().equals("")) || (query.getText().equals(""))) {
            JOptionPane.showMessageDialog(null, "Wszystkie pola muszą być wypełnione!", "Komunikat", JOptionPane.WARNING_MESSAGE);
            noEmptyFields = false;
        }
        else if((!password2.getText().equals(password.getText()))) {
            noEmptyFields = false;
            password.setText("");
            password2.setText("");
            JOptionPane.showMessageDialog(null, "Hasła muszą być identyczne!!", "Komunikat", JOptionPane.WARNING_MESSAGE);
        }


        Runnable runnable = () -> {
            try {
                socket = new Socket("localhost", 7575);
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Wystapil nieoczekiwany problem z laczeniem sie..");
            }

            Message message = new Message();
            message.setTyp(MessageType.NEW_USER);

            UsersTable ut = new UsersTable(login.getText(), password.getText(), "OFFLINE", query.getText());

            try {
                oos.writeObject(message);
                oos.writeObject(ut);

                message = (Message) ois.readObject();
                if(message.msg.equals("uzytkownik dodany")) {
                    JOptionPane.showMessageDialog(null, "Twoje konto zostało utworzone!", "Komunikat", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        };

        if(noEmptyFields) {
            threadRegister = new Thread(runnable);
            threadRegister.setDaemon(true);
            threadRegister.start();
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Stage root = (Stage) returnButton.getScene().getWindow();
                root.close();

                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/page/MainPage.fxml"));
                Scene scene = null;
                try {
                    scene = new Scene(fxmlLoader.load());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Stage stage = new Stage();
                stage.setTitle("Oskasieg Chat v.1.0");
                stage.setScene(scene);
                stage.show();
            }
        });


    }

    public void returnToMainMenu(MouseEvent mouseEvent) throws IOException {
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
}
