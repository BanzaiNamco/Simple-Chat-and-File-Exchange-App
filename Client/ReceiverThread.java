
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReceiverThread implements Runnable {
    private boolean stop = false;

    @Override
    public void run() {
        while(!this.stop) {
            try {
                synchronized(Client.monitor3) {
                    if (Client.endSocket != null && !Client.endSocket.isClosed()) {
                        synchronized(Client.monitor) {
                            if(Client.disReader != null && Client.disReader.available() > 0) {
                                String message = Client.disReader.readUTF();
                                messageHandler(message);
                            }
                        }
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
                Client.announcements.add(messageString);
                break;
            case ERROR:
                handleResponseError(messageString);
                break;
            case SUCCESS:
                handleResponseSuccess(messageString);
                break;
            case WHISPER:
                Client.chats.add(messageString);
                break;
            case PING:
                handlePingResponse(messageString);
                break;
        }  
    }

    private boolean handlePingResponse(String messageString) {
        // Check if there is an existing connection
        if (Client.endSocket == null || Client.endSocket.isClosed()) {
            return false;
        }

        return messageString.equals("pong");
    }

    private void handleResponseSuccess(String messageString) {
        if(messageString == null || messageString.equals("")) {
            Client.logs.add("ERROR: The response received from the server could not be identified!");
            return;
        }

        Client.logs.add(messageString);

        if(messageString.startsWith("Server: Sending file")) {
            try {
                receiveFile();
            } catch(IOException e) {
                Client.logs.add("ERROR: Something went wrong when receiving the file.");
            }
        } else if(messageString.startsWith("Server: Receiving file")) {
            try {
                sendFile();
            } catch(IOException e) {
                Client.logs.add("ERROR: Something went wrong when sending the file.");
            }
        }
    }

    private void handleResponseError(String messageString) {
        if(messageString == null || messageString.equals("")) {
            Client.logs.add("ERROR: The response received from the server could not be identified!");
            return;
        }

        Client.logs.add(messageString);
    }

    private static void receiveFile() throws IOException {
        String fileName = Client.disReader.readUTF();

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
        if (!originalFileName.equals(fileName)) {
            success += " as " + fileName;
        }
        Client.logs.add(success);
    }

    private static void sendFile() throws IOException {
        // Get file name
        String fileName = Client.disReader.readUTF();

        // Get file
        File file = new File(fileName);

        // Send file to server
        // Send the file length
        Client.dosWriter.writeLong(file.length());

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int read = 0;
        while ((read = fis.read(buffer)) > 0) {
            Client.dosWriter.write(buffer, 0, read);
        }
        fis.close();
    }

    private static void forcedisconnect() {
        // Check if there is an existing connection
        try {
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

    public void stop() {
        this.stop = true;
    }
}
