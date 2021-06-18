package Utils;

/**
 * @author syb
 * @version 1.0
 * @date 2021/6/2 18:09
 */
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.net.Socket;
//import java.util.ArrayList;
import java.util.Optional;

public class Util {
    public static void alertInformation(String alertString){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("温馨提示");
        alert.setHeaderText(null);
        alert.setContentText(alertString);
        alert.showAndWait();
    }

    public static int alertConfirmation(String alertString){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("温馨提示");
        alert.setHeaderText(null);
        alert.setContentText(alertString);
        Optional<ButtonType> buttonType=alert.showAndWait();
        if(buttonType.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE)){
            return 1;
        }
        if(buttonType.get().getButtonData().equals(ButtonBar.ButtonData.CANCEL_CLOSE)){
            return 0;
        }
        return -1;
    }

    public static byte[] getBytesByFile(String pathStr) {
        File file = new File(pathStr);
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] b = new byte[(int)file.length()];
            fis.read(b);
            fis.close();
            return b;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] byteMerger(byte[] bt1, byte[] bt2,byte[] bt3){
        byte[] bt4 = new byte[bt1.length+bt2.length+bt3.length];
        System.arraycopy(bt1, 0, bt4, 0, bt1.length);
        System.arraycopy(bt2, 0, bt4, bt1.length, bt2.length);
        System.arraycopy(bt3,0,bt4,bt1.length+bt2.length,bt3.length);
        return bt4;
    }

    public static void sendPackage(byte[] send,String ip,int port) throws IOException {
        Socket client=new Socket(ip,port);
        OutputStream os= client.getOutputStream();
        DataOutputStream dos=new DataOutputStream(os);
        dos.write(send,0, send.length);
        client.shutdownOutput();
        client.close();
    }
}