package S2.Heavyweight;

public class ProcessA {
    private String token;
    private static final int NUM_LIGHTWEIGHTS = 570;
    private int answersfromLightweigth;

    public void start(){
        while(true){
            while(token == null) listenHeavyweight();
            for (int i=0; i<NUM_LIGHTWEIGHTS; i++)
                sendActionToLightweight();
            while(answersfromLightweigth < NUM_LIGHTWEIGHTS)
                listenLightweight();
            token = null;
            sendTokenToHeavyweight();
        }
    }

    private void sendTokenToHeavyweight() {
    }

    private void listenLightweight() {
    }

    private void sendActionToLightweight() {
    }

    private void listenHeavyweight() {
    }

}
