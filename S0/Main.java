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

