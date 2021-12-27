package S5.Layer_1;

import S5.Core_Layer.CoreLayer;
import S5.Utils.Data;
import S5.Utils.GenericServer;
import S5.Utils.Util;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Layer_1 extends GenericServer {
    //Server Information
    private  int                          myId;
    //Data information
    private Data                          database;
    //Layer 1 Server information
    private Socket                        coreSocket;
    private PrintWriter                   coreOutS;
    private BufferedReader                coreInS;
    private ObjectOutputStream            coreOutOs;
    private ObjectInputStream             coreInOs;

    //Layer 2 Server information
    private ArrayList<Socket>             L2Sockets;
    private ArrayList<PrintWriter>        L2OutS;
    private ArrayList<BufferedReader>     L2InS;
    private ArrayList<ObjectOutputStream> L2OutOs;
    private ArrayList<ObjectInputStream>  L2InOs;




    public Layer_1() {
        L2Sockets =  new ArrayList<Socket>();
        L2OutS    =  new ArrayList<PrintWriter>();
        L2InS     =  new ArrayList<BufferedReader>();
        L2OutOs   =  new ArrayList<ObjectOutputStream>();
        L2InOs    =  new ArrayList<ObjectInputStream>();
        database  =  new Data();
    }
    private void getCoreLayer() {
        System.out.println(ANSI_GREEN+"Esperem a la connexió de Core Layer");
        waitForClient();
        try{
            coreSocket =        serverAccepter;
            coreOutS =          outS;
            coreInS =           inS;
            OutputStream os =   coreSocket.getOutputStream();
            coreOutOs =         new ObjectOutputStream(os);
            InputStream is =    coreSocket.getInputStream();
            coreInOs =          new ObjectInputStream(is);
        }catch (IOException e){
            System.out.println(ANSI_YELLOW+"Error en crear entrades i sortides");
        }
        System.out.println(ANSI_YELLOW+"connectat amb la Core Layer!");
    }
    private void connectToLayer2() {
        for (int i = 0; i < NUM_L2; i++){
            try {
                L2Sockets.add(new Socket(LOCALHOST, PORT_L2+i));
                L2InS.add(new BufferedReader(new InputStreamReader(L2Sockets.get(L2Sockets.size()-1).getInputStream())));
                L2OutS.add(new PrintWriter(L2Sockets.get(L2Sockets.size()-1).getOutputStream(), true));
                L2InOs.add(new ObjectInputStream(L2Sockets.get(L2Sockets.size()-1).getInputStream()));
                L2OutOs.add(new ObjectOutputStream(L2Sockets.get(L2Sockets.size()-1).getOutputStream()));
            }catch (IOException e){
                System.out.println(ANSI_CYAN+"There has been an error connecting to Layer 2. We will try again");
                i--;
            }
        }
    }

    public void work(int id){
        myId=id;
        CreateServer(PORT_L1+id);
        getCoreLayer();
        if (id == 1) connectToLayer2();
        mainFunction();
    }

    //FIXME: OLD
    private void listenCoreLayer() {
        while (true){
            try {
                System.out.println(ANSI_GREEN+"Llegint data");
                Data d = (Data) coreInOs.readObject();

                System.out.println(ANSI_GREEN+"Ha arribat data");
                synchronized (this){
                    database = d;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    private void listenReadsCoreLayer() {
        String buffer;
        while(true){
            try {
                buffer = coreInS.readLine();
                if (buffer.split("-")[0].equals("r")){
                    if(buffer.split("-")[1].equals("1")){
                        int value = getValue(Integer.parseInt(buffer.split("-")[1]));
                        coreOutS.println(value);
                    }else{
                        //(int)Math.floor(Math.random()*(max-min+1)+min)
                        int randomInt = (int)Math.floor(Math.random()*(1-0+1)+0);
                        L2OutS.get(randomInt).println(buffer);
                        buffer = L2InS.get(randomInt).readLine();
                        coreOutS.println(buffer);
                    }
                }else if (buffer.split("-")[0].equals("u")){
                    replaceValue(Integer.parseInt(buffer.split("-")[1]),
                            Integer.parseInt(buffer.split("-")[2]));
                    coreOutS.println("ack");
                }else if(buffer.split("-")[0].equals("d")){
                    //TODO: Escriure la update al arxiu
                    Util.writeUpdate("Layer1-"+myId);
                    System.out.println("Update finalitzada");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void sendDB(int i) throws IOException {
        String buffer;
        for (Map.Entry<Integer, Integer> set :
                database.getDatabase().entrySet()) {
            L2OutS.get(i).println("u-" + set.getKey() + "-" + set.getValue());
            do {buffer = L2InS.get(i).readLine();} while (!buffer.equals("ack"));
        }
        L2OutS.get(i).println("d-" + myId);
    }
    private void mainFunction() {
        //Una funció per escoltar els reads
        new Thread(this::listenReadsCoreLayer).start();
        //FIXME: OLD
        //Una funció per escoltar els updates
        //new Thread(this::listenCoreLayer).start();

        //Una funció principal que fagi la repliació
        System.out.println(ANSI_GREEN+"Llençat el thread de listenCoreLayer!");
        while (true) {
            try {
                sleep(10000);
                synchronized (this){
                    //ID==0 no hará esto porque para él L2OutOs es size 0, por lo que no entra en el bucle
                    for (int i = 0; i < L2OutOs.size(); i++) {
                        //FIXME: OLD
                        //L2OutOs.get(i).writeObject(database);
                        //L2OutOs.get(i).reset();
                        sendDB(i);
                        System.out.println("DB enviada");
                    }
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }


    private int getValue(int position){
        synchronized (this){
            if (database.getDatabase().containsKey(position)){
                int value = database.getDatabase().get(position);
                return value;
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

}

class mainLayer1 {
    public static void main(String[] args) {
        Layer_1 core = new Layer_1();

        core.work(Integer.parseInt(args[0]));
    }
}