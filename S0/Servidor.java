package S0;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import static S0.Servidor.globalValue;

class dedicatedServer extends Thread{
    private PrintWriter out;
    private BufferedReader in;
    protected  Socket serverAccepter= null;

    public dedicatedServer(BufferedReader in, PrintWriter out) {
        this.out=out;
        this.in=in;
    }

    public dedicatedServer(PrintWriter out, BufferedReader in, Socket serverAccepter) {
        this.out = out;
        this.in = in;
        this.serverAccepter = serverAccepter;
    }

    @Override
    public synchronized void start() {
        while(true){
            try {
                String msg = in.readLine();
                String parts[] = msg.split(" ");
                if (parts[0].equals("REQUEST")){
                    synchronized (this){
                        out.println(globalValue);
                    }
                }else if (parts[0].equals("UPDATE")){
                    synchronized (this){
                        globalValue=Integer.parseInt(parts[1]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopDedicatedServer() throws IOException {
        in.close();
        out.close();
        this.serverAccepter.close();
    }
    public void sendMsg(String msg){
        out.println(msg);
    }

    public String getMsg(){
        try {
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

public class Servidor extends GenericServer{
    protected static boolean token = true;
    protected static int globalValue;
    private int numServers=0;

    private ArrayList<dedicatedServer> servers = new ArrayList<dedicatedServer>();
    private HashMap<Integer,Integer> serverMap= new HashMap<>(); //Guardem la estructura del token ring

    public void start(int port) throws IOException {
        while (true){
            System.out.println("Esperem benvinguda");
            String greeting = inS.readLine();
            System.out.println("Benvinguda: "+ greeting);
            if ("hello server".equals(greeting)) {
                outS.println("hello client "+numServers);
            }
            else {
                System.out.println("unrecognised greeting");
                outS.close();
                inS.close();
                serverAccepter.close();
                return;
            }
            if (numServers!=0){
                //Enllacem el nou servidor
                //Treiem la connexió del anterior amb 0 i el possem al següent. El actual a 0
                //TODO: Només funciona sense fallades
                serverMap.put(numServers,0);
                serverMap.put(numServers-1,numServers);
                //Avisem el nou
                outS.println(0);
                //Avisem el anterior
                //Per fer que el anterior segueixi escoltant i diferencii entre nou valor i nova connexio: Trama amb prefix.
                servers.get(servers.size()).sendMsg("Reconnect " + numServers);
            }
            servers.add(new dedicatedServer(outS,inS,serverAccepter));
            servers.get(numServers++).start();
        }
    }

    public void stopServer() throws IOException {
        /*
        inS.close();
        outS.close();
        serverAccepter.close();
        */
        serverSocket.close();
    }
    public void config() throws IOException {
        CreateServer(PORT_SERVER_T);
        this.start(PORT_SERVER_T);
    }

}
