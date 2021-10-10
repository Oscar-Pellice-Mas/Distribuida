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

    private static PrintWriter out = null;
    private static BufferedReader in = null;


    private static void CreateServer(){
        try {
            serverSocket = new ServerSocket(PORT_HWB);
            serverAccepter=serverSocket.accept();//establishes connection
            //DataInputStream serverDataInputStream=new DataInputStream(serverAccepter.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateSockets(){
        try {
            lightweights = new Socket[NUM_LIGHTWEIGHTS];
            heavyWeight = new Socket("127.0.0.1", PORT_HWB);
            out = new PrintWriter(heavyWeight.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(heavyWeight.getInputStream()));
            for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
                lightweights[i] = new Socket("127.0.0.1", STARTING_PORT_LWA + i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            CreateServer();
            System.out.println("Server B ready?");
            scanner.nextInt();
            generateSockets();
            scanner.nextInt();
            while(true){
                while(token == null) listenHeavyweight(in);
                for (int i=0; i<NUM_LIGHTWEIGHTS; i++)
                    sendActionToLightweight(out);
                answersfromLightweigth=0;
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
