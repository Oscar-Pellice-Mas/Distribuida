package S2.Heavyweight;

import S2.Lightweight.LightweightA;
import S2.Utils.GenericServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class ProcessB extends GenericServer  {
    private static final int NUM_LIGHTWEIGHTS = 2;

    private  String token;
    private ArrayList<Socket> lightweights;
    private  Socket heavyWeight_A = null;
    private int answersfromLightweigth = 0;

    private  PrintWriter outHW = null;
    private  BufferedReader inHW = null;
    private  PrintWriter outLW[] = new PrintWriter[NUM_LIGHTWEIGHTS];
    private  BufferedReader inLW[] = new BufferedReader[NUM_LIGHTWEIGHTS];

    private  LinkedList<String> cuaLW = new LinkedList<>(); // Useless

    private  void CreateServer(){
            System.out.print(ANSI_GREEN+"Create server socket...");
            super.CreateServer(PORT_HWB);
            System.out.println(ANSI_YELLOW+"Done!");

            new Thread(
                    new Runnable() {
                        public void run() {
                            try {
                                System.out.print(ANSI_GREEN+"Waiting process A...");
                                waitForClient();
                                System.out.println(ANSI_YELLOW+"Process A connected to server!");

                                lightweights = new ArrayList<>();
                                for(int i =0; i<NUM_LIGHTWEIGHTS;i++) {
                                    System.out.print(ANSI_GREEN+"Waiting for lightweight "+ (i+1) + "...");
                                    lightweights.add(serverSocket.accept());
                                    outLW[i] = new PrintWriter(lightweights.get(i).getOutputStream(), true);
                                    inLW[i] = new BufferedReader(new InputStreamReader(lightweights.get(i).getInputStream()));
                                    outLW[i].println(i+1);
                                    System.out.println(ANSI_YELLOW+" connected to server!");
                                }
                                System.out.println(ANSI_YELLOW+"LWBs connected!");
                                startWorking();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            ).start();
    }

    private  void generateSockets(){
        boolean connectionEstablished =false;
        try {
            System.out.print(ANSI_GREEN+"Creating connexion to heavyweight A...");
            while (!connectionEstablished){
                try{
                    heavyWeight_A = new Socket("127.0.0.1", PORT_HWA);
                    connectionEstablished=true;
                }catch (IOException e){
                    System.out.println(ANSI_GREEN+"...");
                    sleep(3000);
                }
            }
            inHW = new BufferedReader(new InputStreamReader(heavyWeight_A.getInputStream()));
            outHW = new PrintWriter(heavyWeight_A.getOutputStream(), true);
            System.out.println(ANSI_YELLOW + "connected to HWA!");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    void mainFunction(String[] args) {
        token="TOKEN";

        try {
            CreateServer();
            generateSockets();

            synchronized (this){
                wait();
                System.out.println(ANSI_BLUE+"Config done!");
            }
            System.out.println(ANSI_GREEN+"Listening...");
            while (true) {
                while(token == null) listenHeavyweight(inS);

                for (int i=0; i<NUM_LIGHTWEIGHTS; i++)
                    sendActionToLightweight(outLW[i]);
                System.out.println(ANSI_YELLOW + "Token sent to lightweights");
                answersfromLightweigth=0; // Useless???
                for (int i=0; answersfromLightweigth < NUM_LIGHTWEIGHTS; i++)
                    listenLightweight(inLW[i]);
                System.out.println(ANSI_YELLOW+ "Lightweights are done");

                Thread.sleep(1000);

                sendTokenToHeavyweight(outHW);
                System.out.println(ANSI_CYAN+"Token sent to HWA");
            }
        } catch(IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

    private  void sendTokenToHeavyweight(PrintWriter out) {
        out.println("TOKEN");
        token = null;
    }

    private  void listenHeavyweight(BufferedReader in) throws IOException {
        String msg = in.readLine();
        if (msg.equalsIgnoreCase("TOKEN")){
            token = "TOKEN";
            System.out.println("Token received");
        }
        else System.out.println(msg);
    }

    private  void listenLightweight(BufferedReader in) throws IOException {
        String msg = in.readLine(); //estructura: <request/okay> <ID> <timestamp> NO???

        if (msg.equalsIgnoreCase("TOKEN")){
            answersfromLightweigth++;
        }
        else System.out.println(msg);
    }

    private  void sendActionToLightweight(PrintWriter out) {
        for (int i=0; i < NUM_LIGHTWEIGHTS; i++){
            out.println("TOKEN");
        }
    }
    private void startWorking(){
        synchronized (this){
            this.notify();
        }
    }
}
class MainHWB {
    public static void main(String[] args) {
        ProcessB HWB = new ProcessB();
        HWB.mainFunction(args);
    }
}