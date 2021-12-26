package S5.Layer_1;

import S5.Core_Layer.CoreLayer;
import S5.Utils.Data;
import S5.Utils.GenericServer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Layer_1 extends GenericServer {
    //Server Information
    private  int                          myId;
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


    //Data information
    private Data      database;

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

    private void mainFunction() {
        new Thread(
                this::listenCoreLayer
        ).start();
        //TODO: Una funció principal que fagi la fumada de la repliació
        System.out.println(ANSI_GREEN+"Llençat el thread de listenCoreLayer!");
        while (true) {
            try {
                sleep(10000);
                synchronized (this){
                    for (int i = 0; i < L2OutOs.size(); i++) {
                        L2OutOs.get(i).writeObject(database);
                    }
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

}

class mainLayer1 {
    public static void main(String[] args) {
        Layer_1 core = new Layer_1();

        core.work(Integer.parseInt(args[0]));
    }
}