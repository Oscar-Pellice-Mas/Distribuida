package S5.Utils.WebSockets;

import S5.Core_Layer.CoreLayer;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;

//@ServerEndpoint(value = "/{layer}/{id}")
@ServerEndpoint("/monitor")
public class EndPoint {
    @OnOpen
    public void Open(){
        CoreLayer core = CoreLayer.getcoreLayer();

    }
    @OnMessage
    public String handleTextMessage(String message) {
        CoreLayer core = CoreLayer.getcoreLayer();
        String buffer="";
        for (Map.Entry<Integer, Integer> set :
                core.getDatabase().getDatabase().entrySet()) {
            //TODO: Hacer un string para enviar
            buffer = buffer.concat(set.getKey() + "-" + set.getValue() + "<br>");
        }
        System.out.println("New Text Message Received");
        return buffer;
    }

    @OnMessage(maxMessageSize = 1024000)
    public byte[] handleBinaryMessage(byte[] buffer) {
        System.out.println("New Binary Message Received");
        return buffer;
    }


}
