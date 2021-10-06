public class main {
    private Servidor serverT = new Servidor();
    private Client serversN = new Client();
    public static void main(String[] args) {
        serverT.config(123);
        serversN.config(123);
    }
}
