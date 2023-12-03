
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class CommandThread implements Runnable {
    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        Boolean exit = false;

        while (!exit) {
            try {
                System.out.print("\nEnter command: ");
                String cmd = sc.nextLine();
                String[] cmdArr = cmd.split(" ");

                switch (cmdArr[0]) {
                    case "/join":
                        join(cmdArr);
                        break;
                    case "/leave":
                        disconnect();
                        break;
                    case "/exit":
                        disconnect();
                        exit = true;
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
                    case "/?":
                        printCommands();
                        break;
                    default:
                        System.out.println("Error: Unidentified command.");
                }

                System.out.println("LOGS");
                System.out.println("----------------------");
                System.out.println(Client.logs);
                System.out.println("----------------------");

                System.out.println("CHATS");
                System.out.println("----------------------");
                for(String text: Client.chats) {
                    System.out.println(text);
                }
                System.out.println("----------------------");

                System.out.println("ANNOUNCEMENTS");
                System.out.println("----------------------");
                for(String text: Client.announcements) {
                    System.out.println(text);
                }
                System.out.println("----------------------");

            } catch (Exception e) {
                if (e instanceof SocketException) {
                    System.out.println("Error: Connection to server lost.");
                    forcedisconnect();
                } else {
                    System.out.println("Error: Command parameters do not match or is not allowed.");
                }
            }
        }

        sc.close();
    }

    // Double check if there are other exceptions other than SocketException (server
    // is closed)
    private static void sendCommand(String cmd) throws IOException {
        if (Client.endSocket == null || Client.endSocket.isClosed()) {
            System.out.println("Error: Not connected to a server.");
            return;
        }

        // Send command to server
        Client.dosWriter.writeUTF(cmd);
    }

    private static void getFile(String[] cmdArr) throws IOException {
        if (Client.endSocket == null || Client.endSocket.isClosed()) {
            System.out.println("Error: Not connected to a server.");
            return;
        }

        if (cmdArr.length != 2) {
            System.out.println("Error: Command parameters do not match or is not allowed.");
            return;
        }

        // Send command to server
        Client.dosWriter.writeUTF(cmdArr[0] + " " + cmdArr[1]);
    }

    private static void sendFile(String[] cmdArr) throws IOException {
        if (Client.endSocket == null || Client.endSocket.isClosed()) {
            System.out.println("Error: Not connected to a server.");
            return;
        }

        if (cmdArr.length != 2) {
            System.out.println("Error: Command parameters do not match or is not allowed.");
            return;
        }

        // Get file name
        String fileName = cmdArr[1];

        // Get file
        File file = new File(fileName);

        // Check if file exists
        if (!file.exists() || !file.isFile()) {
            // File not found
            System.out.println("Error: File not found.");
            return;
        }

        // Send command to server
        Client.dosWriter.writeUTF(cmdArr[0] + " " + cmdArr[1]);
    }

    private static void sendMessage(String[] cmdArr) throws IOException {
        if (Client.endSocket == null || Client.endSocket.isClosed()) {
            System.out.println("Error: Not connected to a server.");
            return;
        }

        if (cmdArr.length < 3) {
            System.out.println("Error: Command parameters do not match or is not allowed.");
            return;
        }

        //Stitch the message back together
        String message = "";
        for(int i = 2; i < cmdArr.length; i++) {
            message += cmdArr[i] + " ";
        }

        //Add to chat log
        Client.chats.add("You to " + cmdArr[1] + ": " + message);

        // Send command to server
        Client.dosWriter.writeUTF(cmdArr[0] + " " + cmdArr[1] + " " + message);
    }

    private static void sendAnnouncement(String[] cmdArr) throws IOException {
        if (Client.endSocket == null || Client.endSocket.isClosed()) {
            System.out.println("Error: Not connected to a server.");
            return;
        }

        if (cmdArr.length < 2) {
            System.out.println("Error: Command parameters do not match or is not allowed.");
            return;
        }

        //Stitch the message back together
        String message = "";
        for(int i = 1; i < cmdArr.length; i++) {
            message += cmdArr[i] + " ";
        }

        //Add to chat log
        Client.chats.add("You to Everyone: " + message);

        // Send command to server
        Client.dosWriter.writeUTF(cmdArr[0] + " " + message);
    }

    private static void printCommands() {
        Client.logs.add("Commands:");
        for (String cmd : Client.commands) {
            Client.logs.add(cmd);
        }

        if (Client.endSocket != null && !Client.endSocket.isClosed()) {
            try {
                Client.dosWriter.writeUTF("/?");
            } catch (IOException e) {
                Client.logs.add("Error: Server connection lost.");
                forcedisconnect();
            }
        }
        return;
    }

    private static void join(String[] args) {
        // Set host and port
        Client.host = args[1];
        Client.port = Integer.parseInt(args[2]);

        try {
            if(Client.endSocket != null && !Client.endSocket.isClosed()) {
                synchronized (Client.logs) {
                    Client.logs.add("ERROR: Already connected to a server!");
                }
                return;
            }

            synchronized(Client.monitor) {
                // Create new socket
                Client.endSocket = new Socket(Client.host, Client.port);

                // Create new DataOutputStream and DataInputStream
                Client.dosWriter = new DataOutputStream(Client.endSocket.getOutputStream());
                Client.disReader = new DataInputStream(Client.endSocket.getInputStream());

                System.out.println("Connection to the File Exchange Server is successful!");
            }
        } catch (IOException e) {
            System.out.println("Error: Server connection lost.");
            forcedisconnect();
        }

        return;
    }

    private static void disconnect() {
        // Check if there is an existing connection
        if (Client.endSocket == null || Client.endSocket.isClosed()) {
            System.out.println("Error: Disconnection failed. Please connect to the server first.");
            return;
        }

        try {
            Client.dosWriter.writeUTF("/leave");

            synchronized (Client.monitor3) {
                // Close sockets and streams
                Client.endSocket.close();

                if (Client.disReader != null)
                    Client.disReader.close();

                if (Client.dosWriter != null)
                    Client.dosWriter.close();
            }

            synchronized(Client.monitor2) {
                Client.receiver.stop();
            }

        } catch (Exception e) {
            // Do nothing; There is no point in doing anything here
        }
        // Reset variables
        resetVariables();

        System.out.println("Connection closed. Thank you!");
        return;
    }

    private static void forcedisconnect() {
        // Check if there is an existing connection

        try {
            Client.dosWriter.writeUTF("/leave");

            // Close sockets and streams
            if (Client.endSocket != null && !Client.endSocket.isClosed()) {
                System.out.println("Disconnected from server.");
                Client.endSocket.close();
            }

            if (Client.disReader != null)
                Client.disReader.close();

            if (Client.dosWriter != null)
                Client.dosWriter.close();

            Client.receiver.stop();

        } catch (Exception e) {
            // Do nothing; There is no point in doing anything here
        }
        // Reset variables
        resetVariables();
        return;
    }

    private static void resetVariables() {
        Client.host = null;
        Client.port = -1;
        Client.endSocket = null;
        Client.dosWriter = null;
        Client.disReader = null;
        return;
    }
}
