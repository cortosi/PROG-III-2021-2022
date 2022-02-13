package unito.prog3.models;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Mail implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String source;
    private String object;
    private String content;
    private String belonging = "inbox";
    private String moveto;
    private Mail prec;
    private ArrayList<String> dests;
    private int read = 0;
    private Date date;

    public Mail() {

    }

    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getBelonging() {
        return belonging;
    }

    public String getMoveto() {
        return moveto;
    }

    public String getContent() {
        return content;
    }

    public ArrayList<String> getDests() {
        return dests;
    }

    public String getSource() {
        return source;
    }

    public String getObject() {
        return object;
    }

    public Mail getPrec() {
        return prec;
    }

    public int getRead() {
        return read;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDests(ArrayList<String> dests) {
        this.dests = dests;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public void setBelonging(String belonging) {
        this.belonging = belonging;
    }

    public void setMoveto(String moveto) {
        this.moveto = moveto;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPrec(Mail prec) {
        this.prec = prec;
    }


    @Override
    public String toString() {
        return "Mail{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", object='" + object + '\'' +
                ", content='" + content + '\'' +
                ", belonging='" + belonging + '\'' +
                ", moveto='" + moveto + '\'' +
                ", prec=" + prec +
                ", dests=" + dests +
                ", read=" + read +
                ", date=" + date +
                '}';
    }
}
