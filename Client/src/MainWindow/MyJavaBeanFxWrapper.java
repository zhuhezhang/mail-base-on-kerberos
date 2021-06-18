package MainWindow;

import javafx.beans.property.StringProperty;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;

public class MyJavaBeanFxWrapper {
    private MyJavaBean myJavaBean;
    private StringProperty accountAlertString;
    private StringProperty passwordAlertString;
    private StringProperty welcomeString;
    private StringProperty emailAddressString;
    private StringProperty attachmentString;

    public MyJavaBeanFxWrapper(MyJavaBean myJavaBean) throws NoSuchMethodException {
        this.myJavaBean=myJavaBean;
        myProperty();
    }

    private void myProperty() throws NoSuchMethodException {
        accountAlertString=JavaBeanStringPropertyBuilder.create().bean(this.myJavaBean).name("accountAlertString").build();
        passwordAlertString=JavaBeanStringPropertyBuilder.create().bean(this.myJavaBean).name("passwordAlertString").build();
        welcomeString = JavaBeanStringPropertyBuilder.create().bean(this.myJavaBean).name("welcomeString").build();
        emailAddressString = JavaBeanStringPropertyBuilder.create().bean(this.myJavaBean).name("emailAddressString").build();
        attachmentString=JavaBeanStringPropertyBuilder.create().bean(this.myJavaBean).name("attachmentString").build();
    }

    public MyJavaBean getMyJavaBean() {
        return myJavaBean;
    }

    public void setMyJavaBean(MyJavaBean myJavaBean) throws NoSuchMethodException {
        this.myJavaBean = myJavaBean;
        myProperty();
    }

    public String getAccountAlertString(){ return accountAlertString.get();}
    public StringProperty accountAlertStringProperty(){ return accountAlertString;}

    public String getPasswordAlertString(){ return passwordAlertString.get();}
    public StringProperty passwordAlertStringProperty(){ return passwordAlertString;}

    public String getWelcomeString() {
        return welcomeString.get();
    }
    public StringProperty welcomeStringProperty() {
        return welcomeString;
    }

    public String getEmailAddressString(){
        return emailAddressString.get();
    }
    public StringProperty emailAddressStringProperty(){
        return emailAddressString;
    }

    public String getAttachmentString() { return attachmentString.get();}
    public StringProperty attachmentStringProperty() { return attachmentString;}
}