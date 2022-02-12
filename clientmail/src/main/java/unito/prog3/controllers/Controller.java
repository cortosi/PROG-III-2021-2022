package unito.prog3.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import unito.prog3.clientmail.Connection;
import unito.prog3.clientmail.MailClient;
import unito.prog3.models.Account;
import unito.prog3.models.Mail;
import unito.prog3.utils.Security;

public class Controller {

    //
    private Account my_acc;
    private ArrayList<Mail> msglist;
    private Mail selectedMail;
    private VBox lastFocussed;

    // Connection Interface
    private Connection connection;

    // GP variables
    private static final String IPV4_PATTERN = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";

    private static final String PORT_PATTERN = "^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";

    private static final String NETWORK_PATTERN = "^([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]):([0-9]+)$";

    private static final String EMAIL_PATTERN = "^([a-z]|[A-Z])\\w{0,20}$";

    private final int folders_section_width = 200;

    private final Image can_send = new Image(Objects.requireNonNull(MailClient.class.getResource("imgs/can_send.png")).toString());
    private final Image cannot_send = new Image(Objects.requireNonNull(MailClient.class.getResource("imgs/cannot_send.png")).toString());

    // Regions

    private final Rectangle folders_section_clip = new Rectangle();

    private final Rectangle login_wrong_input_clip = new Rectangle();

    private final Rectangle signup_wrong_input_clip = new Rectangle();

    // Transition/Animation Objs
    RotateTransition rotateLoginLoader = new RotateTransition();

    private final EmptyMailBox empty_msg_list = new EmptyMailBox();

    //FXML Objs
    @FXML
    private ScrollPane mail_content_replies_outer;

    @FXML
    private ImageView reply_msg_send_btn;

    @FXML
    private VBox mail_content_replies_wrap;

    @FXML
    private VBox reply_mail_wrap;

    @FXML
    private AnchorPane folders_section;

    @FXML
    private StackPane folders_section_wrap;

    @FXML
    private Label head_username;

    @FXML
    private Pane login_loader;

    @FXML
    private AnchorPane login_logo_wrap;

    @FXML
    private TextField login_net_field;

    @FXML
    private AnchorPane login_outer;

    @FXML
    private TextField login_psw_field;

    @FXML
    private ImageView login_submit_btn;

    @FXML
    private TextField login_usr_field;

    @FXML
    private Label login_wrong_input;

    @FXML
    private AnchorPane mail_content;

    @FXML
    private Label mail_content_date;

    @FXML
    private Label mail_content_sender;

    @FXML
    private Label mail_content_title;

    @FXML
    private Label mail_content_to;

    @FXML
    private Label mail_description;

    @FXML
    private Label mail_head_sender_icon;

    @FXML
    private BorderPane main;

    @FXML
    private HBox main_content_head;

    @FXML
    private AnchorPane main_window;

    @FXML
    private AnchorPane new_mail_outer;

    @FXML
    private VBox new_mail_wrap;

    @FXML
    private TextArea new_msg_content;

    @FXML
    private TextArea reply_msg_content;

    @FXML
    private TextField new_msg_obj_datafield;

    @FXML
    private ImageView new_msg_send_btn;

    @FXML
    private TextField new_msg_to_datafield;

    @FXML
    private VBox no_selected_mail_wrap;

    @FXML
    private VBox reply_area;

    @FXML
    private Label reply_box_sender;

    @FXML
    private Label reply_box_sender_icon;

    @FXML
    private Label reply_box_title;

    @FXML
    private AnchorPane reply_box_window;

    @FXML
    private AnchorPane reply_mail_window;

    @FXML
    private Label reply_mail_head_desc;

    @FXML
    private TextField reply_msg_obj_datafield;

    @FXML
    private TextField reply_msg_to_datafield;

    @FXML
    private VBox selected_mail_list;

    @FXML
    private Label selected_msg_n;

    @FXML
    private Label show_folders_btn;

    @FXML
    private Label show_signup_btn;

    @FXML
    private AnchorPane signup_logo_wrap;

    @FXML
    private AnchorPane signup_outer;

    @FXML
    private TextField signup_psw_field;

    @FXML
    private TextField signup_usr_field;

    @FXML
    private Label signup_wrong_input;

    // Login/Signup
    @FXML
    public void login() {
        new Thread(new loginThread()).start();
    }

    @FXML
    public void signUp() {
        new Thread(new signUpThread()).start();
    }

    @FXML
    public void hideLoginWindow() {
        login_submit_btn.setVisible(false);

        Circle clip = new Circle();
        clip.setCenterX(login_outer.getWidth() / 2);
        clip.setCenterY(login_outer.getHeight() / 2);
        clip.radiusProperty().set(login_outer.getWidth());
        login_outer.setClip(clip);

        Timeline tl_login_fadeout = new Timeline(new KeyFrame(Duration.millis(450),
                new KeyValue(clip.radiusProperty(), 0),
                new KeyValue(main.opacityProperty(), 1)));

        tl_login_fadeout.setOnFinished(
                e -> {
                    login_outer.setVisible(false);
                    signup_outer.setVisible(false);
                    login_outer.setManaged(false);
                    signup_outer.setManaged(false);
                    // Removing Clip
                    login_outer.setClip(null);
                });

        tl_login_fadeout.play();
    }

    @FXML
    public void signup_transition(MouseEvent e) {
        if (e.getSource() == show_signup_btn)
            show_signup_section();
        else
            show_login_section();
    }

    public void show_signup_section() {
        login_outer.setVisible(false);
        signup_outer.setVisible(true);
    }

    public void show_login_section() {
        signup_outer.setVisible(false);
        login_outer.setVisible(true);
    }

    public void show_login_wrong(String text) {
        login_wrong_input.setText(text);

        Timeline tl = new Timeline(new KeyFrame(Duration.millis(400),
                new KeyValue(login_logo_wrap.translateYProperty(), 0),
                new KeyValue(login_wrong_input.prefHeightProperty(), 60),
                new KeyValue(login_wrong_input.opacityProperty(), 1)
        ));

        tl.play();
    }

    public void show_signup_wrong(String text) {
        signup_wrong_input.setText(text);

        Timeline tl = new Timeline(new KeyFrame(Duration.millis(400),
                new KeyValue(signup_logo_wrap.translateYProperty(), 0),
                new KeyValue(signup_wrong_input.prefHeightProperty(), 60),
                new KeyValue(signup_wrong_input.opacityProperty(), 1)
        ));

        tl.play();
    }

    // Side section
    @FXML
    private void close_folders() {
        // Create a clip and apply on the pane to hide
        folders_section_clip.setWidth(folders_section_width);
        folders_section_clip.heightProperty().bind(folders_section.heightProperty());
        folders_section.setClip(folders_section_clip);

        Animation animation = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(folders_section_wrap.prefWidthProperty(), 0),
                        new KeyValue(folders_section.translateXProperty(),
                                -folders_section_width),
                        new KeyValue(folders_section_clip.translateXProperty(),
                                folders_section_width)));

        animation.setOnFinished(e -> {
            // Hiding the panes at the end
            folders_section_wrap.setVisible(false);
            folders_section.setVisible(false);
        });

        show_folders_btn.setVisible(true);

        animation.play();
    }

    @FXML
    public void show_folders() {
        // Restore Visibility
        folders_section_wrap.setVisible(true);
        folders_section.setVisible(true);

        Animation animation = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(folders_section_wrap.prefWidthProperty(),
                                folders_section_width),
                        new KeyValue(folders_section.translateXProperty(), 0),
                        new KeyValue(folders_section_clip.translateXProperty(), 0)));
        animation.play();

        show_folders_btn.setVisible(false);
    }

    @FXML
    public void onFolderClick(MouseEvent me) {
        HBox hb = (HBox) me.getSource();
        Label l = (Label) hb.getChildren().get(1);

        clearMailboxList();

        new Thread(new refreshMailbox(l.getText().toLowerCase())).start();

        // Closing folder section
        close_folders();
    }

    // New Mail Window
    @FXML
    public void show_new_mail() {
        new_mail_wrap.translateYProperty().set(main_window.getHeight());

        new_mail_outer.setVisible(true);

        Animation animation = new Timeline(
                new KeyFrame(Duration.millis(100),
                        new KeyValue(new_mail_outer.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(150),
                        new KeyValue(new_mail_wrap.translateYProperty(), 0)),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(new_mail_wrap.opacityProperty(), 1)));

        animation.play();
    }

    @FXML
    public void hide_new_mail() {
        Animation animation = new Timeline(
                new KeyFrame(Duration.millis(100),
                        new KeyValue(new_mail_wrap.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(150),
                        new KeyValue(new_mail_wrap.translateYProperty(),
                                main_window.getHeight())),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(new_mail_outer.opacityProperty(), 0)));

        animation.setOnFinished(e -> new_mail_outer.setVisible(false));

        animation.play();
    }

    @FXML
    private void send_new_mail(MouseEvent e) {
        if (e.getSource() == new_msg_send_btn)
            new Thread(new sendMailThread(false)).start();
        else
            new Thread(new sendMailThread(true)).start();
    }

    // Reply Box Functions
    @FXML
    void mailDelete(MouseEvent event) {

    }

    @FXML
    void mailForward(MouseEvent event) {

    }

    @FXML
    void mailReply(MouseEvent event) {
        clearReplyMail();
        fillReplyMail();
        showReplyWindow();
    }

    @FXML
    void mailReplyAll(MouseEvent event) {

    }

    // 
    public void clearMailboxList() {
        // Clearing Mail List (GUI)
        selected_mail_list.getChildren().clear();
    }

    public void clearNewMail() {
        new_msg_to_datafield.clear();
        new_msg_obj_datafield.clear();
        new_msg_content.clear();
    }

    public void clearReplyMail() {
        reply_msg_to_datafield.clear();
        reply_msg_obj_datafield.clear();
        reply_msg_content.clear();
        reply_mail_head_desc.setText("");
        reply_area.getChildren().remove(1, reply_area.getChildren().size());
    }

    public void clearSelectedMail() {
        //
        no_selected_mail_wrap.setVisible(false);
        mail_content_replies_outer.setVisible(true);
        main_content_head.setVisible(true);
        mail_content_replies_wrap.getChildren().clear();
    }

    public void showEmptySelectedMail() {
        mail_content_replies_outer.setVisible(false);
        mail_content_replies_wrap.getChildren().clear();
        no_selected_mail_wrap.setVisible(true);
    }

    public void fillReplyMail() {
        reply_msg_to_datafield.setText(selectedMail.getSource());
        reply_msg_obj_datafield.setText("RE: " + selectedMail.getObject());
        reply_mail_head_desc.setText("RE: " + selectedMail.getObject());
        //
        Mail act = selectedMail.getPrec();
        while (act != null) {
            reply_area.getChildren().add(new ReplyItem(act));
            act = act.getPrec();
        }
    }

    // Reply
    @FXML
    public void openMailReplyBox() {
        reply_box_sender_icon.setText(selectedMail.getSource().charAt(0) + "");
        reply_box_sender.setText(selectedMail.getSource());
        reply_box_title.setText(selectedMail.getObject());
        reply_box_window.setVisible(true);
    }

    @FXML
    public void closeMailReplyBox() {
        reply_box_window.setVisible(false);
    }

    @FXML
    public void deleteSelectedMail() {
        if (selectedMail.getBelonging().equals("trash")) {
            showEmptySelectedMail();
            new Thread(new delMailThread(selectedMail)).start();
        } else {
            selectedMail.setMoveto("trash");
            showEmptySelectedMail();
            new Thread(new moveMailThread(selectedMail)).start();
        }
    }

    @FXML
    public void hide_reply_mail() {
        Animation animation = new Timeline(
                new KeyFrame(Duration.millis(100),
                        new KeyValue(reply_mail_wrap.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(150),
                        new KeyValue(reply_mail_wrap.translateYProperty(),
                                main_window.getHeight())),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(reply_mail_window.opacityProperty(), 0)));

        animation.setOnFinished(e -> reply_mail_window.setVisible(false));

        animation.play();
    }

    public void showReplyWindow() {
        reply_mail_wrap.translateYProperty().set(main_window.getHeight());

        reply_mail_window.setVisible(true);

        Animation animation = new Timeline(
                new KeyFrame(Duration.millis(100),
                        new KeyValue(reply_mail_window.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(150),
                        new KeyValue(reply_mail_wrap.translateYProperty(), 0)),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(reply_mail_wrap.opacityProperty(), 1)));

        animation.play();
    }

    @FXML
    public void initialize() {
        // Binding min/max to pref, to not allow the panes width change.
        folders_section_wrap.minWidthProperty().bind(folders_section_wrap.prefWidthProperty());
        folders_section_wrap.maxWidthProperty().bind(folders_section_wrap.prefWidthProperty());

        // LOGIN WRONG
        login_logo_wrap.setTranslateY(35);

        login_wrong_input_clip.heightProperty().bind(login_wrong_input.prefHeightProperty());
        login_wrong_input_clip.widthProperty().bind(login_wrong_input.prefWidthProperty());
        login_wrong_input.setClip(login_wrong_input_clip);

        login_wrong_input.prefHeightProperty().set(0);

        // SIGNUP WRONG
        signup_logo_wrap.setTranslateY(35);

        signup_wrong_input_clip.heightProperty().bind(signup_wrong_input.prefHeightProperty());
        signup_wrong_input_clip.widthProperty().bind(signup_wrong_input.prefWidthProperty());
        signup_wrong_input.setClip(signup_wrong_input_clip);

        signup_wrong_input.prefHeightProperty().set(0);

        //
        rotateLoginLoader.byAngleProperty().set(360);
        rotateLoginLoader.setCycleCount(Animation.INDEFINITE);
        rotateLoginLoader.setDuration(Duration.millis(1000));
        rotateLoginLoader.setNode(login_loader);

        //
        new_msg_content.textProperty().addListener(e -> {
            if (new_msg_content.getText().length() == 0) {
                new_msg_send_btn.setImage(cannot_send);
                new_msg_send_btn.getStyleClass().removeAll("can-send");
            } else {
                new_msg_send_btn.setImage(can_send);
                new_msg_send_btn.getStyleClass().add("can-send");
            }
        });

        reply_msg_content.textProperty().addListener(e -> {
            if (reply_msg_content.getText().length() == 0) {
                reply_msg_send_btn.setImage(cannot_send);
                reply_msg_send_btn.getStyleClass().removeAll("can-send");
            } else {
                reply_msg_send_btn.setImage(can_send);
                reply_msg_send_btn.getStyleClass().add("can-send");
            }
        });

        new_msg_to_datafield.textProperty().addListener(e -> {
            new_msg_to_datafield.getStyleClass().removeAll("new-mail-wrong-to");
        });
    }

    // Custom JavaFX Graphics
    public class MailItem extends VBox {
        private final Mail mailbind;
        private BooleanProperty focussed;

        public MailItem(Mail tobind) {
            super();
            this.mailbind = tobind;
        }

        public Mail getMailbind() {
            return mailbind;
        }
    }

    public class EmptyMailBox extends StackPane {

        private Label description;

        public EmptyMailBox() {
            super();
            this.getStyleClass().add("empty-msg-list");
            this.alignmentProperty().set(Pos.TOP_CENTER);

            // Label
            description = new Label("There is no messages");
            description.getStyleClass().add("empty-msg-list-description");
            this.getChildren().add(description);
        }
    }

    public class ReplyItem extends TextField {
        private final Mail reply;
        private BooleanProperty focussed;
        private static int spaced = 0;

        public ReplyItem(Mail reply) {
            super();
            this.reply = reply;
            Color rColor = Color.color(Math.random(), Math.random(), Math.random());
            this.setBorder(new Border(new BorderStroke(Color.RED, Color.RED, Color.RED, rColor,
                    BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY, new BorderWidths(3), Insets.EMPTY)));
            this.getStyleClass().add("reply-item");
            this.styleProperty().set(String.format("-fx-text-fill: %s;", toHexString(rColor)));
            this.setText(getReplyText());
            this.setEditable(false);
        }

        public Mail getReply() {
            return reply;
        }

        private static String toHexString(Color color) {
            int r = ((int) Math.round(color.getRed() * 255)) << 24;
            int g = ((int) Math.round(color.getGreen() * 255)) << 16;
            int b = ((int) Math.round(color.getBlue() * 255)) << 8;
            int a = ((int) Math.round(color.getOpacity() * 255));
            return String.format("#%08X", (r + g + b + a));
        }

        public String getReplyText() {
            return "on date: " + reply.getDate()
                    + " <" + reply.getSource() + ">" + " have wrote: "
                    + "\n" + reply.getContent();
        }
    }

    // Threads/Tasks
    public class refreshMailbox implements Runnable {

        private final String mailbox;
        private static final double MAIL_ITEM_MAX_H = 100.0;

        public refreshMailbox(String mailbox) {
            this.mailbox = mailbox;
        }

        @Override
        public void run() {
            try {
                getMailboxList();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void getMailboxList()
                throws IOException, ClassNotFoundException {

            msglist = connection.mailListRequest(mailbox);

            if (msglist == null || msglist.size() == 0) {
                showEmptySection();
            } else {
                Platform.runLater(() -> {
                    selected_msg_n.setText(msglist.size() + " messages");
                    selected_mail_list.getChildren().clear();
                    for (Mail mail : msglist) {
                        selected_mail_list.getChildren().
                                addAll(createMailItem(mail));
                    }
                });
            }
        }

        private void showEmptySection() {
            Platform.runLater(() -> {
                selected_msg_n.setText("");
                selected_mail_list.getChildren().clear();
                selected_mail_list.getChildren().add(empty_msg_list);
            });
        }

        private VBox createMailItem(Mail mail) {
            // MailItem
            MailItem new_msg = new MailItem(mail);
            new_msg.getStyleClass().add("mailbox-list-item");
            new_msg.setMaxHeight(MAIL_ITEM_MAX_H);

            // Context menu
            final ContextMenu contextMenu = new ContextMenu();
            contextMenu.setAutoHide(true);
            contextMenu.getStyleClass().add("mailbox-list-item-cm");

            // Sub Context menu
            final Menu move = new Menu("Move");
            MenuItem inbox = new MenuItem("Inbox");
            inbox.getStyleClass().addAll("mailbox-list-item-menu");
            MenuItem spam = new MenuItem("Spam");
            spam.getStyleClass().addAll("mailbox-list-item-menu");
            MenuItem trash = new MenuItem("Trash");
            trash.getStyleClass().addAll("mailbox-list-item-menu");

            move.getItems().addAll(inbox, spam, trash);
            move.getStyleClass().add("mailbox-list-item-menu");

            inbox.setOnAction(e -> {
                if (!(mail.getBelonging().equals("inbox"))) {
                    mail.setMoveto("inbox");
                    new Thread(new moveMailThread(mail)).start();
                }
            });
            trash.setOnAction(e -> {
                if (!(mail.getBelonging().equals("trash"))) {
                    mail.setMoveto("trash");
                    new Thread(new moveMailThread(mail)).start();
                }
            });
            spam.setOnAction(e -> {
                if (!(mail.getBelonging().equals("spam"))) {
                    mail.setMoveto("spam");
                    new Thread(new moveMailThread(mail)).start();
                }
            });

            MenuItem delete = new MenuItem("Delete");
            delete.getStyleClass().addAll("mailbox-list-item-menu", "mailbox-list-item-last");
            delete.setOnAction(e -> {
                if (!(mail.getBelonging().equals("trash"))) {
                    mail.setMoveto("trash");
                    new Thread(new moveMailThread(mail)).start();
                } else {
                    new Thread(new delMailThread(mail)).start();
                }
            });

            MenuItem reply = new MenuItem("Reply");
            reply.getStyleClass().add("mailbox-list-item-menu");

            MenuItem open = new MenuItem("Open");
            open.getStyleClass().add("mailbox-list-item-menu");

            contextMenu.getItems().addAll(open, reply, move, delete);

            new_msg.setOnContextMenuRequested(event -> {
                contextMenu.setY(event.getScreenY());
                contextMenu.setX(event.getScreenX());
                contextMenu.show(new_msg.getScene().getWindow());
            });

            // MailItemList content
            Label source_lb = new Label(mail.getSource());
            source_lb.getStyleClass().add("mailbox-list-item-source");

            Label title_lb = new Label(mail.getObject());
            title_lb.getStyleClass().add("mailbox-list-item-title");

            Label cont_lb = new Label(mail.getContent());
            cont_lb.getStyleClass().add("mailbox-list-item-content-preview");
            cont_lb.setMaxHeight(MAIL_ITEM_MAX_H / 2);

            AnchorPane read_section = new AnchorPane();
            read_section.getStyleClass().add("read-section");
//            Image dotIMG = new Image(String.valueOf(MailClient.class.getResource("imgs/dot.png")));
//            ImageView dot = new ImageView(dotIMG);
//            dot.maxHeight(10);
//            dot.prefWidth(10);
//            dot.getStyleClass().add("mailbox-list-item-dot");
//            read_section.getChildren().add(dot);
            new_msg.getChildren().addAll(read_section, source_lb, title_lb, cont_lb);

            new_msg.setOnMouseClicked(e -> {
                if (lastFocussed == null || lastFocussed != new_msg) {
                    if (lastFocussed != null)
                        lastFocussed.getStyleClass().remove("mailbox-list-item__focussed");
                    lastFocussed = new_msg;
                }
                new_msg.getStyleClass().add("mailbox-list-item__focussed");

                new Thread(new showMailThread(new_msg.getMailbind())).start();
            });

            return new_msg;
        }
    }

    public class loginThread implements Runnable {
        @Override
        public void run() {
            login_submit_btn.setVisible(false);
            try {
                show_login_loader();
                rotate_login_loader();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                login();
            });
        }

        private void login() {
            String inputUser = login_usr_field.getText();
            String inputPass = login_psw_field.getText();
            String inputNet = login_net_field.getText();

            String res = null;
            boolean passed = false;
            // regex for NetWork
            Pattern NeworkPattern = Pattern.compile(NETWORK_PATTERN, Pattern.CASE_INSENSITIVE);
            Matcher networkMatcher = NeworkPattern.matcher(inputNet);

            if (networkMatcher.find()) {
                // extracting network info
                String ipv4addr = inputNet.split(":")[0];
                String port = inputNet.split(":")[1];

                // regex for ipv4
                Pattern IPV4pattern = Pattern.compile(IPV4_PATTERN, Pattern.CASE_INSENSITIVE);
                Matcher IPV4matcher = IPV4pattern.matcher(ipv4addr);
                // regex for port
                Pattern PORTpattern = Pattern.compile(PORT_PATTERN, Pattern.CASE_INSENSITIVE);
                Matcher PORTmatcher = PORTpattern.matcher(port);

                if (IPV4matcher.find()) {
                    if (PORTmatcher.find()) {
                        try {
                            connection = new Connection(ipv4addr, Integer.parseInt(port));

                            // Login request
                            String auth = connection.login_request(new Account(inputUser, Security.encryptSHA(inputPass)));
                            if (auth != null) {
                                if (auth.equals("AUTH"))
                                    passed = true;

                                if (auth.equals("PSW_WRONG"))
                                    res = "Wrong password";

                                if (auth.equals("USR_WRONG"))
                                    res = "Username does not exist";
                            } else {
                                res = "Error during authentication";
                            }
                        } catch (IOException e) {
                            res = "Server Unreachable";
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        res = "Insert a valid port number (1 - 65535)";
                    }
                } else {
                    res = "Insert a valid IPV4 address (es. 140.123.90.16:1231)";
                }
            } else
                res = "Wrong network pattern: <ip_number> : <porn_number>\n(es. 192.168.1.1:1231)";

            if (passed) {
                my_acc = new Account(inputUser, Security.encryptSHA(inputPass));
                head_username.setText(my_acc.getUsername());
                login_submit_btn.setManaged(true);
                hideLoginWindow();
                new Thread(new refreshMailbox("inbox")).start();
            } else {
                assert res != null;
                hide_login_loader();
                login_submit_btn.setVisible(true);
                show_login_wrong(res);
                stop_login_loader();
            }
        }

        private void show_login_loader() {
            login_loader.setVisible(true);
        }

        private void rotate_login_loader() {
            rotateLoginLoader.play();
        }

        private void stop_login_loader() {
            rotateLoginLoader.stop();
        }

        private void hide_login_loader() {
            login_loader.setVisible(false);
        }
    }

    public class signUpThread implements Runnable {

        @Override
        public void run() {
            Platform.runLater(() -> {
                try {
                    signUp();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }

        private void signUp()
                throws ClassNotFoundException {

            // Extracting fields
            String username = signup_usr_field.getText();
            String password = signup_psw_field.getText();

            // Regex check
            Pattern emailPattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
            Matcher emailMatcher = emailPattern.matcher(username);

            if (emailMatcher.find()) {
                if (password.length() != 0) {
                    // Encryption
                    password = Security.encryptSHA(password);

                    Account new_acc = new Account(username, password);

                    String res = connection.signupRequest(new_acc);
                }
            } else
                show_signup_wrong("Wrong Username");
        }
    }

    public class sendMailThread implements Runnable {

        private boolean reply = false;

        public sendMailThread(boolean reply) {
            this.reply = reply;
        }

        @Override
        public void run() {
            try {
                if (reply) {
                    connection.replyMailRequest();
                    replyMail();
                } else {
                    connection.sendMailRequest();
                    sendMail();
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Impossibile to send email, try later");
            }
        }

        private void sendMail()
                throws IOException, ClassNotFoundException {

            Mail new_mail = new Mail();
            // Getting sending data
            String to = new_msg_to_datafield.getText();
            String content = new_msg_content.getText();
            String object = new_msg_obj_datafield.getText();

            String[] splitted = to.split(";");

            ArrayList<String> dests = new ArrayList<>(Arrays.asList(splitted));

            // Saving data
            new_mail.setSource(my_acc.getUsername());
            new_mail.setDests(dests);
            new_mail.setObject(object);
            new_mail.setContent(content);
            new_mail.setMoveto("inbox");

            // Sending Mail..
            String res = connection.sendMessage(new_mail);

            if (res.equals("OK")) {
                hide_new_mail();
                clearNewMail();
            } else if (res.equals("USR_NOT_EXIST")) {
                new_msg_to_datafield.getStyleClass().add("new-mail-wrong-to");
            }
        }

        private void replyMail()
                throws IOException, ClassNotFoundException {

            Mail older = selectedMail;
            Mail newer = new Mail();

            // Saving data
            newer.setSource(my_acc.getUsername());
            newer.setDests(new ArrayList<>(Arrays.asList(older.getSource())));
            newer.setObject(reply_msg_obj_datafield.getText());
            newer.setContent(reply_msg_content.getText());
            newer.setMoveto("inbox");

            newer.setPrec(older);

            System.out.println(newer);
            // Sending Mail..
            connection.sendMessage(newer);

//            if (res.equals("OK")) {
//                hide_new_mail();
//                clearNewMail();
//            } else if (res.equals("USR_NOT_EXIST")) {
//                new_msg_to_datafield.getStyleClass().add("new-mail-wrong-to");
//            }
        }
    }

    public class moveMailThread implements Runnable {

        private Mail toMove;

        public moveMailThread(Mail mail) {
            this.toMove = mail;
        }

        @Override
        public void run() {
            // Move request
            try {
                connection.moveMailRequest(toMove);
                Platform.runLater(() -> {
                    clearMailboxList();
                    new Thread(new refreshMailbox(toMove.getBelonging().toLowerCase())).start();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class delMailThread implements Runnable {

        private Mail toDelete;

        public delMailThread(Mail toDelete) {
            this.toDelete = toDelete;
        }

        @Override
        public void run() {
            // Move request
            try {
                connection.delMailRequest(toDelete);
                Platform.runLater(() -> {
                    clearMailboxList();
                    new Thread(new refreshMailbox(toDelete.getBelonging().toLowerCase())).start();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class showMailThread implements Runnable {

        private final Mail toShow;

        public showMailThread(Mail toShow) {
            this.toShow = toShow;
        }

        @Override
        public void run() {
            Platform.runLater(() -> {
                clearSelectedMail();
                fillMailContent();
                selectedMail = toShow;
            });
        }

        private void fillMailContent() {
            VBox mail = getNewMailReply(toShow);
            mail.getStyleClass().add("mail-content-last-reply");
            mail_content_replies_wrap.getChildren().add(mail);
            //
            Mail act = toShow.getPrec();
            while (act != null) {
                mail_content_replies_wrap.getChildren().add(getNewMailReply(act));
                act = act.getPrec();
            }
        }
    }

    private VBox getNewMailReply(Mail reply) {
        VBox n_reply = new VBox();
        n_reply.getStyleClass().add("mail-content");

        HBox mail_head = new HBox();
        mail_head.getStyleClass().add("mail-head");
        mail_head.setAlignment(Pos.CENTER_LEFT);

        AnchorPane.setTopAnchor(mail_head, 0.0);
        AnchorPane.setLeftAnchor(mail_head, 0.0);
        AnchorPane.setRightAnchor(mail_head, 0.0);
        AnchorPane.setBottomAnchor(mail_head, 501.0);

        Label icon_sender = new Label();
        icon_sender.setPrefHeight(50);
        icon_sender.setPrefWidth(50);
        icon_sender.setAlignment(Pos.CENTER);
        icon_sender.getStyleClass().add("mail-head-sender-icon");
        //HEAD
        VBox mid_head = new VBox();
        HBox.setHgrow(mid_head, Priority.ALWAYS);
        mid_head.setAlignment(Pos.CENTER_LEFT);
        mid_head.getStyleClass().add("mid-head");
        Label source = new Label();
        source.getStyleClass().add("mail-content-title");
        Label dests = new Label();
        dests.getStyleClass().add("mail-content-to");
        mid_head.getChildren().addAll(source, dests);
        //
        Label date = new Label();
        date.getStyleClass().add("mail-content-date");
        mail_head.getChildren().addAll(icon_sender, mid_head, date);

        Label title2 = new Label();
        title2.getStyleClass().add("mail-content-title");

        VBox mail_description_outer = new VBox();
        mail_description_outer.getStyleClass().add("mail-description-outer");
        Label mail_description = new Label(reply.getContent());
        mail_description.setWrapText(true);
        mail_description.setTextAlignment(TextAlignment.JUSTIFY);
        mail_description_outer.getChildren().add(mail_description);

        n_reply.getChildren().addAll(mail_head, title2, mail_description_outer);

        //
        icon_sender.setText(reply.getSource().charAt(0) + "");
        source.setText(reply.getSource());
        dests.setText("A: " + reply.getDests().toString()
                .replace("[", "")
                .replace("]", ""));
        date.setText("Today");
        title2.setText(reply.getObject());

        return n_reply;
    }
}


