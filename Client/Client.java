import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class Client {
    //Operational Data
    protected static String host; // can be changed
    protected static int port;
    protected static DataOutputStream dosWriter;
    protected static DataInputStream disReader;
    protected static Socket endSocket;
    protected static Object monitor = new Object();
    protected static Object monitor2 = new Object();
    protected static Object monitor3 = new Object();
    
    //Constants
    protected final static String[] commands = { "/join <server ip> <port>", "/exit" };
    
    //Threads
    protected static CommandThread reader = new CommandThread();
    protected static ReceiverThread receiver = new ReceiverThread();

    //Message Handlers
    protected static CopyOnWriteArrayList<String> announcements = new CopyOnWriteArrayList<String>();
    protected static CopyOnWriteArrayList<String> chats = new CopyOnWriteArrayList<String>();
    protected static CopyOnWriteArrayList<String> logs = new CopyOnWriteArrayList<String>();

    public static void main(String[] args) {
        new Thread(reader).start();
        new Thread(receiver).start();
    }

    public static int getAnnouncementLength() {
        return announcements.size();
    }

    public static int getChatLength() {
        return chats.size();
    }

    public static int getLogLength() {
        return logs.size();
    }
}
