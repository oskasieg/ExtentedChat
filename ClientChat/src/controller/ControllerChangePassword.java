package controller;

import add.MessageType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import table.UsersTable;
import add.Message;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ControllerChangePassword {
    public TextField password;
    public TextField query;
    public TextField password2;
    public Button confirmChangePassword;

    private static Socket socket = null;
    private static ObjectOutputStream oos = null;
    private static ObjectInputStream ois = null;
    public TextField old_password;
    public TextField login;

    private boolean noEmptyFields, oldPasswordWrong;


    public void confirmChangePassword(MouseEvent mouseEvent) {

        if((login.getText().equals("")) || (password.getText().equals("")) || (password2.equals("")) ||(query.equals(""))) {
            JOptionPane.showMessageDialog(null, "Wszystkie pola muszą być wypełnione!", "Komunikat", JOptionPane.WARNING_MESSAGE);
            noEmptyFields = false;
        }
        else if((!password2.getText().equals(password.getText()))) {
            noEmptyFields = false;
            password.setText("");
            password2.setText("");
            JOptionPane.showMessageDialog(null, "Nowe hasła muszą być identyczne!!", "Komunikat", JOptionPane.WARNING_MESSAGE);
        }
        else {
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
                ut.setPassword(password.getText());
                ut.setLogin(login.getText());
                ut.setQuery(query.getText());
                try {
                    Message message = new Message();
                    message.setTyp(MessageType.CHANGE_PASSWORD);
                    oos.writeObject(message);
                    oos.flush();
                    oos.writeObject(ut);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };

            Thread thread = new Thread(runnable);
            thread.start();
        }
    }

    public void sBackToMainPage(MouseEvent mouseEvent) throws IOException {
        Stage root = (Stage) confirmChangePassword.getScene().getWindow();
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

