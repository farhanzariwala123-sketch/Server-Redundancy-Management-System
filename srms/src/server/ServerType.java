package server;

public abstract class ServerType {
    protected final int id;
    protected final int port;
    protected final String monitorHost;
    protected final int monitorPort;

    public ServerType(int id, int port, String monitorHost, int monitorPort) {
        this.id = id;
        this.port = port;
        this.monitorHost = monitorHost;
        this.monitorPort = monitorPort;
    }

    public abstract void start() throws Exception;
}