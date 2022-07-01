package myprotocol;

public class Rules {
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final int MAX_USERNAME_LENGTH = 15;
    
    public static final String legalizeUsername(String username){
        return username.trim();
    }

    
    public static final boolean isUsernameLegal(String username){
        if(username.isBlank()){
            System.out.println("Username is not valid: username is blank");
            return false;
        }
        
        if(!username.equals(username.trim())){
            System.out.println("Username is not valid: remove white space");
            return false;
        }
        
        if(username.contains(" ")) {
            System.out.println("Username is not valid: remove white space");
            return false;
        }
        
        if(username.length() > MAX_USERNAME_LENGTH){
            System.out.println("Username too long: keep it under "+ MAX_USERNAME_LENGTH + " characters.");
            return false;
        }
        
        return true;
    }
}
