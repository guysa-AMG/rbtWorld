// # Main entry point, listens for connections

package za.co.wethinkcode.robots.server;
import java.util.Scanner;

import za.co.wethinkcode.flow.Recorder;
import za.co.wethinkcode.robots.client.RobotClient;
import za.co.wethinkcode.robots.models.IpAddr;


public class Server {

    public static void main(String[] args){
       
    if(isHosting()){
        if (args.length==1){ new RobotServer(args[0]) ;
        }else{ new RobotServer(); }
    }
    else{
   
           IpAddr addr =  ConnectionInfo();


        RobotClient client = new RobotClient(addr);
        client.start();
    }
}

    // The following initialisation is REQUIRED for `flow` monitoring.
    // DO NOT REMOVE OR MODIFY THIS CODE.
    static {
        new Recorder().logRun();
    }

    static public IpAddr ConnectionInfo(){
        Scanner scan = new Scanner(System.in);
        System.out.print("ip addr: ");
        String ip = scan.nextLine();
        System.out.print("port: ");
        String port = scan.nextLine();
        
        
        return new IpAddr(ip, Integer.parseInt(port));
    }

    public static Boolean isHosting(){
        Boolean ret=true;
   System.out.print("\n\nHi Would You Like To Host or Connect To A Robot World!\n[1] Host\n[2] Connect\ninput> ");

   String selection =  System.console().readLine();
   if (Integer.decode(selection)==1){
   ret=true;
   }
   if (Integer.decode(selection)==2){
    ret=false;
   }
return ret;
    }

}
