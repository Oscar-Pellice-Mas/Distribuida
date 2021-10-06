package S2.Heavyweight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ProcessA {
    private static String token;
    private static final int NUM_LIGHTWEIGHTS = 3;
    private static int answersfromLightweigth;

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
