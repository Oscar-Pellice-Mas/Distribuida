package S0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Servidor{
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public String sendMessage(String msg) throws IOException {
        out.println(msg);
        String resp = in.readLine();
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public void config(int port) throws IOException {
        Client client = new Client();
        client.startConnection("127.0.0.1", port);
        String response = client.sendMessage("hello server");
        
        if ("hello client".equals(response)) {
            client.startCommunication();
        }
        else {
            System.out.println("unrecognised greeting");
        }
    }
    
    public void startCommunication(){
        out.println("hello client");
    }
}
