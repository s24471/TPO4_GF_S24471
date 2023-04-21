/**
 *
 *  @author Gocławski Filip S24471
 *
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;


public class Client {

    public final String id;
    private final String host;
    private final int port;
    private SocketChannel channel;
    private Selector selector;
    private ByteBuffer buffer;

    public Client(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
    }

    public void connect() {
        try {
            this.channel = SocketChannel.open();
            this.channel.configureBlocking(false);
            this.channel.connect(new InetSocketAddress(host, port));
            this.selector = Selector.open();
            this.channel.register(selector, SelectionKey.OP_CONNECT);

            while (!channel.finishConnect()) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isConnectable()) {
                        completeConnection(key);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.buffer = ByteBuffer.allocate(1024);
    }

    public String send(String req) {
        try {
            String fullRequest = id + " " + req;
            buffer.clear();
            buffer.put(fullRequest.getBytes());
            buffer.flip();
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            buffer.clear();
            int bytesRead;
            while (true) {
                    bytesRead = channel.read(buffer);
                    if (bytesRead > 0) {
                        break;
                    } else if (bytesRead < 0) {
                        channel.close();
                        return "";
                    }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            buffer.flip();
            return new String(buffer.array(), 0, bytesRead);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void completeConnection(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void writeRequest(SelectionKey key, String req) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        buffer.clear();
        buffer.put((id + " " + req + "\n").getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        socketChannel.write(buffer);
        key.interestOps(SelectionKey.OP_READ);
    }

    private String readResponse(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    public void disconnect() throws IOException {
        channel.close();
        selector.close();
    }
}
