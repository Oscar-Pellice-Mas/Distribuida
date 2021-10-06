package S2.Heavyweight;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ProcessB {
    private static final int NUM_LIGHTWEIGHTS = 2;
    private static final int PORT_HWA = 5000;
    private static final int PORT_HWB = 6000;
    private static final int STARTING_PORT_LWB = 6001;

    private static int answersfromLightweigth;
    private static String token;
    private static Socket lightweights[] = new Socket[0];
    private static Socket heavyWeight = null;
    private static ServerSocket listener = null;

    private static void generateSockets(){
        try {
            Socket clientSocket = new Socket("127.0.0.1", PORT_HWA);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //listener = new ServerSocket(PORT_HWB);
            lightweights = new Socket[NUM_LIGHTWEIGHTS];
            //heavyWeight = new Socket("127.0.0.1", PORT_HWA);
            for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
                lightweights[i] = new Socket("127.0.0.1", STARTING_PORT_LWB + i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //Creació dels socket cap el ligthweight
        generateSockets();


        try {
            while (true) {
                while (token == null) listenHeavyweight(heavyWeight.getInputStream());
                for (int i = 0; i < NUM_LIGHTWEIGHTS; i++)
                    sendActionToLightweight(lightweights[i].getOutputStream());

                answersfromLightweigth = 0;

                while (answersfromLightweigth < NUM_LIGHTWEIGHTS)
                    listenLightweight(lightweights[answersfromLightweigth].getInputStream());

                token = null;
                sendTokenToHeavyweight(heavyWeight.getOutputStream());
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void sendTokenToHeavyweight(PrintWriter out) {
        out.write("TOKEN");
    }

    private static void listenHeavyweight(BufferedReader in) throws IOException {
        String msg = in.readLine();
        if (msg.equalsIgnoreCase("TOKEN")) token = "TOKEN";
        else System.out.println(msg);
    }

    private static void listenLightweight(PrintWriter out) {
    }

    private static void sendActionToLightweight(BufferedReader in) {

    }


}
