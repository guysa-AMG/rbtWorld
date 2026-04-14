// # Main entry point, listens for connections

package za.co.wethinkcode.robots.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

import za.co.wethinkcode.flow.Recorder;
import za.co.wethinkcode.robots.server.util.WorldReader;

public class Server {

    public static void main(String[] args){

    if(isHosting()){
        if (args.length<1){
            new RobotServer();
        }else{
        new RobotServer(args[0]);
        }
    }
    else{
        
    }
}

    // The following initialisation is REQUIRED for `flow` monitoring.
    // DO NOT REMOVE OR MODIFY THIS CODE.
    static {
        new Recorder().logRun();
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
