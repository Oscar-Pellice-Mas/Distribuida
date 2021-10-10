package S2.Heavyweight;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ProcessB {
    private static final int NUM_LIGHTWEIGHTS = 0;
    //private static final int NUM_LIGHTWEIGHTS = 2;
    private static final int PORT_HWA = 5000;
    private static final int PORT_HWB = 6000;
    private static final int STARTING_PORT_LWB = 6001;

    static ServerSocket serverSocket= null;
    static Socket serverAccepter= null;


    private static int answersfromLightweigth;
    private static String token;
    private static Socket lightweights[] = new Socket[0];
    private static Socket heavyWeight_A = null;

    private static PrintWriter outS = null;
    private static BufferedReader inS = null;
    private static PrintWriter outHW = null;
    private static BufferedReader inHW = null;
    private static PrintWriter outLW[] = new PrintWriter[NUM_LIGHTWEIGHTS];
    private static BufferedReader inLW[] = new BufferedReader[NUM_LIGHTWEIGHTS];




    private static void CreateServer(){
        try {
            serverSocket = new ServerSocket(PORT_HWB);
            serverAccepter=serverSocket.accept();//establishes connection
            inS = new BufferedReader(new InputStreamReader(serverAccepter.getInputStream()));
            outS = new PrintWriter(serverAccepter.getOutputStream(), true);
            System.out.println("Conecta");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateSockets(){
        try {
            lightweights = new Socket[NUM_LIGHTWEIGHTS];
            heavyWeight_A = new Socket("127.0.0.1", PORT_HWA);
            inHW = new BufferedReader(new InputStreamReader(heavyWeight_A.getInputStream()));
            outHW = new PrintWriter(heavyWeight_A.getOutputStream(), true);
            for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
                lightweights[i] = new Socket("127.0.0.1", STARTING_PORT_LWB + i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //Creació dels socket cap el ligthweight
        token="TOKEN";
        Scanner scanner = new Scanner(System.in);
        try {
            CreateServer();
            System.out.println("Server A ready?");
            scanner.nextInt();
            generateSockets();
            while (true) {
                while(token == null) listenHeavyweight(inS);
                for (int i=0; i<NUM_LIGHTWEIGHTS; i++)
                    sendActionToLightweight(outLW[i]);
                answersfromLightweigth=0;
                for (int i=0; answersfromLightweigth < NUM_LIGHTWEIGHTS; i++)
                    listenLightweight(inLW[i]);

                sendTokenToHeavyweight(outHW);
                System.out.println("PROVO SI ARRIBO");
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void sendTokenToHeavyweight(PrintWriter out) {
        out.write("TOKEN");
        token = null;
    }

    private static void listenHeavyweight(BufferedReader in) throws IOException {
        String msg = in.readLine();
        if (msg.equalsIgnoreCase("TOKEN")){
            token = "TOKEN";
            System.out.println("Sóc el Process B i puc parlar.");
        }
        else System.out.println(msg);
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


}
