package unito.prog3.utils;

import unito.prog3.models.Account;
import unito.prog3.models.Mail;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class FilesManager {

    public static final String USERS_FILE_PATH = "files/users.txt";
    public static final String USERS_DIR_PATH = "files/";
    public static final String INBOX_FILENAME = "inbox.txt";
    public static final String SPAM_FILENAME = "spam.txt";
    public static final String TRASH_FILENAME = "trash.txt";

    public FilesManager() {

    }

    public static void createUserFiles(String username, String password) throws Exception {
        if (username == null ||
                password == null)
            throw new Exception();

        File users = new File(USERS_FILE_PATH);

        // Adding user into users file list
        BufferedWriter buff = new BufferedWriter(new PrintWriter(users));
        buff.append(username + ":" + password + "\n").flush();

        // Creating User dir for the user
        Files.createDirectories(Paths.get(USERS_DIR_PATH + username));

        // Creating messages file for the user
        File file = new File(USERS_DIR_PATH + username + "/" + INBOX_FILENAME);
        file.createNewFile();

        // Closing buffers
        buff.close();
    }

    public static ArrayList<Account> extractUsers() throws IOException {
        String line = null;

        File usersFile = new File(USERS_FILE_PATH);
        ArrayList<Account> userlist = new ArrayList<>();

        // Check for users empty
        if (usersFile.length() == 0)
            return null;

        // If not empty, extract all users
        BufferedReader buff = new BufferedReader(new FileReader(usersFile));

        // Loop over lines and
        while ((line = buff.readLine()) != null) {
            String[] splitted = line.split(":");

            userlist.add(new Account(splitted[0], splitted[1]));
        }

        // Closing buffers
        buff.close();

        return userlist;
    }

    public static void addUserToFile(Account new_user) throws IOException {
        if (new_user == null)
            throw new IllegalArgumentException();

        File usersfile = new File(USERS_FILE_PATH);
        BufferedWriter buff = new BufferedWriter(new FileWriter(usersfile, true));
        buff.write(new_user.getUsername() + ":" + new_user.getPassword() + "\n");
        buff.close();
    }

    public static boolean dirExist(String username) {
        return Files.exists(Paths.get(USERS_DIR_PATH + username));
    }

    public static boolean inboxExist(String username) {
        return Files.exists(Paths.get(USERS_DIR_PATH + username + "/" + INBOX_FILENAME));
    }

    public static File getInboxFile(String username) {
        if (dirExist(username))
            if (inboxExist(username))
                return new File(USERS_DIR_PATH + username + "/" + INBOX_FILENAME);
        return null;
    }

    // Mailboxes methods
    public static ArrayList<Mail> getMailBox(String mbox_name, String username) throws Exception {
        String line = null;

        ArrayList<Mail> mailbox = new ArrayList<>();

        // Check for user directory exists
        File userDir = new File(USERS_DIR_PATH + username);
        if (!(userDir.exists() && userDir.isDirectory()))
            throw new Exception();

        // Check for requested file exist
        File mbox = new File(USERS_DIR_PATH + username + "/" + mbox_name + ".txt");
        if (!userDir.exists())
            throw new Exception();

        if (mbox.length() == 0)
            return null;

        // If not empty, extract all users
        BufferedReader buff = new BufferedReader(new FileReader(mbox));

        // Loop over lines
        Mail actMail = null;
        while ((line = buff.readLine()) != null) {
            actMail = new Mail();
            actMail.setBelonging(mbox_name);

            String[] splitted = line.split(":");

            actMail.setSource(splitted[0]);
            actMail.setObject(splitted[1]);
            actMail.setContent(splitted[splitted.length - 1]);

            String[] destSplit = splitted[2].replaceAll("[<>]", "").split(",");

            actMail.setDests(new ArrayList<>(Arrays.asList(destSplit)));

            mailbox.add(actMail);
        }

        // Closing buffers
        buff.close();

        return mailbox;
    }

    public static void rmMailFromMailbox(String owner, Mail toremove) throws IOException {
        String mailboxName = toremove.getBelonging();
        String lineToRemove = toremove.toString();

        // Aux file
        String aux_path = USERS_DIR_PATH + owner + "/inboxt.txt";
        File newMailbox = new File(aux_path);

        // Building mailbox path
        File oldMailbox = new File(USERS_DIR_PATH + owner + "/" + mailboxName + ".txt");

        BufferedReader oldMailboxBuff = new BufferedReader(new FileReader(oldMailbox));
        BufferedWriter newMailboxBuff = new BufferedWriter(new FileWriter(newMailbox));

        String line = null;
        while ((line = oldMailboxBuff.readLine()) != null) {
            if (!line.equals(lineToRemove)) {
                newMailboxBuff.write(line);
                newMailboxBuff.newLine();
            }
        }

        newMailboxBuff.close();
        oldMailboxBuff.close();

        oldMailbox.delete();
        newMailbox.renameTo(oldMailbox);
    }

    public static void insMailToMailbox(String owner, Mail toinsert) throws IOException {
        if (toinsert == null || toinsert.getMoveto() == null)
            throw new IllegalArgumentException();

        // Building file dest path
        String fdest_path = USERS_DIR_PATH +
                owner + "/" +
                toinsert.getMoveto() + ".txt";

        PrintWriter writer = new PrintWriter(new FileWriter(fdest_path, true));
        writer.append(toinsert + "\n");

        //
        writer.close();
    }

    public static void moveMail(String owner, Mail tomove) throws IOException {
        if (tomove == null)
            throw new IllegalArgumentException();

        // Remove mail from source file
        rmMailFromMailbox(owner, tomove);

        // Adding mail to dest file
        insMailToMailbox(owner, tomove);
    }
}

