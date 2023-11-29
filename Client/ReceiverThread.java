package Client;

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
        String[] splitMessage = message.split("|", 2);
        Type messageType;
        String messageString;

        //Get the message type
        try {
            messageType = Type.valueOf(splitMessage[0]);
            messageString = splitMessage[1];
        } catch(Exception e) {
            messageType = Type.ERROR;
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
        }
    }

    private void handleResponseSuccess(String messageString) {
    }

    private void handleResponseError(String messageString) {
    }
}
