package LogRegWindow;

/**
 * @author xyb
 * @version 1.0
 * @date 2021/6/2 18:09
 */

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import Utils.*;
import pack.PackageAstoCRegister;
import pack.PackageCtoAsRegister;
import pack.PackageCtoAsRegisterEkc;
import struct.JavaStruct;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;

public class RegisterWindow extends Application {
    @FXML
    private TextField accountTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private TextField passwordConfirmTextField;
    @FXML
    private Button registerButton;

    private String KeyCAS = "12345678";
    private String IpAS = "172.20.10.8";

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("RegisterWindow.fxml"));
        primaryStage.setTitle("注册");
        primaryStage.getIcons().add(new Image(getClass().getResource("icon1.png").toExternalForm()));
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public void register(ActionEvent actionEvent) throws Exception {
        String account = accountTextField.getText();
        String password = passwordTextField.getText();
        String passwordConfirm = passwordConfirmTextField.getText();
        if (account.length() != 10) {
            registerAlert("账号需为10位！");
        } else if (password.length() < 6 || password.length() > 16) {
            registerAlert("请输入6-16位密码！");
        } else if (!passwordConfirm.equals(password)) {
            registerAlert("两次输入密码不一样！");
        } else {
            if (register(account, password)) {
                registerAlert("注册成功！");
                Log.registerLog(1, account);
                Stage primaryStage = (Stage) registerButton.getScene().getWindow();
                primaryStage.close();
            } else {
                Util.alertInformation("请重新输入账号、密码");
                Log.registerLog(0, account);
            }
        }
    }

    public void registerAlert(String registerAlert) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(registerAlert);
        alert.showAndWait();
    }

    public Boolean register(String account, String passwd) throws Exception {
        Long TS1 = System.currentTimeMillis();
        MessageDigest md5=MessageDigest.getInstance("MD5");
        PackageCtoAsRegisterEkc pcarEkc = new PackageCtoAsRegisterEkc(account, TS1.toString(), md5.digest(passwd.getBytes(StandardCharsets.UTF_8)));
        byte[] pcarEkcPack = JavaStruct.pack(pcarEkc);
        Log.PackageContentLog("PackageCtoAsRegisterEkc",pcarEkcPack,0,"发送");
        byte[] pcarEkcPackE = new MainBody(pcarEkcPack, KeyCAS, 1).mainBody();
        Log.PackageContentLog("PackageCtoAsRegisterEkc",pcarEkcPackE,1,"发送");
        PackageCtoAsRegister pcar = new PackageCtoAsRegister(pcarEkcPackE);
        byte[] pcarPack = JavaStruct.pack(pcar);
        byte[] pacrPack=Connect("PackageCtoAsRegister","PackageAstoCRegister",pcarPack,IpAS,8888);
        if(pacrPack[0]==1)
        {
            PackageAstoCRegister pacr=new PackageAstoCRegister((byte)10);
            JavaStruct.unpack(pacr,pacrPack);
            if(pacr.registerStatus==1)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
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

