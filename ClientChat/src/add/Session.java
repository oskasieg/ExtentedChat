package add;

import java.io.Serializable;
import java.util.ArrayList;

public class Session implements Serializable {
    private int id;
    private int numUsers;
    private String name;
    ArrayList<String> activeUsers;


    Session() {

    }

    public Session(int id, String name) {
        this.id = id;
        this.name = name;
        this.numUsers = 0;
        activeUsers = new ArrayList<>();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addNewUser(String login) {
        this.numUsers++;
        activeUsers.add(login);
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public int getIdByName(String name) {
        for(int i=0; i<activeUsers.size(); i++) {
            if(activeUsers.get(i).equals(name))
                return this.id;
        }
        return -1;
    }

    public int findUser(String login) {
        for(int i=0; i<activeUsers.size(); i++) {
            if(login.equals(activeUsers.get(i)))
                return id;
        }
        return -1;
    }

    public ArrayList<String> getActiveUsers() {
        return this.activeUsers;
    }

    public void setActiveUsers(ArrayList<String> activeUsers) {
        this.activeUsers = activeUsers;
    }
}
