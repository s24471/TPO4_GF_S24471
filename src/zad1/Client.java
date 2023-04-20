/**
 *
 *  @author Gocławski Filip S24471
 *
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Client {

    private final String host;
    private final int port;
    public final String id;
    private SocketChannel channel;
    private ByteBuffer buffer;

    public Client(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
    }

    public void connect() {
        try {
            this.channel = SocketChannel.open(new InetSocketAddress(host, port));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.buffer = ByteBuffer.allocate(1024);
    }

    public String send(String req)  {
        buffer.clear();
        buffer.put((id + " " + req).getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        try {
            channel.write(buffer);
            buffer.clear();
            channel.read(buffer);
            buffer.flip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    public void disconnect() throws IOException {
        channel.close();
    }
}
