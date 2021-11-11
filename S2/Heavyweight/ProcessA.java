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


    private int answersfromLightweigth;
    private String token;

    private ArrayList<Socket> lightweights;
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
        // Connect to HW
        System.out.print(ANSI_GREEN+"Creating server socket...");
        super.CreateServer(PORT_HWA);
        System.out.println(ANSI_YELLOW+"Done!");
        new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            System.out.print(ANSI_GREEN+"Waiting process B...");
                            waitForClient();
                            System.out.println(ANSI_YELLOW+"Process B connected to server!");

                            System.out.println(ANSI_GREEN+"Creating lightweight sockets...");
                            lightweights = new ArrayList<Socket>();
                            for(int i =0; i<NUM_LIGHTWEIGHTS;i++){
                                System.out.println(ANSI_GREEN+"Waiting for lightweight "+ (i+1) + "...");
                                lightweights.add(serverSocket.accept());
                                outLW[i]= new PrintWriter(lightweights.get(i).getOutputStream(), true);
                                inLW[i] = new BufferedReader(new InputStreamReader(lightweights.get(i).getInputStream()));
                                outLW[i].println((i+1));
                                System.out.println(ANSI_YELLOW+"lightweight "+(i+1)+" connected to server!");
                            }
                            System.out.println(ANSI_YELLOW+"LWAs connected!");
                            startWorking();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).start();

    }

    private void generateSockets() {
        boolean connectionEstablished = false;
        // Connect to HW
        try {
            System.out.println(ANSI_GREEN + "Creating connexion to heavyweight B...");
            while (!connectionEstablished) {
                try {
                    heavyWeight = new Socket("127.0.0.1", PORT_HWB);
                    connectionEstablished = true;
                } catch (IOException e) {
                    System.out.println(ANSI_GREEN + "...");
                    sleep(3000);
                }
            }

            inHW = new BufferedReader(new InputStreamReader(heavyWeight.getInputStream()));
            outHW = new PrintWriter(heavyWeight.getOutputStream(), true);
            System.out.println(ANSI_YELLOW + "Connected to HWB!");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void mainFunction(String[] args) {
        token = null;
        try {
            CreateServer();
            generateSockets();

            synchronized (this){
                wait();
                System.out.println(ANSI_BLUE+"Config done!");
            }
            while(true){
                System.out.println(ANSI_GREEN+"Listening...");

                while(token == null) listenHeavyweight(inS);
                /*
                for (int i=0; i<NUM_LIGHTWEIGHTS; i++)
                    sendActionToLightweight(outLW[i]);
                answersfromLightweigth=0; // For innutilitza el answers
                for (int i=0; i < NUM_LIGHTWEIGHTS; i++)
                    listenLightweight(inLW[i]);
                 */
                Thread.sleep(1000);
                token = null;
                sendTokenToHeavyweight(outHW);
                System.out.println(ANSI_CYAN+"Token enviat");
            }
        } catch (IOException | InterruptedException e) {
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
    private void startWorking(){
        synchronized (this){
            this.notify();
        }
    }
}

class MainHWA{
    public static void main(String[] args) {
        ProcessA HWA = new ProcessA();
        HWA.mainFunction(args);
    }
}