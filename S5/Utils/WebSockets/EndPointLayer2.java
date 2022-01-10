package S5.Utils.WebSockets;

import S5.Layer_2.Layer_2;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;

@ServerEndpoint("/monitor")
public class EndPointLayer2 {
    @OnOpen
    public void Open(){
        Layer_2 l2 = Layer_2.getLayer_2();

    }
    @OnMessage
    public String handleTextMessage(String message) {
        System.out.println("New Text Message Received");
        String buffer="";
        Layer_2 l2 = Layer_2.getLayer_2();
        for (Map.Entry<Integer, Integer> set :
                l2.getDatabase().getDatabase().entrySet()) {
            //TODO: Hacer un string para enviar
            buffer = buffer.concat(set.getKey() + "-" + set.getValue() + "<br>");
        }
        return message;
    }

    @OnMessage(maxMessageSize = 1024000)
    public byte[] handleBinaryMessage(byte[] buffer) {
        System.out.println("New Binary Message Received");
        return buffer;
    }


}
