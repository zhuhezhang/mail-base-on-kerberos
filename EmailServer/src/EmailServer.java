import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import struct.JavaStruct;
import struct.StructException;

import des.MainBody;
import log.Log;
import pack.*;

/**
 * Kerberos之EmailServer（邮件服务器）类
 * 
 * @author zhz000
 */
public class EmailServer implements Runnable {

	private Socket socket;// 连接客户端返回的socket对象
	private byte[] receivePackage;// 接收到的数据包
	private final static String keyVTgs = "12345678";// V和Tgs会话的des密钥
	private final static String EmailServerIp = "172.20.10.7";// EmailServerIP地址

	public EmailServer(Socket socket) {
		this.socket = socket;
	}

	public static void main(String[] args) {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(9999);
			System.out.println("邮件服务器EmailServer运行中...");
			while (true) {
				Socket socket = serverSocket.accept();// 等待连接
				new Thread(new EmailServer(socket)).start();// 每有一个连接新建一个线程处理
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
		ArrayList<Byte> receivePackageTmp = new ArrayList<Byte>();// 临时保存的接收到的数据包
		int len = 0;
		try {
			while ((len = socket.getInputStream().read(buffer)) != -1) {// 获得所有接收到的数据临时保存在动态数组中
				for (int i = 0; i < len; i++) {
					receivePackageTmp.add(buffer[i]);
				}
			}
			receivePackage = new byte[receivePackageTmp.size()];
			for (int i = 0; i < receivePackageTmp.size(); i++) {// 动态数组转化为静态数组
				receivePackage[i] = receivePackageTmp.get(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		byte status = receivePackage[0];
		switch (status) {
		case 0:
			handleAuth();
			break;
		case 6:
			handlePost();
			break;
		case 2:
			handleRequest();
			break;
		default:
			break;
		}
	}

	/**
	 * 处理客户端认证的函数
	 */
	public void handleAuth() {
		try {
			PackageCtoVAuth packageCtoVAuth = new PackageCtoVAuth(new byte[1], new byte[1]);// 解第一层包
			JavaStruct.unpack(packageCtoVAuth, receivePackage, ByteOrder.BIG_ENDIAN);
			byte[] ticketDecrypted = new MainBody(packageCtoVAuth.TicketV, keyVTgs, 0).mainBody();// 解密ticketv
			TicketV ticketv = new TicketV("", "", "", "", "", "");// 解第二层包，ticketv
			JavaStruct.unpack(ticketv, ticketDecrypted, ByteOrder.BIG_ENDIAN);

			Log.receivePackageLog("客户端认证包", socket.getInetAddress().getHostAddress(), EmailServerIp);
			String idv = String.valueOf(ticketv.IDv);// 传过来的邮件服务器ip地址
			if (!checkTicket(idv)) {// 票据验证
				return;
			}
			PackageVtoCAuth packageVtoCAuth = new PackageVtoCAuth(System.currentTimeMillis() + "");// 验证成功，返回包
			byte[] returnData = JavaStruct.pack(packageVtoCAuth, ByteOrder.BIG_ENDIAN);
			socket.getOutputStream().write(returnData);
			socket.shutdownOutput();
			Log.sendPackageLog("客户端登录认证成功包", EmailServerIp, socket.getInetAddress().getHostAddress());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (StructException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理客户端发过来的邮件的函数
	 */
	public void handlePost() {
		try {
			PackageCtoVPost packageCtoVPost = new PackageCtoVPost(0, new byte[1], new byte[1]);// 解第一层包
			JavaStruct.unpack(packageCtoVPost, receivePackage, ByteOrder.BIG_ENDIAN);
			byte[] ticketDecrypted = new MainBody(packageCtoVPost.TicketV, keyVTgs, 0).mainBody();// 解密第二层包中的ticketv包
			TicketV ticketV = new TicketV("", "", "", "", "", "");// 解第二层包中的ticketv包
			JavaStruct.unpack(ticketV, ticketDecrypted, ByteOrder.BIG_ENDIAN);

			Log.receivePackageLog("客户端发邮件包", socket.getInetAddress().getHostAddress(), EmailServerIp);
			String idv = String.valueOf(ticketV.IDv);// 传过来的邮件服务器ip地址
			if (!checkTicket(idv)) {// 票据验证
				return;
			}

			String keyCV = String.valueOf(ticketV.Kcv);
			byte[] emailInfoDecrypted = new MainBody(packageCtoVPost.EkCV, keyCV, 0).mainBody();// 解密第二层包中的邮件信息包
			PackageCtoVPostEkCV packageCtoVPostEkCV = new PackageCtoVPostEkCV("", "", "", 0, 0, "", 0, new byte[1],
					new byte[1]);// 解第二层包的邮件信息报文包
			JavaStruct.unpack(packageCtoVPostEkCV, emailInfoDecrypted, ByteOrder.BIG_ENDIAN);
			String receiverId = String.valueOf(packageCtoVPostEkCV.ReceiverID);// 发件人账号
			String sendTime = String.valueOf(packageCtoVPostEkCV.TS);// 邮件发送时间
			String senderId = String.valueOf(packageCtoVPostEkCV.SenderID);// 收件人账号
			String senderSignature = String.valueOf(packageCtoVPostEkCV.EscMd5);// 发件人签名

			PreparedStatement preSta = operateDataBase(// 数据库添加发件人、收件人、发件时间、发件人签名
					"insert into sendMail(receiverId,sendTime,senderId,senderSignature) values(?,?,?,?)");
			preSta.setString(1, receiverId);
			preSta.setString(2, sendTime);
			preSta.setString(3, senderId);
			preSta.setString(4, senderSignature);
			preSta.executeUpdate();

			int themeLen = packageCtoVPostEkCV.SubjectLen;// 主题长度
			int textLen = packageCtoVPostEkCV.TextLen;// 正文长度
			int annexLen = packageCtoVPostEkCV.AttachmentLen;// 附件长度
			byte[] content = packageCtoVPostEkCV.Content;// 主题、正文、附件信息
			new File("./mail/" + receiverId + "/" + senderId + "_" + sendTime).mkdirs();// 文件夹存储发过来的邮件内容
			if (themeLen != 0) {// 邮件主题、正文、附件的保存
				byte[] theme = Arrays.copyOfRange(content, 0, themeLen);
				saveToLocal("./mail/" + receiverId + "/" + senderId + "_" + sendTime + "/theme", theme);
			}
			if (textLen != 0) {
				byte[] text = Arrays.copyOfRange(content, themeLen, themeLen + textLen);
				saveToLocal("./mail/" + receiverId + "/" + senderId + "_" + sendTime + "/text", text);
			}
			if (annexLen != 0) {
				byte[] annex = Arrays.copyOfRange(content, themeLen + textLen, themeLen + textLen + annexLen);
				String annexName = String.valueOf(packageCtoVPostEkCV.AttachmentName);
				saveToLocal("./mail/" + receiverId + "/" + senderId + "_" + sendTime + "/" + annexName, annex);
			}
		} catch (StructException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理客户端请求获取邮件的包的函数
	 */
	public void handleRequest() {
		try {
			PackageCtoVRequestMail packageCtoVRequestMail = new PackageCtoVRequestMail(new byte[1], new byte[1]);// 解第一层包
			JavaStruct.unpack(packageCtoVRequestMail, receivePackage, ByteOrder.BIG_ENDIAN);
			byte[] ticketVDecrypted = new MainBody(packageCtoVRequestMail.TicketV, keyVTgs, 0).mainBody();// 解密第二层包的ticket
			TicketV ticketV = new TicketV("", "", "", "", "", "");// 解第二层包中的ticketv包
			JavaStruct.unpack(ticketV, ticketVDecrypted, ByteOrder.BIG_ENDIAN);
			String idv = String.valueOf(ticketV.IDv);// 传过来的邮件服务器ip地址
			if (!checkTicket(idv)) {// 票据验证
				return;
			}

			String keyCV = String.valueOf(ticketV.Kcv);
			byte[] contentDecrypted = new MainBody(packageCtoVRequestMail.EkCV, keyCV, 0).mainBody();// 解密kcv加密的内容
			PackageCtoVRequestMailEkCV packageCtoVRequestMailEkCV = new PackageCtoVRequestMailEkCV("", "");// 解第二层的ekcv报文
			JavaStruct.unpack(packageCtoVRequestMailEkCV, contentDecrypted, ByteOrder.BIG_ENDIAN);

			String receiverId = String.valueOf(packageCtoVRequestMailEkCV.IDc);
			Log.receivePackageLog("客户端请求邮件包", socket.getInetAddress().getHostAddress(), EmailServerIp);
			PreparedStatement preSta = operateDataBase("select * from sendMail where receiverId=?");
			preSta.setString(1, receiverId);
			ResultSet res = preSta.executeQuery();// 查询数据中保存的该收件人还未接收到的邮件，返回结果集对象
			int flag = 0;
			while (res.next()) {// 遍历结果集，逐条邮件发送
				flag = 1;
				String sendTime = res.getString("sendTime");
				String senderId = res.getString("senderId");
				byte[] senderSignature = res.getString("senderSignature").getBytes();

				String annexName = "";// 得到附件名
				File file = new File("./mail/" + receiverId + "/" + senderId + "_" + sendTime + "/");
				File[] files = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					String name = files[i].getName();
					if (!name.matches("theme") && !name.matches("text")) {
						annexName = name;
						break;
					}
				}

				byte[][] mailContent = { new byte[0], new byte[0], new byte[0] };
				String[] mailConentName = { "theme", "text", annexName };
				for (int i = 0; i < mailConentName.length; i++) {// 逐项读出邮件的主题、正文、附件
					file = new File("./mail/" + receiverId + "/" + senderId + "_" + sendTime + "/" + mailConentName[i]);
					if (file.isFile()) {// 是文件且存在
						mailContent[i] = readMail(file);
					}
				}
				byte[] content = new byte[mailContent[0].length + mailContent[1].length + mailContent[2].length];// 将主题、正文、附件组合起来再封包
				System.arraycopy(mailContent[0], 0, content, 0, mailContent[0].length);
				System.arraycopy(mailContent[1], 0, content, mailContent[0].length, mailContent[1].length);
				System.arraycopy(mailContent[2], 0, content, mailContent[0].length + mailContent[1].length,
						mailContent[2].length);

				PackageVtoCPostEkCV packageVtoCPostEkCV = new PackageVtoCPostEkCV(receiverId, senderId, sendTime,
						mailContent[0].length, mailContent[1].length, annexName, mailContent[2].length, content,
						senderSignature);// 封第二层包
				byte[] ekcvPack = JavaStruct.pack(packageVtoCPostEkCV, ByteOrder.BIG_ENDIAN);
				byte[] ekcvPackEn = new MainBody(ekcvPack, keyCV, 1).mainBody();// 对第二层包进行加密
				PackageVtoCPost packageVtoCPost = new PackageVtoCPost(ekcvPackEn.length, ekcvPackEn);// 封第一层包
				byte[] returnData = JavaStruct.pack(packageVtoCPost, ByteOrder.BIG_ENDIAN);
				socket.getOutputStream().write(returnData);
			}
			if (flag == 1) {
				Log.sendPackageLog("发邮件给客户端包", EmailServerIp, socket.getInetAddress().getHostAddress());
				preSta = operateDataBase("delete from sendMail where receiverId=?");// 数据库未收件信息表删除相应的记录
				preSta.setString(1, receiverId);
				preSta.executeUpdate();
			} else {// 没有邮件，返回空邮件包
				Log.sendPackageLog("发给客户端空邮件包", EmailServerIp, socket.getInetAddress().getHostAddress());
				PackageVtoCPostStatus packageVtoCPostStatus = new PackageVtoCPostStatus(
						System.currentTimeMillis() + "");
				byte[] returnData = JavaStruct.pack(packageVtoCPostStatus, ByteOrder.BIG_ENDIAN);
				socket.getOutputStream().write(returnData);
			}
			socket.shutdownOutput();
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
	 * 连接数据库函数，返回PrepareStatement对象
	 */
	private PreparedStatement operateDataBase(String sql) {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String url = "jdbc:sqlserver://localhost:1433;DatabaseName=V";
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

	/**
	 * 验证票据，对比票据中的和本机的IP地址
	 */
	private boolean checkTicket(String idv) {
		if (idv.equals(EmailServerIp)) {// 对比接收到的邮件服务器ip地址是否和本机获取的是否一致，以此来进行验证
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 保存邮件的主题/内容/附件至对应文件夹
	 */
	private void saveToLocal(String filePath, byte[] data) {
		RandomAccessFile raf = null;
		try {
			File file = new File(filePath);
			file.createNewFile();
			raf = new RandomAccessFile(file, "rw");
			raf.write(data);
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 读取邮件的主题、正文、附件，以byte数组的形式返回
	 */
	private byte[] readMail(File file) {
		try {
			byte[] buffer = new byte[1024];
			int len = 0;
			ArrayList<Byte> tmp = new ArrayList<Byte>();
			RandomAccessFile ras = new RandomAccessFile(file, "r");
			while ((len = ras.read(buffer)) != -1) {
				for (int i = 0; i < len; i++) {
					tmp.add(buffer[i]);
				}
			}
			byte[] content = new byte[tmp.size()];
			for (int i = 0; i < tmp.size(); i++) {
				content[i] = tmp.get(i);
			}
			ras.close();
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
