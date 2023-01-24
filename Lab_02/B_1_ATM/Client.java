import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class Client {

    private static Socket socket;
    private static ServerSocket server;
    private static DataInputStream input = null ;
    private static DataOutputStream out ;
    private static Scanner scanner = null;
    public static void main(String[] args) throws IOException, InterruptedException {

        // Create client socket
        socket = new Socket("localhost", 1225);
        input = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        scanner = new Scanner(System.in);




        String op = "";
        String line = "";

        boolean auth = false;
        String username = null;
        String password = null;
        boolean mainmenu = true;
        while(mainmenu){
            if(!auth) {
                System.out.println("Enter Username");
                username = scanner.nextLine();
                System.out.println("Enter Password");
                password = scanner.nextLine();
                op = "00 " + username + " " + password;
                out.writeUTF(op);
                String response = input.readUTF();
                if(response.equals("0")){
                    System.out.println("Authentication Failed!");
                }
                else auth = true;
                System.out.flush();
            }
            else{

                System.out.println("1.Credit \n2.Debit\n3.Check balance\nEnter 0 to EXTT!\n");
                 line = scanner.nextLine();
                if(line.equals("1")){
                    System.out.println("Enter amount");
                    String amount = scanner.nextLine();

                    if(valid(amount) != -1){
                        op = "01 " + username + " " + password + " " + amount;
                        out.writeUTF(op);
                        String response = input.readUTF();
                        System.out.println("Transaction Successful!");
                        sleep(2);
                        System.out.flush();
                    }
                    else{
                        System.out.println("Incorrect Number");
                    }

                }
                else if(line.equals("2")){
                    System.out.println("Enter amount");
                    String amount = scanner.nextLine();
                    if(valid(amount) != -10
                    ){
                        op = "02 " + username + " " + password + " " +amount;
                        out.writeUTF(op);
                        String response = input.readUTF();
                        if(response.equals("low")){
                            System.out.println("Low balance\nTransaction Failed! ");
                        }
                        else{
                            System.out.println("Transaction Successful!");
                        }
                    }
                    else{
                        System.out.println("Incorrect Number");
                    }
                }
                else if(line.equals("3")){
                    out.writeUTF("03 " + username + " " + password);
                    String res = input.readUTF();
                    System.out.println("Your current balance is " + res);
                    System.out.println("\n\n ");
                }
                else if(line.equals("0")){
                    out.writeUTF("0");
                    mainmenu = false;

                }
                else{
                    System.out.println("Wrong number");
                }
                sleep(20);
                System.out.flush();
            }
        }

        socket.close();


    }

    static int valid(String str){
        try{
            int amount = Integer.getInteger(str);
            return amount;
        }
        catch (Exception e){
            return -1;
        }
    }
}

