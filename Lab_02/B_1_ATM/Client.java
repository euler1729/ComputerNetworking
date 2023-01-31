import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.awt.*;
import java.awt.desktop.UserSessionEvent;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

import User.User;

public class Server{
    private static Socket socket;
    private static ServerSocket server;

    private static DataInputStream input ;
    private static DataOutputStream out;
    static int balance = 0;
    public Server() {
    }
    private static List<User> list = new ArrayList<>();
    static User user2 = new User("arif", "1232", 0);

    public static void main(String[] args) throws Exception{
        init();

        server = new ServerSocket(1240);
        System.out.println("Waiting for client");
        socket = server.accept();
        System.out.println("connected!");
        input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(socket.getOutputStream());

        String line = "";
        String username = "";
        String password = "";
        String amount = null;
        while(!line.equals("0")) {
            line = input.readUTF();
            if(line.equals("0")) break;
            System.out.println(line);
            String[] op = line.split(" ");
            String type = op[0];
            username = op[1];
            password = op[2];
            int id = find_id(username, password);

            if(type.equals("00")){
                if(id != -1)
                    out.writeUTF("1");
                else {
                    out.writeUTF("0");
                    continue;
                }
            }
            User user = list.get(id);
            if(type.equals("01")){
                int money = Integer.parseInt(op[3]);
                money += money;
                user.setBalance(user.getBalance() + money);
                list.set(id, user);
                out.writeUTF("credited");
            }
            else if(type.equals("02")){

                int money = Integer.parseInt(op[3]);
                if(money > user.getBalance())
                    out.writeUTF("low");
                else {
                    user.setBalance(user.getBalance() - money);
                    list.set(id, user);
                    out.writeUTF("debited");
                }


            }
            else if(type.equals("03")){
                System.out.println("checking balance");
                String current = String.valueOf(user.getBalance());
                System.out.println(current);
                out.writeUTF(current);
            }
            System.out.flush();

        }


        socket.close();
        server.close();
        input.close();
        out.close();
        finish();

    }

    static int find_id(String usr, String pass){
        int id = 0;
        for(User u : list){
            if(u.getUserName().equals(usr) && u.getPassword().equals(pass))
                return id;
        }
        return -1;

    }
    static void init() throws IOException {
        File file = new File("File");
        BufferedReader br
                = new BufferedReader(new FileReader(file));
        String data = "";
        while ((data = br.readLine()) != null){

            String[] op = data.split(" ");
//            System.out.println(op[0]);
//            System.out.println(op[1]);
//            System.out.println(op[2]);
            User user = new User(op[0], op[1], Integer.parseInt(op[2]));
            list.add(user);
        }
    }
    static void finish() throws IOException {
        File file = new File("File");
        BufferedWriter br
                = new BufferedWriter(new FileWriter(file));
        for(User u : list){
            String st = u.getUserName() + " " + u.getPassword() + " " + u.getBalance() + "\n";
            br.write(st);
        }
       br.close();
    }
}