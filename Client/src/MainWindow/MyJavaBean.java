package MainWindow;

public class MyJavaBean {
    private String accountAlertString;
    private String passwordAlertString;
    private String welcomeString;
    private String emailAddressString;
    private String attachmentString;

    public String getAccountAlertString(){ return accountAlertString;}
    public void setAccountAlertString(String accountAlertString){ this.accountAlertString=accountAlertString;}

    public String getPasswordAlertString(){ return passwordAlertString;}
    public void setPasswordAlertString(String passwordAlertString){ this.passwordAlertString=passwordAlertString;}

    public String getWelcomeString(){
        return welcomeString;
    }
    public void setWelcomeString(String welcomeString){
        this.welcomeString = welcomeString;
    }

    public String getEmailAddressString(){
        return emailAddressString;
    }
    public void setEmailAddressString(String emailAddressString){
        this.emailAddressString=emailAddressString;
    }

    public String getAttachmentString(){ return attachmentString;}
    public void setAttachmentString(String attachmentString){ this.attachmentString=attachmentString;}
}