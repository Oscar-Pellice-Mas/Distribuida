package S2.Heavyweight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ProcessA {
    private static final int NUM_LIGHTWEIGHTS = 0;
    //private static final int NUM_LIGHTWEIGHTS = 3;
    private static final int PORT_HWA = 5000;
    private static final int PORT_HWB = 6000;
    private static final int STARTING_PORT_LWA = 5001;


    static ServerSocket serverSocket= null;
    static Socket serverAccepter= null;

    private static int answersfromLightweigth;
    private static String token;
    private static Socket lightweights[] = new Socket[0];
    private static Socket heavyWeight = null;

    private static PrintWriter outS = null;
    private static BufferedReader inS = null;
    private static PrintWriter outHW = null;
    private static BufferedReader inHW = null;
    private static PrintWriter outLW[] = new PrintWriter[NUM_LIGHTWEIGHTS];
    private static BufferedReader inLW[] = new BufferedReader[NUM_LIGHTWEIGHTS];

    private static void CreateServer(){
        try {
            serverSocket = new ServerSocket(PORT_HWA);
            serverAccepter=serverSocket.accept();//establishes connection
            inS = new BufferedReader(new InputStreamReader(serverAccepter.getInputStream()));
            outS = new PrintWriter(serverAccepter.getOutputStream(), true);
            System.out.println("Conecta al HW");
            for(int i =0; i<NUM_LIGHTWEIGHTS;i++){
                lightweights[i] = serverSocket.accept();
                outLW[i]= new PrintWriter(lightweights[i].getOutputStream(), true);
                inLW[i] = new BufferedReader(new InputStreamReader(lightweights[i].getInputStream()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateSockets(){
        try {
            lightweights = new Socket[NUM_LIGHTWEIGHTS];
            heavyWeight = new Socket("127.0.0.1", PORT_HWB);
            inHW = new BufferedReader(new InputStreamReader(heavyWeight.getInputStream()));
            outHW = new PrintWriter(heavyWeight.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        token = null;
        try {
            generateSockets();
            System.out.println("Server B ready?");
            CreateServer();
            while(true){
                while(token == null) listenHeavyweight(inS);
                for (int i=0; i<NUM_LIGHTWEIGHTS; i++)
                    sendActionToLightweight(outLW[i]);
                answersfromLightweigth=0;
                for (int i=0; answersfromLightweigth < NUM_LIGHTWEIGHTS; i++)
                    listenLightweight(inLW[i]);
                token = null;
                sendTokenToHeavyweight(outHW);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendTokenToHeavyweight(PrintWriter out) {
        out.println("TOKEN");
    }

    private static void listenLightweight(BufferedReader in) throws IOException {
        String msg = in.readLine();
        if (msg.equalsIgnoreCase("TOKEN")){
            token = "TOKEN";
            answersfromLightweigth++;
        }
        else System.out.println(msg);
    }

    private static void sendActionToLightweight(PrintWriter out) {
    }

    private static void listenHeavyweight(BufferedReader in) throws IOException {
        String msg = in.readLine();

        if (msg.equalsIgnoreCase("TOKEN")){
            token = "TOKEN";
            System.out.println("Sóc el Process A i puc parlar.");
        }else System.out.println(msg);
    }

}
