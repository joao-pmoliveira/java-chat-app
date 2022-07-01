package serverchatapp.Net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import serverchatapp.ServerChatApp;
import myprotocol.*;

/**
 * Classe que vai guardar informação sobre o socket client conectado ao servidor
 * 
 * @author joaoo
 */
public class ClientHandler implements Runnable{
    private String username;
    private final Socket clientSocket;
    private final BufferedOutputStream buffOut;
    private final BufferedInputStream buffIn;
    private final PacketReader packetReader;
    private final PacketBuilder packetBuilder;
    private final int id;
    private Thread thread;
    
    public ClientHandler(Socket clientSocket, int clientID) throws IOException{
        
        id = clientID;
        username = "";
        this.clientSocket = clientSocket;
        this.buffIn = new BufferedInputStream(clientSocket.getInputStream());
        this.buffOut = new BufferedOutputStream(clientSocket.getOutputStream());
        this.packetReader = new PacketReader(buffIn);
        this.packetBuilder = new PacketBuilder();
        
        //Read username sendt by client
        int opCode = buffIn.read();
        username = packetReader.readString();
        System.out.println(username+" connected");

        //Send to client their unique ID
        packetBuilder.packetClientID(clientID);
        
        //Add Client to server's list of users
        ServerChatApp.addClientToUsersList(id, this);
        
        //Broadcast connection to all other connected clients
        ServerChatApp.broadcastConnectionToAllUsers(id, username);
        
        //Receive all online users
        ServerChatApp.broadcastToUserAllAlreadyOnlineUsers(id);
        
        ServerChatApp.addClientToAllChatRoom(this);
        
        
        this.sendNewRoom(0, "All");
//        this.sendClientConnect(0, "All");
        
        thread = new Thread(this);
    }
    
    public String getUsername(){ return username; }
    public int getUniqueID(){ return id; }
    
    @Override
    public void run() {
        boolean runThread = true;
        while(runThread){
            try {
                int opCode = buffIn.read();
                switch(opCode){
                    
                    case Opcode.MESSAGE -> {   //deprecated - unused for now.
                        packetReader.readString();
                    }

                    case Opcode.MESSAGE_WITH_USERNAME ->{
                        String msg;
                        int targetID;
                        targetID = packetReader.readInteger();
                        msg = packetReader.readString();
                        System.out.println("Message received from "+targetID+": "+msg);
                        ServerChatApp.broadcastToUser(id, targetID, msg);
                        
                        //Log message
                        File logFile = new File("log.txt");
                        logFile.createNewFile();
                        FileWriter fileWriter = new FileWriter(logFile, true);
                        fileWriter.write("De |"+username+"| Para |"+ ServerChatApp.getRoomNameFromID(targetID) + "|: "+"\""+msg+"\"\n");
                        fileWriter.close();
                    }
                    
                    case Opcode.RESERVE_ROOM ->{
                        System.out.println("RESERVING NEW ROOM");
                        int usersAmount = packetReader.readInteger();
                        ArrayList<Integer> users = new ArrayList<>();
                        
                        for(int i = 0; i < usersAmount; i++){
                            users.add(packetReader.readInteger());
                            System.out.println("Received user id: "+ id);
                        }
                        
                        users.add(this.id);
                        
                        ServerChatApp.createNewChatRoom(users);
                
                    }

                    default -> System.out.println("Server doesn't recognize OpCode: "+ opCode);
                }
            } catch (IOException ex) {
                System.out.println(ex);
                if(clientSocket != null){
                    try {
                        clientSocket.close();
                        System.out.println(username + " disconnected");
                        ServerChatApp.removeClientFromUsersList(id);
                        ServerChatApp.removeClientFromRooms(id);
                        ServerChatApp.broadcastDisconnectionToAllUsers(id, username);
                    } catch (IOException ex1) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
                
                break;
            }
        }
    }
    
    public void SendMessage(String message) throws IOException{
        buffOut.write(packetBuilder.packetMessage(message));
        buffOut.flush();
    }
    
    public void SendMessageFromUser(int senderID, String message) throws IOException{
        buffOut.write(packetBuilder.packetMessageWithUsername(senderID, message));
        buffOut.flush();
    }
    
    public void sendClientConnect(int clientID, String username) throws IOException{
        buffOut.write(packetBuilder.packetUserConnectInfo(clientID, username));
        buffOut.flush();
    }
    
    public void sendClientDisconnect(int clientID, String username) throws IOException{
        buffOut.write(packetBuilder.packetUserDisconnectInfo(clientID, username));
        buffOut.flush();
    }
    public void sendNewRoom(int roomID, String roomName) throws IOException{
        buffOut.write(packetBuilder.packetNewRoomInf(roomID, roomName));
        buffOut.flush();
    }

// REFACTOR LATER -> metodos iguais tanto nesta class como na class Client dentro do ClientConsoleApp
    
}
