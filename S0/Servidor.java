package S0;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import static S0.GenericServer.ANSI_YELLOW;
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
    public void run() {
        String msg = "";
        while(true){
            try {
                try{
                   msg = in.readLine();
                }catch(SocketException e){
                    System.err.println("El client ha mort. Aquest servidor cessarà de funcionar");
                    break;
                }

                String parts[] = msg.split(" ");
                if (parts[0].equals("REQUEST")){
                    synchronized (this){
                        out.println(globalValue);
                    }
                }else if (parts[0].equals("UPDATE")){
                    synchronized (this){
                        globalValue=Integer.parseInt(parts[1]);
                    }
                    System.out.println(ANSI_YELLOW + " Valor incrementat! Valor actual: " + globalValue);
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
            System.out.println(ANSI_GREEN+"Esperem Client");
            waitForClient();
            System.out.println(ANSI_GREEN+"Esperem benvinguda");
            String greeting = inS.readLine();
            System.out.println(ANSI_GREEN+"Benvinguda: "+ greeting);
            if ("hello server".equals(greeting)) {
                outS.println("hello client "+numServers);
            } else {
                System.err.println("unrecognised greeting");
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
                //outS.println("Reconnect "+0);
                outS.println("0");
                //Avisem el anterior
                //Per fer que el anterior segueixi escoltant i diferencii entre nou valor i nova connexio: Trama amb prefix.
                servers.get(servers.size()-1).sendMsg("Reconnect " + numServers);
            }else{
                System.out.println(ANSI_CYAN+"Token per al server 0");
            }
            servers.add(new dedicatedServer(outS,inS,serverAccepter));
            servers.get(servers.size()-1).start();
            numServers++;
            System.out.println(ANSI_BLUE+"Num de Servers: "+numServers);
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
