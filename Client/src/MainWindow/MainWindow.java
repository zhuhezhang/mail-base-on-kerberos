package MainWindow;

/**
 * @author syb
 * @version 1.0
 * @date 2021/6/2 18:09
 */
import PersonalManageWindow.*;
import Utils.*;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import pack.*;
import struct.JavaStruct;
import struct.StructException;

import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainWindow extends Application implements Initializable {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label emailAddressLabel;
    @FXML
    private Label accountLabel;
    @FXML
    private Label attachmentLabel;
    @FXML
    private Button homeButton;
    @FXML
    private Button personalManageButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button writeEmailButton;
    @FXML
    private Button checkInboxButton;
    @FXML
    private Button checkOutboxButton;
    @FXML
    private TextField receiverTextField;
    @FXML
    private TextField subjectTextField;
    @FXML
    private TextArea contentTextArea;
    @FXML
    private Button addAttachmentButton;
    @FXML
    private Button deleteAttachmentButton;
    @FXML
    private Button sendEmailButton;
    @FXML
    private HBox hboxChangeView;
    @FXML
    private VBox vboxMain;

    @FXML
    private Label senderLabel;

    @FXML
    private VBox vboxInboxChangeCheckEmail;
    @FXML
    private CheckBox selectAllInbox;
    @FXML
    private Button inboxDeleteButton;
    @FXML
    private ListView<EmailData> inboxListView;

    @FXML
    private VBox vboxOutboxChangeCheckEmail;
    @FXML
    private CheckBox selectAllOutbox;
    @FXML
    private Button outboxDeleteButton;
    @FXML
    private ListView<EmailData> outboxListView;

    @FXML
    private VBox vboxChangeInbox;
    @FXML
    private VBox vboxChangeOutbox;
    @FXML
    private Label subjectLabel;
    @FXML
    private Label senderLabel_checkEmail;
    @FXML
    private Label dateLabel;
    @FXML
    private Label receiverLabel;
    @FXML
    private Label attachmentLabel_checkEmail;
    @FXML
    private Label textLabel;
    @FXML
    private Button returnInboxButton;
    @FXML
    private Button returnOutboxButton;
    @FXML
    private Button checkAttachmentInboxButton;
    @FXML
    private Button checkAttachmentOutboxButton;

    private StringProperty welcomeString =new SimpleStringProperty();
    private StringProperty emailAddressString=new SimpleStringProperty();
    private StringProperty attachmentString=new SimpleStringProperty();
    private StringProperty subjectString=new SimpleStringProperty();
    private StringProperty senderString=new SimpleStringProperty();
    private StringProperty dataString=new SimpleStringProperty();
    private StringProperty receiverString=new SimpleStringProperty();
    private StringProperty textString=new SimpleStringProperty();
    private StringProperty attachmentInbox =new SimpleStringProperty();
    private ArrayList<EmailData> emailData;
    private static EmailData selected;
    private File attachmentFile;
    private String IDv="172.20.10.7";
    private String IDAS="172.20.10.8";
    private String KeyCAS="12345678";
    private String Kcv="12345678";
    private String tmpAttachPath;

    private static String account;
    private static String password;
    private static byte[] ticketV;
    public void setId(String id) {
        MainWindow.account = id;
    }
    public void setPassword(String password){ MainWindow.password=password;}
    public void setTicketV(byte[] ticketV){ MainWindow.ticketV =ticketV; }

    private BigInteger e=new BigInteger("65537");
    private static BigInteger[] nd=new BigInteger[2];
    private static BigInteger[] ne=new BigInteger[2];
    public void getKey(BigInteger[] publicKey,BigInteger[] privateKey ){
        privateKey=RSA.generateKey(e);
        publicKey[0]=privateKey[0];
        publicKey[1]=e;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String loadLocation=location.toString();
        if(loadLocation.contains("MainWindow.fxml")){
            initMainWindow();
        }
        else if(loadLocation.contains("writeEmail.fxml")){
            senderLabel.textProperty().bind(emailAddressString);
            attachmentLabel.textProperty().bind(attachmentString);
            emailAddressString.set("<"+ account +"@203.com>");
        }
        else if(loadLocation.contains("checkInbox.fxml")){
            emailData=getInboxEmail();
            setInboxListViewItems(inboxListView);
            ObservableList<EmailData> items3 = FXCollections.observableArrayList();
            if(emailData!=null){
                items3.addAll(emailData);
            }
            inboxListView.setItems(items3);
        }
        else if(loadLocation.contains("checkOutbox.fxml")){
            emailData=getOutboxEmail();
            setOutboxListViewItems(outboxListView);
            ObservableList<EmailData> items4 = FXCollections.observableArrayList();
            if(emailData!=null){
                items4.addAll(emailData);
            }
            outboxListView.setItems(items4);
        }
        else if(loadLocation.contains("checkInboxEmail.fxml")){
            try {
                initInboxCheckEmail();
            } catch (ParseException parseException) {
                parseException.printStackTrace();
            }
        }
        else if(loadLocation.contains("checkOutboxEmail.fxml")){
            try {
                initOutboxCheckEmail();
            } catch (ParseException parseException) {
                parseException.printStackTrace();
            }
        }
    }

    private void initMainWindow() {
        welcomeLabel.textProperty().bind(welcomeString);
        emailAddressLabel.textProperty().bind(emailAddressString);
        accountLabel.textProperty().bind(emailAddressString);
        attachmentLabel.textProperty().bind(attachmentString);
        welcomeString.set("用户"+ account +"，欢迎使用203邮箱");
        emailAddressString.set("<"+ account +"@203.com>");
    }

    public void initInboxCheckEmail() throws ParseException {
        subjectLabel.textProperty().bind(subjectString);
        senderLabel_checkEmail.textProperty().bind(senderString);
        dateLabel.textProperty().bind(dataString);
        receiverLabel.textProperty().bind(receiverString);
        textLabel.textProperty().bind(textString);
        checkAttachmentInboxButton.textProperty().bind(attachmentInbox);
        subjectString.set(selected.getSubject());
        senderString.set("发件人："+selected.getSender());
        dataString.set("时间："+selected.getDate());
        receiverString.set("收件人："+account);
        textString.set(getText());
        if(!selected.getAttachmentName().equals("")){
            attachmentInbox.set("附件："+selected.getAttachmentName());
            String time;
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date=sdf.parse(selected.getDate());
            long ts=date.getTime();
            time=String.valueOf(ts);
            tmpAttachPath="./Mail/"+selected.getReceiver()+"/收件箱/"+selected.getSender()+" "+time.substring(0,10)+"/attachment";
            checkAttachmentInboxButton.setVisible(true);
        }
        else{
            checkAttachmentInboxButton.setVisible(false);
            tmpAttachPath="";
        }
    }

    private void initOutboxCheckEmail() throws ParseException {
        subjectLabel.textProperty().bind(subjectString);
        senderLabel_checkEmail.textProperty().bind(senderString);
        dateLabel.textProperty().bind(dataString);
        receiverLabel.textProperty().bind(receiverString);
        textLabel.textProperty().bind(textString);
        checkAttachmentOutboxButton.textProperty().bind(attachmentInbox);
        subjectString.set(selected.getSubject());
        senderString.set("发件人："+account);
        dataString.set("时间："+selected.getDate());
        receiverString.set("收件人："+selected.getSender());
        textString.set(getText());
        if(!selected.getAttachmentName().equals("")){
            attachmentInbox.set("附件："+selected.getAttachmentName());
            String time;
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date=sdf.parse(selected.getDate());
            long ts=date.getTime();
            time=String.valueOf(ts);
            tmpAttachPath="./Mail/"+selected.getSender()+"/发件箱/"+selected.getReceiver()+" "+time.substring(0,10)+"/attachment";
            checkAttachmentOutboxButton.setVisible(true);
        }
        else {
            checkAttachmentOutboxButton.setVisible(false);
            tmpAttachPath="";
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
        primaryStage.setTitle("203邮箱");
        primaryStage.getIcons().add(new Image(getClass().getResource("icon2.png").toExternalForm()));
        primaryStage.setScene(new Scene(root,1000,600));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public void goHome() throws IOException {
        vboxMain.getChildren().setAll((VBox)FXMLLoader.load(getClass().getResource("MainWindow.fxml")));
    }

    public void personalManage() throws Exception {
        PersonalManageWindow pmw=new PersonalManageWindow();
        pmw.setId(account);
        pmw.setPassword(password);
        pmw.start(new Stage());
    }

    public void logout() {
        int confirm= Util.alertConfirmation("确认退出吗？");
        if(confirm==1){
            Stage primaryStage=(Stage)logoutButton.getScene().getWindow();
            primaryStage.close();
        }
    }

    public void writeEmail() throws IOException {
        hboxChangeView.getChildren().setAll((HBox)FXMLLoader.load(getClass().getResource("writeEmail.fxml")));
    }

    public void checkInbox() throws IOException, StructException {
        byte[] receive=requestEmail();
        if(receive!=null){
            receiveEmail(receive);
        }
        hboxChangeView.getChildren().setAll((VBox)FXMLLoader.load(getClass().getResource("checkInbox.fxml")));
    }

    public void checkOutbox() throws IOException {
        hboxChangeView.getChildren().setAll((VBox)FXMLLoader.load(getClass().getResource("checkOutbox.fxml")));
    }

    public void addAttachment() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("添加附件");
        attachmentFile=fileChooser.showOpenDialog(new Stage());
        if (attachmentFile!=null){
            attachmentString.set(attachmentFile.getName());
            deleteAttachmentButton.setVisible(true);
        }
    }

    public void deleteAttachment() {
        attachmentFile=null;
        attachmentString.set("");
        deleteAttachmentButton.setVisible(false);
    }

    public void sendEmail() throws NoSuchAlgorithmException, StructException, IOException, RSA.pqException {
        int confirm= Util.alertConfirmation("确认发送吗？");
        if(confirm==1){
            long TS1=System.currentTimeMillis();
            String receiver=receiverTextField.getText();
            if(checkReceiver(Long.toString(TS1),receiver)){//查询是否存在该收件人
                byte[] subject=subjectTextField.getText().getBytes(StandardCharsets.UTF_8);
                byte[] text=contentTextArea.getText().getBytes(StandardCharsets.UTF_8);
                byte[] attachment;
                String attachmentFileName;
                if(attachmentFile!=null){
                    attachment= Util.getBytesByFile(attachmentFile.toString());
                    attachmentFileName=attachmentFile.getName();
                }
                else {
                    attachment=new byte[]{};
                    attachmentFileName="null";
                }
                assert attachment != null;
                saveToLocalOutbox(receiver,TS1,subject,text,attachment);
                byte[] content= Util.byteMerger(subject,text,attachment);
                if(content.length==0){
                    Util.alertInformation("主题、正文、附件不可全为空！");
                }
                else{
                    MessageDigest md5= MessageDigest.getInstance("MD5");
                    byte[] md=md5.digest(content);

                    getKey(ne,nd);
                    byte[] EpkcMD5 = RSA.encryption(md,ne[0],ne[1]);//私钥签名
                    PackageCtoVPostEkCV packageCtoVPostEkCV=new PackageCtoVPostEkCV(receiverTextField.getText(),account, Long.toString(TS1),subject.length,text.length,attachmentFileName,attachment.length,content,EpkcMD5);
                    //Log.PackageContentLog("PackageCtoVPostEkCV",JavaStruct.pack(packageCtoVPostEkCV),0,"发送");
                    MainBody des=new MainBody(JavaStruct.pack(packageCtoVPostEkCV),Kcv,1);
                    byte[] EkCV=des.mainBody();
                    //Log.PackageContentLog("PackageCtoVPostEkCV",EkCV,1,"发送");
                    int EkCVLen=EkCV.length;
                    int totalLen=EkCVLen+ticketV.length;
                    PackageCtoVPost packageCtoVPost=new PackageCtoVPost(totalLen,EkCV,ticketV);
                    byte[] message=JavaStruct.pack(packageCtoVPost);
                    Util.sendPackage(message,IDv,9999);
                    //Log.PackageContentLog("PackageCtoVPost",message,2,"发送");
                    Log.sendPackageLog("packageCtoVPost",account,IDv);
                    Util.alertInformation("发送成功");
                }
            }
        }
    }

    private boolean checkReceiver(String TS,String receiver) throws StructException, IOException {
        PackageCtoAsCheckEkc packageCtoAsCheckEkc=new PackageCtoAsCheckEkc(account,TS,receiver.getBytes(StandardCharsets.UTF_8));
        byte[] packCASCheck=JavaStruct.pack(packageCtoAsCheckEkc);
        Log.PackageContentLog("PackageCtoAsCheckEkc",packCASCheck,0,"发送");
        MainBody des=new MainBody(packCASCheck,KeyCAS,1);
        byte[] EKCV=des.mainBody();
        Log.PackageContentLog("PackageCtoAsCheckEkc",EKCV,1,"发送");
        PackageCtoAsCheck packageCtoAsCheck=new PackageCtoAsCheck(EKCV);
        byte[] message=JavaStruct.pack(packageCtoAsCheck);
        byte[] receive=Connect("packageCtoAsCheck","packageAstoCCheck",message,IDAS,8888);
        if(receive[0]==(byte)3){
            PackageAstoCCheck packageAstoCCheck=new PackageAstoCCheck((byte) 2);
            JavaStruct.unpack(packageAstoCCheck,receive);
            if(packageAstoCCheck.checkStatus==(byte)1){
                return true;
            }
            if(packageAstoCCheck.checkStatus==(byte)0){
                Util.alertInformation("收件人不存在");
                return false;
            }
        }
        return false;
    }

    public void setInboxListViewItems(ListView<EmailData> listView){
        listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                selected=listView.getSelectionModel().getSelectedItem();
                try {
                    vboxInboxChangeCheckEmail.getChildren().setAll((VBox)FXMLLoader.load(getClass().getResource("checkInboxEmail.fxml")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        listView.setCellFactory(new Callback<ListView<EmailData>, ListCell<EmailData>>() {
            @Override
            public ListCell<EmailData> call(ListView<EmailData> param) {
                ListCell<EmailData> listCell=new ListCell<EmailData>(){
                    @Override
                    protected void updateItem(EmailData item,boolean empty){
                        super.updateItem(item,empty);
                        if(!empty){
                            HBox hBox=new HBox();
                            hBox.setSpacing(30);
                            CheckBox checkBox=new CheckBox();
                            checkBox.setPrefWidth(50);
                            checkBox.setSelected(item.getSelect());
                            checkBox.selectedProperty()
                                    .addListener((observable, oldValue, newValue) -> {
                                        if(newValue){ item.setSelect(true); }
                                        if(!newValue){ item.setSelect(false); }
                                        for(EmailData item1 :listView.getItems()){
                                            if(!item1.getSelect()){
                                                selectAllInbox.setSelected(false);
                                                break;
                                            }
                                            selectAllInbox.setSelected(true);
                                        }
                                    });
                            Label senderLabel=new Label(item.getSender());
                            senderLabel.setPrefWidth(100);
                            Label subjectLabel=new Label(item.getSubject());
                            subjectLabel.setPrefWidth(400);
                            Label dateLabel=new Label(item.getDate());
                            hBox.getChildren().addAll(checkBox,senderLabel,subjectLabel,dateLabel);
                            this.setGraphic(hBox);
                        }
                    }
                };
                return listCell;
            }
        });
    }

    public void setOutboxListViewItems(ListView<EmailData> listView){
        listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                selected=listView.getSelectionModel().getSelectedItem();
                if(selected!=null){
                    try {
                        vboxOutboxChangeCheckEmail.getChildren().setAll((VBox)FXMLLoader.load(getClass().getResource("checkOutboxEmail.fxml")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        listView.setCellFactory(new Callback<ListView<EmailData>, ListCell<EmailData>>() {
            @Override
            public ListCell<EmailData> call(ListView<EmailData> param) {
                ListCell<EmailData> listCell=new ListCell<EmailData>(){
                    @Override
                    protected void updateItem(EmailData item,boolean empty){
                        super.updateItem(item,empty);
                        if(!empty){
                            HBox hBox=new HBox();
                            hBox.setSpacing(30);
                            CheckBox checkBox=new CheckBox();
                            checkBox.setPrefWidth(50);
                            checkBox.setSelected(item.getSelect());
                            checkBox.selectedProperty()
                                    .addListener((observable, oldValue, newValue) -> {
                                        if(newValue){ item.setSelect(true); }
                                        if(!newValue){ item.setSelect(false); }
                                        for(EmailData item1 :listView.getItems()){
                                            if(!item1.getSelect()){
                                                selectAllOutbox.setSelected(false);
                                                break;
                                            }
                                            selectAllOutbox.setSelected(true);
                                        }
                                    });
                            Label senderLabel=new Label(item.getReceiver());
                            senderLabel.setPrefWidth(100);
                            Label subjectLabel=new Label(item.getSubject());
                            subjectLabel.setPrefWidth(400);
                            Label dateLabel=new Label(item.getDate());
                            hBox.getChildren().addAll(checkBox,senderLabel,subjectLabel,dateLabel);
                            this.setGraphic(hBox);
                        }
                    }
                };
                return listCell;
            }
        });
    }

    public ArrayList<EmailData> getInboxEmail(){
        String inboxPath="./Mail/"+account+"/收件箱";
        File file=new File(inboxPath);
        File[] files=file.listFiles();
        if(files!=null){
            String[] filename=new String[files.length];
            for(int i=0;i<files.length;i++){
                filename[i]=files[i].getName();
            }
            ArrayList<EmailData> emailData=new ArrayList<>();
            for(int i=0;i<files.length;i++){
                String[] senderDate=filename[i].split(" ");
                String subFilePath=files[i].getAbsolutePath();
                byte[] bytes= Util.getBytesByFile(subFilePath+"/subject.txt");
                assert bytes != null;
                String subject=new String(bytes);
                File attachmentFolder=new File(subFilePath+"/attachment");
                File[] attachmentName=attachmentFolder.listFiles();
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if(attachmentName.length!=0){
                    emailData.add(new EmailData(senderDate[0],account,subject,sdf.format(Long.parseLong(senderDate[1])*1000),attachmentName[0].getName(),false));
                }
                if(attachmentName.length==0){
                    emailData.add(new EmailData(senderDate[0],account,subject,sdf.format(Long.parseLong(senderDate[1])*1000),"",false));
                }
            }
            return emailData;
        }
        else {
            return null;
        }
    }

    public ArrayList<EmailData> getOutboxEmail(){
        String outboxPath="./Mail/"+account+"/发件箱";
        File file=new File(outboxPath);
        File[] files=file.listFiles();
        if(files!=null){
            String[] filename=new String[files.length];
            for(int i=0;i<files.length;i++){
                filename[i]=files[i].getName();
            }

            ArrayList<EmailData> emailData=new ArrayList<>();
            for(int i=0;i<files.length;i++){
                String[] receiverDate=filename[i].split(" ");
                String subFilePath=files[i].getAbsolutePath();
                byte[] bytes= Util.getBytesByFile(subFilePath+"/subject.txt");
                assert bytes != null;
                String subject=new String(bytes);
                File attachmentFolder=new File(subFilePath+"/attachment");
                File[] attachmentName=attachmentFolder.listFiles();
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if(attachmentName.length!=0){
                    emailData.add(new EmailData(account,receiverDate[0],subject,sdf.format(Long.parseLong(receiverDate[1])*1000),attachmentName[0].getName(),false));
                }
                if(attachmentName.length==0){
                    emailData.add(new EmailData(account,receiverDate[0],subject,sdf.format(Long.parseLong(receiverDate[1])*1000),"",false));
                }
            }
            return emailData;
        }
        else {
            return null;
        }
    }

    public void selectAll_Inbox() {
        setInboxListViewItems(inboxListView);
        ObservableList<EmailData> items = FXCollections.observableArrayList();
        items.addAll(emailData);
        inboxListView.setItems(items);
        if(selectAllInbox.isSelected()){
            for(EmailData item:emailData){
                item.setSelect(true);
            }
        }
        else {
            for(EmailData item:emailData){
                item.setSelect(false);
            }
        }
    }

    public void selectAll_Outbox() {
        setInboxListViewItems(outboxListView);
        ObservableList<EmailData> items = FXCollections.observableArrayList();
        items.addAll(emailData);
        outboxListView.setItems(items);
        if(selectAllOutbox.isSelected()){
            for(EmailData item:emailData){
                item.setSelect(true);
            }
        }
        else {
            for(EmailData item:emailData){
                item.setSelect(false);
            }
        }
    }

    public void deleteInboxEmail() throws Exception {
        int confirm= Util.alertConfirmation("确认删除选中邮件吗？");
        if(confirm==1){
            for(EmailData e:emailData){
                if(e.getSelect()){
                    String time;
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date=sdf.parse(e.getDate());;
                    long ts=date.getTime();
                    time=String.valueOf(ts);
                    String deleteFileName=e.getSender()+" "+time.substring(0,10);
                    deletefile("./Mail/"+account+"/收件箱/"+deleteFileName);
//                    File file=new File("./Mail/"+account+"/收件箱/"+deleteFileName);
//                    System.out.println(file.getAbsolutePath());
//                    if(file.isDirectory()){
//                        for(File f:file.listFiles()){
//                            if(!f.delete()){
//                                System.out.println("文件"+f.getName()+"删除失败");
//                            }
//                        }
//                        if(!file.delete()){
//                            System.out.println("目录删除失败");
//                        }
//                    }
                }
            }
            inboxListView.getItems().removeIf(item -> item.getSelect());
            emailData.removeIf(e -> e.getSelect());
            setOutboxListViewItems(inboxListView);
            ObservableList<EmailData> items = FXCollections.observableArrayList();
            items.addAll(emailData);
            inboxListView.setItems(items);
        }
    }

    public void deleteOutboxEmail() throws Exception {
        int confirm= Util.alertConfirmation("确认删除选中邮件吗？");
        if(confirm==1){
            for(EmailData e:emailData){
                if(e.getSelect()){
                    String time;
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date=sdf.parse(e.getDate());;
                    long ts=date.getTime();
                    time=String.valueOf(ts);
                    String deleteFileName=e.getReceiver()+" "+time.substring(0,10);

//                    File file=new File("./Mail/"+account+"/发件箱/"+deleteFileName);
//                    System.out.println(file.getAbsolutePath());
                    deletefile("./Mail/"+account+"/发件箱/"+deleteFileName);
//                    if(file.isDirectory()){
//                        for(File f: Objects.requireNonNull(file.listFiles())){
//                            if(!f.delete()){
//                                System.out.println("文件"+f.getName()+"删除失败");
//                            }
//                        }
//                        if(!file.delete()){
//                            System.out.println("目录删除失败");
//                        }
//                    }
                }
            }
            outboxListView.getItems().removeIf(item -> item.getSelect());
            emailData.removeIf(e -> e.getSelect());
            setOutboxListViewItems(outboxListView);
            ObservableList<EmailData> items = FXCollections.observableArrayList();
            items.addAll(emailData);
            outboxListView.setItems(items);
        }
    }

    public void returnInbox() throws IOException {
        vboxChangeInbox.getChildren().setAll((VBox)FXMLLoader.load(getClass().getResource("checkInbox.fxml")));
    }
    public void returnOutbox() throws IOException {
        vboxChangeOutbox.getChildren().setAll((VBox)FXMLLoader.load(getClass().getResource("checkOutbox.fxml")));
    }

    private byte[] requestEmail() throws StructException, IOException {
        long TS=System.currentTimeMillis();
        PackageCtoVRequestMailEkCV packageCtoVRequestMailEkCV=new PackageCtoVRequestMailEkCV(account,Long.toString(TS));
        byte[] pack=JavaStruct.pack(packageCtoVRequestMailEkCV);
        Log.PackageContentLog("PackageCtoVRequestMailEkCV",pack,0,"发送");
        MainBody des=new MainBody(pack,Kcv,1);
        byte[] EkCV=des.mainBody();
        Log.PackageContentLog("PackageCtoVRequestMailEkCV",EkCV,1,"发送");
        PackageCtoVRequestMail packageCtoVRequestMail=new PackageCtoVRequestMail(EkCV,ticketV);
        byte[] message=JavaStruct.pack(packageCtoVRequestMail);
        byte[] receive=Connect("packageCtoVRequestMail","packageVtoCPost",message,"172.20.10.7",9999);
        if(receive[0]==9){
            return null;
        }
        return receive;
    }

    public void receiveEmail(byte[] receive) throws StructException, IOException {
        byte[] pre=receive;
        byte[] cur=receive;
        while(cur.length>=9) {
            byte[] totalLen_byte = new byte[4];
            int totalLen = 0;
            System.arraycopy(cur, 1, totalLen_byte, 0, 4);
            totalLen=new BigInteger(totalLen_byte).intValue();
            if(cur.length-9<totalLen){
                break;
            }
            byte[] packVtoCPost = new byte[totalLen+9];
            System.arraycopy(cur, 0, packVtoCPost, 0, totalLen+9);
            PackageVtoCPost packageVtoCPost = new PackageVtoCPost(0, new byte[1]);
            JavaStruct.unpack(packageVtoCPost, packVtoCPost);
            saveToLocalInbox(packageVtoCPost);
            cur=new byte[pre.length-9-totalLen];
            System.arraycopy(pre,9+totalLen,cur,0,pre.length-9-totalLen);
            pre=new byte[pre.length-9-totalLen];
            pre=cur;
        }
    }

    public void saveToLocalInbox(PackageVtoCPost p) throws IOException, StructException {
        //Log.PackageContentLog("PackageVtoCPostEkCV",p.EkCV,1,"收到");
        MainBody mainBody=new MainBody(p.EkCV,Kcv,0);
        byte[] EkCV=mainBody.mainBody();
        //Log.PackageContentLog("PackageVtoCPostEkCV",EkCV,0,"收到");
        PackageVtoCPostEkCV postEkCV=new PackageVtoCPostEkCV("1","1","1",1,1,"1",1,new byte[1],new byte[1]);
        JavaStruct.unpack(postEkCV,EkCV);
        File file1=new File("./Mail/"+account+"/收件箱/"+String.valueOf(postEkCV.SenderID) +" "+ String.valueOf(postEkCV.TS).substring(0,10));
        if(!file1.exists()){
            if(file1.mkdirs()){
                System.out.println("创建收件箱邮件成功");
            }
        }
        String currentPath="./Mail/"+account+"/收件箱/"+String.valueOf(postEkCV.SenderID) +" "+ String.valueOf(postEkCV.TS).substring(0,10)+"/";
        byte[] content=postEkCV.Content;
        byte[] subject = new byte[postEkCV.SubjectLen];
        System.arraycopy(content,0,subject,0,postEkCV.SubjectLen);
        byte[] text=new byte[postEkCV.TextLen];
        System.arraycopy(content,postEkCV.SubjectLen,text,0,postEkCV.TextLen);
        byte[] attachment=new byte[postEkCV.AttachmentLen];
        System.arraycopy(content,postEkCV.SubjectLen+postEkCV.TextLen,attachment,0,postEkCV.AttachmentLen);
        String subjectFile=currentPath+"subject.txt";
        String textFile=currentPath+"text.txt";
        File attachmentfolder=new File(currentPath+"/attachment");
        if(!attachmentfolder.isDirectory()){
            if(!attachmentfolder.mkdirs()){
                System.out.println("创建失败");
            }
        }
        String attachmentFile= attachmentfolder.getAbsolutePath()+"/"+String.valueOf(postEkCV.AttachmentName);
        writeToFile(subjectFile,subject);
        writeToFile(textFile,text);
        writeToFile(attachmentFile,attachment);
    }

    public void saveToLocalOutbox(String receiver,long date,byte[] subject,byte[] text,byte[] attachment) throws IOException {
        String path="./Mail/"+account+"/发件箱/"+receiver+" "+String.valueOf(date).substring(0,10);
        File file=new File(path);
        if(!file.exists()){
            if(file.mkdirs()){
                System.out.println("创建发件箱邮件文件夹");
            }
        }
        String subjectFilePath=path+"/subject.txt";
        String textFilePath=path+"/text.txt";
        File attachmentfolder=new File(path+"/attachment");
        if(!attachmentfolder.exists()){
            if(!attachmentfolder.mkdirs()){
                System.out.println("创建失败");
            }
        }
        writeToFile(subjectFilePath,subject);
        writeToFile(textFilePath,text);
        if(attachmentFile!=null){
            String attachmentFilePath= attachmentfolder.getAbsolutePath()+"/"+attachmentFile.getName();
            writeToFile(attachmentFilePath,attachment);
        }
    }

    public void writeToFile(String filepath,byte[] data) throws IOException {
        File file=new File(filepath);
        if(!file.exists()){
            if(file.createNewFile()){
                System.out.println(filepath+"创建成功");
                FileOutputStream outStream = new FileOutputStream(file);
                outStream.write(data);
                outStream.close();
            }
            else{
                System.out.println(filepath+"创建失败！");
            }
        }
    }

    public String getText() throws ParseException {
        if(selected.getSender().equals(account)){
            String time;
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date=sdf.parse(selected.getDate());;
            long ts=date.getTime();
            time=String.valueOf(ts);
            String filePathName="./Mail/"+account+"/发件箱/"+selected.getReceiver()+" "+time.substring(0,10)+"/text.txt";
            byte[] textByte= Util.getBytesByFile(filePathName);
            if(textByte!=null) {
                return new String(textByte);
            }
        }
        else {
            String time;
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date=sdf.parse(selected.getDate());
            long ts=date.getTime();
            time=String.valueOf(ts);
            String filePathName="./Mail/"+account+"/收件箱/"+selected.getSender()+" "+time.substring(0,10)+"/text.txt";
            byte[] textByte= Util.getBytesByFile(filePathName);
            if(textByte!=null) {
                return new String(textByte);
            }
        }
        return null;
    }
    public byte[] Connect(String sendpackagename,String receivepackagename,byte[] send,String ip,int port) throws IOException {
        Log.PackageContentLog(sendpackagename,send,2,"发送");
        Socket client=new Socket(ip,port);
        OutputStream os= client.getOutputStream();
        os.write(send);
        client.shutdownOutput();
        Log.sendPackageLog(sendpackagename,account,ip);
        byte[] receive=new byte[]{0};
        byte[] buffer = new byte[1024];
        ArrayList<Byte> receivePackageTmp = new ArrayList<>();// 临时保存接收到的数据包
        int len = 0;
        while ((len = client.getInputStream().read(buffer)) != -1) {// 获得所有接收到的数据临时保存在动态数组中
            for (int i = 0; i < len; i++) {
                receivePackageTmp.add(buffer[i]);
            }
        }
        receive = new byte[receivePackageTmp.size()];
        for (int i = 0; i < receivePackageTmp.size(); i++) {// 动态数组转化为静态数组
            receive[i] = receivePackageTmp.get(i);
        }
        if(!receivepackagename.equals("packageVtoCPost"))
            Log.PackageContentLog(receivepackagename,receive,2,"接收");
        Log.receivePackageLog(receivepackagename,account,ip);
        client.close();
        return receive;
    }

    public void checkAttachInbox() throws IOException {
        if(tmpAttachPath!=null){
            File attachfile=new File(tmpAttachPath);
            File[] attachlist=attachfile.listFiles();
            if(attachlist!=null){
                Desktop.getDesktop().open(attachlist[0]);
            }
        }
    }

    public void checkAttachOutbox() throws IOException {
        if(tmpAttachPath!=null){
            File attachfile=new File(tmpAttachPath);
            File[] attachlist=attachfile.listFiles();
            if(attachlist!=null){
                Desktop.getDesktop().open(attachlist[0]);
            }
        }
    }
    public void deletefile(String delpath) throws Exception {
        try {
            File file = new File(delpath);
            // 当且仅当此抽象路径名表示的文件存在且 是一个目录时，返回 true
            if (!file.isDirectory()) {
                file.delete();
            } else if (file.isDirectory()) {
                String[] filelist = file.list();
                for (int i = 0; i < filelist.length; i++) {
                    File delfile = new File(delpath + "/" + filelist[i]);
                    if (!delfile.isDirectory()) {
                        delfile.delete();
                        System.out.println(delfile.getAbsolutePath() + "删除文件成功");
                    } else if (delfile.isDirectory()) {
                        deletefile(delpath + "/" + filelist[i]);
                    }
                }
                System.out.println(file.getAbsolutePath() + "删除成功");
                file.delete();
            }

        } catch (FileNotFoundException e) {
            System.out.println("deletefile() Exception:" + e.getMessage());
        }
    }
}
