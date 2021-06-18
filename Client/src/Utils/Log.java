package Utils;
import org.apache.log4j.Logger;
/**
 * @author xyb
 * @version 1.0
 * @date 2021/6/3 12:49
 */
public class Log {
    public static void sendPackageLog(String packageName,String senderID,String receiverID)
    {
        String className=new Exception().getStackTrace()[1].getClassName();
        Logger LOGGER = Logger.getLogger(className);
        LOGGER.debug(senderID+" send "+packageName+" to "+receiverID);
    }
    public static void receivePackageLog(String packageName,String senderID,String receiverID)
    {
        String className=new Exception().getStackTrace()[1].getClassName();
        Logger LOGGER = Logger.getLogger(className);
        LOGGER.debug(receiverID+" receive "+packageName+" from "+senderID);
    }

    /**
     *
     * @param status=0/1,0代表注册失败，1代表注册成功
     */
    public static void registerLog(int status,String IDc)
    {
        String className=new Exception().getStackTrace()[1].getClassName();
        Logger LOGGER = Logger.getLogger(className);
        if(status==1)
            LOGGER.debug("客户端"+IDc+"注册成功");
        else
            LOGGER.error("客户端"+IDc+"注册失败");
    }
    /**
     *
     * @param status=0/1,0代表注册失败，1代表注册成功
     */
    public static void loginLog(int status,String IDc)
    {
        String className=new Exception().getStackTrace()[1].getClassName();
        Logger LOGGER = Logger.getLogger(className);
        if(status==1)
            LOGGER.debug("客户端"+IDc+"登录成功");
        else
            LOGGER.error("客户端"+IDc+"登录失败");
    }
    /**
     *
     * @param status=0/1,0代表注册失败，1代表注册成功
     */
    public static void modifyPwdLog(int status,String IDc)
    {
        String className=new Exception().getStackTrace()[1].getClassName();
        Logger LOGGER = Logger.getLogger(className);
        if(status==1)
            LOGGER.debug("客户端"+IDc+"修改密码成功");
        else
            LOGGER.error("客户端"+IDc+"修改密码失败");
    }

    /**
     *
     * @param name=AS/TGS/V,C和谁认证
     * @param status=0/1，C和谁认证成功或失败，0失败，1成功
     */
    public static void AuthLog(String name,int status,String IDc)
    {
        String className=new Exception().getStackTrace()[1].getClassName();
        Logger LOGGER = Logger.getLogger(className);
        if(status==1)
            LOGGER.debug("客户端"+IDc+"与"+name+"认证成功");
        else
            LOGGER.error("客户端"+IDc+"与"+name+"认证失败");
    }

    /**
     *
     * @param packagename 包名
     * @param packagecontent   包的内容
     * @param status=0/1/2,0代表原报文，1代表加密报文，2代表完整报文
     * @param rs=发送/收到
     */
    public static void PackageContentLog(String packagename,byte[] packagecontent,int status,String rs)
    {
        String className=new Exception().getStackTrace()[1].getClassName();
        Logger LOGGER = Logger.getLogger(className);
        if(status==0)
            LOGGER.info(rs+"包"+packagename+"原报文："+new String(packagecontent));
        else if(status==1)
            LOGGER.info(rs+"包"+packagename+"加密报文："+new String(packagecontent));
        else
            LOGGER.info(rs+"包"+packagename+"完整报文："+new String(packagecontent));
    }
}
