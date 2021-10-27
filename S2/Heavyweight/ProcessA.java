package S2.Heavyweight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class ProcessA {
    public static ProcessA Instance = null;

    public ProcessA (){
        Instance = this;
    }

    private static final int NUM_LIGHTWEIGHTS = 3;
    private static final int PORT_HWA = 5000;
    private static final int PORT_HWB = 6000;
    private static final int STARTING_PORT_LWA = 5001;

    static ServerSocket serverSocket= null;
    static Socket serverAccepter= null;

    private static int answersfromLightweigth;
    private static String token;

    private static List<Socket> lightweights;
    private static Socket heavyWeight = null;

        // Server socket
    private static PrintWriter outS = null;
    private static BufferedReader inS = null;
        // HW socket
    private static PrintWriter outHW = null;
    private static BufferedReader inHW = null;
        // LW socket
    private static PrintWriter outLW[] = new PrintWriter[NUM_LIGHTWEIGHTS];
    private static BufferedReader inLW[] = new BufferedReader[NUM_LIGHTWEIGHTS];

    private static LinkedList<String> cuaLW = new LinkedList<>();

    private static void CreateServer(){
        try {
            // Connect to HW
            System.out.print("Creating server socket...");
            serverSocket = new ServerSocket(PORT_HWA);
            System.out.println("Done!");
            serverAccepter = serverSocket.accept(); //establishes connection
            inS = new BufferedReader(new InputStreamReader(serverAccepter.getInputStream()));
            outS = new PrintWriter(serverAccepter.getOutputStream(), true);
            System.out.println("Connection recieved");

            // Connect to all LW
            System.out.println("Creating lightweight sockets...");
            lightweights = new ArrayList<>();
            for(int i =0; i<NUM_LIGHTWEIGHTS;i++){
                System.out.print("Waiting for lightweight "+ (i+1) + "...");
                lightweights.add(serverSocket.accept());
                outLW[i]= new PrintWriter(lightweights.get(i).getOutputStream(), true);
                inLW[i] = new BufferedReader(new InputStreamReader(lightweights.get(i).getInputStream()));
                outLW[i].println((i+1));
                System.out.println("Done!");
            }
            System.out.println("Done!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateSockets(){
        try {
            // Connect to HW
            System.out.print("Creating connexion to heavyweight B...");
            heavyWeight = new Socket("127.0.0.1", PORT_HWB);
            inHW = new BufferedReader(new InputStreamReader(heavyWeight.getInputStream()));
            outHW = new PrintWriter(heavyWeight.getOutputStream(), true);
            System.out.println("Done!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        token = null;
        try {
            generateSockets();
            CreateServer();
            System.out.println("Config done!");
            while(true){
                System.out.println("Listening...");
                while(token == null) listenHeavyweight(inS);
                for (int i=0; i<NUM_LIGHTWEIGHTS; i++)
                    sendActionToLightweight(outLW[i]);
                answersfromLightweigth=0; // For innutilitza el answers
                for (int i=0; i < NUM_LIGHTWEIGHTS; i++)
                    listenLightweight(inLW[i]);
                token = null;
                sendTokenToHeavyweight(outHW);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void listenHeavyweight(BufferedReader in) throws IOException {
        String msg = in.readLine();

        if (msg.equalsIgnoreCase("TOKEN")){
            token = "TOKEN";
        }
        else System.out.println("HW ->" + msg);
    }

    private static void sendTokenToHeavyweight(PrintWriter out) {
        out.println("TOKEN");
    }

    private static void listenLightweight(BufferedReader in) throws IOException {
        String msg = in.readLine();

        if (msg.equalsIgnoreCase("TOKEN")){
            answersfromLightweigth++;
        }
        else System.out.println("HW ->" + msg);
    }

    private static void sendActionToLightweight(PrintWriter out) {
        out.println("TOKEN");
    }

}
