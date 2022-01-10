package S5.Utils.WebSockets;

import S5.Layer_1.Layer_1;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;

@ServerEndpoint("/monitor")
public class EndPointLayer1 {
    @OnOpen
    public void Open(){
        Layer_1 l1 = Layer_1.getLayer_1();

    }
    @OnMessage
    public String handleTextMessage(String message) {
        Layer_1 l1 = Layer_1.getLayer_1();
        System.out.println("New Text Message Received");
        String buffer="";
        for (Map.Entry<Integer, Integer> set :
                l1.getDatabase().getDatabase().entrySet()) {
            //TODO: Hacer un string para enviar
            buffer = buffer.concat(set.getKey() + "-" + set.getValue() + "<br>");
        }
        return buffer;
    }

    @OnMessage(maxMessageSize = 1024000)
    public byte[] handleBinaryMessage(byte[] buffer) {
        System.out.println("New Binary Message Received");
        return buffer;
    }

}
