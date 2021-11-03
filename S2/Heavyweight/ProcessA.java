package S2.Heavyweight;

import S2.Utils.GenericServer;

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

public class ProcessA extends GenericServer {
    public ProcessA Instance = null;

    public ProcessA (){
        Instance = this;
    }

    private static final int NUM_LIGHTWEIGHTS = 3;


    static ServerSocket serverSocket= null;
    static Socket serverAccepter= null;

    private int answersfromLightweigth;
    private String token;

    private List<Socket> lightweights;
    private Socket heavyWeight = null;

        // Server socket - inS i outS ve del pare

        // HW socket
    private PrintWriter outHW = null;
    private BufferedReader inHW = null;
        // LW socket
    private PrintWriter outLW[] = new PrintWriter[NUM_LIGHTWEIGHTS];
    private BufferedReader inLW[] = new BufferedReader[NUM_LIGHTWEIGHTS];

    private LinkedList<String> cuaLW = new LinkedList<>();

    private void CreateServer(){
        try {
            // Connect to HW
            System.out.print(ANSI_GREEN+"Creating server socket...");
            CreateServer(PORT_HWA);
            System.out.println(ANSI_YELLOW+"Done!");
            waitForClient();
            System.out.println(ANSI_YELLOW+"Connection recieved");

            // Connect to all LW
            System.out.println(ANSI_GREEN+"Creating lightweight sockets...");
            lightweights = new ArrayList<>();
            for(int i =0; i<NUM_LIGHTWEIGHTS;i++){
                System.out.print(ANSI_GREEN+"Waiting for lightweight "+ (i+1) + "...");
                lightweights.add(serverSocket.accept());
                outLW[i]= new PrintWriter(lightweights.get(i).getOutputStream(), true);
                inLW[i] = new BufferedReader(new InputStreamReader(lightweights.get(i).getInputStream()));
                outLW[i].println((i+1));
                System.out.println(ANSI_YELLOW+"Done!");
            }
            System.out.println(ANSI_YELLOW+"Done!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateSockets(){
        try {
            // Connect to HW
            System.out.print(ANSI_GREEN+"Creating connexion to heavyweight B...");
            heavyWeight = new Socket("127.0.0.1", PORT_HWB);
            inHW = new BufferedReader(new InputStreamReader(heavyWeight.getInputStream()));
            outHW = new PrintWriter(heavyWeight.getOutputStream(), true);
            System.out.println(ANSI_YELLOW+"Done!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void mainFunction(String[] args) {
        token = null;
        try {
            generateSockets();
            CreateServer();
            System.out.println(ANSI_BLUE+"Config done!");
            while(true){
                System.out.println(ANSI_GREEN+"Listening...");
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

    private void listenHeavyweight(BufferedReader in) throws IOException {
        String msg = in.readLine();

        if (msg.equalsIgnoreCase("TOKEN")){
            token = "TOKEN";
        }
        else System.out.println("HW ->" + msg);
    }

    private void sendTokenToHeavyweight(PrintWriter out) {
        out.println("TOKEN");
    }

    private void listenLightweight(BufferedReader in) throws IOException {
        String msg = in.readLine();

        if (msg.equalsIgnoreCase("TOKEN")){
            answersfromLightweigth++;
        }
        else System.out.println("HW ->" + msg);
    }

    private void sendActionToLightweight(PrintWriter out) {
        out.println("TOKEN");
    }

}

class MainHWA{
    public static void main(String[] args) {
        ProcessA HWA = new ProcessA();
        HWA.mainFunction(args);
    }
}