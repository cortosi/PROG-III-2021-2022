package unito.prog3.servermail;

public class MailServer {

    private final static int DEFAULT_PORT = 1998;

    public static void main(String[] args) throws Exception {
        Server server = new Server(DEFAULT_PORT);
        server.loadUsersList();
        server.run();
    }
}