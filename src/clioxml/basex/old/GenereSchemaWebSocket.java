package clioxml.basex.old;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
 
/**
 * Basic Echo Client Socket
 */


@WebSocket(maxTextMessageSize = 64 * 1024)
public class GenereSchemaWebSocket {
 
   
 
    @SuppressWarnings("unused")
    private Session session;
    private HttpSession httpSession = null;
 
    public GenereSchemaWebSocket(HttpSession httpSession) {
        this.httpSession = httpSession;
        System.out.println(this.httpSession);
    }
 
    
 
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        this.session = null;
        
    }
 
    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.printf("Got connect: %s%n", session);
        this.session = session;
        
        
    }
 
    @OnWebSocketMessage
    public void onMessage(String msg) {
        System.out.printf("Got msg: %s%n", msg);
        GenereSchemaThread genereSchema = new GenereSchemaThread(session,msg);
        Thread t = new Thread(genereSchema);
        t.start();
        
    }
}