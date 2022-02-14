package unito.prog3.servermail;


import unito.prog3.controllers.Controller;
import unito.prog3.models.Account;
import unito.prog3.utils.FilesManager;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static unito.prog3.utils.FilesManager.USERS_DIR_PATH;

public class Server implements Runnable {

    private final int NOTIFY_PORT = 2021;

    private final ServerSocket clientSocket;
    private final ServerSocket notifySocket;
    private final Controller controller;

    private static HashMap<String, Account> accounts;

    public Server(final int port, Controller controller)
            throws IOException {
        this.clientSocket = new ServerSocket(port);
        this.notifySocket = new ServerSocket(NOTIFY_PORT);
        this.controller = controller;
        accounts = new HashMap<>();
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

    public void loadUsersList() {
        ArrayList<Account> accountList = FilesManager.getUsers();

        if (accountList == null)
            return;

        for (Account account : accountList) {
            accounts.put(account.getUsername(), account);
        }
    }

    public void loadUser(Account acc) {
        if (acc == null)
            throw new IllegalArgumentException();

        if (!accounts.containsKey(acc.getUsername()))
            accounts.put(acc.getUsername(), acc);
    }

    public void serverSetUp() throws IOException {
        // Check for main dir files
        if (!(Files.exists(Paths.get(USERS_DIR_PATH)))) {
            // Creating 'files' dir
            Files.createDirectories(Paths.get(USERS_DIR_PATH));
        }

        loadUsersList();

        for (String user : accounts.keySet()) {
            String userdir = (USERS_DIR_PATH + user);
            if (!(Files.exists(Paths.get(userdir)))) {
                // Creating 'files' dir
                FilesManager.createFiles(user);
            }
        }

    }

    @Override
    public void run() {
        Thread.currentThread().setName("Server");
        try {
            serverSetUp();
        } catch (IOException e) {
            e.printStackTrace();
        }

        controller.log("[SERVER]: START WAITING FOR CLIENTS...\n");
        while (true) {
            try {
                Socket newClient = waitClient();
                new Thread(new HandleClient(newClient, this, controller)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
