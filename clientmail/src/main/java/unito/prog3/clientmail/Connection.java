package unito.prog3.clientmail;

import unito.prog3.models.Account;
import unito.prog3.models.Mail;
import unito.prog3.utils.Protocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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

    public String loginRequest(Account acc)
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

    public ArrayList<Mail> mailboxRequest(String mailbox, String username)
            throws IOException, ClassNotFoundException {
        // Request
        out.writeObject(Protocol.MAILBOX_LIST);

        // Sending mailbox
        out.writeObject(mailbox);
        out.writeObject(username);

        // Getting msgs list
        Object res = in.readObject();

        if (res instanceof ArrayList<?>) {
            return ((ArrayList<Mail>) res);
        } else
            return null;
    }

    public String sendMessage(String username, Mail msg, boolean reply)
            throws IOException, ClassNotFoundException {
        if (msg == null)
            throw new IllegalArgumentException();

        if (reply)
            out.writeObject(Protocol.REPLY_REQ);
        else
            out.writeObject(Protocol.SEND_MSG);

        out.writeObject(msg);
        out.writeObject(username);

        // Wait response
        return getServerResponse();
    }

    public synchronized void editMessage(String username, Mail msg, Protocol p)
            throws IOException {
        out.writeObject(p);
        out.writeObject(msg);

        out.writeObject(username);
    }
}
