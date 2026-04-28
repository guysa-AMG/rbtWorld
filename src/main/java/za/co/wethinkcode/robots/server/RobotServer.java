package za.co.wethinkcode.robots.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;

public class RobotServer {
   
    private int port;

    public RobotServer( String arg_port){
        
        this.port = Integer.decode(arg_port);
        try{
        HTTPServer prometheusServer = HTTPServer.builder()
                                                .port(9200)
                                                .buildAndStart();
        }catch(IOException excep){
            System.err.println(excep);
        }
        this.init();
    }
     public RobotServer(){

        this("2146");

        
    }

    public void init(){
        
       try{
       ServerSocket servSock =  new ServerSocket(this.port);
       boolean loop = true;
       while(loop){
        System.out.println("...listening to incoming connection");
       Socket client = servSock.accept();

      Thread th = new Thread(new ClientHandler(client));
      th.start();
     
       }

       servSock.close();
       }
      catch (Exception e) {
        // TODO: handle exception
       }
      
    }

}
