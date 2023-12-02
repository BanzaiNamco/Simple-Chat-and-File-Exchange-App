import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ClientHandler extends Thread {
    private Socket client;
    public String handle;

    private DataOutputStream dosWriter;
    private DataInputStream disReader;

    private static String[] commands = { "/register <handle>", "/leave", "/dir", "/get <filename>", "/store <filename>", "/userlist", "/msg <handle> <message>", "/all <message>", "/?" };

    public ClientHandler(Socket client) {
        this.client = client;
        try {
            this.dosWriter = new DataOutputStream(client.getOutputStream());
            this.disReader = new DataInputStream(client.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Boolean exit = false;
        while (!exit) {
            try {
                // Get user input
                String cmd = disReader.readUTF();
                String[] cmdArr = cmd.split(" ");

                switch (cmdArr[0]) {
                    case "/ping":
                        dosWriter.writeUTF("PING | pong");
                        break;
                    case "/leave":
                        exit = true;
                        break;
                    case "/register":
                        register(cmdArr);
                        break;
                    case "/dir":
                        if (isRegistered())
                            getDir(cmdArr);
                        break;
                    case "/get":
                        if (isRegistered())
                            getFile(cmdArr);
                        break;
                    case "/store":
                        if (isRegistered())
                            acceptFile(cmdArr);
                        break;
                    case "/userlist":
                        if(isRegistered()) {
                            sendUserDirectory();
                        }
                        break;
                    case "/msg":
                        sendMessage(cmdArr);
                        break;
                    case "/all":
                        sendAnnouncement(cmdArr);
                        break;
                    case "/?":
                        getCommandList();
                        break;
                    default:
                        dosWriter.writeUTF("ERROR | Error: Command not found.");
                        break;
                }
            } catch (Exception e) {
                if (e instanceof SocketException) {
                    exit = true;
                } else {
                    e.printStackTrace();
                }
                // e.printStackTrace();
            }
        }
        System.out.println("Client disconnected");
        terminate();
        System.out.println(Server.clients.size() + " clients connected");

    }

    private void sendMessage(String[] cmdArr) throws IOException {
        if (cmdArr.length < 3) {
            dosWriter.writeUTF("ERROR | Error: Command parameters do not match or is not allowed.");
            return;
        }
        
        //Stitch the message back together
        String message = "";
        for(int i = 2; i < cmdArr.length; i++) {
            message += cmdArr[i] + " ";
        }

        //Check if the recipient is active
        if(!isActive(cmdArr[1])) {
            dosWriter.writeUTF("ERROR | Error: User is inactive.");
            return;
        }

        //Check if the recipient is self
        if(cmdArr[1].equals(this.handle)) {
            dosWriter.writeUTF("ERROR | Error: Can't send message to self.");
            return;
        }

        //Write message to intended recipient
        DataOutputStream recipientWriter = Server.userDirectory.get(cmdArr[1]).getOutputStream(); 
        recipientWriter.writeUTF("WHISPER | (Whisper) " + handle + ": " + message);
    }

    private void sendAnnouncement(String[] cmdArr) throws IOException {
        if (cmdArr.length < 2) {
            dosWriter.writeUTF("ERROR | Error: Command parameters do not match or is not allowed.");
            return;
        }
        
        //Stitch the message back together
        String message = "";
        for(int i = 1; i < cmdArr.length; i++) {
            message += cmdArr[i] + " ";
        }

        //Write message to all users
        synchronized (Server.userDirectory) {
            for(String handles: Server.userDirectory.keySet()) {
                if(!handles.equals(this.handle)) {
                    Server.userDirectory.get(handles).getOutputStream().writeUTF("ANNOUNCEMENT | <Announcement>" + handle + ": " + message); 
                }
            }
        }
    }

    private boolean isActive(String handle) {
        for(String userName: Server.userDirectory.keySet()) {
            if(handle.equals(userName)) {
                return true;
            }
        }

        return false;
    }

    private void register(String[] args) throws IOException {
        if (args.length != 2) {
            dosWriter.writeUTF("ERROR | Error: Command parameters do not match or is not allowed.");
            return;
        }

        if (handle != null) {
            dosWriter.writeUTF("ERROR | Error: Already registered.");
            return;
        }

        // Check if handle is already taken
        synchronized (Server.userDirectory) {
            for (String handle : Server.userDirectory.keySet()) {
                if (handle.equals(args[1])) {
                    dosWriter.writeUTF("ERROR | Error: Registration failed. Handle or alias already exists.");
                    return;
                }
            }
        }

        // Register client
        handle = args[1];
        synchronized (Server.userDirectory) {
            Server.userDirectory.put(handle, this);
        }
        
        dosWriter.writeUTF("SUCCESS | Server: Welcome " + handle + "!");
    }

    private void getDir(String[] args) throws IOException {

        if (args.length != 1) {
            dosWriter.writeUTF("ERROR | Error: Command parameters do not match or is not allowed.");
            return;
        }

        // Get list of files in current directory
        File dir = new File(".");
        File[] files = dir.listFiles();

        // String Array of files
        ArrayList<String> fileNames = new ArrayList<>();

        // Send list of files to client
        String msg = "Server directory:\n";
        for (File file : files) {
            // Filter out .java and .class files
            if (file.isFile()) {
                if (!file.getName().endsWith(".java") && !file.getName().endsWith(".class")
                        && !file.getName().endsWith(".git")) {
                    fileNames.add(file.getName());
                }
            }
        }

        // Handle file listings
        if (fileNames.size() == 0) {
            msg += "No Files Found!";
        } else {
            for (String fileName : fileNames) {
                msg += fileName + "\n";
            }
        }

        dosWriter.writeUTF("SUCCESS | " + msg);

        return;
    }

    private void getFile(String[] args) throws IOException {
        // This part should never run, however it is here for safety
        if (args.length != 2) {
            return;
        }

        // Get file name
        String fileName = args[1];

        // Get file
        File file = new File(fileName);

        // Check if file exists
        if (!file.exists() || !file.isFile()) {
            // File not found
            dosWriter.writeUTF("ERROR | Error: File not found in server.");
            return;
        }

        // Send file to client
        dosWriter.writeUTF("SUCCESS | Server: Sending file " + fileName + "...");
        // Send metadata of the file
        dosWriter.writeUTF(fileName);
        dosWriter.writeLong(file.length());

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int read = 0;
        while ((read = fis.read(buffer)) > 0) {
            dosWriter.write(buffer, 0, read);
        }
        fis.close();

        return;

    }

    private void acceptFile(String[] args) throws IOException {
        // This part should never run, however it is here for safety
        if (args.length != 2) {
            return;
        }

        // Get file name
        String fileName = args[1];

        // Get server-side filename
        String serverFileName = fileName;

        // Check if file exists
        File file = new File(serverFileName);
        int counter = 1;
        while (file.exists() && file.isFile()) {
            serverFileName = fileName + "-" + String.valueOf(counter);
            file = new File(serverFileName);
            counter++;
        }

        // Download file from client
        dosWriter.writeUTF("SUCCESS | Server: Receiving file " + fileName + " as " + serverFileName + "...");
        //Resend original file name to be sent by the client
        dosWriter.writeUTF(fileName);
        long fileSize = disReader.readLong();

        // Read file
        byte[] buffer = new byte[4096];
        int read = 0;
        long remaining = fileSize;
        FileOutputStream fos = new FileOutputStream(file);
        while ((read = disReader.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
            remaining -= read;
            fos.write(buffer, 0, read);
        }

        // Close file
        fos.close();

        // Print success message
        // Get current date and time
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String success = handle + " <" + dtf.format(now) + ">: Uploaded " + fileName;
        dosWriter.writeUTF("SUCCESS | " + success);
    }

    private void getCommandList() throws IOException {
        String msg = "";
        for (String cmd : commands) {
            msg += cmd + "\n";
        }
        dosWriter.writeUTF("SUCCESS | " + msg);
    }

    private Boolean isRegistered() throws IOException {
        if (handle == null) {
            dosWriter.writeUTF("ERROR | Error: Not registered.");
            return false;
        }
        return true;
    }

    private void terminate() {
        try {
            dosWriter.close();
            disReader.close();
            client.close();

            // Remove client from list
            synchronized (Server.clients) {
                Server.clients.remove(this);
            }

            //Remove client mapping 
            synchronized (Server.userDirectory) {
                Server.userDirectory.remove(this.handle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendUserDirectory() throws IOException {
        String msg = "Active Users:\n";
        
        for(String handle: Server.userDirectory.keySet()) {
            msg += handle + "\n";
        }

        //Send response
        dosWriter.writeUTF("SUCCESS | " + msg);
    }

    public DataInputStream getInputStream() {
        return this.disReader;
    }

    public DataOutputStream getOutputStream() {
        return this.dosWriter;
    }
}