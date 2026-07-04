package monitor;

import common.Message;
import common.Serializer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MonitorMain {
    private final int port;
    private final Map<Integer, Long> lastHeartbeat = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> serverPort = new ConcurrentHashMap<>();
    private volatile Integer primaryId = null;
    private final long heartbeatTimeoutMs = 5000L;
    private final File logFile = new File("logs/monitor.log");

    public MonitorMain(int port) { this.port = port; }

    public static void main(String[] args) throws Exception {
        new File("logs").mkdirs();
        int port = 9000;
        MonitorMain m = new MonitorMain(port);
        m.log("Starting Monitor on port " + port);
        m.start();
    }

    public void start() throws IOException {
        try (ServerSocket ss = new ServerSocket(port)) {
            Executors.newSingleThreadExecutor().submit(this::failureDetectorLoop);
            while (true) {
                Socket s = ss.accept();
                Executors.newCachedThreadPool().submit(() -> handleConnection(s));
            }
        }
    }

    private void handleConnection(Socket s) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                Message m = Serializer.deserialize(line);
                if ("HEARTBEAT".equals(m.type)) {
                    lastHeartbeat.put(m.serverId, System.currentTimeMillis());
                    serverPort.put(m.serverId, m.port);
                    log("HEARTBEAT from " + m.serverId + " port=" + m.port);
                    if (primaryId == null) electPrimary();
                } else if ("GET_PRIMARY".equals(m.type)) {
                    Message resp = new Message("PRIMARY_INFO");
                    if (primaryId != null) {
                        resp.serverId = primaryId;
                        resp.port = serverPort.getOrDefault(primaryId,0);
                        resp.payload = "localhost";
                    } else {
                        resp.payload = "";
                    }
                    out.write(Serializer.serialize(resp));
                    out.flush();
                } else if ("ADMIN_PROMOTE".equals(m.type)) {
                    // manual promote request: payload contains id
                    try {
                        int id = Integer.parseInt(m.payload.trim());
                        log("ADMIN_PROMOTE request for " + id);
                        sendPromote(id);
                    } catch (Exception e) {}
                }
            }
        } catch (IOException e) {
            // connection closed
        }
    }

    private void failureDetectorLoop() {
        while (true) {
            try { Thread.sleep(heartbeatTimeoutMs/2); } catch (InterruptedException e) {}
            if (primaryId != null) {
                Long last = lastHeartbeat.get(primaryId);
                if (last == null || System.currentTimeMillis() - last > heartbeatTimeoutMs) {
                    log("Primary " + primaryId + " timed out");
                    electPrimary();
                }
            } else {
                electPrimary();
            }
        }
    }

    private synchronized void electPrimary() {
        Optional<Integer> candidate = lastHeartbeat.keySet().stream().min(Integer::compareTo);
        if (candidate.isPresent()) {
            Integer newPrimary = candidate.get();
            if (!newPrimary.equals(primaryId)) {
                primaryId = newPrimary;
                log("Elected new primary: " + primaryId);
                sendPromote(primaryId);
            }
        } else {
            primaryId = null;
            log("No candidates for primary");
        }
    }

    private void sendPromote(int serverId) {
        Integer port = serverPort.get(serverId);
        if (port == null || port == 0) {
            log("Cannot send PROMOTE to " + serverId + " (unknown port)");
            return;
        }
        try (Socket s = new Socket("localhost", port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            Message m = new Message("PROMOTE");
            m.serverId = 0;
            m.payload = "";
            out.write(Serializer.serialize(m));
            out.flush();
            log("PROMOTE sent to " + serverId + " on port " + port);
        } catch (IOException e) {
            log("Failed to send PROMOTE to " + serverId + " port " + port + " : " + e.getMessage());
        }
    }

    private void log(String s) {
        String line = String.format("[%s] MON: %s", new Date(), s);
        System.out.println(line);
        try (FileWriter fw = new FileWriter(logFile, true)) { fw.write(line + "\n"); } catch (IOException e) {}
    }
}