package myprotocol;

public class Opcode {
    public static final byte MESSAGE = 0;
    public static final byte MESSAGE_WITH_USERNAME = 1;
    public static final byte MESSAGE_ALL = 2;
    public static final byte CLIENT_ID = 3;
    public static final byte USER_CONNECT = 4;
    public static final byte USER_DISCONNECT = 5;
    public static final byte NEW_ROOM = 6;
    public static final byte RESERVE_ROOM = 7;
}