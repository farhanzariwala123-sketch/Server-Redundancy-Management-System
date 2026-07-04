package client;

import common.Message;
import common.Serializer;

import java.io.*;
import java.net.*;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: ClientMain <job-payload>");
            return;
        }
        String payload = args[0];
        String monitorHost = "localhost";
        int monitorPort = 9000;
        while (true) {
            try (Socket s = new Socket(monitorHost, monitorPort);
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
                Message q = new Message("GET_PRIMARY");
                out.write(Serializer.serialize(q));
                out.flush();
                String line = in.readLine();
                if (line == null) { Thread.sleep(1000); continue; }
                Message resp = Serializer.deserialize(line);
                if (resp.serverId == 0) { Thread.sleep(1000); continue; }
                int serverPort = resp.port;
                // connect to primary
                try (Socket ps = new Socket("localhost", serverPort);
                     BufferedWriter pout = new BufferedWriter(new OutputStreamWriter(ps.getOutputStream()));
                     BufferedReader pin = new BufferedReader(new InputStreamReader(ps.getInputStream()))) {
                    Message pm = new Message("PROCESS");
                    pm.payload = payload;
                    pout.write(Serializer.serialize(pm));
                    pout.flush();
                    String rline = pin.readLine();
                    if (rline != null) {
                        Message r = Serializer.deserialize(rline);
                        System.out.println("Client received: " + r.payload);
                        return;
                    }
                } catch (IOException e) {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                Thread.sleep(1000);
            }
        }
    }
}