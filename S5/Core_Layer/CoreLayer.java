package S5.Core_Layer;

import S5.Utils.Data;
import S5.Utils.GenericServer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class CoreLayer extends GenericServer {
    //Server Information
    private  int                         myId;
    private  int                         updates;
    //Data information
    private Data                         database;
    //Client information
    private  ArrayList<Socket>           clientSocketList;
    private  ArrayList<PrintWriter>      clientOutList;
    private  ArrayList<BufferedReader>   clientInList;
    private  ArrayList<ClientConnection> clients;
    //Core Layer Server information
    private  ArrayList<Socket>           coreLayers;
    private  ArrayList<PrintWriter>      coreOutS;
    private  ArrayList<BufferedReader>   coreInS;
    //Core Layer Client information
    private  ArrayList<Socket>           coreLayersClient;
    private  ArrayList<PrintWriter>      coreOutSClient;
    private  ArrayList<BufferedReader>   coreInSClient;
    //Layer 1 Server information
    private  Socket                      L1Socket;
    private  PrintWriter                 L1OutS;
    private  BufferedReader              L1InS;
    private  ObjectOutputStream          L1OutOs;
    private  ObjectInputStream           L1InOs;


    public CoreLayer() {
        clientSocketList  = new ArrayList<Socket>();
        clientOutList     = new ArrayList<PrintWriter>();
        clientInList      = new ArrayList<BufferedReader>();
        clients           = new ArrayList<ClientConnection>();
        coreLayers        = new ArrayList<Socket>();
        coreOutS          = new ArrayList<PrintWriter>();
        coreInS           = new ArrayList<BufferedReader>();
        coreLayersClient  = new ArrayList<Socket>();
        coreOutSClient    = new ArrayList<PrintWriter>();
        coreInSClient     = new ArrayList<BufferedReader>();
        database          = new Data();
    }
    void coreLayerConnection(int i) throws IOException {
        while (true) {
            String buffer = coreInS.get(i).readLine();
            if (buffer.charAt(0)=='w'){
                String[] transaction = buffer.split("-");
                replaceValue(Integer.parseInt(transaction[1]),Integer.parseInt(transaction[2]));
                coreOutS.get(i).println("ack");
                synchronized (this){
                    updates++;// Incrementem les updates
                }
            }else{
                int value = getValue(Integer.parseInt(buffer.split("-")[1]),Integer.parseInt(buffer.split("-")[2]));
                coreOutS.get(i).println(value);
            }
        }
    }
    private void connectToServer(){
        System.out.println(ANSI_GREEN+"Waiting for other Core Layer Servers to connect...");
        for (int i = 0; i < NUM_CORE_LAYER; i++) {
            try{
                if (i != myId){ // To not connect with the owned id
                    Socket aux = new Socket(LOCALHOST,PORT_CORE+i);
                    coreLayersClient.add(aux);
                    coreInSClient.add( new BufferedReader(new InputStreamReader(aux.getInputStream())));
                    coreOutSClient.add(  new PrintWriter(aux.getOutputStream(), true));
                    coreOutSClient.get(coreOutSClient.size() - 1).println("Core-"+myId);
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
                    L1Socket = new Socket(LOCALHOST, PORT_L1 + (myId == 2 ? 0 : 1));
                    L1InS = new BufferedReader(new InputStreamReader(L1Socket.getInputStream()));
                    L1OutS = new PrintWriter(L1Socket.getOutputStream(), true);
                    OutputStream os =   L1Socket.getOutputStream();
                    L1OutOs =           new ObjectOutputStream(os);
                    InputStream is =    L1Socket.getInputStream();
                    L1InOs =            new ObjectInputStream(is);
                    System.out.println(ANSI_YELLOW+"Connectat al Layer 1!");
                    break;
                }catch (IOException e){
                        System.out.println(ANSI_CYAN+"There has been an error connecting to Layer 1. We will try again");
                }
            }
        }
    }
    private void serverFunction(){
        String buffer;
        while(true){
            try {
                waitForClient();
                clientSocketList.add(serverAccepter);
                clientOutList.add(outS);
                clientInList.add(inS);
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
                        //Layer connection, start
                        new Thread(
                                () ->{
                                    try {
                                        coreLayerConnection(coreLayers.size()-1);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                        ).start();
                        System.out.println(ANSI_YELLOW+"S'ha connectat un server Core");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void work(int id) throws InterruptedException, IOException {
        myId = id;
        //Obrim el servidor
        CreateServer(PORT_CORE+myId);
        //Obrim un thread per escoltar els clients que entrin
        new Thread(
                this::serverFunction
        ).start();
        //Connectem a servers de la layer
        connectToServer();
        mainFunction();
    }

    private void mainFunction() throws InterruptedException, IOException {
        //TODO: Una funció principal que fagi la fumada de la repliació
        while (true){
            synchronized (this){
                if (updates >= 10 ){
                    updates   = 0;
                    if (myId != 1) {
                        L1OutOs.writeObject(database);
                        L1OutOs.reset(); //Resetejem per a que envii el objecte modificat
                        System.out.println(ANSI_YELLOW+"Update sent to L1");
                    }
                    System.out.println(ANSI_BLUE+"10 updates!");
                }

                //System.out.println(ANSI_GREEN+"Num of updates: " + updates);
            }
            sleep(4000);
        }
    }

    private int getValue(int layer, int position){
        //TODO: Que se puedan hacer reads layers 2
        if (layer == 0){
            if (database.getDatabase().containsKey(position)){
                database.getDatabase().get(position);
            }
        }else{
            try {
                if (myId != 1){
                L1OutS.println("r-"+position);
                    int value = Integer.parseInt(L1InS.readLine());
                    L1OutS.println("ack");
                    return value;
                }else{
                    coreOutSClient.get(0).println("r-"+layer+"-"+position);
                    int value = Integer.parseInt(coreInSClient.get(0).readLine());
                    coreOutSClient.get(0).println("ack");
                    return value;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private void replaceValue(int position, int value){
        if (database.getDatabase().containsKey(position)){
            database.getDatabase().replace(position,value);
        }else{
            database.getDatabase().put(position,value);
        }
    }

    class ClientConnection extends Thread{
        private BufferedReader  inS;
        private PrintWriter     outS;
        private CoreLayer       instance;

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
                            outS.println(instance.getValue(layer, Integer.parseInt(transaction[1])));
                        }else{
                            instance.replaceValue(Integer.parseInt(transaction[1]),Integer.parseInt(transaction[2]));
                            //Actualizar toda la coreLayer Aquí. EAGER ACTIVE REPLICATION + UPDATE EVERYWHERE.
                            for (int i = 0; i < coreOutSClient.size(); i++){
                                    coreOutSClient.get(i).println(buffer);
                            }
                            //Esperem a rebre tots els ack
                            for (int i = 0; i < coreInSClient.size(); i++){
                                    buffer = coreInSClient.get(i).readLine();
                                    if (!buffer.equals("ack")) i--;
                            }
                            //Fer el ack al client
                            outS.println("ack");
                            synchronized (this){
                                updates++;// Incrementem les updates
                            }
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

        try {
            core.work(Integer.parseInt(args[0]));
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}