package S5.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class GenericServer extends Thread{
    /**IP I PORTS**/
    protected static final int PORT_CORE    = 8000;
    protected static final int PORT_L1      = 8010;
    protected static final int PORT_L2      = 8020;
    protected static final String LOCALHOST = "127.0.0.1";
    /**INFO DE LA ESTRUCTURA**/
    protected static final int NUM_CORE_LAYER = 3;
    protected static final int NUM_L1 = 2;
    protected static final int NUM_L2 = 2;

    /** CODIS DE COLORS **/

    public static final String ANSI_YELLOW  = "\u001B[33m"; //Codi de colors per actualitzacions del valor
    public static final String ANSI_GREEN   = "\u001B[32m"; //Codi de colors per missatges de estat
    public static final String ANSI_BLUE    = "\u001B[34m"; //Color per num servers
    public static final String ANSI_CYAN    = "\u001B[36m"; //Color per token

    //Camps del servidor
    protected ServerSocket serverSocket;
    protected Socket serverAccepter = null;
    protected PrintWriter outS      = null;
    protected BufferedReader inS    = null;

    protected void CreateServer(int port){
        try {
            // Connect to HW
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Port occupied. Leaving program");
            System.exit(0);
        }
    }

    protected void waitForClient(){
        try {
            serverAccepter = serverSocket.accept(); //establishes connection
            inS = new BufferedReader(new InputStreamReader(serverAccepter.getInputStream()));
            outS = new PrintWriter(serverAccepter.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

