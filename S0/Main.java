package S0;

import java.io.IOException;

public class Main {
    private static final Servidor serverT = new Servidor();
    private static final Client serversN = new Client();

    public static void main(String[] args) {
        try {
            serverT.config(123);
            serversN.config(123);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
