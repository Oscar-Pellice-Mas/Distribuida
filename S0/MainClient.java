package S0;

import java.io.IOException;

public class MainClient {

    private static final Client serversN = new Client();

    public static void main(String[] args) {
        try {
            serversN.config(Integer.parseInt(args[0]));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
