
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ReceiverThread implements Runnable {
    private boolean stop = false;
    private View view;
    public DataOutputStream dosWriter;
    public DataInputStream disReader;
    public Socket endSocket;

    public ReceiverThread(View view) {
        this.view = view;
        this.dosWriter = null; 
        this.disReader = null;
        this.endSocket = null;
    }

    @Override
    public void run() {
        while(!this.stop) {
            try {
                if(this.endSocket != null && !this.endSocket.isClosed()) {
                    if(this.disReader != null && this.disReader.available() > 0) {
                        String message = this.disReader.readUTF();
                        messageHandler(message);
                    }
                }
                
            } catch(Exception e) {
                forcedisconnect();
                e.printStackTrace();
            }
        }
    }

    private void messageHandler(String message) {
        String[] splitMessage = message.split("\\ \\|\\ ", 2);
        MessageType messageType;
        String messageString;

        //Get the message type
        try {
            messageType = MessageType.valueOf(splitMessage[0]);
            messageString = splitMessage[1];
        } catch(Exception e) {
            messageType = MessageType.ERROR;
            messageString = "Error: Invalid message header";
        }
        
        switch(messageType) {
            case ANNOUNCEMENT:
                this.view.addAnnouncementLog(messageString);
                break;
            case ERROR:
                handleResponseError(messageString);
                break;
            case SUCCESS:
                handleResponseSuccess(messageString);
                break;
            case WHISPER:
                this.view.addChatLog(messageString);
                break;
            case PING:
                handlePingResponse(messageString);
                break;
        }  
    }

    private boolean handlePingResponse(String messageString) {
        // Check if there is an existing connection
        if (this.endSocket == null || this.endSocket.isClosed()) {
            return false;
        }

        return messageString.equals("pong");
    }

    private void handleResponseSuccess(String messageString) {
        if(messageString == null || messageString.equals("")) {
            this.view.addServerLog("ERROR: The response received from the server could not be identified!");
            return;
        }

        this.view.addServerLog(messageString);

        if(messageString.startsWith("Server: Sending file")) {
            try {
                receiveFile();
            } catch(IOException e) {
                this.view.addServerLog("ERROR: Something went wrong when receiving the file.");
            }
        } else if(messageString.startsWith("Server: Receiving file")) {
            try {
                sendFile();
            } catch(IOException e) {
                this.view.addServerLog("ERROR: Something went wrong when sending the file.");
            }
        } else if(messageString.startsWith("Server: Welcome")) {
            try {
                this.view.setUsername(this.disReader.readUTF());
            } catch(IOException e) {
                this.view.addServerLog("ERROR: Something went wrong when registering");
            }
        }
    }

    private void handleResponseError(String messageString) {
        if(messageString == null || messageString.equals("")) {
            this.view.addServerLog("ERROR: The response received from the server could not be identified!");
            return;
        }

        this.view.addServerLog(messageString);
    }

    private void receiveFile() throws IOException {
        String fileName = this.disReader.readUTF();

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
        long fileSize = this.disReader.readLong();

        // Read file
        byte[] buffer = new byte[4096];
        int read = 0;
        long remaining = fileSize;
        while ((read = this.disReader.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
            remaining -= read;
            fos.write(buffer, 0, read);
        }

        // Close file
        fos.close();

        // Print success message
        String success = "File received from Server: " + originalFileName;
        if (!originalFileName.equals(fileName)) {
            success += " as " + fileName;
        }
        this.view.addServerLog(success);
    }

    private void sendFile() throws IOException {
        // Get file name
        String fileName = this.disReader.readUTF();

        // Get file
        File file = new File(fileName);

        // Send file to server
        // Send the file length
        this.dosWriter.writeLong(file.length());

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int read = 0;
        while ((read = fis.read(buffer)) > 0) {
            this.dosWriter.write(buffer, 0, read);
        }
        fis.close();
    }

    public void forcedisconnect() {
        this.stop = true;

        // Check if there is an existing connection
        try {
            // Close sockets and streams
            if (this.endSocket != null && !this.endSocket.isClosed()) {
                System.out.println("Disconnected from server.");
                this.endSocket.close();
            }

            if (this.disReader != null)
                this.disReader.close();

            if (this.dosWriter != null)
                this.dosWriter.close();

        } catch (Exception e) {
            // Do nothing; There is no point in doing anything here
        }
        // Reset variables
        resetVariables();
        return;
    }

    private void resetVariables() {
        Client.host = null;
        Client.port = -1;
        this.endSocket = null;
        this.dosWriter = null;
        this.disReader = null;
        return;
    }

    public void stop() {
        this.stop = true;
    }

    public void connect(DataInputStream disReader, DataOutputStream dosWriter, Socket endSocket) {
        this.dosWriter = dosWriter; 
        this.disReader = disReader;
        this.endSocket = endSocket;
        this.stop = false;
    }
}
