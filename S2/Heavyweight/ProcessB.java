package S2.Heavyweight;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ProcessB {
    private static final int NUM_LIGHTWEIGHTS = 2;
    private static final int PORT_HWA = 5000;
    private static final int PORT_HWB = 6000;
    private static final int STARTING_PORT_LWB = 6001;

    private int answersfromLightweigth;
    private String token;
    private Socket lightweights[] = new Socket[0];
    private Socket heavyWeight = null;
    private ServerSocket listener = null;

    private void generateSockets(){
        try {

            listener = new ServerSocket(PORT_HWB);
            lightweights = new Socket[NUM_LIGHTWEIGHTS];
            heavyWeight = new Socket("127.0.0.1", PORT_HWA);
            for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
                lightweights[i] = new Socket("127.0.0.1", STARTING_PORT_LWB + i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void start() {

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

    private void sendTokenToHeavyweight(OutputStream outputStream) {
    }

    private void listenHeavyweight(InputStream inputStream) {
    }

    private void listenLightweight(InputStream inputStream) {
    }

    private void sendActionToLightweight(OutputStream outputStream) {

    }


}
