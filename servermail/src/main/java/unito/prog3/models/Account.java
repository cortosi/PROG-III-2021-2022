package unito.prog3.models;


import java.io.Serializable;
import java.util.ArrayList;

public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private ArrayList<Mail> mail;

    public Account() {

    }


    public Account(String username, String password) {
        this.username = username;
        this.password = password;
        mail = null;
    }

    public Account(String username, String password,
                   ArrayList<Mail> mail) {
        this.username = username;
        this.password = password;
        this.mail = mail;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<Mail> getMessages() {
        return mail;
    }


    public void setMessages(ArrayList<Mail> mail) {
        this.mail = mail;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMail(ArrayList<Mail> mail) {
        this.mail = mail;
    }
    
    @Override
    public String toString() {
        return "Account{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", mail=" + mail +
                '}';
    }
}
