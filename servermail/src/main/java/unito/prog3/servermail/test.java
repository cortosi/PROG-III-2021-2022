package unito.prog3.servermail;

import unito.prog3.models.Account;
import unito.prog3.utils.FilesManager;

import java.io.IOException;
import java.nio.file.Files;

public class test {
    public static void main(String[] args) throws IOException {
        System.out.println(FilesManager.getMailBox("inbox", "admin"));
    }
}
