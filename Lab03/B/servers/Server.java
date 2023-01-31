import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new FileHandler());
        server.setExecutor(null);
        server.start();
    }

    static class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            String response = "Request Received";
            try{
                if(method.equals("GET")){
                    System.out.println("GET request received");
                    String fileName = t.getRequestURI().toString().substring(1);
                    System.out.println(fileName);
                    t.sendResponseHeaders(200, Files.size(Paths.get(fileName)));
                    Files.copy(Paths.get(fileName), t.getResponseBody());
                    t.getResponseBody().close();
                }else if(method.equals("POST")){
                    System.out.println("POST request received");
                }else{
                    throw new Exception("Not Valid Request Method");
                }
            }catch (Exception e){
                System.out.println("An erroneous request");
                response = e.toString();
                e.printStackTrace();
            }
            //Setting Headers for the response message
            Headers responseHeaders = t.getResponseHeaders();
            responseHeaders.add("Access-Control-Allow-Origin", "*");
            responseHeaders.add("Access-Control-Allow-Headers","origin, content-type, accept, authorization");
            responseHeaders.add("Access-Control-Allow-Credentials", "true");
            responseHeaders.add("Access-Control-Allow-Methods", "GET, POST");

            //Sending back response to the client
            t.sendResponseHeaders(200, response.length());
            OutputStream outStream = t.getResponseBody();
            outStream.write(response.getBytes());
            outStream.close();
        }
    }
}
