package S2.Utils;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class RicAndAgr extends Thread{
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

    public void requestCS(List<PrintWriter> outLW) throws InterruptedException {
        clock.tick();
        myts = clock.getValue();
        broadcastMSG(outLW, myId + " request " + myts);
        numOk=0;
        synchronized(this){
            wait();
        }

    }

    private void broadcastMSG(List<PrintWriter> outLW, String s) {
        for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {if (i!=myId-1)sendMSG(outLW.get(i), s);}

    }

    public void releaseCS(List<PrintWriter> outHW) {
        myts= Integer.MAX_VALUE;
        while (!cua.isEmpty()){
            int pid = cua.remove(0);
            sendMSG(outHW.get(pid-1),"okay " +" " + myId + " " + clock.getValue());
        }
    }

    public void handleMSG(PrintWriter out, String m, int src){
        String[] sections = m.split(" ");
        int time = Integer.parseInt(sections[2]);
        clock.receiveAction(src, time);
        if (sections[0].equals("request")){
            if ((myts==Integer.MAX_VALUE)||(time<myts)||((time==myts)&&(src < myId))){
                sendMSG(out, "okay " +" " + myId + " " + clock.getValue());
            }else{
                cua.add(src);
            }
        }else if (sections[0].equals("okay")){
            numOk++;
            if (numOk == NUM_LIGHTWEIGHTS-1){
                synchronized (this){
                    this.notify();
                }
            }
        }
    }

    private void sendMSG(PrintWriter out, String s) {
        out.println(s);
    }
}
