package S2.Lightweight;

import S2.Utils.Lamport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class LightweightA {
    private static final int PORT_HWA = 5000;
    private static final int NUM_LIGHTWEIGHTS = 3;

    private static Lamport lamport;
    private static int myID;
    // Heavyweight
    private static Socket socketHW;
    private static PrintWriter outHW;
    private static BufferedReader inHW;

    // Lightweights
    private static List<Canal> serverCanalList;
    private static List<Socket> serverSocketList;

    // INICIALITZACIÓ

    private static void connectarHW(){
        try {
            socketHW = new Socket("127.0.0.1",PORT_HWA);
            inHW = new BufferedReader(new InputStreamReader(socketHW.getInputStream()));
            outHW = new PrintWriter(socketHW.getOutputStream(), true);
            myID = Integer.parseInt(inHW.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void crearSockets() {
        try {
            serverSocketList = new ArrayList<>();
            for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
                if (i == myID) continue;
                serverSocketList.add(new Socket("127.0.0.1", PORT_HWA + i + 1));
            }

            serverCanalList = new ArrayList<>();
            for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
                if (i == myID) continue;
                Canal aux = new Canal(i);
                aux.start();
                serverCanalList.add(aux);
            }
        } catch (IOException e) {
                e.printStackTrace();
        }
    }

    // EXECUCIO

    public static void main(String[] args) {
        connectarHW();
        crearSockets();
        lamport = new Lamport(myID);
        while (true){
            waitHeavyWeight();
            for (int i = 0; i < NUM_LIGHTWEIGHTS; i++) {
                lamport.requestCS(serverSocketList.get(i));
            }
            for (int i=0; i<10; i++){
                System.out.println("Sóc el procés lightweight: "+ myID+"\n");
                espera1Segon();
            }
            lamport.releaseCS(outHW);
            notifyHeavyWeight();
        }
    }

    private static void notifyHeavyWeight() {
        outHW.println("TOKEN");
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

    private static void waitHeavyWeight() {
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

    private static class Canal extends Thread {
        private final int id;

        private BufferedReader inS;
        private PrintWriter outS;
        public Canal(int id){
            this.id = id;
        }

        @Override
        public synchronized void start() {
            super.start();
            ServerSocket serverSocket;
            try { // Nomes fer lectura, escritura desde thread principal
                serverSocket = new ServerSocket(PORT_HWA + myID + 1);
                Socket serverAccepter = serverSocket.accept();//establishes connection
                inS = new BufferedReader(new InputStreamReader(serverAccepter.getInputStream()));
                outS = new PrintWriter(serverAccepter.getOutputStream(), true);
                System.out.println("Conecta al lightweight " + id);
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
