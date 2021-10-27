package S2.Heavyweight;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

public class ProcessB {
    private static final int NUM_LIGHTWEIGHTS = 0;
    //private static final int NUM_LIGHTWEIGHTS = 2;
    private static final int PORT_HWA = 5000;
    private static final int PORT_HWB = 6000;
    private static final int STARTING_PORT_LWB = 6001;

    static ServerSocket serverSocket= null;
    static Socket serverAccepter= null;


    private static int answersfromLightweigth;
    private static String token;
    private static Socket lightweights[] = new Socket[NUM_LIGHTWEIGHTS];
    private static Socket heavyWeight_A = null;

    private static PrintWriter outS = null;
    private static BufferedReader inS = null;
    private static PrintWriter outHW = null;
    private static BufferedReader inHW = null;
    private static PrintWriter outLW[] = new PrintWriter[NUM_LIGHTWEIGHTS];
    private static BufferedReader inLW[] = new BufferedReader[NUM_LIGHTWEIGHTS];


    private static LinkedList<String> cuaLW = new LinkedList<String>();

    private static void CreateServer(){
        try {
            System.out.print("Create server socket...");
            serverSocket = new ServerSocket(PORT_HWB);
            System.out.println("Done!");
            System.out.print("Waiting process A...");
            serverAccepter=serverSocket.accept();//establishes connection
            inS = new BufferedReader(new InputStreamReader(serverAccepter.getInputStream()));
            outS = new PrintWriter(serverAccepter.getOutputStream(), true);
            System.out.println("Done!");
            /*lightweights = new Socket[NUM_LIGHTWEIGHTS];
            for(int i =0; i<NUM_LIGHTWEIGHTS;i++){
                lightweights[i] = serverSocket.accept();
                outLW[i]= new PrintWriter(lightweights[i].getOutputStream(), true);
                inLW[i] = new BufferedReader(new InputStreamReader(lightweights[i].getInputStream()));
                outLW[i].println(Integer.toString(i));
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateSockets(){
        try {
            System.out.print("Creating connexion to heavyweight A...");
            heavyWeight_A = new Socket("127.0.0.1", PORT_HWA);
            inHW = new BufferedReader(new InputStreamReader(heavyWeight_A.getInputStream()));
            outHW = new PrintWriter(heavyWeight_A.getOutputStream(), true);
            System.out.println("Done!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //Creació dels socket cap el ligthweight
        token="TOKEN";
        Scanner scanner = new Scanner(System.in);
        try {
            CreateServer();
            System.out.println("- Server A ready? (press any button to continue)");
            scanner.next();
            generateSockets();
            while (true) {
                System.out.println("Waiting to start... (Press to coninue)");
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
                System.out.println("Token enviat");
            }
        } catch(IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

    private static void sendTokenToHeavyweight(PrintWriter out) {
        out.println("TOKEN");
        token = null;
    }

    private static void listenHeavyweight(BufferedReader in) throws IOException {
        String msg = in.readLine();
        if (msg.equalsIgnoreCase("TOKEN")){
            token = "TOKEN";
            System.out.println("Sóc el Process B i puc parlar.");
        }
        else System.out.println(msg);
    }

    private static void listenLightweight(BufferedReader in) throws IOException {
        String msg = in.readLine(); //estructura: <request/okay> <ID> <timestamp>

        cuaLW.addFirst(msg);
        answersfromLightweigth++;
        /*
        if (msg.equalsIgnoreCase("TOKEN")){
            token = "TOKEN";
            answersfromLightweigth++;
        }
        else System.out.println(msg);*/
    }

    private static void sendActionToLightweight(PrintWriter out) {
        for (int i=0; i < NUM_LIGHTWEIGHTS; i++){
            out.println(cuaLW.get(i));
        }
    }


}
