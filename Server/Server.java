import java.net.*;
import java.util.*;

import java.io.*;

public class Server {
    private static final int PORT = 12345;
    protected static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<ClientHandler>());
    private static ServerSocket server;
    protected static Map<String, ClientHandler> userDirectory = Collections.synchronizedMap(new HashMap<String, ClientHandler>());

    public static void main(String[] args) {
        try {
            // Create new ServerSocket on the specified port
            server = new ServerSocket(PORT);

            System.out.println("Server started on port " + PORT);

            // Forever loop
            while (true) {
                System.out.println(clients.size() + " clients connected");
                
                // Accept new client
                Socket client = server.accept();

                System.out.println("Client connected");

                // Create new ClientHandler for client; this is a thread
                ClientHandler handler = new ClientHandler(client);
                clients.add(handler);
                // Start the thread
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server shutting down");
        }
    }
}