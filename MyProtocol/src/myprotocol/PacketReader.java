package myprotocol;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PacketReader {
    
    BufferedInputStream buffIn;
    ByteBuffer byteBuffer;
    
    public PacketReader(BufferedInputStream buffIn){
        this.buffIn = buffIn;
    }
    
    public String readString() throws IOException{
        byte[] stringBuffer;
        byte[] lengthBytes = buffIn.readNBytes(Short.BYTES);
        
        //Read size (in bytes) of the string that follows
        byteBuffer = ByteBuffer.allocate(Short.BYTES);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.put(lengthBytes);
        byteBuffer.rewind();
        
        short lengthShort = byteBuffer.getShort();
        int length = Integer.valueOf(lengthShort);

        //Read message
        stringBuffer = new byte[length];
        buffIn.read(stringBuffer, 0, length);
        String message = new String(stringBuffer, StandardCharsets.UTF_8);

        return message;
    }
    
    public int readInteger() throws IOException{
        byte[] integerBuffer = buffIn.readNBytes(Integer.BYTES);
        byteBuffer = ByteBuffer.allocate(Integer.BYTES);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.put(integerBuffer);
        byteBuffer.rewind();
        int value = byteBuffer.getInt();
        
        return value;
    }

}
