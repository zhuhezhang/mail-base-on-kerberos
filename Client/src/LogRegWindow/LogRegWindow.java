package LogRegWindow;

/**
 * @author xyb
 * @version 1.0
 * @date 2021/6/2 18:11
 */

import MainWindow.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

import Utils.*;
import pack.*;
import struct.JavaStruct;

public class LogRegWindow extends Application {
    @FXML
    private AnchorPane pane;
    @FXML
    private TextField accountTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private Label accountAlertLabel;
    @FXML
    private Label passwordAlertLabel;
    private String IpAS="172.20.10.8";
    private String IDtgs="172.20.10.7";
    private String IDv="172.20.10.7";
    private String KeyCAS="12345678";
    private String KeyCTgs;
    private String KeyCV;
    private byte[] ticketTgs;
    private byte[] ticketV;
    private String ADc="172.20.10.7";
    public LogRegWindow() {
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("LogRegWindow.fxml"));
        primaryStage.setTitle("登录/注册");
        primaryStage.getIcons().add(new Image(getClass().getResource("icon1.png").toExternalForm()));
        primaryStage.setScene(new Scene(root,400,300));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void login() throws Exception {
        String account=accountTextField.getText();
        String password=passwordField.getText();
        if(account.length()!=10){
            accountAlertLabel.setText("账号长度需为10位！");
        }
        if(account.length()==10){
            accountAlertLabel.setText("");
        }
        if(password.length()<6||password.length()>16){
            passwordAlertLabel.setText("请输入6-16位密码！");
        }
        if((password.length()>=6)&&(password.length()<=16)){
            passwordAlertLabel.setText("");
        }
        if((account.length()==10)&&(password.length()>=6)&&(password.length()<=16)){
            for(;;)
            {
                if(Auth(account, password))
                {
                    Log.loginLog(1,account);
                    enterMainWindow(account,password);
                    break;
                }
                else
                {
                    Log.loginLog(0,account);
                    JOptionPane.showMessageDialog(null,"登录失败");
                    break;
                }
            }
        }


    }
    public void register(ActionEvent actionEvent) throws Exception {
        RegisterWindow registerWindow=new RegisterWindow();
        registerWindow.start(new Stage());
    }

    public void enterMainWindow(String account,String pwd) throws Exception {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText("登录成功，欢迎用户 "+account);
        alert.showAndWait();

        MainWindow mainWindow=new MainWindow();
        mainWindow.setId(account);
        mainWindow.setPassword(pwd);
        mainWindow.setTicketV(ticketV);
        mainWindow.start(new Stage());

        Stage primaryStage=(Stage)loginButton.getScene().getWindow();
        primaryStage.close();
    }
    public Boolean Auth(String account,String passwd) throws Exception {
        Long TS1=System.currentTimeMillis();
        MessageDigest md5=MessageDigest.getInstance("MD5");
        PackageCtoAsLoginEkc pcale=new PackageCtoAsLoginEkc(account,IDtgs,TS1.toString(),md5.digest(passwd.getBytes(StandardCharsets.UTF_8)));
        byte[] pcalePack= JavaStruct.pack(pcale);
        Log.PackageContentLog("PackageCtoAsLoginEkc",pcalePack,0,"发送");
        byte[] pacalePackE=new MainBody(pcalePack,KeyCAS,1).mainBody();
        Log.PackageContentLog("PackageCtoAsLoginEkc",pacalePackE,1,"发送");
        PackageCtoAsLogin pcal=new PackageCtoAsLogin(pacalePackE);
        byte[] pcalPack=JavaStruct.pack(pcal);
        byte[] paclPack=Connect("PackageCtoAsLogin","PackageAstoCAuth",pcalPack,IpAS,8888);
        if(paclPack[0]==0)           //状态位为0代表和AS认证成功
        {
            Log.AuthLog("AS",1,account);
            PackageAstoCAuth paca=new PackageAstoCAuth(new byte[]{0});
            JavaStruct.unpack(paca,paclPack);
            byte[] paclEkcPack=new MainBody(paca.Ekc,KeyCAS,0).mainBody();
            PackageAstoCAuthEkc pacaEkc=new PackageAstoCAuthEkc("","","","",new byte[]{0});
            JavaStruct.unpack(pacaEkc,paclEkcPack);
            KeyCTgs=String.valueOf(pacaEkc.KcTgs);
            ticketTgs=pacaEkc.TicketTgs;

            TS1=System.currentTimeMillis();
            Authenticator auth=new Authenticator(account,ADc,TS1.toString());
            byte[] authPack=JavaStruct.pack(auth);
            byte[] authE=new MainBody(authPack,KeyCTgs,1).mainBody();
            PackageCtoTgs pct=new PackageCtoTgs(IDv,ticketTgs,authE);
            byte[] pctPack=JavaStruct.pack(pct);
            byte[] ptcPack=Connect("PackageCtoTgs","PackageTgstoC",pctPack,IDtgs,8888);
            System.out.println(Arrays.toString(ptcPack));
            if(ptcPack[0]==4)
            {
                Log.AuthLog("TGS",1,account);
                PackageTgstoC ptc=new PackageTgstoC(new byte[]{0});
                JavaStruct.unpack(ptc,ptcPack);
                Log.PackageContentLog("PackageTgstoCEkCTgs",ptc.EkCTgs,1,"接收");
                byte[] ptcEkcPack=new MainBody(ptc.EkCTgs,KeyCTgs,0).mainBody();
                Log.PackageContentLog("PackageTgstoCEkCTgs",ptcEkcPack,0,"接收");
                PackageTgstoCEkCTgs ptcEkc=new PackageTgstoCEkCTgs("","","",new byte[]{0});
                JavaStruct.unpack(ptcEkc,ptcEkcPack);
                KeyCV=String.valueOf(ptcEkc.Kcv);
                ticketV=ptcEkc.TicketV;

                TS1=System.currentTimeMillis();
                auth=new Authenticator(account,ADc,TS1.toString());
                authPack=JavaStruct.pack(auth);
                System.out.println(Arrays.toString(authPack));
                Log.PackageContentLog("Authenticator",authPack,0,"发送");
                authE=new MainBody(authPack,KeyCV,1).mainBody();
                Log.PackageContentLog("Authenticator",authE,1,"发送");
                PackageCtoVAuth pcva=new PackageCtoVAuth(ticketV,authE);
                byte[] pcvaPack=JavaStruct.pack(pcva);
                System.out.println("b");
                byte[] pvcaPack=Connect("PackageCtoVAuth","PackageVtoCAuth",pcvaPack,IDv,9999);
                if(pvcaPack[0]==7)
                {
                    Log.AuthLog("V",1,account);
                    return true;
                }
                else
                {
                    Log.AuthLog("V",0,account);
                    return false;
                }
            }
            else
            {
                Log.AuthLog("TGS",0,account);
                return false;
            }
        }
        else                               //状态位为2代表和AS认证失败
        {
            Log.AuthLog("AS",0,account);
            return false;
        }
    }

    public byte[] Connect(String sendpackagename,String receivepackagename,byte[] send,String ip,int port) throws IOException {
        Log.PackageContentLog(sendpackagename,send,2,"发送");
        Socket client=new Socket(ip,port);
        OutputStream os= client.getOutputStream();
        os.write(send);
        client.shutdownOutput();
        Log.sendPackageLog(sendpackagename,accountTextField.getText(),ip);
        byte[] receive=new byte[]{0};
        byte[] buffer = new byte[1024];
        ArrayList<Byte> receivePackageTmp = new ArrayList<Byte>();// 临时保存接收到的数据包
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
        Log.PackageContentLog(receivepackagename,receive,2,"接收");
        Log.receivePackageLog(receivepackagename,accountTextField.getText(),ip);
        client.close();
        return receive;
    }
}

