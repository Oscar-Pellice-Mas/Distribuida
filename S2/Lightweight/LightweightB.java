package S2.Lightweight;

import S2.Utils.GenericServer;
import S2.Utils.RicAndAgr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LightweightB extends GenericServer {
    // Number of lightweights
    private final int NUM_LIGHTWEIGHTS = 2;

    // Socket information
    private static Socket socket;
    private static PrintWriter outHW;
    private static BufferedReader inHW;

    // LW ID
    static int myID;

    // Lightweights information
    private  List<LightweightB.Canal> serverCanalList; // Listening channels
    private  List<Socket> socketList;
    private  List<PrintWriter> outLWList;
    private  List<BufferedReader> inLWLIST;

    // Initialization
    private boolean serverDone =false;
    protected RicAndAgr ra;

    // Connexion to Heavyweight
    private static void ConnectHeavyweights(){
        try {
            System.out.print(ANSI_GREEN+"Creating socket to heavyweight...");
            socket = new Socket("127.0.0.1",PORT_HWB);
            inHW = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outHW = new PrintWriter(socket.getOutputStream(), true);
            myID = Integer.parseInt(inHW.readLine());
            System.out.println(ANSI_YELLOW+"Done!");
            System.out.println(ANSI_BLUE+"My id is:"+ myID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Connexion to Lightweight
    private void ConnectLightweights(){
        System.out.println(ANSI_GREEN+"Waiting for other lightweights to connect...");

        for (int i = 1; i <= NUM_LIGHTWEIGHTS; i++) {
            try{
                if (i != myID){ // To not connect with the owned id
                    Socket aux = new Socket(LOCALHOST,STARTING_PORT_LWB + i);
                    socketList.add(aux);
                    inLWLIST.add( new BufferedReader(new InputStreamReader(aux.getInputStream())));
                    outLWList.add(  new PrintWriter(aux.getOutputStream(), true));
                    outLWList.get(outLWList.size() - 1).println(myID);
                }
            }catch (IOException e){
                try {
                    sleep(3000);
                    System.out.println(ANSI_CYAN+"There has been an error with LW "+ i + " we will try again");
                    i--;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        System.out.println(ANSI_YELLOW+"Done!");
    }

    // UTIL?
    private void rearrangeChanneloutStoNextChannelinS(){
        int j=0;
        for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
            //Mirem si la id es igual a la nostra
            if (i != myID-1){
                PrintWriter aux =outLWList.get(j);
                serverCanalList.get(i).setOutS(aux);
                j++;
            }
        }
    }

    // Socket creation
    private  void crearSockets() {

        socketList = new ArrayList<>();
        outLWList = new ArrayList<>();
        inLWLIST = new ArrayList<>();
        serverCanalList = new ArrayList<>();

        System.out.print(ANSI_GREEN+"Creant server socket...");
        CreateServer(STARTING_PORT_LWB+myID);
        System.out.println(ANSI_YELLOW+"Done!");

        System.out.println(ANSI_GREEN+"Running connexion threads...");
        //Anonymous thread to receive connexions
        new Thread(
                () -> {
                    int id;
                    for (int i = 0; i < NUM_LIGHTWEIGHTS; i++){serverCanalList.add(new Canal());}
                    for (int i = 0; i < NUM_LIGHTWEIGHTS; i++){
                        // Register all the connexion with the id
                        try {
                            if (i != myID-1) {
                                //Wait to receive ID
                                waitForClient();
                                id = Integer.parseInt(inS.readLine());
                                serverCanalList.set(id-1, new Canal(id, inS, outS));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println(ANSI_CYAN+"Al LW connexions ready!");
                    serverDone=true;
                }
        ).start();

        // Try to connect to other servers meanwhile
        ConnectLightweights();
        while(!serverDone){
            //Si no fico això el tercer LW es queda al bucle
            System.out.print(""); // Print necesari o només espera activa? Possible colisio de dades per actualitzar desde el thread?
        }

        rearrangeChanneloutStoNextChannelinS();

        System.out.println(ANSI_GREEN + "Activating connexion with lightweights...");
        for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
            if (i != myID-1){
                serverCanalList.get(i).start();
            }
        }
        System.out.println(ANSI_YELLOW + "Done!");
    }

    // Main function
    public void mainFunction(String[] args) throws InterruptedException {
        ConnectHeavyweights();
        ra = new RicAndAgr(myID);
        crearSockets();
        while (true){
            waitHeavyWeight();
            ra.requestCS(outLWList);
            for (int i=0; i<10; i++){
                System.out.println(ANSI_YELLOW+"("+i+") Sóc el procés lightweight: "+ myID);
                espera1Segon();
            }
            ra.releaseCS(outLWList);
            notifyHeavyWeight();
        }
    }

    private static void waitHeavyWeight() {
        String msg;
        try {
            msg = inHW.readLine();
            if (msg.equalsIgnoreCase("TOKEN")){
                System.out.println("Token received form HW");}
            else System.out.println("HW ->" + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void notifyHeavyWeight() {
        outHW.println("TOKEN");
        System.out.println("HW notified");
    }

    private static void espera1Segon() {
        try
        {
            Thread.sleep(1000);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    public class Canal extends Thread {
        private int id=0;
        private BufferedReader inS;
        private PrintWriter outS;

        public Canal(int id, BufferedReader inS, PrintWriter outS){
            this.id = id;
            this.inS = inS;
            this.outS = outS;
        }

        public Canal() {

        }

        @Override
        public synchronized void run() {
            try { // Only reading mode, writing form main thread
                while (true){
                    String command = inS.readLine();
                    System.out.println(ANSI_BLUE+"Missatge: "+ANSI_CYAN+command);
                    ra.handleMSG(outS,command, id);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setOutS(PrintWriter outS) {
            this.outS = outS;
        }
    }
}

class MainLWB{
    public static void main(String[] args) {
        LightweightB LWB = new LightweightB();
        try {
            LWB.mainFunction(args);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}