package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private String host;
    private int port;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ExecutorService executorService;
    private Map<SocketChannel, StringBuilder> clientLogs;
    private StringBuilder serverLog;

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
        this.clientLogs = new HashMap<>();
        this.serverLog = new StringBuilder();
    }

    public void startServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(host, port));
            serverSocketChannel.configureBlocking(false);
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(this::runServer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runServer() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        acceptClient();
                    } else if (key.isReadable()) {
                        handleClientRequest((SocketChannel) key.channel());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }

    private void acceptClient() throws IOException {
        SocketChannel client = serverSocketChannel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        clientLogs.put(client, new StringBuilder());
        //serverLog.append(client.getRemoteAddress().toString() + " logged in at " + Time.now() + "\n");
    }

    private void handleClientRequest(SocketChannel client) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        StringBuilder message = new StringBuilder();

        while (true) {
            buffer.clear();
            int bytesRead = client.read(buffer);

            if (bytesRead > 0) {
                buffer.flip();
                //message.append
                String r = (new String(buffer.array(), 0, bytesRead));
                //System.out.println(message.toString());
               // String[] requests = message.toString().split("\n");
                //for (String request : requests) {
                    //System.out.println("request " + r.trim() +"#");
                    String response = processRequest(client, r.trim());
                    buffer.clear();
                    buffer.put(response.getBytes());
                    buffer.flip();
                    client.write(buffer);
                //System.out.println("send");
                //}
                message.setLength(0);
            } else if (bytesRead == 0) {
                break;
            } else {
                //serverLog.append(client.getRemoteAddress().toString() + " logged out at " + Time.now() + "\n");
                clientLogs.remove(client);
                client.close();
                break;
            }
        }
    }



    private String processRequest(SocketChannel client, String request2) {
        //System.out.println(request2);
        StringBuilder request = new StringBuilder();
        for(int i =1; i<request2.split(" ").length; i++)
            request.append(request2.split(" ")[i]).append(" ");
        request = new StringBuilder(request.toString().trim());
        String response;
        //System.out.println("r: " + request);
        if (request.toString().startsWith("login")) {
            String id = request.toString().split(" ")[1];
            clientLogs.put(client, new StringBuilder("=== " + id + " log start ===\nlogged in\n"));
            response = "logged in";
            serverLog.append(id +" logged in at " + Time.now() + '\n');
        } else if (request.toString().startsWith("bye")) {
            StringBuilder clientLog = clientLogs.get(client);
            response = clientLog.toString() + "logged out\n=== " + clientLog.toString().split(" ")[1] + " log end ===\n";
            clientLogs.remove(client);

            serverLog.append(clientLog.toString().split(" ")[1] +" logged out at " + Time.now() + '\n');

            if(request.toString().equals("bye")){
                return "logged out";
            }

        } else {
            StringBuilder clientLog = clientLogs.get(client);
            serverLog.append(clientLog.toString().split(" ")[1] +" requested at " + Time.now() + " \"" + request +"\"\n");
            clientLog.append("Request: " + request + "\n");
            response = Time.passed(request.toString().split(" ")[0], request.toString().split(" ")[1]);
            clientLog.append("Result:\n" + response);
        }
        //System.out.println("res " + response);
        return response;
    }

    public void stopServer() {
        try {
            if (selector != null) {
                selector.close();
            }

            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }

            if (executorService != null) {
                executorService.shutdownNow();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getServerLog() {
        return serverLog.toString();
    }
}

