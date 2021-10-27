package S0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class GenericServer {
    protected static final int PORT_SERVER_T= 6000;
    protected static final String LOCALHOST= "127.0.0.1";

    //Camps del servidor
    protected ServerSocket serverSocket;
    protected  Socket serverAccepter= null;
    protected  PrintWriter outS = null;
    protected  BufferedReader inS = null;

    void CreateServer(int port){
        try {
            // Connect to HW
            serverSocket = new ServerSocket(port);
            serverAccepter = serverSocket.accept(); //establishes connection
            inS = new BufferedReader(new InputStreamReader(serverAccepter.getInputStream()));
            outS = new PrintWriter(serverAccepter.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
