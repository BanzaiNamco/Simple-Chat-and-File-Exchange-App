import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class Client implements ActionListener {
    //The view
    private View view;
    
    //Operational Data
    public static String host; // can be changed
    public static int port;
    public DataOutputStream dosWriter;
    public DataInputStream disReader;
    public Socket endSocket;
    
    //Constants
    private final static String[] commands = { "/join <server ip> <port>", "/?" };
    
    //Threads
    private ReceiverThread receiver;

    public Client() {
        this.view = new View();
        this.view.setActionListeners(this);
        this.receiver = new ReceiverThread(this.view);
    }

    public void updateChatDisplay(String text) {
        this.view.addChatLog(text);
    }

    public void updateAnnouncementDisplay(String text) {
        this.view.addAnnouncementLog(text);
    }

    public void updateServerDisplay(String text) {
        this.view.addServerLog(text);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        String cmd = this.view.getInput();
        String[] cmdArr = cmd.split(" ");

        try {
            switch (cmdArr[0]) {
                case "/join":
                    join(cmdArr);
                    break;
                case "/leave":
                    disconnect();
                    break;
                case "/register":
                    sendCommand(cmd);
                    break;
                case "/dir":
                    sendCommand(cmd);
                    break;
                case "/get":
                    getFile(cmdArr);
                    break;
                case "/store":
                    sendFile(cmdArr);
                    break;
                case "/userlist":
                    sendCommand(cmd);
                    break;
                case "/msg":
                    sendMessage(cmdArr);
                    break;
                case "/all":
                    sendAnnouncement(cmdArr);
                    break;
                case "/?":
                    printCommands();
                    break;
                default:
                    this.updateServerDisplay("Error: Unidentified command.");
                    break;
            }
        } catch (Exception e) {
            if (e instanceof SocketException) {
                this.updateServerDisplay("Error: Connection to server lost.");
                this.receiver.forcedisconnect();
                this.resetVariables();
            } else {
                this.updateServerDisplay("Error: Command parameters do not match or is not allowed.");
            }
        }
    }

    // Double check if there are other exceptions other than SocketException (server
    // is closed)
    private void sendCommand(String cmd) throws IOException {
        if (this.endSocket == null || this.endSocket.isClosed()) {
            this.updateServerDisplay("Error: Not connected to a server.");
            return;
        }

        // Send command to server
        this.dosWriter.writeUTF(cmd);
    }

    private void getFile(String[] cmdArr) throws IOException {
        if (this.endSocket == null || this.endSocket.isClosed()) {
            this.updateServerDisplay("Error: Not connected to a server.");
            return;
        }

        if (cmdArr.length != 2) {
            this.updateServerDisplay("Error: Command parameters do not match or is not allowed.");
            return;
        }

        // Send command to server
        this.dosWriter.writeUTF(cmdArr[0] + " " + cmdArr[1]);
    }

    private void sendFile(String[] cmdArr) throws IOException {
        if (this.endSocket == null || this.endSocket.isClosed()) {
            this.updateServerDisplay("Error: Not connected to a server.");
            return;
        }

        if (cmdArr.length != 2) {
            this.updateServerDisplay("Error: Command parameters do not match or is not allowed.");
            return;
        }

        // Get file name
        String fileName = cmdArr[1];

        // Get file
        File file = new File(fileName);

        // Check if file exists
        if (!file.exists() || !file.isFile()) {
            // File not found
            this.updateServerDisplay("Error: File not found.");
            return;
        }

        // Send command to server
        this.dosWriter.writeUTF(cmdArr[0] + " " + cmdArr[1]);
    }

    private void sendMessage(String[] cmdArr) throws IOException {
        if (this.endSocket == null || this.endSocket.isClosed()) {
            this.updateServerDisplay("Error: Not connected to a server.");
            return;
        }

        if (cmdArr.length < 3) {
            this.updateServerDisplay("Error: Command parameters do not match or is not allowed.");
            return;
        }

        //Stitch the message back together
        String message = "";
        for(int i = 2; i < cmdArr.length; i++) {
            message += cmdArr[i] + " ";
        }

        //Add to chat log
        this.updateChatDisplay("You to " + cmdArr[1] + ": " + message);

        // Send command to server
        this.dosWriter.writeUTF(cmdArr[0] + " " + cmdArr[1] + " " + message);
    }

    private void sendAnnouncement(String[] cmdArr) throws IOException {
        if (this.endSocket == null || this.endSocket.isClosed()) {
            this.updateServerDisplay("Error: Not connected to a server.");
            return;
        }

        if (cmdArr.length < 2) {
            this.updateServerDisplay("Error: Command parameters do not match or is not allowed.");
            return;
        }

        //Stitch the message back together
        String message = "";
        for(int i = 1; i < cmdArr.length; i++) {
            message += cmdArr[i] + " ";
        }

        //Add to chat log
        this.updateChatDisplay("You to Everyone: " + message);

        // Send command to server
        this.dosWriter.writeUTF(cmdArr[0] + " " + message);
    }

    private void printCommands() {
        this.updateServerDisplay("Commands:");
        for (String cmd : Client.commands) {
            this.updateServerDisplay(cmd);
        }

        if (this.endSocket != null && !this.endSocket.isClosed()) {
            try {
                this.dosWriter.writeUTF("/?");
            } catch (IOException e) {
                this.updateServerDisplay("Error: Server connection lost.");
                this.receiver.forcedisconnect();
                this.resetVariables();
            }
        }
        return;
    }

    private void join(String[] args) {
        // Set host and port
        Client.host = args[1];
        Client.port = Integer.parseInt(args[2]);

        try {
            if(this.endSocket != null && !this.endSocket.isClosed()) {
                this.updateServerDisplay("ERROR: Already connected to a server!");
                return;
            }

            // Create new socket
            this.endSocket = new Socket(Client.host, Client.port);

            // Create new DataOutputStream and DataInputStream
            this.dosWriter = new DataOutputStream(this.endSocket.getOutputStream());
            this.disReader = new DataInputStream(this.endSocket.getInputStream());

            this.view.setJoinInfo(Client.host, String.valueOf(Client.port));
            this.receiver.connect(disReader, dosWriter, endSocket);
            this.updateServerDisplay("Connection to the File Exchange Server is successful!");
            new Thread(this.receiver).start();

        } catch (IOException e) {
            this.updateServerDisplay("Error: Server connection lost.");
            this.receiver.forcedisconnect();
            this.resetVariables();
        }

        return;
    }

    private void disconnect() {
        // Check if there is an existing connection
        if (this.endSocket == null || this.endSocket.isClosed()) {
            this.updateServerDisplay("Error: Disconnection failed. Please connect to the server first.");
            return;
        }

        try {
            this.dosWriter.writeUTF("/leave");

            this.receiver.stop();
            this.receiver.forcedisconnect();
            this.resetVariables();

            this.view.setJoinInfo("Disconnected", "Disconnected");

        } catch (Exception e) {
            // Do nothing; There is no point in doing anything here
        }

        this.updateServerDisplay("Connection closed. Thank you!");
        return;
    }
    
    private void resetVariables() {
        this.endSocket = null;
        this.dosWriter = null;
        this.disReader = null;
    }
}
