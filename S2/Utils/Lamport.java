package S2.Utils;

import S2.Lightweight.LightweightA;


import java.io.BufferedReader;
import java.io.IOException;
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

    public synchronized void requestCS(List<PrintWriter> out, List<BufferedReader> in) throws InterruptedException, IOException {
        String response;
        boolean okay = false;
        int j=0; //Index secundari per comptar l'accès a l'array
        clock.tick(myId-1);
        cua.set(myId-1,clock.getValue(myId-1));
        /*Evitem que s'envii a si mateix que no existeix*/

        for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
            if (i!=myId-1){
                sendMSG(out.get(j), "request " + myId + " " + cua.get(myId-1));
                j++;
                //FIXME: INTENTEM ARREGLAR LA GESTIÓ DE LA ACK
                /*
                do{
                    response = in.get(j).readLine();
                    //System.out.println("Missatge que es: "+ response);
                    if (response.split(" ")[1].equals("ack")){
                        System.out.println("ACK rebuda");
                        j++;
                        break;
                    }
                }while(true);
                */
            }
        }
        System.out.println("\u001B[32m"+" Requests enviades");
        okay = okayCS();
        System.out.println("Estoy OK? " + okay);
        while (!okay){
            synchronized (this){
                this.wait();
                System.out.println("Estic fora del wait");
            }
            okay=okayCS();
            System.out.println("clock: " +clock.getValue(0)+","+clock.getValue(1)+","+clock.getValue(2));
            System.out.println("CUA: " + cua.get(0) +","+cua.get(1)+","+cua.get(2));
            System.out.println("Okay? :" + okay );
        }
    }

    private void sendMSG(PrintWriter out, String request) {
        out.println(request/*+myId*/);
    }

    public synchronized void releaseCS(List<PrintWriter> out) {
        int j =0;
        cua.set(myId-1, Integer.MAX_VALUE);
        /*Evitem que s'envii a si mateix que no existeix*/
        for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
            if (i!=myId-1){
                sendMSG(out.get(j), "release " + myId + " " + clock.getValue(myId-1));
                j++;
            }
        }

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
        return ((entry1 > entr2)||
                ((entry1.equals(entr2))&&(myId > yourId)));
    }

    public  void handleMSG(PrintWriter out, String m, int src, LightweightA instance) {

        String[] sections = m.split(" ");
        int time = Integer.parseInt(sections[2]);

        clock.recieveAction(src-1, time, myId-1);
        if (sections[0].equals("request")){
            cua.set(src-1, time);
            //FIXME: no està arribant el ack a la resta. potse estic enviant per on no toca
            sendMSG(out, "ack "+ myId + " " + clock.getValue(myId-1));
        }else if (sections[0].equals("release")){
            cua.set(src-1,Integer.MAX_VALUE);
        }

        //System.out.println("\u001B[33m"+"MSG handled: "+ m);
        synchronized (this){
            this.notify();// Desperta el waits
            System.out.println("NOTIFY FET");
        }
    }

}
