package za.co.wethinkcode.robots.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import za.co.wethinkcode.robots.errors.InvalidCommandException;
import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.commands.CommandTypeEnum;
import za.co.wethinkcode.robots.server.npc.KillerNPCController;
import za.co.wethinkcode.robots.server.world.BattleArenaWorld;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.server.world.RobotWorld;
import za.co.wethinkcode.robots.services.ITCService;
import za.co.wethinkcode.robots.shared.Protocol;

public class RobotServer {
   
    private int port;

    public RobotServer( String arg_port){
        if(arg_port==null){
            arg_port="2146";
        }
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
       Iworld world = BattleArenaWorld.build();
       ITCService.getInstance().loadWorld(world);
       System.out.println("Loaded Battle Arena world (" + world.getWidth() + "x" + world.getHeight() + ") with " + world.getObstacles().size() + " obstacles");
        
       Thread serv_interact_thread = new Thread(new ServerCli());
        serv_interact_thread.start();

       if (world instanceof RobotWorld rw) {
           KillerNPCController npcCtrl = new KillerNPCController(rw);
           ITCService.getInstance().setKillerController(npcCtrl);
           Thread npcThread = new Thread(npcCtrl, "killer-npc");
           npcThread.setDaemon(true);
           npcThread.start();
           System.out.println("Guyser_Thekiller NPC controller started");
       }
   


       while(loop){
       // System.out.println("...listening to incoming connection");
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
/**
 * ServerCli
 * this is the console cli to interact with the world as admin
 */
class ServerCli implements Runnable{

    @Override
    public void run() {
        Scanner scan = new Scanner(System.in);
      String req = null;
      
      String parsedReq=null; //json string

     do{
        if (req != null){
            try{
           parsedReq = parseServerSideCommand(req);
           String res =  ITCService.getInstance().doThisCommandUnRestricted(parsedReq);
           ServerResponse resObj = new Protocol().decodeResponse(res);
           System.out.println(resObj.getData().getMessage());}
           catch(InvalidCommandException ex){
            System.out.println("[x] Invalid Command");
           }
        }
         System.out.print("Server >");
      }
      while((req=scan.nextLine())!=null);

    }
    String parseServerSideCommand(String data) throws InvalidCommandException{
        String[] parts = data.split("\\s+");
        String command="";
        if(parts.length < 1){
            //TODO rather call interaction into print
            System.out.println("Invalid input. Use:<command> [arguments....] (example: HAL launch)>");
            return null;
        }
        else{
            command = parts[0].toLowerCase();
        }

       
       try{
        CommandTypeEnum.valueOf(command);
       }
       catch(IllegalArgumentException illegal){
              throw new InvalidCommandException();
       }
        
        String[] arguments = (parts.length > 1) ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        ServerRequest req = new ServerRequest("Server", command, arguments);
        Protocol parser = new Protocol();
       return  parser.encodeRequest(req);
    }

}
