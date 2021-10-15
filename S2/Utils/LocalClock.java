package S2.Utils;

public class LocalClock {
    private static final int NUM_LIGHTWEIGHTS = 3;
    private int ticks;
    private int value[] = new int[NUM_LIGHTWEIGHTS];
    private static LocalClock instance;

    public static LocalClock getLocalClock(){
        if (instance == null){
            instance = new LocalClock();
        }
        return instance;
    }

    private LocalClock() {
        this.ticks = 0;
        for (int i =0; i<NUM_LIGHTWEIGHTS; i++){
            value[i] = 0;
        }
    }

    public int tick(){
        return ++ticks;
    }

    public int getTicks(){
        return ticks;
    }

    public int getValue(int id){
        return value[id];
    }
    public void recieveAction(){

    }
}
