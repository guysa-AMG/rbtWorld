// # Runnable/Thread to manage each connected robot
package za.co.wethinkcode.robots.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;

import za.co.wethinkcode.robots.services.ITCService;

class ClientHandler implements Runnable{
    private Socket specificSock;
    
    public ClientHandler(Socket sock){
    this.specificSock=sock;
    System.out.println("new connection");
    }

    @Override
    public void run()  {
        try {
            
            BufferedReader br =  new BufferedReader(new InputStreamReader(this.specificSock.getInputStream()));
            String data;

            Scanner scan = new Scanner(System.in);

            while ( ( data = br.readLine()) !=null){
            
            ITCService.getInstance().doThisCommand(data);
             System.out.print("response> ");
             data = scan.nextLine()+"\n";
             System.out.println(data);
           this.specificSock.getOutputStream().write(data.getBytes());
           this.specificSock.getOutputStream().flush();
            }
            
            scan.close();
        
        } catch (IOException e) {
          
            e.printStackTrace();
        }

    }
    
}