package S5.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import S5.Utils.WebSockets.EndPoint;
import S5.Utils.WebSockets.EndPointLayer1;
import S5.Utils.WebSockets.EndPointLayer2;
import S5.Utils.WebSockets.clientEnd;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.server.Server;

import javax.websocket.DeploymentException;

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
    //Camps del WebSocket
    Server websocket;

    protected void CreateServer(int port,int wsOffset){
        try {
            // Create SErver TCP
            serverSocket = new ServerSocket(port);
            //Create WebSocket
            /** wsOffset
             *  CORE: 0,1,2
             *  LAYER_1: 3,4
             *  LAYER_2: 5,6
              */
            websocket = new Server ("localhost", 8050+wsOffset, "/S5",
                    (wsOffset<=2) ? EndPoint.class :
                            ((wsOffset<=4)&&(wsOffset>=3))? EndPointLayer1.class :
                                    EndPointLayer2.class);
            websocket.start();

        } catch (IOException | DeploymentException e) {
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

