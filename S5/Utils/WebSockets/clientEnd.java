package S5.Utils.WebSockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import org.glassfish.tyrus.client.ClientManager;

@ClientEndpoint
public class clientEnd {
    /*
    @OnOpen
    public void onOpen(Session session) {
        try {
            session.getBasicRemote().sendText("start");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        try {
            String userInput = bufferRead.readLine();
            return userInput;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    */
}