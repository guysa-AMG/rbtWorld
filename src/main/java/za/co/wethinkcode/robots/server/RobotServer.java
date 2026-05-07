package za.co.wethinkcode.robots.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.server.world.RobotWorld;
import za.co.wethinkcode.robots.services.ITCService;

public class RobotServer {
   
    private int port;

    public RobotServer( String arg_port){
        
        this.port = Integer.decode(arg_port);
        try{
            JvmMetrics.builder().register();
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
       Iworld world = new RobotWorld();
       ITCService.getInstance().loadWorld(world);
   


       while(loop){
        System.out.println("...listening to incoming connection");
       Socket client = servSock.accept();
        
      Thread th = new Thread(new ClientHandler(client));
      th.start();
      ITCService.getInstance().addThreadControllers(client,th);
     
       }

       servSock.close();
       }
      catch (Exception e) {
        // TODO: handle exception
       }
      
    }

}
