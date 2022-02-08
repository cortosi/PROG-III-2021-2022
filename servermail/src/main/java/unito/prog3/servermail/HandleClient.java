package unito.prog3.servermail;

import unito.prog3.models.Account;
import unito.prog3.models.Mail;
import unito.prog3.utils.FilesManager;
import unito.prog3.utils.Protocol;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class HandleClient implements Runnable {

    private boolean exit = false;

    private Account clientAcc;
    private final Socket clientSocket;
    private final Server server;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    // Constructor
    public HandleClient(Socket clientSocket, Server server)
            throws IllegalArgumentException, IOException {

        if (clientSocket == null || server == null)
            throw new IllegalArgumentException();

        this.server = server;
        this.clientSocket = clientSocket;

        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());
    }

    //
    public void setupThread()
            throws Exception {
    }

    public void sendResponse(Object response)
            throws IllegalArgumentException, IOException {

        if (response == null)
            throw new IllegalArgumentException("[Illegal Argument]: Response cannot be null");

        out.writeObject(response);
    }

    // Login / Signup
    public void waitSignupData()
            throws IOException, ClassNotFoundException {

        Object obj = in.readObject();

        if (!(obj instanceof Account new_acc))
            throw new InvalidObjectException("[Invalid Object]: Account required for SignUp");

        String username = new_acc.getUsername();
        String password = new_acc.getPassword();

        // Check username already exists
        if (server.accountExist(username))
            sendResponse("USR_EXIST");
        else {
            if (password != null) {
                System.out.println(
                        "[" + Thread.currentThread() + "]: Client(" + clientSocket.getInetAddress()
                                + ") signup data accepted, saved into users file");
                sendResponse("REG");
                createNewUser(new_acc);
            } else
                sendResponse("PSW_ERR");
        }
    }

    public void waitLoginData()
            throws ClassNotFoundException, IOException {

        Object obj = in.readObject();

        if (!(obj instanceof Account acc))
            throw new InvalidObjectException("[Invalid Object]: Account required for Login");

        String username = acc.getUsername();
        String password = acc.getPassword();

        // Check username
        if (server.accountExist(username)) {
            //Check password
            if (password.equals(server.getAccount(username).getPassword())) {
                System.out.println(
                        "[" + Thread.currentThread() + "]: Client(" + clientSocket.getInetAddress()
                                + ") authenticated");

                clientAcc = acc;
                sendResponse("AUTH");
            } else {
                System.out.println(
                        "[" + Thread.currentThread() + "]: Client(" + clientSocket.getInetAddress()
                                + ") rejected due password wrong");
                sendResponse("PSW_WRONG");
            }
        } else {
            System.out.println(
                    "[" + Thread.currentThread() + "]: Client(" + clientSocket.getInetAddress()
                            + ") rejected due username not exist");
            sendResponse("USR_WRONG");
        }
    }

    public void createNewUser(Account new_acc)
            throws IOException {

        if (new_acc == null)
            throw new IllegalArgumentException("[Illegal Argument]: account must be not null");

        FilesManager.addUserToFile(new_acc);

        // Update server list
        server.loadUser(new_acc);
    }

    // Messages list
    public void waitMailbox()
            throws Exception {

        ArrayList<Mail> mail = null;
        Object obj = in.readObject();

        if (!(obj instanceof String mailbox))
            throw new InvalidObjectException("[Invalid Object]: String required for mailbox mail");

        mail = FilesManager.getMailBox(clientAcc.getUsername(), mailbox);

        clientAcc.setMessages(mail);
    }

    // Mail sending
    public void waitMailToSend()
            throws IOException, ClassNotFoundException {
        // Getting message from client
        Object obj = in.readObject();

        if (!(obj instanceof Mail mail))
            throw new InvalidObjectException("[Invalid Object]: Mail required for sending");

        // Sending server response
        sendResponse(server.sendMail(mail));
    }

    // Mail Moving
    public void waitMailToMove()
            throws Exception {
        // Getting message from client
        Object obj = in.readObject();

        if (!(obj instanceof Mail tomove))
            throw new InvalidObjectException("[Invalid Object]: Mail required for moving");

        FilesManager.moveMail(clientAcc.getUsername(), tomove);
    }

    // Mail Delete
    public void waitMailToDelete()
            throws Exception {
        // Getting message from client
        Object obj = in.readObject();

        if (!(obj instanceof Mail toDelete))
            throw new InvalidObjectException("[Invalid Object]: Mail required for moving");

        FilesManager.rmMailFromMailbox(clientAcc.getUsername(), toDelete);
    }

    @Override
    public synchronized void run() {
        // Thread Name
        // Thread.currentThread().setName("");

        System.out.println(
                "[" + Thread.currentThread() + "]: Connection established with: " + clientSocket.getInetAddress());

        while (!exit) {
            Object read = null;
            try {
                if ((read = in.readObject()) != null) {
                    if (read instanceof Protocol) {
                        switch ((Protocol) read) {
                            case REG_REQUEST -> {
                                System.out.println(
                                        "[" + Thread.currentThread() + "]: check signup data");
                                waitSignupData();
                            }
                            case LOGIN_REQUEST -> {
                                System.out.println(
                                        "[" + Thread.currentThread() + "]: check credentials");
                                waitLoginData();
                            }
                            case MAILBOX_LIST -> {
                                System.out.println(
                                        "[" + Thread.currentThread() + "]: Loading messages");
                                try {
                                    waitMailbox();
                                    out.writeObject(clientAcc.getMessages());
                                } catch (Exception e) {
                                    System.out.println(
                                            "[" + Thread.currentThread() + "]: ERROR: " + e.getMessage());
                                }
                            }
                            case SEND_MSG -> {
                                System.out.println(
                                        "[" + Thread.currentThread() + "]: Reding Message...");
                                waitMailToSend();
                            }
                            case MOVE_REQ -> {
                                System.out.println(
                                        "[" + Thread.currentThread() + "]: Moving Message...");
                                waitMailToMove();
                            }
                            case DEL_REQ -> {
                                System.out.println(
                                        "[" + Thread.currentThread() + "]: Deleting Message...");
                                waitMailToDelete();
                            }
                            case REPLY_REQ -> {
                                System.out.println(
                                        "[" + Thread.currentThread() + "]: Reply Message...");
                                waitMailToDelete();
                            }
                        }
                    } else
                        throw new InvalidObjectException("[Invalid Object]: ServerAPI required");
                } else
                    throw new IllegalArgumentException("[Illegal Argument]: API should be not null");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println(
                        "[" + Thread.currentThread() + "]: CLIENT DISCONNECTED");
                exit = true;
            } catch (ClassNotFoundException e) {
                System.out.println("Illegal class sent");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
