package MainWindow;

//import javafx.scene.control.CheckBox;

public class EmailData {
    private String sender;
    private String receiver;
    private String subject;
    private String date;
    private String attachmentName;
    private boolean isSelect;

    public EmailData(String sender,String receiver,String subject,String date,String attachmentName,boolean isSelect){
        this.sender=sender;
        this.receiver=receiver;
        this.subject=subject;
        this.date=date;
        this.attachmentName=attachmentName;
        this.isSelect=isSelect;
    }

    public String getSender(){ return sender;}
    public void setSender(String sender){ this.sender=sender;}

    public String getReceiver(){ return receiver;}
    public void setReceiver(String receiver){ this.receiver=receiver;}

    public String getSubject(){ return subject;}
    public void setSubject(String subject){ this.subject=subject;}

    public String getDate(){ return date;}
    public void setDate(String date){ this.date=date;}

    public String getAttachmentName(){ return attachmentName;}
    public void setAttachmentName(String attachmentName){ this.attachmentName=attachmentName;}

    public boolean getSelect(){ return isSelect;}
    public void setSelect(boolean isSelect){ this.isSelect=isSelect;}
}