package pack;

import struct.ArrayLengthMarker;
import struct.StructClass;
import struct.StructField;
/**
 * @author  xyb
 * @version 1.0
 */
@StructClass
public class PackageCtoVRequestMail {
    @StructField(order = 0)
    public byte status;
    @StructField(order = 1)
    @ArrayLengthMarker(fieldName = "EkCV")
    public int EkCVLen;
    @StructField(order = 2)
    public byte[] EkCV;
    @StructField(order = 3)
    @ArrayLengthMarker(fieldName = "TicketV")
    public int TicketVLen;
    @StructField(order = 4)
    public byte[] TicketV;
    public PackageCtoVRequestMail(byte[] EkCV1,byte[] TicketV1){
        this.status=2;
        this.EkCVLen= EkCV1.length;
        this.EkCV=EkCV1;
        this.TicketVLen=TicketV1.length;
        this.TicketV=TicketV1;
    }
}
