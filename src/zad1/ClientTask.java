package zad1;

import java.io.IOException;
import java.util.List;

public class ClientTask implements Runnable {

    private final Client client;
    private final List<String> requests;
    private final boolean showSendRes;
    String log;

    public ClientTask(Client client, List<String> requests, boolean showSendRes) {
        this.client = client;
        this.requests = requests;
        this.showSendRes = showSendRes;
    }

    public void run() {
        StringBuilder log = new StringBuilder();
        try {
            System.out.println("logged?");
            client.connect();
            System.out.println("logged");
            log.append(client.send("login " + client.id)).append("\n");
            for (String req : requests) {
                String res = client.send(req);
                if (showSendRes) {
                    System.out.println(res);
                }
            }
            String clog = client.send("bye and log transfer");
            log.append(clog).append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.log = log.toString();
    }

    public String get() throws InterruptedException {
        return log;
    }

    public static ClientTask create(Client c, List<String> reqs, boolean showSendRes) {
        return new ClientTask(c, reqs, showSendRes);
    }
}
