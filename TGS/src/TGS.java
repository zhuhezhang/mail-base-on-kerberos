import pack.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import des.MainBody;
import struct.JavaStruct;
import struct.StructException;

public class TGS implements Runnable{
	private Socket socket2;
	private static byte[] packageReceived;
	static String KEY="12345678";
	private String IDtgs;
	private String KCV;
	private String IDV;
	private String Lifetime4="10s";
	private String TS4;
	private String TS;
	public TGS(Socket socket)
	{
		this.socket2=socket;
		this.IDtgs="172.20.10.7";
		this.KCV="12345678";
		this.IDV="172.20.10.7";
	}
	@Override
	public  void run()
	{
		byte[] buffer=new byte[1024];	
		ArrayList<Byte> receivePackageTmp = new ArrayList<Byte>();
		int len = 0;
		try {
			while((len=socket2.getInputStream().read(buffer))!=-1)
			{
				for(int i=0;i<len;i++)
				{
					receivePackageTmp.add(buffer[i]);
				}
			}
			packageReceived=new byte[receivePackageTmp.size()];
			for(int i=0;i<receivePackageTmp.size();i++)
			{
				packageReceived[i]=receivePackageTmp.get(i);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		PackageCtoTgs b=new PackageCtoTgs("", new byte[]{(byte) 0}, new byte[]{(byte) 0});
		try {
			JavaStruct.unpack(b, packageReceived);
		    byte[] ticketTgs = null;
		    ticketTgs = new MainBody(b.TicketTgs,KEY,0).mainBody();
		    TicketTgs a=new TicketTgs("","","","","","");
		    JavaStruct.unpack(a, ticketTgs);
		    byte[] AuTgs = null;
		    AuTgs = new MainBody(b.Authenticator,new String(a.KcTgs),0).mainBody();
		    Authenticator c=new Authenticator("", "", "");
		    JavaStruct.unpack(c, AuTgs);
		    System.out.println(String.valueOf(a.IDc)+"\t"+String.valueOf(c.IDc)+"\t"+String.valueOf(a.ADc)+"\t"+String.valueOf(c.ADc));
		    if(Arrays.equals(a.IDc,c.IDc)&&Arrays.equals(a.ADc,c.ADc))//发送包
		    {
		    	log.Log.AuthLog(IDtgs, 0, String.valueOf(a.IDc));
			    System.out.println("用户认证成功");
			    Date date1=new Date(0);
				SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				TS=df1.format(date1);
				System.out.println(IDV);
			    TicketV a2=new TicketV(KCV, String.valueOf(a.IDc),String.valueOf(a.ADc), IDV, TS, Lifetime4);
				byte[] a2fb = JavaStruct.pack(a2);
				byte[] a2jiami = new MainBody(a2fb,KEY,1).mainBody();
				Date date=new Date(0);
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				TS4=df.format(date);
			    PackageTgstoCEkCTgs d2=new PackageTgstoCEkCTgs(KCV, IDV, TS4, a2jiami);
			    System.out.println(String.valueOf(d2.IDv));
			    System.out.println(String.valueOf(a2jiami));
			    byte[] d2fb = JavaStruct.pack(d2);
			    byte[] d2jiami = new MainBody(d2fb,String.valueOf(a.KcTgs),1).mainBody();
			    PackageTgstoC c2=new PackageTgstoC(d2jiami);
				byte[] c2fb=JavaStruct.pack(c2);
				System.out.println(Arrays.toString(c2fb));
				socket2.getOutputStream().write(c2fb);
				socket2.shutdownOutput();
		    }
		    else
		    {
		    	log.Log.AuthLog(IDtgs, 1, String.valueOf(a.IDc));
			    System.out.println("非法入侵！");
		    }
		}catch (IOException | StructException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws Exception
	{
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(8888);
			System.out.println("TGS服务器启动");
			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("用户已连接");
				new Thread(new TGS(socket)).start();
			}
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
