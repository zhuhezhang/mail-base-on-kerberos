package pack;

import struct.ArrayLengthMarker;
import struct.StructClass;
import struct.StructField;
/**
 * @author  xyb
 * @version 1.0
 */
@StructClass
public class PackageCtoVRequestMailEkCV {
    @StructField(order = 0)
    @ArrayLengthMarker(fieldName = "IDc")
    public int IDcLen;
    @StructField(order = 1)
    public char[] IDc;
    @StructField(order = 2)
    @ArrayLengthMarker(fieldName = "TS")
    public int TSLen;
    @StructField(order = 3)
    public char[] TS;
    @StructField(order = 4)
    public byte[] redundancy;
    public PackageCtoVRequestMailEkCV(String IDc1,String TS1){
        this.IDcLen=IDc1.length();
        this.IDc=IDc1.toCharArray();
        this.TSLen=TS1.length();
        this.TS=TS1.toCharArray();
        this.redundancy=new byte[10];
    }
}
