package add;

import table.UsersTable;

import java.sql.*;

public class MySql {
    private Connection con;
    private String s1 = "INSERT INTO users VALUES (?, ?, ?, ?)";
    private String s2 = "SELECT password FROM users WHERE login = ?";
    private String s3 = "UPDATE users SET status = ? WHERE login = ?";
    private String s4 = "UPDATE users SET status = ? WHERE login = ?";

    public MySql() throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
        Class.forName("com.mysql.jdbc.Driver");
        con= DriverManager.getConnection("jdbc:mysql://localhost:3306/ExtentedChat","oska","9012667");
    }

    public boolean addUser(UsersTable ut) throws SQLException {
        PreparedStatement stmt = con.prepareStatement(s1);
        stmt.setString(1, ut.getLogin());
        stmt.setString(2, ut.getPassword());
        stmt.setString(3, ut.getStatus());
        stmt.setString(4, ut.getQuery());
        stmt.executeUpdate();
        System.out.println("NEW USER");
        return true;
    }

    public void updateUser(String login, String mode) throws SQLException {
        if(mode.equals("login")) {
            PreparedStatement stmt = con.prepareStatement(s3);
            stmt.setString(1, "ONLINE");
            stmt.setString(2, login);
            int row = stmt.executeUpdate();
            System.out.println(row);
        }
        else if(mode.equals("offline")) {
            PreparedStatement stmt = con.prepareStatement(s4);
            stmt.setString(1, "OFFLINE");
            stmt.setString(2, login);
            stmt.executeUpdate();
        }
    }

    public void changePassword(String login, String password, String query) throws SQLException {
        String s = "SELECT query FROM users WHERE login = ?";
        PreparedStatement stmt = con.prepareStatement(s);
        stmt.setString(1, login);
        ResultSet rs = stmt.executeQuery();

        System.out.println("siema");

        while(rs.next()) {
            if(query.equals(rs.getString(4))) {
                System.out.println("pytanie pom zgodne");
            }
        }
    }

    public void printUsers() throws SQLException {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM users");
        while(rs.next())
            System.out.println(rs.getInt(1)+"  "+rs.getString(2)+"  "+rs.getString(3));
    }

    public boolean verification(String login, String haslo) throws SQLException {

        PreparedStatement stmt = con.prepareStatement(s2);
        stmt.setString(1, login);
        ResultSet rs = stmt.executeQuery();

        while(rs.next()) {
            if (haslo.equals(rs.getString(1))) {
                System.out.println("UDANE LOGOWANIE");
                return true;
            }
        }
        return false;
    }
}
