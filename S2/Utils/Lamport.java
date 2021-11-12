package S2.Utils;

import S2.Lightweight.LightweightA;


import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Lamport extends Thread {
    private final LocalClock clock;
    private final ArrayList<Integer> cua;
    private final int myId;
    private static final int NUM_LIGHTWEIGHTS = 3;


    public Lamport(int myId) {
        this.myId = myId;
        this.clock = LocalClock.getLocalClock();
        this.cua = new ArrayList<>();
        for (int i = 0; i < NUM_LIGHTWEIGHTS;i++ ){
            cua.add(Integer.MAX_VALUE);
        }
    }

    public synchronized void requestCS(List<LightweightA.Canal> out) throws InterruptedException {
        clock.tick(myId-1);
        cua.set(myId-1,clock.getValue(myId-1));
        /*Evitem que s'envii a si mateix que no existeix*/
        for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {if (i!=myId-1)sendMSG(out.get(i).getOutS(), "request " + myId + " " + cua.get(myId-1));}
        System.out.println("\u001B[32m"+" Requests enviades");
        while (!okayCS()){
            synchronized (this){
                this.wait();
            }
        }
    }

    private void sendMSG(PrintWriter out, String request) {
        out.println(request+myId);
    }

    public synchronized void releaseCS(List<LightweightA.Canal> out) {
        cua.set(myId-1, Integer.MAX_VALUE);
        /*Evitem que s'envii a si mateix que no existeix*/
        for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {if (i!=myId-1)sendMSG(out.get(i).getOutS(), "release " + myId + " " + clock.getValue(myId-1));}

    }

    public boolean okayCS() {
        for (int i =0; i<NUM_LIGHTWEIGHTS; i++){
            if (isGreater(cua.get(myId-1), myId, cua.get(i),i)){
                return false;
            }else if (isGreater(cua.get(myId-1), myId, clock.getValue(i),i)){
                return false;
            }
        }
        return true;
    }



    public boolean isGreater(Integer entry1, int myId, Integer entr2, int yourId) {
        if (entr2 == Integer.MAX_VALUE) return false;
        return ((entry1 > entr2)||(entry1.equals(entr2))&&(myId > yourId));
    }

    public synchronized void handleMSG(PrintWriter out, String m, int src) {
        String[] sections = m.split(" ");
        int time = Integer.parseInt(sections[2]);
        clock.recieveAction(src-1, time);
        if (sections[0].equals("request")){
            cua.set(src-1, time);
            sendMSG(out, src + " ack " + myId + " " + clock.getValue(myId-1));
        }else if (sections[0].equals("release")) cua.set(src-1,Integer.MAX_VALUE);

        synchronized (this){
            this.notify();// Desperta el waits
        }
    }

}
