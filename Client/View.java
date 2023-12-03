import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class View extends JFrame {
    //Top panel and its components
    private JPanel serverInfoPanel;
    private JLabel serverAddress;
    private JLabel serverPort;
    private JLabel username;

    //Middle panel and its components
    private JPanel mainDisplayPanel;
    private JPanel messagesPanel;
    private JPanel chatlogPanel;
    private JPanel serverlogPanel;
    private JPanel announcementlogPanel;
    private JScrollPane chatlogScrollable;
    private JScrollPane serverlogScrollable;
    private JScrollPane announcementlogScrollable;
    private ArrayList<JLabel> chatMessages;
    private ArrayList<JLabel> announcementMessages;
    private ArrayList<JLabel> logMessages;

    //Bottom panel
    private JPanel bottomPanel;
    private JPanel buttonPanel;
    private JTextField commandline;
    private JButton sendButton;

    public View() {
        //Setting up the window
        this.setTitle("CSNETWK MP");
        this.setLayout(new BorderLayout());
        this.setResizable(false);
        this.setSize(720, 720);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Initialize the view
        this.init();
        
        //Make the window visible
        this.setVisible(true);
    }
    
    private void init() {
        //Initialize the server information panel
        this.serverInfoPanel = new JPanel();
        this.serverInfoPanel.setLayout(new GridLayout(1, 3));

        //Initialize the info on the top panel
        this.serverAddress = new JLabel("Server Address: Disconnected");
        this.serverPort = new JLabel("Port Number: Disconnected");
        this.username = new JLabel("User: Not Registered");

        //Add the data to the panels
        this.serverInfoPanel.add(this.serverAddress);
        this.serverInfoPanel.add(this.serverPort);
        this.serverInfoPanel.add(this.username);

        //Add panel to frame
        this.add(this.serverInfoPanel, BorderLayout.NORTH);

        //Initialize the middle panels
        this.mainDisplayPanel = new JPanel();
        this.messagesPanel = new JPanel();
        this.chatlogPanel = new JPanel();
        this.serverlogPanel = new JPanel();
        this.announcementlogPanel = new JPanel();

        //Initialize log handlers
        this.chatMessages = new ArrayList<JLabel>();
        this.logMessages = new ArrayList<JLabel>();
        this.announcementMessages = new ArrayList<JLabel>();

        //Initialize the scrollable panes
        this.chatlogScrollable = new JScrollPane(this.chatlogPanel);
        this.serverlogScrollable = new JScrollPane(this.serverlogPanel);
        this.announcementlogScrollable = new JScrollPane(this.announcementlogPanel);
        
        //Set layout of messages panel
        this.messagesPanel.setLayout(new GridLayout(2, 1));

        //Set layout of the scrollables
        this.chatlogPanel.setLayout(new BoxLayout(this.chatlogPanel, BoxLayout.Y_AXIS));
        this.serverlogPanel.setLayout(new BoxLayout(this.serverlogPanel, BoxLayout.Y_AXIS));
        this.announcementlogPanel.setLayout(new BoxLayout(this.announcementlogPanel, BoxLayout.Y_AXIS));

        //Add to the messages panel
        this.messagesPanel.add(this.announcementlogScrollable);
        this.messagesPanel.add(this.chatlogScrollable);

        //Set the layout of the main display panel
        this.mainDisplayPanel.setLayout(new GridLayout(1, 2));

        //Add the scrollables to the main panel
        this.mainDisplayPanel.add(this.chatlogScrollable);
        this.mainDisplayPanel.add(this.serverlogScrollable);

        //Add the main panel to the frame
        this.add(this.mainDisplayPanel, BorderLayout.CENTER);

        //Initialize the bottom panel
        this.bottomPanel = new JPanel();
        this.buttonPanel = new JPanel();
        this.commandline = new JTextField();
        this.sendButton = new JButton("Send");

        //Set layout of bottomPanel
        this.bottomPanel.setLayout(new GridLayout(1, 2));

        //Add the elements to the bottom panel
        this.buttonPanel.add(this.sendButton);
        this.bottomPanel.add(this.commandline);
        this.bottomPanel.add(this.buttonPanel);

        //Add the bottom panel to the frame
        this.add(this.bottomPanel, BorderLayout.SOUTH);
    }

    public void addChatLog(String message) {
        this.chatMessages.add(new JLabel(message));
        this.chatlogPanel.add(this.chatMessages.get(this.chatMessages.size() - 1));
    }

    public void addAnnouncementLog(String message) {
        this.announcementMessages.add(new JLabel(message));
        this.announcementlogPanel.add(this.announcementMessages.get(this.announcementMessages.size() - 1));
    }

    public void addServerLog(String message) {
        this.logMessages.add(new JLabel(message));
        this.serverlogPanel.add(this.logMessages.get(this.logMessages.size() - 1));
    }

    public String getInput() {
        return this.commandline.getText();
    }

    public void setJoinInfo(String address, String port) {
        this.serverAddress.setText("Server Address: " + address);
        this.serverPort.setText("Port Number: " + port);
    }

    public void setUsername(String username) {
        this.username.setText("User: " + username);
    }

    public void setActionListeners(ActionListener listener) {
        this.sendButton.addActionListener(listener);
    }

    public void repaintAnnouncementPanel() {
        this.announcementlogPanel.revalidate();
        this.announcementlogPanel.repaint();
    }

    public void repaintChatPanel() {
        this.chatlogPanel.revalidate();
        this.chatlogPanel.repaint();
    }

    public void repaintServerPanel() {
        this.serverlogPanel.revalidate();
        this.serverlogPanel.repaint();
    }
}
