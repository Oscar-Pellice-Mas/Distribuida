package S2.Lightweight;

import S2.Heavyweight.ProcessA;
import S2.Utils.GenericServer;
import S2.Utils.Lamport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LightweightA extends GenericServer {
    private LightweightA instance;
    private final int PORT_HWA = 5000;
    private final int NUM_LIGHTWEIGHTS = 3;

    private Lamport lamport;
    private int myID;
    // Heavyweight
    private  Socket socketHW;
    private  PrintWriter outHW;
    private  BufferedReader inHW;

    // Lightweights
    private  List<Canal> serverCanalList; //Canals per escoltar
    private  List<Socket> socketList; //Sockets de sortida
    private  List<PrintWriter> outLWList; //outLW de sortida
    private  List<BufferedReader> inLWLIST; //inLW de sortida


    // INICIALITZACIÓ


    public LightweightA() {
        this.instance = this;
    }

    private  void connectarHW(){
        try {
            System.out.print("Creant socket a heavyweight...");
            socketHW = new Socket("127.0.0.1",PORT_HWA);
            inHW = new BufferedReader(new InputStreamReader(socketHW.getInputStream()));
            outHW = new PrintWriter(socketHW.getOutputStream(), true);
            myID = Integer.parseInt(inHW.readLine());
            System.out.println("Done!");
            System.out.println("My id is:"+ myID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ConnectLightweights(){
        Scanner scanner = new Scanner(System.in);
        System.out.println(ANSI_GREEN+"Waiting other lightweights... (Press ENTER to continue)");
        scanner.next();
        for (int i = 0; i < NUM_LIGHTWEIGHTS-1; i++) {
            try{
                Socket aux = new Socket(LOCALHOST,STARTING_PORT_LWA+i);
                socketList.add(aux);
                inLWLIST.add( new BufferedReader(new InputStreamReader(aux.getInputStream())));
                outLWList.add(  new PrintWriter(aux.getOutputStream(), true));
                outLWList.get(outLWList.size()-1).println(myID);
            }catch (IOException e){
                try {
                    sleep(3000);
                    i--;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        System.out.println(ANSI_YELLOW+"Done!");

    }
    private  void crearSockets() throws InterruptedException {
        //TODO: Usar excepciones para conectarnos. Intentamos conectarnos con el socket, si no podemos dará exception y hacemos accept
        socketList = new ArrayList<>();
        serverCanalList = new ArrayList<>();

        System.out.print(ANSI_GREEN+"Creant server socket...");
        CreateServer(PORT_HWA+myID);
        System.out.println(ANSI_YELLOW+"Done!");

        System.out.print(ANSI_GREEN+"Tirant thread per escoltar LW...");
        //Thread anònim per rebre les connexions
        new Thread(
                new Runnable() {
                    public void run() {

                        for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) serverCanalList.add(new Canal());
                        for (int i = 0; i < NUM_LIGHTWEIGHTS; i++){
                            waitForClient();
                            // Rebre quin server es i guardar al seu lloc
                            try {
                                //Rebem la id
                                int id = Integer.parseInt(inS.readLine());
                                //El guardem al lloc que li pertoca a la llista
                                serverCanalList.set(id, new Canal(id, serverAccepter ,inS, outS));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println(ANSI_CYAN+"S'han connectat tots els LW!");
                        instance.notify();
                    }
                }
        ).start();

        //Anem intenant connectar-nos de mentres
        ConnectLightweights();
        wait(); //TODO: No sé si el connect lightweights acabará abans que el thread o no. S'ha de mirar.
        System.out.print(ANSI_GREEN+"Corrent Threads de canals lightweight...");
        for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
            if (i == myID) continue;
            serverCanalList.get(i).start();
        }
        System.out.println(ANSI_YELLOW+"Done!");
    }

    // EXECUCIO

    public void mainFunction(String[] args) throws InterruptedException {
        connectarHW();
        crearSockets();
        lamport = new Lamport(myID);
        while (true){
            waitHeavyWeight();
            for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
                lamport.requestCS(serverCanalList.get(i).outS);
            }
            for (int i=0; i<10; i++){
                System.out.println("Sóc el procés lightweight: "+ myID+"\n");
                espera1Segon();
            }
            lamport.releaseCS(outHW);
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
            if (msg.equalsIgnoreCase("TOKEN")){
                System.out.println("Token recieved");}
            else System.out.println("HW ->" + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Canal extends Thread {
        private int id=0;

        private Socket serverAccepter;
        private BufferedReader inS;
        private PrintWriter outS;
        public Canal(int id, Socket serverAccepter, BufferedReader inS, PrintWriter outS){
            this.id = id;
            this.inS = inS;
            this.outS = outS;
            this.serverAccepter=serverAccepter;
        }

        public Canal() {

        }

        @Override
        public synchronized void start() {
            ServerSocket serverSocket;
            try { // Nomes fer lectura, escritura desde thread principal
                while (true){
                    String command = inS.readLine();
                    lamport.handleMSG(outS, command, id);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
class MainHWA{
    public static void main(String[] args) {
        LightweightA LWA = new LightweightA();
        try {
            LWA.mainFunction(args);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}