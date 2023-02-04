import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientReq implements  Runnable{
    public static ArrayList<ClientReq> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    String clientUsername;


    public ClientReq(Socket socket){
        try {
            this.socket = socket;
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader((new InputStreamReader(socket.getInputStream())));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("You're connected to server");

        } catch (Exception e) {
            closeEverything(socket, bufferedReader,bufferedWriter);
        }
    }


//    public  void removeClientHandler(){
//        clientHandlers.remove(this);
//        broadcastMessage("Server " +clientUsername +"has left the chat" );
//    }
    @Override
    public void run() {
        String messageFromClient;
        while(socket.isConnected()){
            try {
                messageFromClient = bufferedReader.readLine();
                System.out.println(this.clientUsername+": "+messageFromClient);
//                String [] token = messageFromClient.split("?");
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader,bufferedWriter);
                break;
            }
        }
    }

     boolean isPrime(int n){
        if(n==1) return false;
        for(int i=2; i<n; ++i){
            if(n%i==0) return false;
        }
        return true;
    }
    private void broadcastMessage(String msg) {
        for(ClientReq clientHandler : clientHandlers){
            try{
                if(clientHandler.clientUsername.equals(this.clientUsername)) {
                    String is_prime = isPrime(Integer.valueOf(msg))?"Prime":"NotPrime";
                    clientHandler.bufferedWriter.write("Server: "+is_prime);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader,bufferedWriter);
            }
        }
    }
    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}