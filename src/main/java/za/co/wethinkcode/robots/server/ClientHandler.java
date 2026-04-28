// # Runnable/Thread to manage each connected robot
package za.co.wethinkcode.robots.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.Socket;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import za.co.wethinkcode.robots.services.ITCService;

class ClientHandler implements Runnable{
    private Socket specificSock;
    private  Logger log;
    private String client;
    private Scanner scan ;
    public ClientHandler(Socket sock){
    this.client=sock.getLocalAddress().getAddress().toString();
    this.log=LoggerFactory.getLogger(ClientHandler.class);
    this.log.info("new connection to -> "+client);
    this.specificSock=sock;
    System.out.println("new connection");
    this.scan = new Scanner(System.in);
    }

    @Override
    public void run()  {
        try {
            
            BufferedReader br =  new BufferedReader(new InputStreamReader(this.specificSock.getInputStream()));
            String data;
         

            while ((data = br.readLine())!=null){
             
            
            String request= data;
            this.log.info("read => "+ request+" from "+client);
            String sendableData = ITCService.getInstance().doThisCommand(request)+"\n";
    
           this.specificSock.getOutputStream().write(sendableData.getBytes());
           this.specificSock.getOutputStream().flush();
            
            }
          
        
        } catch (IOException e) {
            this.log.error(e.getMessage()+ "["+client+"]");
            e.printStackTrace();
        }

    }
    
}