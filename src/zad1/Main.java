/**
 *
 *  @author Gocławski Filip S24471
 *
 */

package zad1;


import java.util.*;
import java.util.concurrent.*;

public class Main {

  public static void main(String[] args) throws Exception {
    String fileName = System.getProperty("user.home") + "/PassTimeServerOptions.yaml";
    Options opts = Tools.createOptionsFromYaml(fileName);
    String host = opts.getHost();
    int port = opts.getPort();
    boolean concur =  opts.isConcurMode();
    boolean showRes = opts.isShowSendRes();
    Map<String, List<String>> clRequests = opts.getClientsMap();
    ExecutorService es = Executors.newCachedThreadPool();
    List<ClientTask> ctasks = new ArrayList<>();
    List<String> clogs = new ArrayList<>(); 

    Server s = new Server(host, port);
    s.startServer();

    // start clients
    clRequests.forEach( (id, reqList) -> {
      Client c = new Client(host, port, id);
      if (concur) {
        ClientTask ctask = ClientTask.create(c, reqList, showRes);
        ctasks.add(ctask);
        es.execute(ctask);
      } else {
        c.connect();
        System.out.println("con");
        c.send("login " + id);
        System.out.println("send");
        for(String req : reqList) {
          System.out.println(req);
          String res = c.send(req);
          if (showRes) System.out.println(res);
        }
        String clog = c.send("bye and log transfer");
        System.out.println(clog);
      }
    });

    if (concur) {
      ctasks.forEach( task -> {
        try {
          String log = task.get();
          clogs.add(log);
        } catch (InterruptedException | ExecutionException exc) {
          System.out.println(exc);
        }
      });
      clogs.forEach( System.out::println);
      es.shutdown();
    }
    s.stopServer();
    System.out.println("\n=== Server log ===");
    System.out.println(s.getServerLog());
  }

}