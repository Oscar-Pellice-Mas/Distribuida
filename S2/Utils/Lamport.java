package S2.Utils;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Lamport {
    private LocalClock clock;
    private ArrayList<Integer> cua;
    private int myId;
    private static final int NUM_LIGHTWEIGHTS = 3;

    public Lamport(String myId) {
        this.myId = Integer.parseInt(myId);
        this.clock = LocalClock.getLocalClock();
        this.cua = new ArrayList<Integer>();
        for (int i = 0; i < NUM_LIGHTWEIGHTS;i++ ){
            cua.add(Integer.MAX_VALUE);
        }
    }


    public void /*synchronized*/ requestCS(BufferedReader inHW, PrintWriter outHW) {
        clock.tick();
        cua.set(myId,clock.getTicks());
        sendMSG(outHW, "request " + myId + " " + cua.get(myId));
        while (!okayCS(inHW)){
            waitHere();
        }
    }

    private void sendMSG(PrintWriter outHW, String request) {
        outHW.println(request+myId);
    }

    public /*synchronized*/ void releaseCS(PrintWriter outHW) {
        sendMSG(outHW, "request " + myId + " " + cua.get(myId));
        cua.set(myId, Integer.MAX_VALUE);
    }
    public boolean okayCS(BufferedReader inHW) {
        for (int i =0; i<NUM_LIGHTWEIGHTS; i++){
            if (isGreater(cua.get(myId), myId, cua.get(i),i)){
                return false;
            }else if (isGreater(cua.get(myId), myId, clock.getValue(i),i)){
                return false;
            }
        }
        return true;
    }
    public void waitHere(){

    }
    public boolean isGreater(Integer entry1, int myId, Integer entr2, int yourId) {
        if (entr2 == Integer.MAX_VALUE) return false;
        return ((entry1 > entr2)||(entry1 == entr2)&&(myId > yourId));
    }

    public /*synchronized*/ void handleMSG(PrintWriter outHW, String m, int src) {
        String[] sections = m.split(" ");
        int time = Integer.parseInt(sections[2]);
        clock.recieveAction(src, time);
        if (sections[0].equals("request")){
            cua.set(src, time);
            sendMSG(outHW, src + " ack " + myId + " " + clock.getValue(myId));
        }else if (sections[0].equals("release")) cua.set(src,Integer.MAX_VALUE);
        //TODO: Implement notify
        notify();
    }

}
