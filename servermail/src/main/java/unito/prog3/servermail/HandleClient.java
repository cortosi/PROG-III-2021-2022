package unito.prog3.servermail;

import unito.prog3.models.Account;
import unito.prog3.models.Mail;
import unito.prog3.utils.FilesManager;
import unito.prog3.utils.ServerAPI;

import java.io.IOException;
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
    public HandleClient(Socket clientSocket, Server server) throws IOException {
        this.server = server;
        this.clientSocket = clientSocket;

        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());
    }

    //
    public void setupThread() throws Exception {
    }

    public void sendResponse(Object response) throws IOException {
        out.writeObject(response);
    }

    // Login / Signup
    public void waitSignupData() throws IOException, ClassNotFoundException {
        Account acc = (Account) in.readObject();

        String username = acc.getUsername();
        String password = acc.getPassword();

        // Check username
        if (server.accountExist(username))
            sendResponse("USR_EXIST");
        else {
            if (password != null) {
                System.out.println(
                        "[" + Thread.currentThread() + "]: Client(" + clientSocket.getInetAddress()
                                + ") signup data accepted, saved into users file");
                sendResponse("REG");
                createNewUser(acc);
            } else
                sendResponse("PSW_ERR");
        }
    }

    public void waitLoginData() throws ClassNotFoundException, IOException {
        Account acc = (Account) in.readObject();

        String username = acc.getUsername();
        String password = acc.getPassword();

        // Check username
        if (server.accountExist(username)) {
            Account aux = server.getAccount(username);

            //Check password
            if (password.equals(aux.getPassword())) {
                System.out.println(
                        "[" + Thread.currentThread() + "]: Client(" + clientSocket.getInetAddress()
                                + ") authenticated");

                clientAcc = acc;
                sendResponse("AUTH"); // If of, authorized
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

    public void createNewUser(Account new_acc) throws IOException {
        if (new_acc == null)
            throw new IllegalArgumentException();

        FilesManager.addUserToFile(new_acc);

        // Update server list
        server.loadUser(new_acc);
    }

    // Messages list
    public void waitMailbox() throws Exception {
        // Waiting for mailbox
        String mailbox = (String) in.readObject();

        ArrayList<Mail> mail = FilesManager.getMailBox(mailbox, clientAcc.getUsername());

        clientAcc.setMessages(mail);
    }

    // Mail sending
    public void waitMailToSend() throws IOException, ClassNotFoundException {
        // Getting message from client
        Object obj = in.readObject();
        Mail msg;

        if (obj == null)
            throw new IOException();

        if (obj instanceof Mail) {
            msg = (Mail) obj;

            // Sending server response
            sendResponse(server.sendMail(msg));
        }
    }

    // Mail Moving
    public void waitMailToMove() throws IOException, ClassNotFoundException {
        // Getting message from client
        Object obj = in.readObject();
        Mail tomove;

        if (obj == null)
            throw new IOException();

        if (obj instanceof Mail) {
            tomove = (Mail) obj;

            FilesManager.moveMail(clientAcc.getUsername(), tomove);
        }
    }

    @Override
    public synchronized void run() {
        // Thread Name
//        Thread.currentThread().setName("");

        //
        System.out.println(
                "[" + Thread.currentThread() + "]: Connection established with: " + clientSocket.getInetAddress());

        try {
            setupThread();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (!exit) {
            Object read = null;
            try {
                if ((read = in.readObject()) != null) {
                    if (read instanceof ServerAPI) {
                        switch ((ServerAPI) read) {
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
                                    e.printStackTrace();
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
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                exit = true;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
