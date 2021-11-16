package S2.Utils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

public class RicAndAgr {
    private static final int NUM_LIGHTWEIGHTS = 3;
    private int myts;
    private LamportClock clock;
    private LinkedList<Integer> cua;
    private int myId= 0;
    private int numOk=0;

    public RicAndAgr(int myId) {
        this.myId = myId;
        this.myts = Integer.MAX_VALUE;
        clock  = LamportClock.getLamportClock();
        this.cua =  new LinkedList<Integer>();
    }

    public void requestCS(PrintWriter outHW) {
        clock.tick();
        myts = clock.getValue();
        broadcastMSG( outHW ,myId + " request" + myts);
        numOk=0;
        while (numOk < NUM_LIGHTWEIGHTS-1) waitHere();
    }
    public void waitHere(){

    }
    private void broadcastMSG(PrintWriter outHW, String s) {
        outHW.println(s);
    }
    public void releaseCS(PrintWriter outHW) {
        myts= Integer.MAX_VALUE;
        while (!cua.isEmpty()){
            int pid = cua.remove(0);
            sendMSG(outHW,"okay " +" " + myId + " " + clock.getValue());
        }
    }

    public void handleMSG(PrintWriter outHW, String m, int src){
        String[] sections = m.split(" ");
        int time = Integer.parseInt(sections[2]);
        clock.receiveAction(src, time);
        if (sections[0].equals("request")){
            if ((myts==Integer.MAX_VALUE)||(time<myts)||((time==myts)&&(src < myId))){
                sendMSG(outHW, "okay " +" " + myId + " " + clock.getValue());
            }else{
                cua.add(src);
            }
        }else if (sections[0].equals("okay")){
            numOk++;
            if (numOk == NUM_LIGHTWEIGHTS-1){
                //TODO: Implement notify
                notify();
            }
        }
    }

    private void sendMSG(PrintWriter outHW, String s) {
        outHW.println(s);
    }
}
