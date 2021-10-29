package S0;

import java.io.IOException;

public class Main {
    private static final Servidor serverT = new Servidor();

    public static void main(String[] args) {
        try {
            serverT.config();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MainClient {

    private static final Client serversN = new Client();

    public static void main(String[] args) {
        try {
            serversN.config();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
