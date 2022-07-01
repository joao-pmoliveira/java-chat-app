package myprotocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;


public class PacketBuilder {
    ByteArrayOutputStream byteStream;
    ByteBuffer byteBuffer;
    
    public PacketBuilder(){
        byteStream = new ByteArrayOutputStream();
    }
    
    /**
     * Packets the message and the opcode into an array of bytes.
     * @param message the message to be packed
     * @return byte array with the opcode follow by message length (4 bytes) and then the message
     * @throws IOException 
     */
    public byte[] packetMessage(String message) throws IOException{
        writeOpCode(Opcode.MESSAGE);
        writeString(message);
        return getPacketBytes();
    }
  
    public byte[] packetUsername(String username) throws IOException{
        writeOpCode( Opcode.MESSAGE);
        writeString(username);
        return getPacketBytes();
    }
    
    public byte[] packetMessageWithUsername(int targetID, String message) throws IOException{
        writeOpCode( Opcode.MESSAGE_WITH_USERNAME);
        writeInteger(targetID);
        writeString(message);
        return getPacketBytes();
    }
    
    public byte[] packetClientID(int value) throws IOException{
        writeOpCode( Opcode.CLIENT_ID );
        writeInteger(value);
        return getPacketBytes();
    }
    
    public byte[] packetUserConnectInfo(int clientID, String username) throws IOException{
        writeOpCode( Opcode.USER_CONNECT );
        writeInteger(clientID);
        writeString(username);
        return getPacketBytes();
    }
    
    public byte[] packetUserDisconnectInfo(int clientID, String username) throws IOException{
        writeOpCode( Opcode.USER_DISCONNECT );
        writeInteger(clientID);
        writeString(username);
        return getPacketBytes();
    }
    
    public byte[] packetNewRoomInf(int roomID, String roomName) throws IOException{
        writeOpCode( Opcode.NEW_ROOM );
        writeInteger(roomID);
        writeString(roomName);
        return getPacketBytes();
    }
    
    public byte[] packetChatUsers(ArrayList<Integer> users) throws IOException{
        writeOpCode( Opcode.RESERVE_ROOM );
        writeInteger(users.size());
        for(int id : users){
            writeInteger(id);
        }
        return getPacketBytes();
    }
    
    //-----------
    
    private void writeOpCode(byte code){
        byteStream.write(code);
    }
    
    private void writeString(String str) throws IOException{
        //Converter string em bytes
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        
        //Coverter tamanho da string em bytes - short para byte array
        Integer lengthInteger = strBytes.length;
        byte[] lengthBytes = convertStringLengthInByteArray(lengthInteger.shortValue());
        
        byteStream.write(lengthBytes);

        //Converter messagem em bytes
        byteStream.write(strBytes);
    }
    
    private void writeInteger(int value) throws IOException{
        byte[] valueBytes;

        byteBuffer = ByteBuffer.allocate(Integer.BYTES);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putInt(value);
        byteBuffer.rewind();
        
        valueBytes = byteBuffer.array();
        //System.out.println(Arrays.toString(valueBytes));
        byteStream.write(valueBytes);
    }
    
    private byte[] convertStringLengthInByteArray(short value){
        byte[] lengthBytes;
        byteBuffer = ByteBuffer.allocate(Short.BYTES);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putShort(value);
        byteBuffer.rewind();
        lengthBytes = byteBuffer.array();
        
        //System.out.println(Arrays.toString(lengthBytes));
        return lengthBytes;
    }
    
    private byte[] getPacketBytes(){
        byte[] toSend =  byteStream.toByteArray();
        byteStream.reset();
        return toSend;
    }
    
    
}
