package S0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class Client extends GenericServer{
    private static final int PORT= 5000;
    private static final int PORT_SERVER_T= 6000;

    private static final int SERVER_T= 0;
    private static final int SERVER_N= 1;

    //[Server T,Server N]
    private Socket clientSocket[];
    private PrintWriter out[];
    private BufferedReader in[];




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

    public void config() throws IOException {
        //Creem el server
        CreateServer(PORT);
        //Fem thread per el server N

        //Connectem al server T
        this.startConnection("127.0.0.1", PORT_SERVER_T,SERVER_T);
        String response = this.sendMessage("hello server",SERVER_T);
        String parts[] = response.split(" ");
        if ((/*"hello client"*/ parts[0] + " " + parts[1]).equals(response)) {
            if (Integer.parseInt(parts[3])!=0){
                //Connectem a altre Server N
                int position = Integer.parseInt(in[SERVER_T].readLine());
                startConnection(LOCALHOST,PORT+position,SERVER_N);
            }
            this.startCommunication();
        } else {
            System.out.println("unrecognised greeting");
        }
    }
    
    public void startCommunication() throws IOException {
        /*        sendMessage("request");
        System.out.println("Tinc el token");
        sendMessage("letGo");
        System.out.println("Ja no");
        */
        for (int i=0; i<10; i++){
            /*...*/
            int valor = getCurrentValue();
            /*...*/
            updateCurrentValue(valor+1);
            /*...*/
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            /*...*/
        }
    }

    private int getCurrentValue() {
        int value =1;
        System.out.println("WIP");
        return value;
    }
    private void updateCurrentValue(int value) {
        System.out.println("WIP");
    }
}
