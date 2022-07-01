package serverchatapp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import serverchatapp.Net.ClientHandler;
import serverchatapp.Net.RoomInfo;

public class ServerChatApp {
    
    static ServerSocket listener;
    static HashMap<Integer, ClientHandler> users;
    static HashMap<Integer, RoomInfo> rooms;
    static int clientsID;
    static int roomID;
    
    public static void main(String[] args) throws IOException{  
        listener = new ServerSocket(7000);
        users = new HashMap<>();
        clientsID = 0;
        roomID = 0;
        rooms = new HashMap<>();
        //Create room for all chat
        rooms.put(roomID, new RoomInfo("All", roomID));
        incrementUniqueRoomId();
        
        while(true){
            Socket clientSocket = listener.accept();
            incrementUniqueClientId();
            ClientHandler client = new ClientHandler(clientSocket, clientsID);
            //users.put(clientsID, client);
            Thread clientThread = new Thread(client);
            clientThread.start();
        }
    }
    
    public static void broadcastToUser(int sender, int targetID, String message) throws IOException{
        
        if(!rooms.containsKey(targetID)) return;
        
        for(int clientID : rooms.get(targetID).usersID){
            if(clientID == sender) continue;
            ServerChatApp.users.get(clientID).SendMessageFromUser(sender, message);
        }
    }
    
    public static void broadcastConnectionToAllUsers(int clientID, String username) throws IOException{
        for(ClientHandler client : users.values()){
            if(client.getUniqueID() == clientID) continue;
            client.sendClientConnect(clientID, username);
        }
    }
    
    public static void broadcastDisconnectionToAllUsers(int clientID, String username) throws IOException{
        for(ClientHandler client : users.values()){
            if(client.getUniqueID() == clientID) continue;
            System.out.println("told "+ client.getUsername() + " to remove me");
            client.sendClientDisconnect(clientID, username);
        }
    }
    
    public static void broadcastToUserAllAlreadyOnlineUsers(int clientID) throws IOException{
        ClientHandler userConnected = users.get(clientID);
        if(userConnected == null) return;
        
        for(ClientHandler client : users.values()){
            if(client.getUniqueID() == clientID ) continue;
            userConnected.sendClientConnect(client.getUniqueID(), client.getUsername());
        }
    }
    
    public static void broadcastToUsersNewChatRoom(int roomID, String roomName, ArrayList<Integer> users) throws IOException{
        
        for(int clientID : users){
            ClientHandler client = ServerChatApp.users.get(clientID);
            client.sendNewRoom(roomID, roomName);
        }
    }
    
    public static void addClientToUsersList(int clientID, ClientHandler client){
        users.put(clientID, client);
    }
    
    public static void removeClientFromUsersList(int clientID){
        users.remove(clientID);
    }
    
    public static void removeClientFromRooms(int clientID){
        for(int roomID : rooms.keySet()){
            
            if(rooms.get(roomID).usersID.contains(clientID)){
                rooms.get(roomID).usersID.remove(Integer.valueOf(clientID));
                
                if(roomID != 0 && rooms.get(roomID).usersID.isEmpty()){
                    rooms.remove(roomID);
                }
            }
        }
    }
    
    public static void addClientToAllChatRoom(ClientHandler client){
        rooms.get(0).usersID.add(client.getUniqueID());
    }
    
    public static void createNewChatRoom(ArrayList<Integer> users) throws IOException{
        incrementUniqueRoomId();
        
        RoomInfo ri = new RoomInfo("Sala "+ roomID, roomID);
        rooms.put(roomID, ri );
        ArrayList<Integer> roomUserIDs = rooms.get(roomID).usersID;
        roomUserIDs.addAll(users);

        broadcastToUsersNewChatRoom(ri.roomID, ri.roomName,roomUserIDs);
        
        //Log new room created
        File logFile = new File("log.txt");
        logFile.createNewFile();
        FileWriter fileWriter = new FileWriter(logFile, true);
        fileWriter.write("Sala "+ ri.roomName+" criada com " + roomUserIDs.size() + " utilizadores \n");
        fileWriter.close();
    }
    
    public static String getRoomNameFromID(int roomID){
        if(rooms.containsKey(roomID)){
            return rooms.get(roomID).roomName;
        }
        return "?";
    }
    
    private static ClientHandler findOnlineUserByUsername(int targetID){
        for(ClientHandler client : users.values()){
            if(client.getUniqueID() == targetID ){
                return client;
            }
        }
        return null;
    }
    
    private static void incrementUniqueClientId(){ clientsID++; }
    private static void incrementUniqueRoomId(){ roomID++; }
    
}
