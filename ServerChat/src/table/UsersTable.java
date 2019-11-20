package table;

import java.io.Serializable;

public class UsersTable implements Serializable {
    private String login;
    private String password;
    private String status;
    private String query;

    public UsersTable() {}

    public UsersTable(String l, String p, String s, String q) {
        login = l;
        password = p;
        status = s;
        query = q;
    }

    public void printData() {
        System.out.println("Login: "+login);
        System.out.println("Haslo: "+password);
        System.out.println("status: "+status);
        System.out.println("Query: "+query);
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }

    public String getQuery() {
        return query;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setQuery(String text) {
        this.query = text;
    }
}
