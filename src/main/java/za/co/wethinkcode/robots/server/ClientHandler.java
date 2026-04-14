// # Runnable/Thread to manage each connected robot
package za.co.wethinkcode.robots.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

class ClientHandler implements Runnable{
    private Socket specificSock;
    
    public ClientHandler(Socket sock){
    this.specificSock=sock;
    System.out.println("new connection");
    }

    @Override
    public void run() {
        try {
            boolean loop =true; 
            BufferedReader br =  new BufferedReader(new InputStreamReader(this.specificSock.getInputStream()));
            String data;
        
            while ( ( data = br.readLine()) !=null){
                 System.out.println( data);
                 data+=" ack";
           this.specificSock.getOutputStream().write(data.getBytes());
           this.specificSock.getOutputStream().flush();
            }
        
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
}