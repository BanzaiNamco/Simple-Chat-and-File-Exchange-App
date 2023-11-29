package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ReceiverThread implements Runnable {
    @Override
    public void run() {
        while(true) {
            try {
                if(Client.disReader != null) {
                    String message = Client.disReader.readUTF();
                    messageHandler(message);
                }
                
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void messageHandler(String message) {
        String[] splitMessage = message.split(" | ", 3);
        MessageType messageType;
        ActionCode actionCode;
        String messageString;

        //Get the message type
        try {
            messageType = MessageType.valueOf(splitMessage[0]);
            actionCode = ActionCode.valueOf(splitMessage[1]);
            messageString = splitMessage[2];
        } catch(Exception e) {
            messageType = MessageType.ERROR;
            actionCode = ActionCode.INVALID;
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
                handleResponseSuccess(actionCode, messageString);
                break;
            case WHISPER:
                Client.chats.add(messageString);
                break;
            case PING:
                handlePingResponse(messageString);
                break;
        }
    }

    private void handlePingResponse(String messageString) {
        if(messageString.equals("Available")) {
            try {
                Client.dosWriter.writeUTF("Proceed");
            } catch (IOException e) {
                System.out.println("Error: Server connection lost.");
                forcedisconnect();
            }
        }
    }

    private void handleResponseSuccess(ActionCode action, String messageString) {
        switch (action) {
            
        }
    }

    private void handleResponseError(String messageString) {
    }

    private static Boolean pingServer(String message) {
        // Check if there is an existing connection
        if (Client.endSocket == null || Client.endSocket.isClosed()) {
            return false;
        }

        return message.equals("pong");
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
}
