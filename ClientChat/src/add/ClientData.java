package add;

import java.io.Serializable;

public class ClientData implements Serializable {
    private int id;
    protected String login;
    private Status status;

    public ClientData() {}

    public ClientData(int id, String login, Status status) {
        this.id = id;
        this.login = login;
        this.status = status;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
