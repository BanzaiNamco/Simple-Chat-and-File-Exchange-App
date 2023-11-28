package Client;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    private static String host; // can be changed
    private static int port;
    private static DataOutputStream dosWriter;
    private static DataInputStream disReader;
    private static Socket endSocket;
    private static String[] commands = {"/join <server ip> <port>", "/exit"};

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Boolean exit = false;

        while (!exit) {
            try {
                System.out.print("\nEnter command: ");
                String cmd = sc.nextLine();
                String[] cmdArr = cmd.split(" ");


                pingServer();
                switch(cmdArr[0]){
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
                    case "/?":
                        printCommands();
                        break;
                }
            } catch (Exception e) {
                if (e instanceof SocketException){
                    System.out.println("Error: Connection to server lost.");
                    forcedisconnect();
                } else {
                    System.out.println("Error: Command parameters do not match or is not allowed.");
                }
                // e.printStackTrace();
            }
        }

        sc.close();
    }

    // Double check if there are other exceptions other than SocketException (server is closed)
    private static void sendCommand(String cmd) throws IOException{
        if (endSocket == null || endSocket.isClosed()){
            System.out.println("Error: Not connected to a server.");
            return;
        }

        // Send command to server
        dosWriter.writeUTF(cmd);
        
        // Read server response
        System.out.println(disReader.readUTF());

        return;
    }

    private static void getFile(String[] cmdArr) throws IOException{
        if (endSocket == null || endSocket.isClosed()){
            System.out.println("Error: Not connected to a server.");
            return;
        }

        if (cmdArr.length != 2){
            System.out.println("Error: Command parameters do not match or is not allowed.");
            return;
        }

        // Send command to server
        dosWriter.writeUTF(cmdArr[0] + " " + cmdArr[1]);

        // Read server response
        String response = disReader.readUTF();
        System.out.println(response);
        if (response.equals("Error: File not found in server.")){
            return;
        }


        String fileName = cmdArr[1];

        // Create new file
        File file = new File(fileName);
        
        String originalFileName = fileName;
        // If file already exists, append (number) to the end of the file name, where number is the number of files with the same name
        int i = 0;
        while(file.exists()){
            i++;
            String[] fileNameArr = fileName.split("\\.");
            String newFileName = fileNameArr[0] + "(" + i + ")." + fileNameArr[1];
            file = new File(newFileName);
        }

        FileOutputStream fos = new FileOutputStream(file);

        // Read file size
        long fileSize = disReader.readLong();

        // Read file
        byte[] buffer = new byte[4096];
        int read = 0;
        long remaining = fileSize;
        while((read = disReader.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
            remaining -= read;
            fos.write(buffer, 0, read);
        }

        // Close file
        fos.close();

        // Print success message
        String success = "File received from Server: " + originalFileName;
        if (!originalFileName.equals(fileName))
            success += " as " + fileName;
        System.out.println(success);
    }

    
    private static void sendFile(String[] cmdArr) throws IOException{
        if (endSocket == null || endSocket.isClosed()){
            System.out.println("Error: Not connected to a server.");
            return;
        }

        if (cmdArr.length != 2){
            System.out.println("Error: Command parameters do not match or is not allowed.");
            return;
        }

        // Get file name
        String fileName = cmdArr[1];

        // Get file
        File file = new File(fileName);

        // Check if file exists
        if(!file.exists() || !file.isFile()){
            // File not found
            System.out.println("Error: File not found.");
            return;
        }

        // Send command to server
        dosWriter.writeUTF(cmdArr[0] + " " + cmdArr[1]);

        // Read server response
        String response = disReader.readUTF();
        System.out.println(response);
        if (response.equals("Error: File with same name already exists.")){
            return;
        }

        // Send file to server
        dosWriter.writeLong(file.length());

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int read = 0;
        while ((read = fis.read(buffer)) > 0){
            dosWriter.write(buffer, 0, read);
        }
        fis.close();

        // Print success message
        System.out.println(disReader.readUTF());

        return;
    }

    private static void printCommands(){
        System.out.println("Commands:");
        for (String cmd : commands){
            System.out.println(cmd);
        }

        if (endSocket != null && !endSocket.isClosed()){
            try {
                dosWriter.writeUTF("/?");
                System.out.println(disReader.readUTF());
            } catch (IOException e) {
                System.out.println("Error: Server connection lost.");
                forcedisconnect();
            }
        }
        return;
    }

    private static void join(String[] args){
        // Check if there is an existing connection
        if (endSocket != null && !endSocket.isClosed()){
            // Check if connection is still alive
            if(pingServer())
                System.out.println("Error: Already connected to a server.");
            return;
        } 
        
        // Check if command parameters are correct
        if (args.length != 3){
            System.out.println("Error: Command parameters do not match or is not allowed.");
            return;
        } 
        
        // Actual connection to server happens here
        try {
            // Get host and port
            host = args[1];
            port = Integer.parseInt(args[2]);

            // Create new socket
            endSocket = new Socket(host, port);

            // Create new DataOutputStream and DataInputStream
            dosWriter = new DataOutputStream(endSocket.getOutputStream());
            disReader = new DataInputStream(endSocket.getInputStream());

            System.out.println("Connected to server " + host + ":" + port);
        } catch (IOException e) {
            System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
            // Call disconnect to close sockets and streams then reset them back to null
            forcedisconnect();
        }
        
        return;
    }

    private static Boolean pingServer(){
        // Check if there is an existing connection
        if (endSocket == null || endSocket.isClosed()){
            return false;
        }
        try {
            dosWriter.writeUTF("/ping");
            String response = disReader.readUTF();
            if (response.equals("/pong")){
                return true;
            }
            return false;
        } catch (IOException e) {
            System.out.println("Error: Server connection lost.");
            forcedisconnect();
            return false;
        }
    }

    private static void disconnect(){
        // Check if there is an existing connection
        if (endSocket == null || endSocket.isClosed()){
            System.out.println("Error: Disconnection failed. Please connect to the server first.");
            return;
        }

        try {
            dosWriter.writeUTF("/leave");

            // Close sockets and streams
            endSocket.close();

            if (disReader != null)
                disReader.close();
            
            if (dosWriter != null)
                dosWriter.close();

        } catch (Exception e) {
            // Do nothing; There is no point in doing anything here
        }
        // Reset variables
        resetVariables();

        System.out.println("Disconnected from server.");
        return;
    }

    private static void forcedisconnect(){
        // Check if there is an existing connection

        try {
            dosWriter.writeUTF("/leave");

            // Close sockets and streams
            if (endSocket != null && !endSocket.isClosed()) {
                System.out.println("Disconnected from server.");
                endSocket.close();
            }

            if (disReader != null)
                disReader.close();
            
            if (dosWriter != null)
                dosWriter.close();

        } catch (Exception e) {
            // Do nothing; There is no point in doing anything here
        }
        // Reset variables
        resetVariables();
        return;
    }

    private static void resetVariables(){
        host = null;
        port = -1;
        endSocket = null;
        dosWriter = null;
        disReader = null;
        return;
    }
}
