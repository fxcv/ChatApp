import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ChatClient{
    private String nickname;
    private JFrame frame;
    private JPanel loginPanel;
    private JPanel chatPanel;
    private JLabel label;
    private JTextField textField;
    private JTextField chatField;
    private PrintWriter serverWriter;
    private JTextArea chat;

    public static void main(String[] args) {
        new ChatClient().startClient();
    }

    private void startClient(){
        setUpGui();
    }

    private void setUpGui(){
        frame = new JFrame("ChatClient");
        loginPanel = new JPanel();
        textField = new JTextField();
        JButton button = new JButton("Join chat");
        label = new JLabel("Enter your nickname");

        textField.setPreferredSize(new Dimension(200, 20));
        textField.requestFocus();
        textField.setAlignmentX(Component.CENTER_ALIGNMENT);
        textField.setAlignmentY(Component.CENTER_ALIGNMENT);
        button.addActionListener(new loginActionHandler());
        loginPanel.add(textField);
        loginPanel.add(button);
        loginPanel.add(label);
        loginPanel.setBackground(Color.pink);
        frame.getContentPane().add(BorderLayout.CENTER, loginPanel);
        frame.setSize(300, 100);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private void changeScene(){
        chatPanel = new JPanel();
        chatField = new JTextField();
        JButton sendMessage = new JButton("Send");

        chatField.setPreferredSize(new Dimension(200, 20));
        chatField.requestFocus();
        sendMessage.addActionListener(new chatActionHandler());
        chatPanel.setBackground(Color.pink);
        chatPanel.add(getChat());
        chatPanel.add(chatField);
        chatPanel.add(sendMessage);

        frame.setVisible(false);
        frame.remove(loginPanel);
        frame.setSize(300, 300);
        frame.getContentPane().add(BorderLayout.CENTER, chatPanel);
        frame.setVisible(true);
    }

    private JScrollPane getChat(){
        chat = new JTextArea();
        chat.setLineWrap(true);
        chat.setEditable(false);
        JScrollPane scrollable = new JScrollPane(chat);
        scrollable.setPreferredSize(new Dimension(240, 180));
        scrollable.setBackground(Color.LIGHT_GRAY);
        scrollable.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollable.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        return scrollable;
    }

    private void connectToServer(){
        ExecutorService messageHandler = Executors.newSingleThreadExecutor();
        try{
            SocketChannel connection = SocketChannel.open(new InetSocketAddress("127.0.0.1", 5000));
            serverWriter = new PrintWriter(Channels.newWriter(connection, StandardCharsets.UTF_8));
            messageHandler.execute(new ServerHandler(connection));
        } catch(IOException ex){
            System.out.println("Couldn't connect to the server");
        }
    }

    private class loginActionHandler implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            nickname = textField.getText();
            if(nickname.length()>4){
                connectToServer();
                changeScene();
            }
            else{
                label.setText("Too short nickname!");
            }
        }
    }

    private class chatActionHandler implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            String message = chatField.getText();
            serverWriter.println(nickname + ": " + message);
            serverWriter.flush();
            chatField.setText("");
            chatField.requestFocus();
        }
    }

    private class ServerHandler implements Runnable {
        private SocketChannel serverConnection;

        public ServerHandler(SocketChannel sc) {
            this.serverConnection = sc;
        }

        @Override
        public void run() {
            String s;
            try {
                BufferedReader clientReader = new BufferedReader(Channels.newReader(serverConnection, StandardCharsets.UTF_8));
                while ((s = clientReader.readLine()) != null) {
                    System.out.println(s);
                    chat.append(s + "\n");
                }
                clientReader.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }
}
