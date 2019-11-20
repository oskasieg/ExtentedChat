package add;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

/** Ta klasa rozszerza clase z modulu ClientChat. Zatem dodane zostaja tylko strumienie do komunikacji*/
public class Client extends ClientData implements Serializable{
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Client(int id, String login, Status status) {
        super(id, login, status);
    }

    public Client(Socket socket) throws IOException {
        super();
        this.login = null;
        this.socket = socket;
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());
    }

    public ObjectInputStream getIn() {
        return in;
    }

    public ObjectOutputStream getOut() {
        return out;
    }

    public void closeStreams() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}