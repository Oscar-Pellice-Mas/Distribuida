package S0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

import static java.lang.Thread.sleep;

class ClientRingConnector extends Thread{
    //TODO: Cuando llegue el segundo server y tenga que canviar el SERVER_ANT del 0, cómo lo hago?
    private static final int SERVER_SEG= 0;
    private static final int SERVER_ANT= 1;
    //[Server SEGÜENT, Server ANTERIOR]
    private Socket clientSocket[];
    private PrintWriter out[];
    private BufferedReader in[];
    private String msg;
    private Client cl;

    public PrintWriter getOutSeg() {
        return out[SERVER_SEG];
    }

    public ClientRingConnector(Socket clientSocket[], PrintWriter out[], BufferedReader in[]) {
        this.clientSocket = clientSocket;
        this.out = out;
        this.in = in;
    }

    public ClientRingConnector(Socket[] clientSocket, PrintWriter[] out, BufferedReader[] in, Client cl) {
        this.clientSocket = clientSocket;
        this.out = out;
        this.in = in;
        this.cl = cl;
    }

    @Override
    public void run() {
        //Creem el thread anònim que constantment espera client i renova SERVER_ANT
        new Thread(
                new Runnable() {
                    public void run() {
                        while (true){
                            waitForClient();
                        }
                    }
                }
        ).start();
        //Tirem a la execució normal de comunicació
        while(true){
            msg=null;
            try {
                try{
                    //FIXME :Se canvia el in mientras esperamos y da locura??
                    msg = in[SERVER_ANT].readLine();
                }catch (NullPointerException e){
                    synchronized (this){
                        this.wait();
                    }
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Error en la lectura. Provarem de nou.");
            }
            //System.out.println("A");
            //Mirem que no sigui un notify
            if (msg != null){
                if (msg.split(" ")[0].equals("TOKEN")){
                    //Avisem al thread 3
                    cl.tenimToken(Integer.parseInt(msg.split(" ")[1]));
                }
            }
        }
    }


    private void waitForClient() {
        cl.waitForClient();
        try {
            this.in[SERVER_ANT].close();
            this.out[SERVER_ANT].close();
        } catch (IOException | NullPointerException e) {
            System.err.println("in i out nuls");
        }
        this.in[SERVER_ANT] = cl.inS;
        this.out[SERVER_ANT] = cl.outS;
        synchronized (this){
            this.notify();
        }
    }

    public void sendToken(int value){
        out[SERVER_SEG].println("TOKEN "+value);
    }
    public void startConnection(int port, int SoA) throws IOException {
        if(clientSocket[SERVER_SEG] != null){
            clientSocket[SERVER_SEG].close(); //da nullpointer
            out[SERVER_SEG].close();
            in[SERVER_SEG].close();
        }
        try{
            clientSocket[SoA] = new Socket(GenericServer.LOCALHOST, port);
        }catch (ConnectException e){
            System.err.println("El puerto introducido es incorrecto. Vigile que el puerto de otros nodos sea correctamente su ID (Orden de ejecución empezando por 0) + 1 ");
            System.exit(0);
        }
        out[SoA] = new PrintWriter(clientSocket[SoA].getOutputStream(), true);
        //FIXME :Se canvia el in mientras esperamos y da locura??
        //FIXME :Se cierra este in también??
        in[SoA] = new BufferedReader(new InputStreamReader(clientSocket[SoA].getInputStream()));
    }
}
class ClientStructureConnector extends Thread{
    private static final int SERVER_SEG= 0;
    private static final int SERVER_ANT= 1;
    //Server T
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private ClientRingConnector crc; //Referència al crc d'aquest servidor per canviar la seva connexió.
    private Client cl;
    private String msg;
    //Booleans improtants
    private boolean primeraConnexio = false;
    public ClientStructureConnector(Socket clientSocket, PrintWriter out, BufferedReader in) {
        this.clientSocket = clientSocket;
        this.out = out;
        this.in = in;
    }

    public ClientStructureConnector(Socket clientSocket, PrintWriter out, BufferedReader in, ClientRingConnector crc, Client cl) {
        this.clientSocket = clientSocket;
        this.out = out;
        this.in = in;
        this.crc = crc;
        this.cl = cl;
    }

    @Override
    public void run() {
        while(true){
            msg=null;
            try {
                msg = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Mirem que no sigui un notify
            if (msg != null){
                if (msg.split(" ")[0].equals("Reconnect")){
                    //Reconnectem al següent
                    try {
                        //Avisem de connectarse
                        crc.startConnection(Client.PORT_SERVER_T+Integer.parseInt(msg.split(" ")[1])+1/*El puerto es siempre la ID más uno*/, SERVER_SEG);
                        if (!primeraConnexio){
                            cl.awake();
                            primeraConnexio=true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    //Avisem al thread 3 que hi ha missatge
                    cl.missatgeNouServerT(msg);
                }
            }
        }
    }
}
public class Client extends GenericServer{
    public static final int PORT= 5000;
    public static final int PORT_SERVER_T= 6000;

    private static final int SERVER_T= 0;
    private static final int SERVER_N= 1;
    private static String myMSG;

    //[Server T,Server N]
    private Socket clientSocket[];
    private PrintWriter out[];
    private BufferedReader in[];
    //Elements de la comunicació
    private boolean token;
    private int myToken;
    //Elements de la estructura i la comunicació
    private ClientRingConnector clientRing;
    private ClientStructureConnector clientStruct;

    public Client() {
        clientSocket = new Socket[2];
        out = new PrintWriter[2];
        in = new BufferedReader[2];
        token = false;
    }


    public void config(int offset) throws IOException, InterruptedException {
        //Creem el server
        CreateServer(PORT_SERVER_T+offset);
        //Fem thread per el server N
        //Connectem al server T
        this.startConnection(LOCALHOST, PORT_SERVER_T,SERVER_T);
        String response = this.sendMessage("hello server",SERVER_T);
        String parts[] = response.split(" ");
        //if ((/*"hello client"*/ parts[0] + " " + parts[1]).equals(response)) {

        if ((parts[0] + " " + parts[1]).equals("hello client")) {
            if (Integer.parseInt(parts[2])!=0){
                //Connectem a altre Server N

                int position = Integer.parseInt(in[SERVER_T].readLine());
                startConnection(LOCALHOST,PORT_SERVER_T+position+1,SERVER_N);
            }else{
                token=true;
            }
            //Tres threads: un controla la actualització de les connexions, un controla la connexió amb l'anterior i el posterior, un controla getValue i updateCurrentValue
            //Thread 1
            clientRing = new ClientRingConnector(new Socket[]{clientSocket[SERVER_N], super.serverAccepter},
                    new PrintWriter[]{out[SERVER_N], super.outS},
                    new BufferedReader[]{in[SERVER_N], super.inS},
                    this);
            clientRing.start();
            System.out.println(ANSI_GREEN+"ClientRingConnector creat");

            //Thread 2
            clientStruct = new ClientStructureConnector(clientSocket[SERVER_T],out[SERVER_T],in[SERVER_T],clientRing,this);
            clientStruct.start();
            System.out.println(ANSI_GREEN+"ClientStructureConnector creat");

            //Thread 3
            this.startCommunication();
        } else {
            System.out.println("unrecognised greeting");
        }
    }
    private void startCommunication() throws IOException, InterruptedException {

        System.out.println(ANSI_GREEN+"Comencem comunicació");
        for (int i=0; i<10; i++){

            if (!token){
                synchronized (this){
                    this.wait(); //Esperem a tenir el token
                }
            }
            int value = getCurrentValue();

            if (myToken==value) {
                myToken = value + 1;
                updateCurrentValue(myToken);
            }else{
                myToken=value;
                i--;
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (clientRing.getOutSeg()==null){
                System.out.println(ANSI_BLUE+"Esperem a tenir un següent server");
                synchronized (this){
                    wait();
                }
            }
            System.out.println(ANSI_BLUE+"Anem a tirar el token al següent");
            clientRing.getOutSeg().println("TOKEN "+myToken);
            token=false;
            System.out.println(ANSI_BLUE+"Li hem tirat el token al següent");
        }
        while (true){
            if (!token){
                synchronized (this){
                    this.wait(); //Esperem a tenir el token
                }
            }
            System.out.println(ANSI_CYAN+"He acabat, passo el token al següent");
            clientRing.getOutSeg().println("TOKEN "+myToken);
            token=false;
        }
    }
    public void missatgeNouServerT(String msg) {
        myMSG = msg;
        awake();
        /*
                synchronized (this){
            this.notify();
        }
         */
    }

    public void startConnection(String ip, int port, int ToN) throws IOException {
        clientSocket[ToN] = new Socket(ip, port);
        out[ToN] = new PrintWriter(clientSocket[ToN].getOutputStream(), true);
        in[ToN] = new BufferedReader(new InputStreamReader(clientSocket[ToN].getInputStream()));
    }

    public String sendMessage(String msg, int ToN) throws IOException {
        out[ToN].println(msg);
        System.out.println(ANSI_GREEN+"Esperem resposta");
        String resp = in[ToN].readLine();
        System.out.println(ANSI_YELLOW+"Resposta: " + resp);
        return resp;
    }

    public void stopConnection(int ToN) throws IOException {
        in[ToN].close();
        out[ToN].close();
        clientSocket[ToN].close();
    }


    private int getCurrentValue() {
        try {
            out[SERVER_T].println("REQUEST");
            synchronized (this){
                this.wait();//Esperem la resposta que arribarà de altre Thread
            }
            return Integer.parseInt(myMSG);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }
    private void updateCurrentValue(int value) {
        out[SERVER_T].println("UPDATE "+value);
    }

    public void tenimToken(int value){
        this.token = true;
        this.myToken=value;
        awake();
        /*
        *
        synchronized (this){
            this.notify();
        }
        */
    }

    public void awake() {
        synchronized (this){
            this.notify();
        }
    }
}
