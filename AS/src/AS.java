import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.sql.*;
import struct.JavaStruct;
import struct.StructException;

import des.MainBody;
import pack.*;
import log.*;

/**
 * Kerberos之AS（认证服务器）类
 * 
 * @author zhz000
 */
public class AS implements Runnable {

	private Socket socket;// 连接客户端返回的socket对象
	private byte[] receivePackage;// 接收到的数据包
	private final static String keyCAs = "12345678";// C和AS会话的des密钥
	private final static String keyCTgs = "12345678";// C和TGS会话的des密钥
	private final static String keyAsTgs = "12345678";// AS和TGS会话的des密钥
	private final static String ipTgs = "172.20.10.7";// tgs的ip地址
	private final static String ASIp = "172.20.10.7";// ASIP地址

	public AS(Socket socket) {
		this.socket = socket;
	}

	public static void main(String[] args) {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(8888);
			System.out.println("认证服务器AS运行中...");
			while (true) {
				Socket socket = serverSocket.accept();// 等待连接
				new Thread(new AS(socket)).start();// 每有一个连接新建一个线程处理
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		byte[] buffer = new byte[1024];
		ArrayList<Byte> receivePackageTmp = new ArrayList<Byte>();// 临时保存接收到的数据包
		int len = 0;
		try {
			InputStream inputStream = socket.getInputStream();
			while ((len = inputStream.read(buffer)) != -1) {// 获得所有接收到的数据临时保存在动态数组中
				for (int i = 0; i < len; i++) {
					receivePackageTmp.add(buffer[i]);
				}
			}
			receivePackage = new byte[receivePackageTmp.size()];
			for (int i = 0; i < receivePackageTmp.size(); i++) {// 动态数组转化为静态数组
				receivePackage[i] = receivePackageTmp.get(i);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		byte status = receivePackage[0];// 根据状态位进入相应的处理函数
		switch (status) {
		case 0:
			handleRegister();
			break;
		case 1:
			handleLogin();
			break;
		case 2:
			handleModifyPwd();
			break;
		case 3:
			handleReceiverIsExist();
			break;
		default:
			break;
		}
	}

	/**
	 * 处理注册包的函数
	 */
	private void handleRegister() {
		try {
			PackageCtoAsRegister packageCtoAsRegister = new PackageCtoAsRegister(new byte[1]);// 解第一层包
			JavaStruct.unpack(packageCtoAsRegister, receivePackage, ByteOrder.BIG_ENDIAN);
			byte[] decryptedData = new MainBody(packageCtoAsRegister.EKc, keyCAs, 0).mainBody();// 解密
			PackageCtoAsRegisterEkc packageCtoAsRegisterEkc = new PackageCtoAsRegisterEkc("", "", new byte[1]);// 解第二层包
			JavaStruct.unpack(packageCtoAsRegisterEkc, decryptedData, ByteOrder.BIG_ENDIAN);

			String id = String.valueOf(packageCtoAsRegisterEkc.IDc);
			byte[] pwd = packageCtoAsRegisterEkc.content;
			Log.receivePackageLog("注册包", socket.getInetAddress().getHostAddress(), ASIp);// 记录收包日志

			PreparedStatement preSta = operateDataBase("select * from userInfo where id=?");
			preSta.setString(1, id);
			ResultSet res = preSta.executeQuery();// 执行查询，返回结果集对象
			while (res.next()) {// 注册失败，返回注册失败包
				PackageAstoCRegister packageAstoCRegister = new PackageAstoCRegister((byte) 0);// 封包
				byte[] returnData = JavaStruct.pack(packageAstoCRegister, ByteOrder.BIG_ENDIAN);
				socket.getOutputStream().write(returnData);// 发包
				socket.shutdownOutput();
				Log.sendPackageLog("AS注册失败包", ASIp, socket.getInetAddress().getHostAddress());// 记录发包日志
				return;
			}

			PackageAstoCRegister packageAstoCRegister = new PackageAstoCRegister((byte) 1);// 封包
			byte[] returnData = JavaStruct.pack(packageAstoCRegister, ByteOrder.BIG_ENDIAN);
			socket.getOutputStream().write(returnData);// 发包
			socket.shutdownOutput();
			preSta = operateDataBase("insert into userInfo(id,pwd) values(?,?)");
			preSta.setString(1, id);// 预处理添加数据
			preSta.setBytes(2, pwd);
			preSta.executeUpdate();// 数据库添加用户注册的账号、密码
			Log.sendPackageLog("AS注册成功包", ASIp, socket.getInetAddress().getHostAddress());// 记录发包日志
		} catch (StructException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理登录包的函数
	 */
	private void handleLogin() {
		try {
			PackageCtoAsLogin packageCtoAsLogin = new PackageCtoAsLogin(new byte[1]);// 解第一层包
			JavaStruct.unpack(packageCtoAsLogin, receivePackage, ByteOrder.BIG_ENDIAN);
			byte[] decryptedData = new MainBody(packageCtoAsLogin.Ekc, keyCAs, 0).mainBody();// 解密
			PackageCtoAsLoginEkc packageCtoAsLoginEkc = new PackageCtoAsLoginEkc("", "", "", new byte[1]);// 解第二层包
			JavaStruct.unpack(packageCtoAsLoginEkc, decryptedData, ByteOrder.BIG_ENDIAN);

			String idc = String.valueOf(packageCtoAsLoginEkc.IDc);
			byte[] pwd = packageCtoAsLoginEkc.content;
			Log.receivePackageLog("AS登录认证包", socket.getInetAddress().getHostAddress(), ASIp);
			PreparedStatement preSta = operateDataBase("select * from userInfo where id=? and pwd=?");
			preSta.setString(1, idc);
			preSta.setBytes(2, pwd);
			ResultSet res = preSta.executeQuery();// 执行查询，返回结果集对象
			while (res.next()) {// 登录成功，返回登录成功包
				String ipc = socket.getInetAddress().getHostAddress();
				String idTgs = String.valueOf(packageCtoAsLoginEkc.IDtgs);
				String ts = System.currentTimeMillis() + "";

				TicketTgs ticketTgs = new TicketTgs(keyCTgs, idc, ipc, idTgs, ts, ts);// 封第一层包，访问tgs的票据
				byte[] ticketPack = JavaStruct.pack(ticketTgs, ByteOrder.BIG_ENDIAN);
				byte[] ticketEe = new MainBody(ticketPack, keyAsTgs, 1).mainBody();// 加密tgs票据
				PackageAstoCAuthEkc packageAstoCAuthEkc = new PackageAstoCAuthEkc(keyCTgs, ipTgs, ts, ts, ticketEe);// 封第二层包
				byte[] authAkcPack = JavaStruct.pack(packageAstoCAuthEkc, ByteOrder.BIG_ENDIAN);
				byte[] authAkcPackEn = new MainBody(authAkcPack, keyCAs, 1).mainBody();// 加密第二层包
				PackageAstoCAuth packageAstoCAuth = new PackageAstoCAuth(authAkcPackEn);// 封第三层包
				byte[] returnData = JavaStruct.pack(packageAstoCAuth, ByteOrder.BIG_ENDIAN);
				socket.getOutputStream().write(returnData);
				socket.shutdownOutput();
				Log.sendPackageLog("登录成功包", ASIp, socket.getInetAddress().getHostAddress());
				return;
			}

			PackageAstoCLogin packageAstoCLogin = new PackageAstoCLogin((byte) 0);// 封包。登录失败，返回登录失败包
			byte[] returnData = JavaStruct.pack(packageAstoCLogin, ByteOrder.BIG_ENDIAN);
			socket.getOutputStream().write(returnData);
			socket.shutdownOutput();
			Log.sendPackageLog("登录失败包", ASIp, socket.getInetAddress().getHostAddress());
		} catch (StructException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理修改密码包的函数
	 */
	private void handleModifyPwd() {
		try {
			PackageCtoAsModify packageCtoAsModify = new PackageCtoAsModify(new byte[1]);// 解第一层包
			JavaStruct.unpack(packageCtoAsModify, receivePackage, ByteOrder.BIG_ENDIAN);
			byte[] decryptedData = new MainBody(packageCtoAsModify.EKc, keyCAs, 0).mainBody();// 解密
			PackageCtoAsModifyEkc packageCtoAsModifyEkc = new PackageCtoAsModifyEkc("", "", new byte[1], new byte[1]);// 解第二层包
			JavaStruct.unpack(packageCtoAsModifyEkc, decryptedData, ByteOrder.BIG_ENDIAN);

			String id = String.valueOf(packageCtoAsModifyEkc.IDc);
			byte[] oldpwd = packageCtoAsModifyEkc.oldPwd;
			byte[] newpwd = packageCtoAsModifyEkc.newPwd;
			Log.receivePackageLog("修改密码包", socket.getInetAddress().getHostAddress(), ASIp);

			PreparedStatement preSta = operateDataBase("select * from userInfo where id=? and pwd=?");
			preSta.setString(1, id);
			preSta.setBytes(2, oldpwd);
			ResultSet reSet = preSta.executeQuery();
			PackageAstoCModify packageAstoCModify;
			byte[] returnData;
			while (reSet.next()) {// 验证密码是否正确，如果正确
				preSta = operateDataBase("update userInfo set pwd=? where id=?");
				preSta.setBytes(1, newpwd);
				preSta.setString(2, id);
				if (preSta.executeUpdate() == 0) {// 密码修改失败
					packageAstoCModify = new PackageAstoCModify(id, (byte) 0, System.currentTimeMillis() + "");
					Log.sendPackageLog("修改密码失败包", ASIp, socket.getInetAddress().getHostAddress());
				} else {// 密码修改成功
					packageAstoCModify = new PackageAstoCModify(id, (byte) 1, System.currentTimeMillis() + "");
					Log.sendPackageLog("修改密码成功包", ASIp, socket.getInetAddress().getHostAddress());
				}

				returnData = JavaStruct.pack(packageAstoCModify, ByteOrder.BIG_ENDIAN);
				socket.getOutputStream().write(returnData);
				socket.shutdownOutput();
				return;
			}
			packageAstoCModify = new PackageAstoCModify(id, (byte) 0, System.currentTimeMillis() + "");// 账号密码错误
			returnData = JavaStruct.pack(packageAstoCModify, ByteOrder.BIG_ENDIAN);
			socket.getOutputStream().write(returnData);
			socket.shutdownOutput();
			Log.sendPackageLog("修改密码失败包", ASIp, socket.getInetAddress().getHostAddress());
		} catch (StructException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理查询收件人是否存在的包的函数
	 */
	private void handleReceiverIsExist() {
		try {
			PackageCtoAsCheck packageCtoAsCheck = new PackageCtoAsCheck(new byte[1]);// 解第一层包
			JavaStruct.unpack(packageCtoAsCheck, receivePackage);
			byte[] decryptedData = new MainBody(packageCtoAsCheck.EKc, keyCAs, 0).mainBody();// 解密
			PackageCtoAsCheckEkc packageCtoAsCheckEkc = new PackageCtoAsCheckEkc("", "", new byte[1]);// 解第二层包
			JavaStruct.unpack(packageCtoAsCheckEkc, decryptedData, ByteOrder.BIG_ENDIAN);

			String receiverId = new String(packageCtoAsCheckEkc.content);
			PreparedStatement preSta = operateDataBase("select * from userInfo where id=?");
			preSta.setString(1, receiverId);
			ResultSet res = preSta.executeQuery();// 执行查询，返回结果集对象
			Log.receivePackageLog("查询收件人是否存在包", socket.getInetAddress().getHostAddress(), ASIp);

			PackageAstoCCheck packageAstoCCheck;
			byte[] returnData;
			while (res.next()) {// 该收件人存在
				packageAstoCCheck = new PackageAstoCCheck(((byte) 1));
				returnData = JavaStruct.pack(packageAstoCCheck, ByteOrder.BIG_ENDIAN);
				socket.getOutputStream().write(returnData);
				socket.shutdownOutput();
				Log.sendPackageLog("收件人存在包", ASIp, socket.getInetAddress().getHostAddress());
				return;
			}
			packageAstoCCheck = new PackageAstoCCheck((byte) 0);// 收件人不存在
			returnData = JavaStruct.pack(packageAstoCCheck, ByteOrder.BIG_ENDIAN);
			socket.getOutputStream().write(returnData);
			socket.shutdownOutput();
			Log.sendPackageLog("收件人不存在包", ASIp, socket.getInetAddress().getHostAddress());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (StructException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 连接数据库函数，返回PrepareStatement对象
	 */
	private PreparedStatement operateDataBase(String sql) {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String url = "jdbc:sqlserver://localhost:1433;DatabaseName=AS";
			String user = "sa";
			String password = "12345678";
			Connection con = DriverManager.getConnection(url, user, password);// 连接数据库
			PreparedStatement preSta = con.prepareStatement(sql);
			return preSta;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
