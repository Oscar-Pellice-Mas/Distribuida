package S2.Lightweight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LightweightA {
    private static final int PORT_HWB = 6000;


    private static Socket socket;
    private static PrintWriter outHW;
    private static BufferedReader inHW;


    private static void connectarHW(){
        try {
            socket = new Socket("127.0.0.1",PORT_HWB);
            inHW = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outHW = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        connectarHW();
    }
}
