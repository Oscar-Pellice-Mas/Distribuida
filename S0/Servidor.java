package S0;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

class dedicatedServer extends Thread{
    private PrintWriter out;
    private BufferedReader in;


    public dedicatedServer(BufferedReader in, PrintWriter out) {
        this.out=out;
        this.in=in;
    }

    @Override
    public synchronized void start() {
        while(true){
            try {
                String msg = in.readLine();
                if (msg.equals("request")){
                    while (true){
                        synchronized (this){
                            Servidor.token=false;
                            out.println("Granted");
                        }
                    }
                }else if (msg.equals("letGo")){
                    synchronized (this){
                        Servidor.token = true;
                        out.println("GotIt");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
public class Servidor {
    public static boolean token = true;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private ArrayList<dedicatedServer> servers = new ArrayList<dedicatedServer>();
    private int numServers=0;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Esperant connexions");
        while (true){
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("Esperem benvinguda");
            String greeting = in.readLine();
            System.out.println("Benvinguda: "+ greeting);
            if ("hello server".equals(greeting)) {
                out.println("hello client");
            }
            else {
                System.out.println("unrecognised greeting");
            }

            servers.add(new dedicatedServer(in,out));
            servers.get(numServers++).start();
        }
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
    public void config(int port) throws IOException {
        Servidor server=new Servidor();
        server.start(port);
    }

}
