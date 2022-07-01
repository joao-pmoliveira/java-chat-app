package serverchatapp.Net;

import java.util.ArrayList;

public class RoomInfo {
    public int roomID;
    public String roomName;
    public ArrayList<Integer> usersID;
    
    
    public RoomInfo(String name, int roomID){
        roomName = name;
        this.roomID = roomID;
        usersID = new ArrayList<>();
    }
}
