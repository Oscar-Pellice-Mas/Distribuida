package S2.Heavyweight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ProcessA {
    private static final int NUM_LIGHTWEIGHTS = 3;
    private static final int PORT_HWA = 5000;
    private static final int PORT_HWB = 6000;
    private static final int STARTING_PORT_LWA = 5001;

    private static int answersfromLightweigth;
    private static String token;
    private Socket lightweights[] = new Socket[0];
    private Socket heavyWeight = null;
    private ServerSocket listener = null;

    private void generateSockets(){
        try {
            listener = new ServerSocket(PORT_HWA);
            lightweights = new Socket[NUM_LIGHTWEIGHTS];
            heavyWeight = new Socket("127.0.0.1", PORT_HWB);
            for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
                lightweights[i] = new Socket("127.0.0.1", STARTING_PORT_LWA + i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Socket clientSocket = new Socket("127.0.0.1", 5000);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while(true){
                while(token == null) listenHeavyweight(in);
                for (int i=0; i<NUM_LIGHTWEIGHTS; i++)
                    sendActionToLightweight(out);
                while(answersfromLightweigth < NUM_LIGHTWEIGHTS)
                    listenLightweight(in);
                token = null;
                sendTokenToHeavyweight(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendTokenToHeavyweight(PrintWriter out) {
        out.write("TOKEN");
    }

    private static void listenLightweight(BufferedReader in) throws IOException {
        String msg = in.readLine();
        if (msg.equalsIgnoreCase("TOKEN")) token = "TOKEN";
        else System.out.println(msg);
    }

    private static void sendActionToLightweight(PrintWriter out) {
    }

    private static void listenHeavyweight(BufferedReader in) {
    }

}
