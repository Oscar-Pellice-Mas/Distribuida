package S2.Lightweight;

import S2.Utils.Lamport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LightweightA {
    private static final int PORT_HWA = 5000;

    private static Socket socket;
    private static PrintWriter outHW;
    private static BufferedReader inHW;
    static String myID;;

    private static void connectarHW(){
        try {
            socket = new Socket("127.0.0.1",PORT_HWA);
            inHW = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outHW = new PrintWriter(socket.getOutputStream(), true);
            myID = inHW.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        connectarHW();
        Lamport lamport = new Lamport(myID);
        while (true){
            waitHeavyWeight();
            lamport.requestCS(inHW, outHW);
            for (int i=0; i<10; i++){
                System.out.println("Sóc el procés lightweight: "+ myID+"\n");
                espera1Segon();
            }
            lamport.releaseCS(outHW);
            notifyHeavyWeight();
        }
    }

    private static void notifyHeavyWeight() {

    }

    private static void espera1Segon() {
    }

    private static void waitHeavyWeight() {
    }
}
