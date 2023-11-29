package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Client {
    //Operational Data
    protected static String host; // can be changed
    protected static int port;
    protected static DataOutputStream dosWriter;
    protected static DataInputStream disReader;
    protected static Socket endSocket;
    
    //Constants
    protected final static String[] commands = { "/join <server ip> <port>", "/exit" };
    
    //Threads
    protected static CommandThread reader = new CommandThread();
    protected static ReceiverThread receiver = new ReceiverThread();

    //Message Handlers
    protected static ArrayList<String> announcements = new ArrayList<String>();
    protected static ArrayList<String> chats = new ArrayList<String>();
    protected static ArrayList<String> logs = new ArrayList<String>();

    public static void main(String[] args) {
        new Thread(reader).start();
        new Thread(receiver).start();
    }
}
