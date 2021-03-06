package S0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class GenericServer extends Thread{
    protected static final int PORT_SERVER_T= 6000;
    protected static final String LOCALHOST= "127.0.0.1";
    /** CODIS DE COLORS **/

    public static final String ANSI_YELLOW = "\u001B[33m"; //Codi de colors per actualitzacions del valor
    public static final String ANSI_GREEN = "\u001B[32m"; //Codi de colors per missatges de estat
    public static final String ANSI_BLUE = "\u001B[34m";; //Color per num servers
    public static final String ANSI_CYAN = "\u001B[36m"; //Color per token

    //Camps del servidor
    protected ServerSocket serverSocket;
    protected  Socket serverAccepter= null;
    protected  PrintWriter outS = null;
    protected  BufferedReader inS = null;

    void CreateServer(int port){
        try {
            // Connect to HW
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Aquest port es troba ocupat");
            System.exit(0);
        }
    }

    void waitForClient(){
        try {
            serverAccepter = serverSocket.accept(); //establishes connection
            inS = new BufferedReader(new InputStreamReader(serverAccepter.getInputStream()));
            outS = new PrintWriter(serverAccepter.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
