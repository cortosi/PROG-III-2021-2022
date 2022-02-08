package unito.prog3.servermail;


import unito.prog3.models.Account;
import unito.prog3.models.Mail;
import unito.prog3.utils.FilesManager;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import static unito.prog3.utils.FilesManager.USERS_DIR_PATH;

public class Server implements Runnable {

    private final int NOTIFY_PORT = 2021;

    private final ServerSocket clientSocket;
    private final ServerSocket notifySocket;

    private static HashMap<String, Account> accounts;
    private static HashMap<String, Socket> connections;

    public Server(final int port)
            throws IOException {
        this.clientSocket = new ServerSocket(port);
        this.notifySocket = new ServerSocket(NOTIFY_PORT);
        this.accounts = new HashMap<String, Account>();
    }

    public Socket waitClient()
            throws IOException {
        return clientSocket.accept();
    }

    public boolean accountExist(String username) {
        if (username == null)
            throw new IllegalArgumentException();

        return (accounts.containsKey(username));
    }

    public Account getAccount(String username) {
        return (accounts.get(username));
    }

    public void loadUsersList()
            throws Exception {
        ArrayList<Account> accountList = FilesManager.getUsers();

        if (accountList == null)
            return;

        for (Account account : accountList) {
            loadUser(account);
        }
    }

    public void loadUser(Account acc) {
        if (acc == null)
            throw new IllegalArgumentException();

        accounts.put(acc.getUsername(), acc);
    }

    public String sendMail(Mail msg)
            throws IOException {
        // Save mail into sent mailbox
        FilesManager.addSentMail(msg.getSource(), msg);

        // src:title:<dst1:dst2>:content
        if (msg == null)
            throw new IllegalArgumentException();

        // Extract destinations
        ArrayList<String> dests = msg.getDests();

        // Check for destinations exist
        for (String dest : dests) {
            if (!accounts.containsKey(dest))
                return "USR_NOT_EXIST";
        }

        // Saving Mail in destinations inboxes
        for (String dest : dests) {
            FilesManager.insMailToMailbox(dest, msg);
        }
        return "OK";
    }

    public void setupFiles() throws IOException {
        // Check for main dir files
        if (!(Files.exists(Paths.get(USERS_DIR_PATH)))) {
            // Creating 'files' dir
            Files.createDirectories(Paths.get(USERS_DIR_PATH));
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Server");

        System.out.println("[SERVER]: START WAITING FOR CLIENTS...");
        while (true) {
            try {
                Socket newClient = waitClient();
                new Thread(new HandleClient(newClient, this)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
