package common;

public class Message {
    public String type; // HEARTBEAT | GET_PRIMARY | PRIMARY_INFO | PROMOTE | PROCESS | RESPONSE
    public int serverId;
    public int port; // server port (for heartbeats)
    public long timestamp;
    public String payload;

    public Message() {}

    public Message(String type) { this.type = type; this.timestamp = System.currentTimeMillis(); }
}