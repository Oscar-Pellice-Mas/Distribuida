package S0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static java.lang.Thread.sleep;

class ClientRingConnector extends Thread{
    private static final int SERVER_SEG= 0;
    private static final int SERVER_ANT= 1;
    //[Server SEGÜENT, Server ANTERIOR]
    private Socket clientSocket[];
    private PrintWriter out[];
    private BufferedReader in[];
    private String msg;
    private Client cl;

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
    public synchronized void start() {
        while(true){
            msg=null;
            try {
                msg = in[SERVER_ANT].readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Mirem que no sigui un notify
            if (msg != null){
                if (msg.split(" ")[0].equals("TOKEN")){
                    //Avisem al thread 3
                    cl.tenimToken(Integer.parseInt(msg.split(" ")[0]));
                }
            }
        }
    }

    public void sendToken(){
        out[SERVER_SEG].println("TOKEN");
    }
    public void startConnection(int port, int SoA) throws IOException {
        clientSocket[SERVER_SEG].close();
        out[SERVER_SEG].close();
        in[SERVER_SEG].close();
        clientSocket[SoA] = new Socket(GenericServer.LOCALHOST, port);
        out[SoA] = new PrintWriter(clientSocket[SoA].getOutputStream(), true);
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
    public synchronized void start() {
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
                        crc.startConnection(Client.PORT+Integer.parseInt(msg.split(" ")[1]), SERVER_SEG);
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
    private static final int PORT_SERVER_T= 6000;

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

    public void missatgeNouServerT(String msg) {
        myMSG = msg;
        notify();
    }


    public void startConnection(String ip, int port, int ToN) throws IOException {
        clientSocket[ToN] = new Socket(ip, port);
        out[ToN] = new PrintWriter(clientSocket[ToN].getOutputStream(), true);
        in[ToN] = new BufferedReader(new InputStreamReader(clientSocket[ToN].getInputStream()));
    }

    public String sendMessage(String msg, int ToN) throws IOException {
        out[ToN].println(msg);
        System.out.println("Esperem resposta");
        String resp = in[ToN].readLine();
        System.out.println("Resposta: " + resp);
        return resp;
    }

    public void stopConnection(int ToN) throws IOException {
        in[ToN].close();
        out[ToN].close();
        clientSocket[ToN].close();
    }

    public void config() throws IOException, InterruptedException {
        //Creem el server
        CreateServer(PORT);
        //Fem thread per el server N

        //Connectem al server T
        this.startConnection(LOCALHOST, PORT_SERVER_T,SERVER_T);
        String response = this.sendMessage("hello server",SERVER_T);
        String parts[] = response.split(" ");
        if ((/*"hello client"*/ parts[0] + " " + parts[1]).equals(response)) {
            if (Integer.parseInt(parts[3])!=0){
                //Connectem a altre Server N
                int position = Integer.parseInt(in[SERVER_T].readLine());
                startConnection(LOCALHOST,PORT+position,SERVER_N);
            }
            //Tres threads: un controla la actualització de les connexions, un controla la connexió amb l'anterior i el posterior, un controla getValue i updateCurrentValue
            //Thread 1
            ClientRingConnector clientRing = new ClientRingConnector(new Socket[]{clientSocket[SERVER_N], super.serverAccepter},
                    new PrintWriter[]{out[SERVER_N], super.outS},
                    new BufferedReader[]{in[SERVER_N], super.inS},
                    this);
            clientRing.start();
            //Thread 2
            ClientStructureConnector clientStruct = new ClientStructureConnector(clientSocket[SERVER_T],out[SERVER_T],in[SERVER_T],clientRing,this);
            clientStruct.start();
            //Thread 3
            this.startCommunication();
        } else {
            System.out.println("unrecognised greeting");
        }
    }
    private void StructureComm() throws IOException{

    }

    private void ringComm() throws IOException{

    }
    private void startCommunication() throws IOException, InterruptedException {

        for (int i=0; i<10; i++){
            //...
            //int valor = getCurrentValue();
            wait(); //Esperem a tenir el token
            int value = getCurrentValue();
            if (myToken==value){
                myToken=value+1;
                updateCurrentValue(myToken);

                /*
                //...
                updateCurrentValue(valor+1);
                //...
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //...
                */
            }
        }
    }

    private int getCurrentValue() {
        try {
            out[SERVER_T].println("REQUEST");
            wait();//Esperem la resposta que arribarà de altre Thread
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
        notify();
    }
}
