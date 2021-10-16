package S2.Utils;

public class LamportClock {
    private static final int NUM_LIGHTWEIGHTS = 2;
    private int tics;
    private int value[] = new int[NUM_LIGHTWEIGHTS];
    private static LamportClock instance;

    public static LamportClock getLamportClock(){
        if (instance == null){
            instance = new LamportClock();
        }
        return instance;
    }

    private LamportClock() {
        this.tics = 0;
        for (int i =0; i<NUM_LIGHTWEIGHTS; i++){
            value[i] = 0;
        }
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
        value[src] = Integer.max(tics, receivedValue) + 1;
    }
}
