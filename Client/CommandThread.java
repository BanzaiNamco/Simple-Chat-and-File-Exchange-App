package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
                    case "/?":
                        printCommands();
                        break;
                    default:
                        System.out.println("Error: Unidentified command.");
                }

            } catch (Exception e) {
                if (e instanceof SocketException) {
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

    // Double check if there are other exceptions other than SocketException (server
    // is closed)
    private static void sendCommand(String cmd) throws IOException {
        if (Client.endSocket == null || Client.endSocket.isClosed()) {
            System.out.println("Error: Not connected to a server.");
            return;
        }

        // Send command to server
        Client.dosWriter.writeUTF(cmd);

        // Read server response
        //System.out.println(Client.disReader.readUTF());

        return;
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

        // Read server response
        String response = Client.disReader.readUTF();
        System.out.println(response);
        if (response.equals("Error: File not found in server.")) {
            return;
        }

        String fileName = cmdArr[1];

        // Create new file
        File file = new File(fileName);

        String originalFileName = fileName;
        // If file already exists, append (number) to the end of the file name, where
        // number is the number of files with the same name
        int i = 0;
        while (file.exists()) {
            i++;
            String[] fileNameArr = fileName.split("\\.");
            String newFileName = fileNameArr[0] + "(" + i + ")." + fileNameArr[1];
            file = new File(newFileName);
        }

        FileOutputStream fos = new FileOutputStream(file);

        // Read file size
        long fileSize = Client.disReader.readLong();

        // Read file
        byte[] buffer = new byte[4096];
        int read = 0;
        long remaining = fileSize;
        while ((read = Client.disReader.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
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

        // Read server response
        String response = Client.disReader.readUTF();
        System.out.println(response);
        if (response.equals("Error: File with same name already exists.")) {
            return;
        }

        // Send file to server
        Client.dosWriter.writeLong(file.length());

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int read = 0;
        while ((read = fis.read(buffer)) > 0) {
            Client.dosWriter.write(buffer, 0, read);
        }
        fis.close();

        // Print success message
        System.out.println(Client.disReader.readUTF());

        return;
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

        // Send command to server
        Client.dosWriter.writeUTF(cmdArr[0] + " " + cmdArr[1] + " " + message);

        // Read server response
        String response = Client.disReader.readUTF();
        System.out.println(response);

        return;
    }

    private static void printCommands() {
        System.out.println("Commands:");
        for (String cmd : Client.commands) {
            System.out.println(cmd);
        }

        if (Client.endSocket != null && !Client.endSocket.isClosed()) {
            try {
                Client.dosWriter.writeUTF("/?");
                System.out.println(Client.disReader.readUTF());
            } catch (IOException e) {
                System.out.println("Error: Server connection lost.");
                forcedisconnect();
            }
        }
        return;
    }

    private static void join(String[] args) {
        // Check if there is an existing connection
        if (Client.endSocket != null && !Client.endSocket.isClosed()) {
            // Check if connection is still alive
            if (pingServer())
                System.out.println("Error: Already connected to a server.");
            return;
        }

        // Check if command parameters are correct
        if (args.length != 3) {
            System.out.println("Error: Command parameters do not match or is not allowed.");
            return;
        }

        // Actual connection to server happens here
        try {
            // Get host and port
            Client.host = args[1];
            Client.port = Integer.parseInt(args[2]);

            // Create new socket
            Client.endSocket = new Socket(Client.host, Client.port);

            // Create new DataOutputStream and DataInputStream
            Client.dosWriter = new DataOutputStream(Client.endSocket.getOutputStream());
            Client.disReader = new DataInputStream(Client.endSocket.getInputStream());

            System.out.println("Connection to the File Exchange Server is successful!");
        } catch (IOException e) {
            System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
            // Call disconnect to close sockets and streams then reset them back to null
            forcedisconnect();
        }

        return;
    }

    private static Boolean pingServer() {
        // Check if there is an existing connection
        if (Client.endSocket == null || Client.endSocket.isClosed()) {
            return false;
        }
        try {
            Client.dosWriter.writeUTF("/ping");
            String response = Client.disReader.readUTF();
            if (response.equals("/pong")) {
                return true;
            }
            return false;
        } catch (IOException e) {
            System.out.println("Error: Server connection lost.");
            forcedisconnect();
            return false;
        }
    }

    private static void disconnect() {
        // Check if there is an existing connection
        if (Client.endSocket == null || Client.endSocket.isClosed()) {
            System.out.println("Error: Disconnection failed. Please connect to the server first.");
            return;
        }

        try {
            Client.dosWriter.writeUTF("/leave");

            // Close sockets and streams
            Client.endSocket.close();

            if (Client.disReader != null)
                Client.disReader.close();

            if (Client.dosWriter != null)
                Client.dosWriter.close();

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
