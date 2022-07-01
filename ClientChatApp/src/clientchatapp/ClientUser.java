package clientchatapp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import myprotocol.PacketBuilder;
import myprotocol.PacketReader;
import myprotocol.Opcode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ClientUser {

    private Socket socket;
    private String username;
    private PacketBuilder packetBuilder;
    private PacketReader packetReader;
    private BufferedInputStream buffIn;
    private BufferedOutputStream buffOut;
    private int id;
    private int port;
    private String add;
    
    private HashMap<Integer, String> users;
    private HashMap<Integer, String> rooms;
    
    private final ClientFormChatApp clientForm;
    
    public ClientUser(ClientFormChatApp clientForm, int port, String add) throws IOException{
        this.clientForm = clientForm;
        id = -1;
        users = new HashMap<>();
        rooms = new HashMap<>();
        this.port = port;
        this.add = add;
    }
    public ClientUser(int port, String add){
        this.clientForm = null;
        id = -1;
        users = new HashMap<>();
        rooms = new HashMap<>();
        
        this.port = port;
        this.add = add;
    }
    
    public boolean isConnected(){ return socket.isConnected(); }
    
    public void ConnectToServer(String username) throws IOException{
        //if(!socket.isConnected()){
            
            //SocketAddress endpoint = new InetSocketAddress("172.30.0.4", 7000);
            //socket.connect(endpoint);
        //}
        System.out.println("Trying to conect");
        socket = new Socket(add, port);
        
        buffIn = new BufferedInputStream(socket.getInputStream());
        buffOut = new BufferedOutputStream(socket.getOutputStream());
        packetReader = new PacketReader(buffIn);
        
        packetBuilder = new PacketBuilder();
        sendUsername(username);
        
        //Criar Runnable e Thread para continuar a ler pacotes enviados pelo servidor
        Runnable task;
        task = this::ReadPackets;
        new Thread(task).start();
    }
    
    /**
     * Handle packet data received from server
     */
    public void ReadPackets(){
        while(true){
            try{
                //Read first byte (opcode)
                int opCode = buffIn.read();
                System.out.println("opcode: "+opCode);
                
                switch(opCode){
                    
                    //Received a message ( message length + message )
                    case Opcode.MESSAGE -> {
                        String msg = readMessage();
                        displayMessage(msg, "");
                    }
                    
                    //Received private message from user ( username length + username + message length + message )
                    case Opcode.MESSAGE_WITH_USERNAME ->{
                        int from = readInteger();
                        String msg = readMessage();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                        String formattedMessage = String.format("[%s] %s: %s", LocalDateTime.now().format(formatter), users.get(from), msg);
                        displayMessage( msg, users.get(from) );
                    }
                    
                    //Received public message from user ( username length + username + message length + message )
                    case Opcode.MESSAGE_ALL -> {
                        int from = readInteger();
                        String msg = readMessage();
                        String formattedMessage = String.format("[%s] %s: %s", LocalDateTime.now().toString(), users.get(from), msg);
                        displayMessage( formattedMessage, users.get(from));
                    }
                    
                    //Received unique client ID from server
                    case Opcode.CLIENT_ID -> id = readInteger();
                    
                    //
                    case Opcode.USER_CONNECT -> {
                        int idConnected = readInteger();
                        String usernameConnected = readMessage();
                        users.put(idConnected, usernameConnected);
                        displayNewUserConnection(idConnected, usernameConnected);
                    }
                    
                    //
                    case Opcode.USER_DISCONNECT -> {
                        int idDisconnected = readInteger();
                        System.out.println(idDisconnected);
                        String usernameDisconnected = readMessage();
                        System.out.println("User: "+ usernameDisconnected + ", id: "+idDisconnected+" disconnected!");
                        users.remove(idDisconnected);
                        displayUserDisconnection(idDisconnected, usernameDisconnected);
                    }
                    
                    case Opcode.NEW_ROOM -> {
                        int roomID = readInteger();
                        String roomName = readMessage();
                        rooms.put(roomID, roomName);
                        displayNewRoomCreated(roomID, roomName);
                    }
                    
                    //All the other
                    default -> System.out.println("Client doesn't recognize this code: "+opCode);
                }
                
            }catch(IOException ex){
                System.out.println(ex);
            }
        }
    }
    
    public void sendMessage(String message) throws IOException{
        buffOut.write(packetBuilder.packetMessage(message));
        buffOut.flush();
    }
    
    public void sendUsername(String username) throws IOException{
        buffOut.write(packetBuilder.packetMessage(username));
        buffOut.flush();
    }
    
    public void sendMessageToUser(int targetID, String message) throws IOException{
        buffOut.write(packetBuilder.packetMessageWithUsername(targetID, message));
        buffOut.flush();
    }
    
    public void reserveNewChatRoom(ArrayList<Integer> users) throws IOException{
        buffOut.write(packetBuilder.packetChatUsers(users));
        buffOut.flush();
    }
    
    public void closeEverything() throws IOException{
        if(buffIn != null) buffIn.close();
        if(buffOut != null) buffOut.close();
        if(socket != null) socket.close();
    }
    
    private String readMessage() throws IOException{
        String msg = packetReader.readString();
        return msg;
    }
    
    private int readInteger() throws IOException{
        return packetReader.readInteger();
    }
    
    private void displayMessage(String message, String from ){
        if(clientForm != null) clientForm.newMessageReceived(message, from);
        else{
            System.out.println(message);
        }
    }
    
    private void displayNewUserConnection(int clientID, String username){
        if(clientForm != null) clientForm.newUserConnected(clientID, username);
        else System.out.println(username + " #" + clientID + " connected!");
    }
    
    private void displayUserDisconnection(int clientID, String username ){
        if(clientForm != null) clientForm.userDisconnected( clientID, username );
        else System.out.println(username + " #" + clientID + " disconnected!");
    }
    
    private void displayNewRoomCreated(int roomID, String roomName){
        if(clientForm != null) clientForm.newRoomChatCreate(roomID, roomName);
        else System.out.println(roomName + " #" + roomID + " added");
    }
    private void displayRoomRemoval(int roomID, String roomName){
        if(clientForm != null) clientForm.roomChatRemoved(roomID, roomName);
        else System.out.println(roomName + " #" + roomID + " removed");
    }
    
}
