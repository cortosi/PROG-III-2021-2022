package unito.prog3.servermail;


import unito.prog3.models.Account;
import unito.prog3.models.Mail;
import unito.prog3.utils.FilesManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server implements Runnable {

    ServerSocket clientbound;

    private static HashMap<String, Account> accounts;

    public Server(final int port) throws IOException {
        this.clientbound = new ServerSocket(port);
        this.accounts = new HashMap<String, Account>();
    }

    public Socket waitClient() throws IOException {
        return clientbound.accept();
    }

    public boolean accountExist(String username) {
        if (username == null)
            throw new IllegalArgumentException();

        return (accounts.containsKey(username));
    }

    public Account getAccount(String username) {
        return (accounts.get(username));
    }

    public void loadUsersList() throws Exception {
        ArrayList<Account> accountList = FilesManager.extractUsers();
        for (Account account : accountList) {
            loadUser(account);
        }
    }

    public void loadUser(Account acc) {
        if (acc == null)
            throw new IllegalArgumentException();

        accounts.put(acc.getUsername(), acc);
    }

    public String sendMail(Mail msg) throws IOException {
        ArrayList<String> dests = msg.getDests();

        // src:title:<dst1:dst2>:content
        if (dests == null || msg == null)
            throw new IllegalArgumentException();

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

    @Override
    public void run() {
        Thread.currentThread().setName("Server");

        System.out.println("[SERVER]: START WAITING FOR CLIENTS...");
        while (true) {
            try {
                Socket newClient = waitClient();
                HandleClient hc = new HandleClient(newClient, this);
                hc.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
