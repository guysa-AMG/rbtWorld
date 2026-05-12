package za.co.wethinkcode.robots.client;

/*
Iteration 1: simple CLI client that sends JSON strings to the server over a socket.
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.EnumValues;

import za.co.wethinkcode.robots.errors.InvalidCommandException;
import za.co.wethinkcode.robots.models.IpAddr;
import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.ServerResponseData;
import za.co.wethinkcode.robots.models.ServerResponseState;
import za.co.wethinkcode.robots.server.commands.CommandTypeEnum;
import za.co.wethinkcode.robots.shared.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RobotClient {

    private final String host;
    private Logger log;
    private final int port;
    private String robotName=null;
    private final ObjectMapper mapper = new ObjectMapper();
    private ServerResponseData data;
    private ServerResponseState state;
    private Socket socket;
    private BufferedReader serverIn;
    private PrintWriter serverOut;
    private ConsoleInteraction ui;

    public RobotClient(IpAddr addr){

        host=addr.ip();
        port=addr.port();

        this.log = LoggerFactory.getLogger(RobotClient.class);
     
    }
    public RobotClient(String host, int port) {
      
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }
        this.host = host;
        this.port = port;
    }

    public void start() {
          boolean run=true;

        try{
            
            socket = new Socket(host, port);



            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            serverOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            
            //TODO rather call interaction into print
            System.out.println("Connected to : " + host + ":" + port);

             ui = new ConsoleInteraction();
            System.out.println(ui.getBenderAscii());

            Scanner scan = new Scanner(System.in);
          while (run) {
            //TODO rather call interaction into print 
            System.out.println("Type: <robotName> <command> [arguments....] (example: HAL launch)");
            //TODO rather call interaction into print
             System.out.println("Type: <robotName> quite to exit");

            commandLoop(scan);}
        }catch (IOException e){
            //TODO rather call interaction into print
            System.out.println("Failed to connect to : " + host + ":" + port + "\n" + e.getMessage());
        }finally {
            shutDown();
        }
           //TODO call onto the interaction class to print Story
    }

    private void commandLoop(Scanner scan) {
        try{

            
          String userLine="";
          //TODO rather call interaction into print      
           System.out.print("command >");
            while((userLine = scan.nextLine())!=null){
               
                userLine = userLine.trim();
                if(userLine.isBlank()){continue;}
                try{
                if (this.robotName!=null){
                    userLine=this.robotName+" "+userLine;
                }
                if(userLine.contains("help")){
                    ui.displayHelp();
                    continue;
                }
                ServerRequest request = toRequest(userLine);

                this.robotName = request.getRobot();
                if(request == null){ continue; };

                String json = new Protocol().encodeRequest(request).toString();
                if(json == null){continue;};

                serverOut.println(json);
               
                serverOut.flush();
                

                String responseJson = serverIn.readLine(); //here am assuming that the server sends one JSON object per line
               
                if(responseJson == null){
                    //TODO rather call interaction into print
                    System.out.println("Server Disconnected");
                    return;
                }
              
                handleResponse(responseJson);

                if("quit".equalsIgnoreCase(request.getCommand())){
                    return;
                }}
                catch(InvalidCommandException err){
                    System.err.println("[x] Invalid Command");
                }
                
                if(this.data.getMessage()=="BLOCKED"){
                        //TODO rather call interaction into print
                 
                    System.out.println("\033[91m you hit a object \033[00m");
               
                }
                if ( this.state !=null && this.robotName!=null){
                    //TODO rather call interaction into print
                 
                    System.out.printf("{%s}[%s,%s] %s > ",this.state.getDirection(),this.state.getPosition().getX(),this.state.getPosition().getY(),this.robotName);
                }
                else{
                    //TODO rather call interaction into print
                    System.out.print("Command > ");
                }

            }
        }catch (IOException e){
            //TODO rather call interaction into print
            System.out.println(" \0x33[91m I/O error in client loop ("+ e.getMessage()+")");
        }
    }

    public static ServerRequest toRequest(String userLine) throws InvalidCommandException  {
        String[] parts = userLine.split("\\s+");
        
        if(parts.length < 2){
            //TODO rather call interaction into print
            System.out.println("Invalid input. Use: <robotName> <command> [arguments....] (example: HAL launch)>");
            return null;
        }
        String robotName = parts[0];
        String command = parts[1].toLowerCase();
       
       try{
        CommandTypeEnum.valueOf(command);
       }
       catch(IllegalArgumentException illegal){
              throw new InvalidCommandException();
       }
        
        String[] arguments = (parts.length > 2) ? Arrays.copyOfRange(parts, 2, parts.length) : new String[0];

        return new ServerRequest(robotName, command, arguments);
    }

   
    private void handleResponse(String responseJson) {
        ServerResponse response =new Protocol().decodeResponse(responseJson);
        if(response == null){
            //TODO rather call interaction into print
            System.out.println("Received non-JSON/invalid response: " + responseJson);
            return;
        }
        this.log.info("client side got "+responseJson);
        String result = (response.getResult() == null ? "UNKNOWN" : response.getResult().toString());
        String message = (response.getData().getMessage() == null ? "" : response.getData().getMessage());
      
        if(response.getData() != null){
            this.data = response.getData();
        }
        
    }

    private void shutDown() {
        try{if (serverIn != null) serverIn.close();}catch (IOException e){}
        if(serverOut != null) serverOut.close();
        try{if (socket != null) socket.close();}catch (IOException e){}
    }
   
}

