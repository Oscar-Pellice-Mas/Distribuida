package S5.Core_Layer;

import S2.Lightweight.LightweightB;
import S5.Utils.GenericServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CoreLayer extends GenericServer {
    //Server Information
    private  int                         myId;
    //Client information
    private  ArrayList<Socket>           socketList;
    private  ArrayList<PrintWriter>      outLWList;
    private  ArrayList<BufferedReader>   inLWLIST;
    private  ArrayList<ClientConnection> clients;
    //Core Layer Server information
    private  ArrayList<Socket>           coreLayers;
    private  ArrayList<PrintWriter>      coreOutS;
    private  ArrayList<BufferedReader>   coreInS;
    //Layer 1 Server information
    private  Socket                      L1Socket;
    private  PrintWriter                 L1OutS;
    private  BufferedReader              L1InS;

    //Data information
    HashMap<Integer,Integer>             database;

    public CoreLayer() {
        socketList  = new ArrayList<Socket>();
        outLWList   = new ArrayList<PrintWriter>();
        inLWLIST    = new ArrayList<BufferedReader>();
        clients     = new ArrayList<ClientConnection>();
        coreLayers  = new ArrayList<Socket>();
        coreOutS    = new ArrayList<PrintWriter>();
        coreInS     = new ArrayList<BufferedReader>();
        database    = new HashMap<Integer,Integer>();
    }

    private void connectToServer(){
        System.out.println(ANSI_GREEN+"Waiting for other Core Layer Servers to connect...");
        for (int i = 0; i < NUM_CORE_LAYER; i++) {
            try{
                if (i != myId){ // To not connect with the owned id
                    Socket aux = new Socket(LOCALHOST,PORT_CORE+i);
                    socketList.add(aux);
                    inLWLIST.add( new BufferedReader(new InputStreamReader(aux.getInputStream())));
                    outLWList.add(  new PrintWriter(aux.getOutputStream(), true));
                    outLWList.get(outLWList.size() - 1).println("Core-"+myId);
                }
            }catch (IOException e){
                try {
                    sleep(3000);
                    System.out.println(ANSI_CYAN+"There has been an error with Server "+ i + ".We will try again");
                    i--;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        System.out.println(ANSI_YELLOW+"Done!");
        //COnnectem a la layer següent
        if (myId!=1){
            //Connectem a B1 o B2
            while (true) {
                try {
                    L1Socket = new Socket(LOCALHOST, PORT_L1 + (myId == 2 ? 1 : 2));
                    L1InS = new BufferedReader(new InputStreamReader(L1Socket.getInputStream()));
                    L1OutS = new PrintWriter(L1Socket.getOutputStream(), true);
                    break;
                }catch (IOException e){
                        System.out.println(ANSI_CYAN+"There has been an error connecting to Layer 1. We will try again");
                }
            }
        }


    }

    public void work(int id){
        myId = id;
        //Obrim el servidor
        CreateServer(PORT_CORE+myId);
        //Obrim un thread per escoltar els clients que entrin
        new Thread(
                () -> {
                    String buffer;
                    while(true){
                        try {
                            waitForClient();
                            socketList.add(serverAccepter);
                            outLWList.add(outS);
                            inLWLIST.add(inS);
                            buffer = inS.readLine();
                            switch (buffer.split("-")[0].toLowerCase()) {
                                case "client" -> {
                                    ClientConnection c = new ClientConnection(inS, outS, this);
                                    c.start();
                                    clients.add(c);
                                }
                                case "core" -> {
                                    coreLayers.add(serverAccepter);
                                    coreOutS.add(outS);
                                    coreInS.add(inS);
                                    //TODO: Layer connection
                                    System.out.println(ANSI_YELLOW+"S'ha connectat un server Core");
                                }

                                case "layer1" -> {
                                    //FIXME: Creo que desde la Layer 1 nunca se conectarán hacia arriba
                                    L1Socket = serverAccepter;
                                    L1OutS = outS;
                                    L1InS = inS;
                                    //TODO: Layer connection
                                }

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).start();
        //Connectem a servers de la layer
        connectToServer();
        mainFunction();
    }

    private void mainFunction() {
        //TODO: Una funció principal que fagi la fumada de la repliació
    }

    private int getValue(int layer, int position){
        return 0;
    }

    private void replaceValue(int position, int value){
        if (database.containsKey(position)){
            database.replace(position,value);
        }else{
            database.put(position,value);
        }
    }

    class ClientConnection extends Thread{
        private BufferedReader inS;
        private PrintWriter outS;
        private CoreLayer instance;

        public ClientConnection(BufferedReader inS, PrintWriter outS, CoreLayer inst) {
            this.inS = inS;
            this.outS = outS;
            instance=inst;
        }

        @Override
        public synchronized void run() {
            String[] transaction;
            String buffer;
            int layer;
            while (true){
                try {
                    buffer = inS.readLine();
                    if (buffer.equals("end")) break;
                    layer = Integer.parseInt(buffer);
                    while (true){
                        buffer = inS.readLine();
                        if (buffer.equals("transactionend")) break;
                        transaction = buffer.split("-");

                        if (transaction[0].equals("r")){
                            //TODO: Que se puedan hacer reads de otras layers
                            outS.println(instance.getValue(layer, Integer.parseInt(transaction[1])));
                        }else{
                            instance.replaceValue(Integer.parseInt(transaction[1]),Integer.parseInt(transaction[2]));
                            outS.println("ack");
                            //TODO: Actualizar toda la coreLayer Aquí???
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}

class mainCore {
    public static void main(String[] args) {
        CoreLayer core = new CoreLayer();

        core.work(Integer.parseInt(args[0]));
    }
}