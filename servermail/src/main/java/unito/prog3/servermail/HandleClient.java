package unito.prog3.servermail;

import unito.prog3.controllers.Controller;
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
    private final Controller controller;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    // Constructor
    public HandleClient(Socket clientSocket, Server server, Controller controller)
            throws IllegalArgumentException, IOException {

        if (clientSocket == null || server == null)
            throw new IllegalArgumentException();

        this.server = server;
        this.clientSocket = clientSocket;
        this.controller = controller;

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
//                System.out.println(
//                        "[" + Thread.currentThread() + "]: Client(" + clientSocket.getInetAddress()
//                                + ") signup data accepted, saved into users file");
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
                controller.log("Client(" + clientSocket.getInetAddress()
                        + ") authenticated\n");

                clientAcc = acc;
                sendResponse("AUTH");
            } else {
                controller.log("Client(" + clientSocket.getInetAddress()
                        + ") rejected due password wrong\n");
                sendResponse("PSW_WRONG");
            }
        } else {
            controller.log("Client rejected due username not exist\n");
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
        String username = (String) in.readObject();

        if (!(obj instanceof String mailbox))
            throw new InvalidObjectException("[Invalid Object]: String required for mailbox mail");

        mail = FilesManager.getMailBox(username, mailbox);

        out.writeObject(mail);
    }

    // Mail sending
    public String sendMail(Mail msg, boolean reply)
            throws IOException {
        // Extract destinations
        ArrayList<String> dests = msg.getDests();

        // Check for destinations exist
        for (String dest : dests) {
            if (!server.accountExist(dest))
                return "USR_NOT_EXIST";
        }

        // Saving Mail in destinations inboxes
        if (!reply)
            for (String dest : dests)
                FilesManager.insMailToMailbox(dest, msg, true);
        else
            for (String dest : dests)
                FilesManager.replyMail(dest, msg);

        // Save mail into sent mailbox
        FilesManager.addSentMail(msg.getSource(), msg);
        return "OK";
    }

    public void waitMailToSend()
            throws IOException, ClassNotFoundException {

        // Getting message from client
        Object obj = in.readObject();

        if (!(obj instanceof Mail mail))
            throw new InvalidObjectException("[Invalid Object]: Mail required for sending");

        controller.log(mail.getSource() + " Sending mail to: " + mail.getDests().toString() + "\n");

        // Sending server response
        sendResponse(sendMail(mail, false));
    }

    // Mail Moving
    public void waitMailToMove()
            throws Exception {
        // Getting message from client
        Object obj = in.readObject();
        String username = (String) in.readObject();

        if (!(obj instanceof Mail toMove))
            throw new InvalidObjectException("[Invalid Object]: Mail required for moving");

        controller.log(username + " Moving mail from: "
                + toMove.getBelonging()
                + " to: " + toMove.getMoveto() + "\n");


        FilesManager.moveMail(username, toMove);
    }

    // Mail Delete
    public void waitMailToDelete()
            throws Exception {
        // Getting message from client
        Object obj = in.readObject();
        String username = (String) in.readObject();

        if (!(obj instanceof Mail toDelete))
            throw new InvalidObjectException("[Invalid Object]: Mail required for moving");

        controller.log(username + " Deleting mail - ID: "
                + toDelete.getId() + "\n");

        FilesManager.rmMailFromMailbox(username, toDelete);
    }

    public void waitMailToReply()
            throws Exception {
        // Getting message from client
        Object obj = in.readObject();
        String username = (String) in.readObject();

        if (!(obj instanceof Mail mail))
            throw new InvalidObjectException("[Invalid Object]: Mail required for sending");

        controller.log(username + " Replying mail with ID: "
                + mail.getId() + " to: "
                + mail.getDests() + "\n");

        // Sending server response
        sendResponse(sendMail(mail, true));
    }

    public void waitMailToNotify()
            throws Exception {
        // Getting message from client
        Object obj = in.readObject();
        String username = (String) in.readObject();

        if (!(obj instanceof Mail msg))
            throw new InvalidObjectException("[Invalid Object]: Mail required for moving");

        try {
            FilesManager.setMailRead(username, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void run() {
        // Thread Name
        // Thread.currentThread().setName("");

        while (!exit) {
            Object read = null;
            try {
                if ((read = in.readObject()) != null) {
                    if (read instanceof Protocol) {
                        switch ((Protocol) read) {
                            case REG_REQUEST -> {
                                waitSignupData();
                            }
                            case LOGIN_REQUEST -> {
                                waitLoginData();
                            }
                            case MAILBOX_LIST -> {
                                try {
                                    waitMailbox();
                                } catch (Exception e) {
                                    System.out.println(
                                            "[" + Thread.currentThread() + "]: ERROR: " + e.getMessage());
                                }
                            }
                            case SEND_MSG -> {
                                waitMailToSend();
                            }
                            case MOVE_REQ -> {
                                waitMailToMove();
                            }
                            case DEL_REQ -> {
                                waitMailToDelete();
                            }
                            case REPLY_REQ -> {
                                waitMailToReply();
                            }
                            case NOTIFY_READ -> {
                                waitMailToNotify();
                            }
                        }
                    } else
                        throw new InvalidObjectException("[Invalid Object]: ServerAPI required");
                } else
                    throw new IllegalArgumentException("[Illegal Argument]: API should be not null");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
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
