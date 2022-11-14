import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ChatServer{
    private final List<PrintWriter> clientWriters = new ArrayList<>();

    public static void main(String[] args) {
        new ChatServer().go();
    }

    private void go(){
        ExecutorService threadPool = Executors.newCachedThreadPool();
        try{
            ServerSocketChannel server = ServerSocketChannel.open();
            server.bind(new InetSocketAddress(5000));
            System.out.println("Server started");
            while(server.isOpen()){
                SocketChannel client = server.accept();
                PrintWriter writer = new PrintWriter(Channels.newWriter(client, StandardCharsets.UTF_8));
                clientWriters.add(writer);
                threadPool.execute(new ClientHandler(client));
            }
        } catch(IOException ex){
            System.out.println("Couldn't start the server");
        }
    }

    private void sendToEveryone(String message){
        for(PrintWriter writers : clientWriters){
            writers.println(message);
            writers.flush();
        }
    }

    private class ClientHandler implements Runnable{
        private SocketChannel clientSocket;
        public ClientHandler(SocketChannel sc){
            this.clientSocket = sc;
        }

        @Override
        public void run(){
            String s;
            SocketAddress address = null;
            try{
                address = clientSocket.getLocalAddress();
                BufferedReader clientReader = new BufferedReader(Channels.newReader(clientSocket, StandardCharsets.UTF_8));
                System.out.println("Client connected: " +  address);
                while((s = clientReader.readLine()) != null){
                    System.out.println(s);
                    sendToEveryone(s);
                }
                clientReader.close();
            } catch(Exception ex){
                System.out.println("Client disconnected: " + address);
            }

        }
    }
}
