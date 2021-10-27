package S2.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Lamport {
    private LocalClock clock;
    private ArrayList<Integer> cua;
    private int myId;
    private static final int NUM_LIGHTWEIGHTS = 3;

    private static boolean end = false;

    public Lamport(int myId) {
        this.myId = myId;
        this.clock = LocalClock.getLocalClock();
        this.cua = new ArrayList<>();
        for (int i = 0; i < NUM_LIGHTWEIGHTS;i++ ){
            cua.add(Integer.MAX_VALUE);
        }
    }

    public synchronized void requestCS(Socket socket) {
        try{
            clock.tick();
            cua.set(myId,clock.getTicks());
            BufferedReader inHW = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outHW = new PrintWriter(socket.getOutputStream(), true);
            sendMSG(outHW, "request " + myId + " " + cua.get(myId));
            while (!okayCS()){
                waitHere();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMSG(PrintWriter outHW, String request) {
        outHW.println(request+myId);
    }

    public synchronized void releaseCS(PrintWriter outHW) {
        sendMSG(outHW, "request " + myId + " " + cua.get(myId));
        cua.set(myId, Integer.MAX_VALUE);
    }
    public boolean okayCS() {
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
        while (!end) {
            try {
                wait(); // Fer wait de un en un es correcte?
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isGreater(Integer entry1, int myId, Integer entr2, int yourId) {
        if (entr2 == Integer.MAX_VALUE) return false;
        return ((entry1 > entr2)||(entry1.equals(entr2))&&(myId > yourId));
    }

    public synchronized void handleMSG(PrintWriter outHW, String m, int src) {
        String[] sections = m.split(" ");
        int time = Integer.parseInt(sections[2]);
        clock.recieveAction(src, time);
        if (sections[0].equals("request")){
            cua.set(src, time);
            sendMSG(outHW, src + " ack " + myId + " " + clock.getValue(myId));
        }else if (sections[0].equals("release")) cua.set(src,Integer.MAX_VALUE);
        notify(); // Desperta el waits
    }

}