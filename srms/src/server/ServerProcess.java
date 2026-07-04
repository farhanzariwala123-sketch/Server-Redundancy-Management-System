package server;

import common.Message;
import common.Serializer;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.concurrent.*;

public class ServerProcess extends ServerType {
    private volatile boolean running = true;
    private volatile boolean isPrimary = false;
    private final File logFile;

    public ServerProcess (int id, int port, String monitorHost, int monitorPort) {
        super(id, port, monitorHost, monitorPort);
        new File("logs").mkdirs();
        this.logFile = new File("logs/server-" + id + ".log");
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("Usage: ServerMain <id> <port> <monitorHost> <monitorPort>");
            return;
        }
        int id = Integer.parseInt(args[0]);
        int port = Integer.parseInt(args[1]);
        String monitorHost = args[2];
        int monitorPort = Integer.parseInt(args[3]);
        ServerProcess s = new ServerProcess(id, port, monitorHost, monitorPort);
        s.start();
    }

    public void start() throws IOException {
        log("Starting server id=" + id + " port=" + port);
        // heartbeat thread
        Executors.newSingleThreadExecutor().submit(this::heartbeatLoop);
        // server socket
        ServerSocket ss = new ServerSocket(port);
        ExecutorService pool = Executors.newCachedThreadPool();
        while (running) {
            Socket client = ss.accept();
            pool.submit(() -> handleConnection(client));
        }
    }

    private void heartbeatLoop() {
        while (running) {
            try {
                sendHeartbeat();
                Thread.sleep(1000);
                // optional delay simulation file
                if (new File("/tmp/delay_heartbeats").exists()) Thread.sleep(2000);
            } catch (InterruptedException e) { break; }
        }
    }

    private void sendHeartbeat() {
        try (Socket s = new Socket(monitorHost, monitorPort);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            Message m = new Message("HEARTBEAT");
            m.serverId = id;
            m.port = port;
            m.timestamp = System.currentTimeMillis();
            out.write(Serializer.serialize(m));
            out.flush();
            log("HEARTBEAT sent");
        } catch (IOException e) {
            log("HEARTBEAT failed: " + e.getMessage());
        }
    }

    private void handleConnection(Socket s) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            String line = in.readLine();
            if (line == null) return;
            Message m = Serializer.deserialize(line);
            if ("PROMOTE".equals(m.type)) {
                isPrimary = true;
                log("PROMOTE received -> now PRIMARY");
                // acknowledge
                Message resp = new Message("RESPONSE");
                resp.payload = "PROMOTED";
                out.write(Serializer.serialize(resp));
                out.flush();
            } else if ("PROCESS".equals(m.type)) {
                log("PROCESS request received payload=" + m.payload);
                if (!isPrimary) {
                    Message resp = new Message("RESPONSE");
                    resp.payload = "NOT_PRIMARY";
                    out.write(Serializer.serialize(resp));
                    out.flush();
                } else {
                    // simple processing: echo with server id and timestamp
                    Message resp = new Message("RESPONSE");
                    resp.payload = "OK from " + id + " at " + new Date() + " payload=" + m.payload;
                    out.write(Serializer.serialize(resp));
                    out.flush();
                    log("Processed request, replied");
                }
            } else if ("PING".equals(m.type)) {
                Message resp = new Message("RESPONSE");
                resp.payload = isPrimary ? "PRIMARY" : "BACKUP";
                out.write(Serializer.serialize(resp));
                out.flush();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    private void log(String s) {
        String line = String.format("[%s] Srv-%d: %s", new Date(), id, s);
        System.out.println(line);
        try (FileWriter fw = new FileWriter(logFile, true)) { fw.write(line + "\n"); } catch (IOException e) {}
    }
}