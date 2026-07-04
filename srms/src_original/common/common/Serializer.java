package common;

// Simple pipe-delimited serializer: type|serverId|port|timestamp|payload
public class Serializer {
    public static String serialize(Message m) {
        String p = m.payload == null ? "" : m.payload.replace("\n"," ");
        return String.format("%s|%d|%d|%d|%s\n", safe(m.type), m.serverId, m.port, m.timestamp, p);
    }
    public static Message deserialize(String line) {
        String[] parts = line.split("\\|",5);
        Message m = new Message();
        m.type = parts.length>0?parts[0]:"";
        m.serverId = parts.length>1?parseInt(parts[1],0):0;
        m.port = parts.length>2?parseInt(parts[2],0):0;
        m.timestamp = parts.length>3?parseLong(parts[3],System.currentTimeMillis()):System.currentTimeMillis();
        m.payload = parts.length>4?parts[4]:"";
        return m;
    }
    private static int parseInt(String s,int def){ try{return Integer.parseInt(s);}catch(Exception e){return def;} }
    private static long parseLong(String s,long def){ try{return Long.parseLong(s);}catch(Exception e){return def;} }
    private static String safe(String s){ return s==null?"":s; }
}
