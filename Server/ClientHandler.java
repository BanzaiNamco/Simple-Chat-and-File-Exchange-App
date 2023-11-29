import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ClientHandler extends Thread {
    private Socket client;
    private String handle;

    private DataOutputStream dosWriter;
    private DataInputStream disReader;

    private static String[] commands = { "/register <handle>", "/leave", "/dir", "/get <filename>",
            "/store <filename>" };

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
                        dosWriter.writeUTF("/pong");
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
                    case "/?":
                        getCommandList();
                        break;
                    default:
                        dosWriter.writeUTF("Error: Command not found.");
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

    private void register(String[] args) throws IOException {
        if (args.length != 2) {
            dosWriter.writeUTF("Error: Command parameters do not match or is not allowed.");
            return;
        }

        if (handle != null) {
            dosWriter.writeUTF("Error: Already registered.");
            return;
        }

        // Check if handle is already taken
        synchronized (Server.clients) {
            for (ClientHandler client : Server.clients) {
                if (client.handle != null && client.handle.equals(args[1])) {
                    dosWriter.writeUTF("Error: Registration failed. Handle or alias already exists.");
                    return;
                }
            }
        }

        // Register client
        handle = args[1];
        dosWriter.writeUTF("Server: Welcome " + handle + "!");
    }

    private void getDir(String[] args) throws IOException {

        if (args.length != 1) {
            dosWriter.writeUTF("Error: Command parameters do not match or is not allowed.");
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

        dosWriter.writeUTF(msg);

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
            dosWriter.writeUTF("Error: File not found in server.");
            return;
        }

        // Send file to client
        dosWriter.writeUTF("Server: Sending file " + fileName + "...");
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
        dosWriter.writeUTF("Server: Receiving file " + serverFileName + "...");
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
        System.out.println(success);
        dosWriter.writeUTF(success);
    }

    private void getCommandList() throws IOException {
        String msg = "";
        for (String cmd : commands) {
            msg += cmd + "\n";
        }
        dosWriter.writeUTF(msg);
    }

    private Boolean isRegistered() throws IOException {
        if (handle == null) {
            dosWriter.writeUTF("Error: Not registered.");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}