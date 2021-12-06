package S2.Lightweight;

import S2.Utils.GenericServer;
import S2.Utils.Lamport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class LightweightA extends GenericServer {
    // Number of lightweights
    private LightweightA instance;
    private final int NUM_LIGHTWEIGHTS = 3;

    // Socket information

    // Heavyweight
    private  Socket socketHW;
    private  PrintWriter outHW;
    private  BufferedReader inHW;

    // Lightweights information
    private  List<Canal> serverCanalList; // Listening channels
    private  List<Socket> socketList;
    private  List<PrintWriter> outLWList;
    private  List<BufferedReader> inLWList;

    // LW ID
    private int myID;

    // Initialization
    private Lamport lamport;
    private boolean serverDone =false;

    public LightweightA() {
        this.instance = this;
    }

    // Connexion to Heavyweight
    private  void ConnectHeavyweights(){
        try {
            System.out.print(ANSI_GREEN+"Creating socket to heavyweight...");
            socketHW = new Socket("127.0.0.1",PORT_HWA);
            inHW = new BufferedReader(new InputStreamReader(socketHW.getInputStream()));
            outHW = new PrintWriter(socketHW.getOutputStream(), true);
            myID = Integer.parseInt(inHW.readLine());
            System.out.println(ANSI_YELLOW+"Done!");
            System.out.println(ANSI_BLUE+"My id is:"+ myID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Connexion to Lightweight
    private void ConnectLightweights(){
        System.out.println(ANSI_GREEN+"Waiting other lightweights...");

        for (int i = 1; i <= NUM_LIGHTWEIGHTS; i++) {
            try{
                if (i != myID){ // To not connect with the owned id
                    Socket aux = new Socket(LOCALHOST,STARTING_PORT_LWA+i);
                    socketList.add(aux);
                    inLWList.add( new BufferedReader(new InputStreamReader(aux.getInputStream())));
                    outLWList.add(  new PrintWriter(aux.getOutputStream(), true));
                    outLWList.get(outLWList.size()-1).println(myID);
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
        inLWList = new ArrayList<>();
        serverCanalList = new ArrayList<>();

        System.out.print(ANSI_GREEN+"Creant server socket...");
        CreateServer(STARTING_PORT_LWA+myID);
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
                            if (i != myID-1){
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
            System.out.print("");
        }

        rearrangeChanneloutStoNextChannelinS();

        System.out.println(ANSI_GREEN + "Activating connexion with lightweights...");
        for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
            if (i != myID-1){
                serverCanalList.get(i).start();
            }
        }
        System.out.println(ANSI_YELLOW+"Done!");
    }

    // Main function
    public void mainFunction(String[] args) throws InterruptedException, IOException {
        ConnectHeavyweights();
        lamport = new Lamport(myID);
        crearSockets();
        while (true){
            waitHeavyWeight();
            lamport.requestCS(outLWList, inLWList);
            for (int i=0; i<10; i++){
                System.out.println(ANSI_YELLOW+"("+i+") Sóc el procés lightweight: "+ myID);
                espera1Segon();
            }
            lamport.releaseCS(outLWList);
            notifyHeavyWeight();
        }
    }

    private void notifyHeavyWeight() {
        outHW.println("TOKEN");
    }

    private void espera1Segon() {
        try
        {
            Thread.sleep(1000);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    private void waitHeavyWeight() {
        String msg;
        try {
            msg = inHW.readLine();
            if (msg.equalsIgnoreCase("TOKEN")) {
                System.out.println("Token received from HW");
            }
            else System.out.println("HW ->" + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class Canal extends Thread {
        private int id=0;
        private BufferedReader inS;
        private PrintWriter outS;
        public Canal( int id, BufferedReader inS, PrintWriter outS){
            this.id = id;
            this.inS = inS;
            this.outS = outS;
        }

        public Canal() {

        }

        @Override
        public synchronized void run() {

            try { // Nomes fer lectura, escritura desde thread principal
                sleep(1000L *myID);
                while (true){
                    String command = inS.readLine();
                    System.out.println(ANSI_BLUE+"Missatge: "+ANSI_CYAN+command);
                    lamport.handleMSG(outS, command, id, instance);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void setOutS(PrintWriter outS) {
            this.outS = outS;
        }
    }
}
class MainLWA{
    public static void main(String[] args) {
        LightweightA LWA = new LightweightA();
        try {
            LWA.mainFunction(args);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}