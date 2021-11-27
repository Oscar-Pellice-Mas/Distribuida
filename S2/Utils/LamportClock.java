package S2.Utils;

public class LamportClock {
    private int tics;
    private static LamportClock instance;

    public static LamportClock getLamportClock(){
        if (instance == null){
            instance = new LamportClock();
        }
        return instance;
    }

    private LamportClock() {
        this.tics = 1;
    }

    public int getValue(){
        return tics;
    }
    public void tick(){ // to be used in internal events
        tics++;
    }
    public void sendAction(){ // incremented before sending
        tics++;
    }
    public void receiveAction(int src, int receivedValue){
        tics = Integer.max(tics, receivedValue) + 1;
    }
}
