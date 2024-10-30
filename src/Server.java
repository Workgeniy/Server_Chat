import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    ServerSocket server = null;
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    int port;
    Map <String, Socket> clients = new HashMap<String, Socket>();

    Server (int port) throws IOException {
        this.port = port;

        try {
            server = new ServerSocket(port);
            System.out.println("Server started");
        }
        catch (IOException e) {
            System.out.println("Server could not be started");
            e.printStackTrace();
        }
    }

    public void listen () throws IOException {
        if (asAlive()){
            Socket client = server.accept();
            System.out.println("Client connected" + client.getInetAddress().getHostAddress());
            try {
                in = new ObjectInputStream(client.getInputStream());
                String number = (String) in.readObject();
                clients.put(number, client);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            for (String cl : clients.keySet()) {
                System.out.println(cl);
            }
            new Thread (new RequestProccesing(client)).start();
        }
    }

    public boolean asAlive(){
        return server != null;
    }

    static class RequestProccesing implements Runnable {
        Socket client = null;
        RequestProccesing(Socket client) throws IOException {
            if (client == null) {
                throw new IOException();
            }
            this.client = client;
        }
        public void run () {

            String path = "History.txt";
            String message = "";

            //Запись в файл
            try {
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                message = (String) in.readObject();
                try {
                    FileWriter file = new FileWriter(path, true);
                    BufferedWriter writer = new BufferedWriter(file);
                    writer.write(client.getInetAddress().getHostAddress() + ":" +  message + "@ \n");
                    writer.close();

                    in.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

            }
            catch (ClassNotFoundException onf){
                System.out.println (message);
            }
            catch (IOException io){
                System.out.println ("Server error");
            }


            //Отправка клиенту
            try {
                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                FileReader file = new FileReader(path);
                BufferedReader reader = new BufferedReader(file);
                String s;
                while((s=reader.readLine())!=null){
                    out.writeObject(s);
                    out.flush();
                }


            }
            catch (IOException io){
                System.out.println ("Не удалось отправить");
            }

            System.out.println (message);
        }
    }

}
