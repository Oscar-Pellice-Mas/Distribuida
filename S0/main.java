package S0;

import java.io.IOException;

public class main {
    private static Servidor serverT = new Servidor();
    private static Client serversN = new Client();

    public static void main(String[] args) {
        try {
            serverT.config(123);
            serversN.config(123);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
