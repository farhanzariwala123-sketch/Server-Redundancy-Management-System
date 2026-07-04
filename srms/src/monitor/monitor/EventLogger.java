package monitor;

public class EventLogger implements Observer {
    @Override
    public void update(String event) {
        System.out.println("[OBSERVER] Event received: " + event);
    }
}
