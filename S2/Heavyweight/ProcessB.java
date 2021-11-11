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
    //private static final int NUM_LIGHTWEIGHTS = 2;

    private  int answersfromLightweight;
    private  String token;
    private ArrayList<Socket> lightweights;
    private  Socket heavyWeight_A = null;

    private  PrintWriter outHW = null;
    private  BufferedReader inHW = null;
    private  PrintWriter outLW[] = new PrintWriter[NUM_LIGHTWEIGHTS];
    private  BufferedReader inLW[] = new BufferedReader[NUM_LIGHTWEIGHTS];


    private  LinkedList<String> cuaLW = new LinkedList<String>();

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


                                lightweights = new ArrayList<Socket>();
                                for(int i =0; i<NUM_LIGHTWEIGHTS;i++) {
                                    System.out.println(ANSI_GREEN+"Waiting for lightweight "+ (i+1) + "...");
                                    lightweights.add(serverSocket.accept());
                                    outLW[i] = new PrintWriter(lightweights.get(i).getOutputStream(), true);
                                    inLW[i] = new BufferedReader(new InputStreamReader(lightweights.get(i).getInputStream()));
                                    outLW[i].println(Integer.toString(i));
                                    System.out.println(ANSI_YELLOW+"lightweight "+(i+1)+" connected to server!");
                                }
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
        //Creació dels socket cap el ligthweight
        token="TOKEN";
        Scanner scanner = new Scanner(System.in);
        try {
            CreateServer();
            generateSockets();

            synchronized (this){
                wait();
                System.out.println(ANSI_BLUE+"Config done!");
            }
            while (true) {
                System.out.println(ANSI_GREEN+"Listening...");
                scanner.next();
                while(token == null) listenHeavyweight(inS);

                /*for (int i=0; i<NUM_LIGHTWEIGHTS; i++)
                    sendActionToLightweight(outLW[i]);
                //Netejem la cua
                cuaLW.removeAll(cuaLW);
                answersfromLightweigth=0;
                for (int i=0; answersfromLightweigth < NUM_LIGHTWEIGHTS; i++)
                    listenLightweight(inLW[i]);*/

                Thread.sleep(1000);
                sendTokenToHeavyweight(outHW);
                System.out.println(ANSI_CYAN+"Token enviat");
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
            System.out.println("Sóc el Process B i puc parlar.");
        }
        else System.out.println(msg);
    }

    private  void listenLightweight(BufferedReader in) throws IOException {
        String msg = in.readLine(); //estructura: <request/okay> <ID> <timestamp>

        cuaLW.addFirst(msg);
        answersfromLightweight++;
        /*
        if (msg.equalsIgnoreCase("TOKEN")){
            token = "TOKEN";
            answersfromLightweigth++;
        }
        else System.out.println(msg);*/
    }

    private  void sendActionToLightweight(PrintWriter out) {
        for (int i=0; i < NUM_LIGHTWEIGHTS; i++){
            out.println(cuaLW.get(i));
        }
    }
    private void startWorking(){
        this.notify();
    }
}
class MainHWB {
    public static void main(String[] args) {
        ProcessB HWB = new ProcessB();
        HWB.mainFunction(args);
    }
}