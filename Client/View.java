import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class View extends JFrame {
    //Top panel and its components
    private JPanel serverInfoPanel;
    private JLabel serverAddress;
    private JLabel serverPort;
    private JLabel username;
    private JPanel leavePanel;
    private JButton leaveButton;

    //Left panel and its components
    private JPanel messagesPanel;
    private JPanel chatlogPanel;
    private JPanel announcementlogPanel;
    private JPanel mainChats;
    private JPanel mainAnnouncements;
    private final JLabel chatLabel = new JLabel("Chat");
    private final JLabel announcementLabel = new JLabel("Announcement");
    private JScrollPane chatlogScrollable;
    private JScrollPane announcementlogScrollable;
    private ArrayList<JLabel> chatMessages;
    private ArrayList<JLabel> announcementMessages;

    //Right panel and its components
    private JPanel rightPanel;
    private JPanel serverlogPanel;
    private JPanel mainServerLog;
    private JPanel mainCommands;
    private final JLabel serverLabel = new JLabel("Server Log");
    private final JLabel commandsLabel = new JLabel("Commands");
    private JScrollPane serverlogScrollable;
    private ArrayList<JLabel> logMessages;
    private JPanel commandsPanel;
    private JScrollPane commandsScrollable;
    private ArrayList<JLabel> commands;

    //Middle panel and its components
    private JPanel mainDisplayPanel;
    //For default
    private JPanel hostInput;
    private JPanel portInput;
    private JLabel hostLbl;
    private JLabel portLbl;
    private JTextField host;
    private JTextField port;
    private JPanel middlebuttonPanel;
    private JButton connectButton;
    //For directory
    private JPanel userDirPanel;
    private JPanel serverDirPanel;
    private String selected;
    private JPanel uploadPanel;
    private JPanel downloadPanel;
    private JButton downloadButton;
    private JButton uploadButton;

    //Bottom panel
    private JPanel bottomPanel;
    private JTextField commandline;
    private JPanel sendPanel;
    private JButton sendButton;

    public View() {
        //Setting up the window
        this.setTitle("CSNETWK MP");
        this.setLayout(new BorderLayout());
        this.setResizable(false);
        this.setSize(1080, 720);
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
        this.rightPanel = new JPanel();
        this.messagesPanel = new JPanel();
        this.chatlogPanel = new JPanel();
        this.serverlogPanel = new JPanel();
        this.announcementlogPanel = new JPanel();
        this.announcementlogPanel = new JPanel();
        this.mainChats = new JPanel();
        this.mainAnnouncements = new JPanel();
        this.mainServerLog  = new JPanel();
        this.mainCommands  = new JPanel();

        //Initialize log handlers
        this.chatMessages = new ArrayList<JLabel>();
        this.logMessages = new ArrayList<JLabel>();
        this.announcementMessages = new ArrayList<JLabel>();
 
        //Set the default commands
        this.commands = new ArrayList<JLabel>();
        this.commandsPanel = new JPanel();
        this.commands.add(new JLabel("/join <server ip>"));
        this.commands.add(new JLabel("/?"));
        this.commandsPanel.add(this.commands.get(0));
        this.commandsPanel.add(this.commands.get(1));

        //Initialize the scrollable panes
        this.chatlogScrollable = new JScrollPane(this.chatlogPanel);
        this.serverlogScrollable = new JScrollPane(this.serverlogPanel);
        this.announcementlogScrollable = new JScrollPane(this.announcementlogPanel);
        this.commandsScrollable = new JScrollPane(this.commandsPanel);
        
        //Set layout of messages panel
        this.messagesPanel.setLayout(new GridLayout(2, 1));
        //Set layout of right panel
        this.rightPanel.setLayout(new GridLayout(2, 1));

        //Set layout of the scrollables
        this.chatlogPanel.setLayout(new BoxLayout(this.chatlogPanel, BoxLayout.Y_AXIS));
        this.serverlogPanel.setLayout(new BoxLayout(this.serverlogPanel, BoxLayout.Y_AXIS));
        this.announcementlogPanel.setLayout(new BoxLayout(this.announcementlogPanel, BoxLayout.Y_AXIS));
        this.commandsPanel.setLayout(new BoxLayout(this.commandsPanel, BoxLayout.Y_AXIS));

        //Set layout of the containers
        this.mainChats.setLayout(new BoxLayout(this.mainChats, BoxLayout.Y_AXIS));
        this.mainAnnouncements.setLayout(new BoxLayout(this.mainAnnouncements, BoxLayout.Y_AXIS));
        this.mainServerLog.setLayout(new BoxLayout(this.mainServerLog, BoxLayout.Y_AXIS));
        this.mainCommands.setLayout(new BoxLayout(this.mainCommands, BoxLayout.Y_AXIS));

        //Add elements to the mains
        this.mainChats.add(this.chatLabel);
        this.mainAnnouncements.add(this.announcementLabel);
        this.mainServerLog.add(this.commandsLabel);
        this.mainCommands.add(this.serverLabel);

        //Add scrollable panels to the mains
        this.mainChats.add(this.chatlogScrollable);
        this.mainAnnouncements.add(this.announcementlogScrollable);
        this.mainServerLog.add(this.serverlogScrollable);
        this.mainCommands.add(this.commandsScrollable);

        //Add to the left panel
        this.messagesPanel.add(this.mainChats);
        this.messagesPanel.add(this.mainAnnouncements);
        
        //Add to the right panel
        this.rightPanel.add(this.mainServerLog);
        this.rightPanel.add(this.mainCommands);

        //Add messages panel to the left of the middle part
        this.add(this.messagesPanel, BorderLayout.WEST);
        //Add right panel to right of the middle part
        this.add(this.rightPanel, BorderLayout.EAST);

        //Set the default layout of the main display panel
        this.setDefaultMid();

        //Initialize the bottom panel
        this.bottomPanel = new JPanel();
        this.sendPanel = new JPanel();
        this.commandline = new JTextField(100);
        this.sendButton = new JButton("Send");

        //Set layout of bottomPanel
        this.bottomPanel.setLayout(new GridLayout(1, 2));

        //Add the elements to the bottom panel
        this.sendPanel.add(this.sendButton);
        this.bottomPanel.add(this.commandline);
        this.bottomPanel.add(this.sendPanel);

        //Add the bottom panel to the frame
        this.add(this.bottomPanel, BorderLayout.SOUTH);
    }

    public void setDefaultMid() {
        //Init main display
        this.mainDisplayPanel = new JPanel();

        //Init default ui elements
        this.hostInput = new JPanel();
        this.portInput = new JPanel();
        this.hostLbl = new JLabel("Host");
        this.portLbl = new JLabel("Port");
        this.host = new JTextField(50);
        this.port = new JTextField(50);
        this.middlebuttonPanel = new JPanel();
        this.connectButton = new JButton("Connect");

        //Add elements
        this.hostInput.add(this.hostLbl);
        this.hostInput.add(this.host);
        this.portInput.add(this.portLbl);
        this.portInput.add(this.port);
        this.middlebuttonPanel.add(this.connectButton);

        //Add to centering panel
        this.mainDisplayPanel.setLayout(new BoxLayout(this.mainDisplayPanel, BoxLayout.Y_AXIS));
        this.mainDisplayPanel.add(this.hostInput);
        this.mainDisplayPanel.add(this.portInput);
        this.mainDisplayPanel.add(this.middlebuttonPanel);
        this.add(this.mainDisplayPanel, BorderLayout.CENTER);

        this.revalidate();
        this.repaint();
    }

    public void setFileMid() {

    }

    public void addChatLog(String message) {
        this.chatMessages.add(new JLabel(message));
        this.chatlogPanel.add(this.chatMessages.get(this.chatMessages.size() - 1));
        this.repaintChatPanel();
    }

    public void addAnnouncementLog(String message) {
        this.announcementMessages.add(new JLabel(message));
        this.announcementlogPanel.add(this.announcementMessages.get(this.announcementMessages.size() - 1));
        this.repaintAnnouncementPanel();
    }

    public void addServerLog(String message) {
        this.logMessages.add(new JLabel(message));
        this.serverlogPanel.add(this.logMessages.get(this.logMessages.size() - 1));
        this.repaintServerPanel();
    }

    public void addCommand(String message) {
        this.commands.add(new JLabel(message));
        this.commandsPanel.add(this.commands.get(this.commands.size() - 1));
        this.repaintCommandPanel();
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

    private void repaintAnnouncementPanel() {
        this.announcementlogPanel.revalidate();
        this.announcementlogPanel.repaint();
        this.revalidate();
        this.repaint();
    }

    private void repaintChatPanel() {
        this.chatlogPanel.revalidate();
        this.chatlogPanel.repaint();
        this.revalidate();
        this.repaint();
    }

    private void repaintServerPanel() {
        this.serverlogPanel.revalidate();
        this.serverlogPanel.repaint();
        this.revalidate();
        this.repaint();
    }

    private void repaintCommandPanel() {
        this.commandsPanel.revalidate();
        this.commandsPanel.repaint();
        this.revalidate();
        this.repaint();
    }
}
