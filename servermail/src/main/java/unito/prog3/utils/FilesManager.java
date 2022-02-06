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
    public static final String SENT_FILENAME = "sent.txt";

    public FilesManager() {

    }

    // File methods

    public synchronized static void createFiles(String username)
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

    public synchronized static boolean dirExist(String username)
            throws IllegalArgumentException {

        if (username == null)
            throw new IllegalArgumentException();

        return Files.exists(Paths.get(USERS_DIR_PATH + username));
    }

    public synchronized static void addUserToFile(Account new_user)
            throws IOException {

        if (new_user == null)
            throw new IllegalArgumentException();

        File usersfile = new File(USERS_FILE_PATH);

        if (!usersfile.exists())
            throw new FileNotFoundException("[File not found]: Cannot add user, users file does not exist");

        PrintWriter writer = new PrintWriter(new FileWriter(usersfile, true));
        writer.append(new_user.getUsername() + ":" + new_user.getPassword() + "\n");

        writer.close();
    }

    public synchronized static ArrayList<Account> getUsers()
            throws IOException {
        String line = null;

        File usersFile = new File(USERS_FILE_PATH);

        // Check for users empty
        if (usersFile.length() == 0)
            return null;

        // If not empty, extract all users
        BufferedReader buff = new BufferedReader(new FileReader(usersFile));

        ArrayList<Account> userlist = new ArrayList<>();

        // Loop over lines and
        while ((line = buff.readLine()) != null) {
            String[] splitted = line.split(":");

            userlist.add(new Account(splitted[0], splitted[1]));
        }

        // Closing buffers
        buff.close();

        return userlist;
    }

    // Mailboxes methods
    public synchronized static ArrayList<Mail> getMailBox(String mailbox_name, String username)
            throws IOException {

        if (mailbox_name == null || username == null)
            throw new IllegalArgumentException();

        String line = null;

        ArrayList<Mail> mailbox = new ArrayList<>();

        // Check for user directory exists
        if (!dirExist(username))
            throw new FileNotFoundException("[File not found]: Impossible to get Mailbox");

        // Check for requested file exist
        File mbox = new File(USERS_DIR_PATH + username + "/" + mailbox_name + ".txt");
        if (!(mbox.exists()))
            mbox.createNewFile();

        // If mailbox is empty
        if (mbox.length() == 0)
            return null;

        // If not empty, extract all users
        BufferedReader buff = new BufferedReader(new FileReader(mbox));

        // Loop over lines
        Mail actMail = null;
        while ((line = buff.readLine()) != null) {
            actMail = new Mail();
            actMail.setBelonging(mailbox_name);

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

    public synchronized static void rmMailFromMailbox(String owner, Mail toremove)
            throws IOException {

        if (owner == null || toremove == null)
            throw new IllegalArgumentException();

        String mailboxName = toremove.getBelonging();
        String lineToRemove = toremove.toString();

        // Creating new file
        String aux_path = USERS_DIR_PATH + owner + mailboxName + "_n.txt";
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

    public synchronized static void insMailToMailbox(String owner, Mail toinsert)
            throws IOException {

        if (toinsert == null || owner == null)
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

    public synchronized static void moveMail(String owner, Mail tomove)
            throws IOException {

        if (tomove == null)
            throw new IllegalArgumentException();

        // Remove mail from source file
        rmMailFromMailbox(owner, tomove);

        // Adding mail to dest file
        insMailToMailbox(owner, tomove);
    }
}

