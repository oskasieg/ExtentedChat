package add;

import add.MessageType;

import java.io.Serializable;

public class Message implements Serializable {
    public int sessionId;
    public String login;
    public String msg;
    public MessageType typ;

    public Message() {
    }

    public Message(String login, String msg) {
        this.login = login;
        this.msg = msg;
    }

    public void setTyp(MessageType typ) {
        this.typ = typ;
    }

    public MessageType getTyp() {
        return typ;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
}
