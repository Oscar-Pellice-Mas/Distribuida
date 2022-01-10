package S5.Layer_2;


import S5.Core_Layer.CoreLayer;
import S5.Layer_1.Layer_1;
import S5.Utils.Data;
import S5.Utils.GenericServer;
import S5.Utils.Util;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Layer_2 extends GenericServer {
    //Server Information
    private  int                          myId;
    public   static Layer_2               instance;
    //Data information
    private Data                          database;
    //Layer 1 Server information
    private Socket L1Socket;
    private PrintWriter L1OutS;
    private BufferedReader L1InS;
    private ObjectOutputStream L1OutOs;
    private ObjectInputStream L1InOs;

    public static Layer_2 getLayer_2(){
        if (instance == null){
            instance = new Layer_2();
        }
        return instance;
    }

    public Data getDatabase() {
        return database;
    }

    public Layer_2() {
        database  =  new Data();
    }
    private void getL1Layer(){
        System.out.println(ANSI_GREEN+"Esperem a que es connecti L1");
        waitForClient();
        try{
            L1Socket =        serverAccepter;
            L1OutS =          outS;
            L1InS =           inS;
            OutputStream os =   L1Socket.getOutputStream();
            L1OutOs =         new ObjectOutputStream(os);
            InputStream is =    L1Socket.getInputStream();
            L1InOs =          new ObjectInputStream(is);
        }catch (IOException e){
            System.out.println(ANSI_YELLOW+"Error en crear entrades i sortides");
        }
        System.out.println(ANSI_YELLOW+"connectat amb la L1 Layer!");
    }
    public void work(int id) {
        myId=id;
        CreateServer(PORT_L2+myId,5+myId);
        getL1Layer();
        mainFunction();
    }

    private void listenL1Layer() {
        while (true){
            try {
                System.out.println(ANSI_GREEN+"Llegint data");
                Data d = (Data) L1InOs.readObject();
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
        String buffer;
        //FIXME: OLD
        //new Thread(this::listenL1Layer).start();
        while(true){
            try {
                buffer = L1InS.readLine();
                if (buffer.split("-")[0].equals("r")){
                    //R-L-ID
                    int value = getValue(Integer.parseInt(buffer.split("-")[1]));
                    L1OutS.println(value);
                }else if (buffer.split("-")[0].equals("u")){
                    replaceValue(Integer.parseInt(buffer.split("-")[1]),
                            Integer.parseInt(buffer.split("-")[2]));
                    L1OutS.println("ack");
                }else if(buffer.split("-")[0].equals("d")){
                    //TODO: Escriure la update al arxiu
                    Util.writeUpdate("Layer2-"+myId);
                    System.out.println("Update finalitzada");
                }
            } catch (IOException e) {
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


class mainLayer2 {
    public static void main(String[] args) {
        Layer_2 core = Layer_2.getLayer_2();

        core.work(Integer.parseInt(args[0]));
    }
}