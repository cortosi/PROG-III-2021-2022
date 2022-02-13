package unito.prog3.clientmail;

import unito.prog3.models.Account;
import unito.prog3.models.Mail;
import unito.prog3.utils.Protocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Connection {

    private Socket serverbound;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Connection(String host, int port)
            throws IOException {
        serverbound = new Socket(host, port);

        out = new ObjectOutputStream(serverbound.getOutputStream());
        in = new ObjectInputStream(serverbound.getInputStream());
    }

    public void checkConnection() throws SocketException {
        serverbound.setSoTimeout(5000);
    }

    public String login_request(Account acc)
            throws IOException, ClassNotFoundException {
        out.writeObject(Protocol.LOGIN_REQUEST);

        //Sending credentials
        sendCredentials(acc);

        return getServerResponse();
    }

    public void sendCredentials(Account acc)
            throws IOException {
        if (acc == null)
            throw new IllegalArgumentException();

        out.writeObject(acc);
    }

    public synchronized String getServerResponse()
            throws ClassNotFoundException, IOException {
        return (String) in.readObject();
    }

    public String signupRequest(Account new_acc)
            throws ClassNotFoundException {

        if (new_acc == null)
            throw new IllegalArgumentException();

        try {
            out.writeObject(Protocol.REG_REQUEST);
            // Sending credentials
            out.writeObject(new_acc);
            // Getting server result
            return (String) in.readObject();
        } catch (IOException e) {
            return "SERVER_UNREACHABLE";
        }
    }

    public ArrayList<Mail> mailListRequest(String mailbox)
            throws IOException, ClassNotFoundException {
        // Request
        out.writeObject(Protocol.MAILBOX_LIST);

        // Sending mailbox
        out.writeObject(mailbox);

        // Getting msgs list
        Object res = in.readObject();
        if (res instanceof ArrayList<?>) {
            return ((ArrayList<Mail>) res);
        } else
            return null;
    }

    public String sendMessage(Mail msg, boolean reply)
            throws IOException, ClassNotFoundException {
        if (msg == null)
            throw new IllegalArgumentException();

        if (reply)
            out.writeObject(Protocol.REPLY_REQ);
        else
            out.writeObject(Protocol.SEND_MSG);

        out.writeObject(msg);

        // Wait response
        return getServerResponse();
    }

    public synchronized void forwardMessage(Mail msg, Protocol p)
            throws IOException {
        out.writeObject(p);

        // Sending Mail to move
        out.writeObject(msg);
    }
}
