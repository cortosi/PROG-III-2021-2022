package unito.prog3.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import unito.prog3.models.Account;
import unito.prog3.models.Mail;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Static class
public class FilesManager {

    public static final String USERS_FILE_PATH = "files/users.json";
    public static final String SERVER_FILE_PATH = "files/server.json";
    public static final String USERS_DIR_PATH = "files/";
    public static final String INBOX_FILENAME = "inbox.json";
    public static final String SPAM_FILENAME = "spam.json";
    public static final String TRASH_FILENAME = "trash.json";
    public static final String SENT_FILENAME = "sent.json";

    private static final Map<File, Lock> FILE_LOCKS = new ConcurrentHashMap<>();

    // File methods

    public static void createFiles(String username)
            throws IllegalArgumentException, IOException {

        if (username == null)
            throw new IllegalArgumentException("[Illegal Argument]: username to be created must be not null");

        // Creating User dir
        Files.createDirectories(Paths.get(USERS_DIR_PATH + username));

        // Creating inboxes files
        File inbox = new File(USERS_DIR_PATH + username + "/" + INBOX_FILENAME);
        File spam = new File(USERS_DIR_PATH + username + "/" + SPAM_FILENAME);
        File sent = new File(USERS_DIR_PATH + username + "/" + SENT_FILENAME);
        File trash = new File(USERS_DIR_PATH + username + "/" + TRASH_FILENAME);

        inbox.createNewFile();
        spam.createNewFile();
        sent.createNewFile();
        trash.createNewFile();
    }

    public static boolean dirExist(String username)
            throws IllegalArgumentException {

        if (username == null)
            throw new IllegalArgumentException();

        return Files.exists(Paths.get(USERS_DIR_PATH + username));
    }

    public static boolean addUserToFile(Account new_user)
            throws IOException {

        JSONObject json = null;
        JSONArray values = null;

        if (new_user == null)
            throw new IllegalArgumentException();

        ArrayList<Account> accounts = getUsers();

        for (Account acc : accounts) {
            if (acc.getUsername().equals(new_user.getUsername())) {
                return false;
            }
        }
        accounts.add(new_user);

        new ObjectMapper().writeValue(new File(USERS_FILE_PATH), accounts);

        return true;
    }

    public static ArrayList<Account> getUsers() {

        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Account> users;

        File json = new File(USERS_FILE_PATH);

        try {
            users = mapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            users = new ArrayList<>();
        }

        return users;
    }

    // Mailboxes methods
    public static ArrayList<Mail> getMailBox(String username, String mailbox_name) {

        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Mail> mailbox;

        if (mailbox_name == null || username == null)
            throw new IllegalArgumentException();

        File json = new File(USERS_DIR_PATH + username + "/" + mailbox_name + ".json");
        try {
            mailbox = mapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            mailbox = new ArrayList<>();
        }

        return mailbox;
    }

    public static void rmMailFromMailbox(String user, Mail toRemove)
            throws Exception {

        boolean removed = false;
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Mail> mailBox;

        if (toRemove == null || user == null)
            throw new IllegalArgumentException();

        File json = new File(USERS_DIR_PATH + user + "/" + toRemove.getBelonging() + ".json");

        synchronized (FILE_LOCKS.computeIfAbsent(json, k -> new ReentrantLock())) {
            try {
                mailBox = mapper.readValue(json, new TypeReference<>() {
                });
            } catch (IOException e) {
                mailBox = new ArrayList<>();
            }
            for (int i = 0; i < mailBox.size() && !removed; i++) {
                if (mailBox.get(i).getId() == toRemove.getId()) {
                    mailBox.remove(i);
                    removed = true;
                }
            }
            if (removed)
                new ObjectMapper().writeValue(json, mailBox);
            else
                throw new Exception("Email Not found");
        }
    }

    public static void insMailToMailbox(String user, Mail toInsert)
            throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Mail> mailBox;

        if (toInsert == null || user == null)
            throw new IllegalArgumentException();

        try {
            toInsert.setId(getLastID());
        } catch (IOException e) {
            toInsert.setId(0);
        }
        toInsert.setBelonging(toInsert.getMoveto());

        File json = new File(USERS_DIR_PATH + user + "/"
                + toInsert.getMoveto() + ".json");

        synchronized (FILE_LOCKS.computeIfAbsent(json, k -> new ReentrantLock())) {
            try {
                mailBox = mapper.readValue(json, new TypeReference<>() {
                });
            } catch (IOException e) {
                mailBox = new ArrayList<>();
            }

            for (int i = 0; i < mailBox.size(); i++) {
                if (mailBox.get(i).getId() == toInsert.getId()) {
                    mailBox.set(i, toInsert);
                    return;
                }
            }

            mailBox.add(toInsert);
            new ObjectMapper().writeValue(json, mailBox);
        }

        //
        incLastID();
    }

    public static void addSentMail(String user, Mail toInsert)
            throws IOException {

        if (toInsert == null || user == null)
            throw new IllegalArgumentException();

        ArrayList<Mail> sentMailBox = new ArrayList<>();
        File destFile = new File(USERS_DIR_PATH + user + "/sent.json");

        synchronized (FILE_LOCKS.computeIfAbsent(destFile, k -> new ReentrantLock())) {
            sentMailBox = getMailBox(user, "sent");
            sentMailBox.add(toInsert);
            new ObjectMapper().writeValue(destFile, sentMailBox);
        }
    }

    public static void moveMail(String user, Mail toMove)
            throws Exception {

        if (toMove == null)
            throw new IllegalArgumentException();

        // Remove mail from source file
        rmMailFromMailbox(user, toMove);

        // Adding mail to dest file
        insMailToMailbox(user, toMove);
    }

    public static void replyMail(String user, Mail msg)
            throws IOException {

        if (msg == null || user == null)
            throw new IllegalArgumentException();

        System.out.println(msg);
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Mail> mailBox;

        int precID = msg.getPrec().getId();

        File json = new File(USERS_DIR_PATH + user + "/inbox.json");

        synchronized (FILE_LOCKS.computeIfAbsent(json, k -> new ReentrantLock())) {
            try {
                mailBox = mapper.readValue(json, new TypeReference<>() {
                });
            } catch (IOException e) {
                mailBox = new ArrayList<>();
            }

            for (int i = 0; i < mailBox.size(); i++) {
                if (mailBox.get(i).getId() == precID) {
                    mailBox.remove(i);
                }
            }

            mailBox.add(msg);
            System.out.println(mailBox);
            new ObjectMapper().writeValue(json, mailBox);
        }
        //
        incLastID();
    }

    public static int getLastID()
            throws IOException {
        File json = new File(SERVER_FILE_PATH);

        HashMap<String, Object> settings = new ObjectMapper().readValue(json, new TypeReference<>() {
        });

        if (settings.containsKey("ID")) {
            return (Integer) settings.get("ID");
        }

        return 0;
    }

    public static void incLastID()
            throws IOException {
        HashMap<String, Object> settings = new HashMap<>();

        File json = new File(SERVER_FILE_PATH);
        try {
            settings = new ObjectMapper().readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            //error check to do

            e.printStackTrace();
        }
        settings.replace("ID", getLastID() + 1);

        new ObjectMapper().writeValue(json, settings);
    }
}

