package za.co.wethinkcode.robots.client;

/*
Iteration 1: simple CLI client that sends JSON strings to the server over a socket.
 */

import com.fasterxml.jackson.databind.ObjectMapper;

import za.co.wethinkcode.robots.client.gui.ClientGui;
import za.co.wethinkcode.robots.errors.InvalidCommandException;
import za.co.wethinkcode.robots.models.IpAddr;
import za.co.wethinkcode.robots.models.OperationalMode;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerRequest;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseObject;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseState;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.commands.CommandTypeEnum;
import za.co.wethinkcode.robots.server.commands.QuitCommand;
import za.co.wethinkcode.robots.shared.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RobotClient {

    private final String host;
    private Logger log;
    private final int port;
    private String robotName=null;
    private volatile String lastCommand = null;
    private volatile boolean lookExpanded = false;
    private final ObjectMapper mapper = new ObjectMapper();
    private ServerResponse oldResponse;

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
        this.log = LoggerFactory.getLogger(RobotClient.class);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: <host> <port> [--console]");
            return;
        }
        String host = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Port must be a number, got: " + args[1]);
            return;
        }
        boolean console = args.length > 2 && "--console".equalsIgnoreCase(args[2]);
        RobotClient client = new RobotClient(host, port);
        if (console) {
            client.start();
        } else {
            client.startGui();
        }
    }

    public void startGui() {
        ClientGui gui = new ClientGui(host, port);
        try {
            socket = new Socket(host, port);
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            serverOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            gui.setStatus("connected to " + host + ":" + port);
            gui.appendLog("Connected to " + host + ":" + port);
            gui.appendLog("Type: <robotName> launch    e.g.  HAL launch");
            gui.appendLog("After launch, you can omit the name and just type commands (look, forward 3, ...)");

            gui.setOnSend(line -> sendUserLine(line, gui));

            Thread reader = new Thread(() -> readerLoop(gui), "server-reader");
            reader.setDaemon(true);
            reader.start();
        } catch (UnknownHostException e) {
            gui.setStatus("unknown host: " + host);
            gui.appendLog("Unknown host: '" + host + "' — try 'localhost' or a valid IP/hostname.");
        } catch (ConnectException e) {
            gui.setStatus("connection refused");
            gui.appendLog("Could not connect to " + host + ":" + port + " — is the server running? (" + e.getMessage() + ")");
        } catch (IOException e) {
            gui.setStatus("I/O error");
            gui.appendLog("Failed to connect: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void sendUserLine(String rawLine, ClientGui gui) {
        String userLine = rawLine.trim();
        if (userLine.isBlank()) return;
        gui.appendLog("> " + userLine);

        try {
            String normalised = normaliseCommand(userLine, gui);
            if (normalised == null) return;
            ServerRequest request = toRequest(normalised);
            if (request == null) return;

            // Only adopt the robot name from a launch request — never overwrite from other commands.
            if ("launch".equalsIgnoreCase(request.getCommand())) {
                this.robotName = request.getRobot();
                gui.setSelfName(this.robotName);
            }

            this.lastCommand = request.getCommand();
            String json = new Protocol().encodeRequest(request).toString();
            serverOut.println(json);
            serverOut.flush();
        } catch (InvalidCommandException err) {
            gui.appendLog("[x] Invalid command: " + userLine);
        } catch (Exception ex) {
            gui.appendLog("[x] Failed to send: " + ex.getMessage());
        }
    }

    /**
     * Decide how to interpret what the user typed.
     * Returns the canonical "<robotName> <command> [args...]" string, or null if it should be dropped.
     */
    private String normaliseCommand(String userLine, ClientGui gui) {
        String[] parts = userLine.split("\\s+");
        if (parts.length == 0) return userLine;

        boolean firstIsCommand = isCommandKeyword(parts[0]);

        if (firstIsCommand) {
            // User typed just "<command> [args]"
            if (this.robotName == null) {
                gui.appendLog("[x] You must launch first. Try: <name> launch");
                return null;
            }
            return this.robotName + " " + userLine;
        }

        // First token is NOT a known command — treat it as a robot name.
        // Only allow this when there's a real command in position 1 AND either we have no locked name yet
        // OR the typed name matches the locked one.
        if (parts.length < 2 || !isCommandKeyword(parts[1])) {
            gui.appendLog("[x] Unknown command. Use: <name> launch  OR  <command> [args]");
            return null;
        }
        if (this.robotName != null && !parts[0].equalsIgnoreCase(this.robotName)) {
            gui.appendLog("[x] This client controls '" + this.robotName + "'. Ignoring '" + parts[0] + "'.");
            // Rewrite to use the locked name instead
            StringBuilder rest = new StringBuilder(this.robotName);
            for (int i = 1; i < parts.length; i++) rest.append(' ').append(parts[i]);
            return rest.toString();
        }
        return userLine;
    }

    private static boolean isCommandKeyword(String token) {
        try {
            CommandTypeEnum.valueOf(token.toLowerCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void readerLoop(ClientGui gui) {
        try {
            String line;
            while ((line = serverIn.readLine()) != null) {
                handleGuiResponse(line, gui);
            }
            gui.appendLog("Server disconnected.");
            gui.setStatus("disconnected");
        } catch (IOException e) {
            gui.appendLog("Connection lost: " + e.getMessage());
            gui.setStatus("disconnected");
        }
    }

    private void handleGuiResponse(String responseJson, ClientGui gui) {
        ServerResponse response = new Protocol().decodeResponse(responseJson);
        if (response == null) {
            gui.appendLog("<< " + responseJson);
            return;
        }
        String result = response.getResult() == null ? "UNKNOWN" : response.getResult().toString();
        ServerResponseData data = response.getData();
        ServerResponseState state = response.getState();
        String message = (data == null || data.getMessage() == null) ? "" : data.getMessage();

        // Global NPC broadcast — keep the line distinct so it stands out.
        if (message != null && message.startsWith("[Guyser_Thekiller]")) {
            gui.appendLog(">>> " + message + " <<<");
        } else {
            gui.appendLog("<< [" + result + "] " + message);
        }

        // Always push the full robot snapshot + pickups so the GUI renders everyone, even after events.
        if (data != null && data.getRobots() != null) {
            gui.setAllRobots(data.getRobots());
        }
        if (data != null && data.getPickups() != null) {
            gui.setPickups(data.getPickups());
        }

        // Combat event pushed from the server: we got hit by someone else.
        if (message != null && message.startsWith("HIT_BY ")) {
            gui.appendLog("[!] " + message + " — shield now "
                    + (state != null ? state.getShields() : "?"));
            if (state != null) gui.setHud(state.getShields(), state.getShots(),
                    za.co.wethinkcode.robots.server.world.Iworld.MAG_MAX, state.getShields(), 0);
            return;
        }

        // Combat event pushed from the server: we were eliminated by someone else.
        if (message != null && message.startsWith("KILLED_BY ")) {
            gui.appendLog("[X] " + message + ". You have been eliminated. Type <name> launch to play again.");
            handleDeath(gui, "was eliminated", state != null ? state.getPosition() : null);
            return;
        }

        // Robot died from a pit on the previous move
        if ("FELL_IN_PIT".equalsIgnoreCase(message)) {
            handleDeath(gui, "fell into a pit", data != null ? data.getPosition() : null);
            return;
        }

        // Server says our robot is unknown — likely we died and missed the message.
        if (message != null && message.contains("has not been launched")) {
            handleDeath(gui, "is no longer in the world", null);
            return;
        }

        // Look toggles the expanded visibility radius on the next response from the server.
        if ("look".equalsIgnoreCase(this.lastCommand)) {
            this.lookExpanded = true;
        } else if ("forward".equalsIgnoreCase(this.lastCommand) || "back".equalsIgnoreCase(this.lastCommand)) {
            this.lookExpanded = false;
        }

        if (state != null) {
            if (state.getPosition() != null && this.robotName != null) {
                gui.updateRobot(this.robotName, state.getPosition(), state.getDirection());
                gui.setFog(state.getPosition(), this.lookExpanded);
                gui.setStatus(this.robotName + " {" + state.getDirection() + "} ["
                        + state.getPosition().getX() + "," + state.getPosition().getY() + "]"
                       
                        + "  Bullets:" + state.getShots() + "/" + za.co.wethinkcode.robots.server.world.Iworld.MAG_MAX
                        + "  Shield:" + state.getShields());
            }
            gui.setHud(state.getShields(), state.getShots(),
                    za.co.wethinkcode.robots.server.world.Iworld.MAG_MAX, state.getShields(), 0);
            this.oldResponse.setState(state);
        }
        if (data != null) {
            this.oldResponse.setData(data);
            if (data.getPickups() != null) {
                gui.setPickups(data.getPickups());
            }
        }

        if ("RESPAWNED".equalsIgnoreCase(message)) {
            gui.appendLog("[*] You died but respawned at ("
                    + (state != null && state.getPosition() != null ? state.getPosition().getX() + "," + state.getPosition().getY() : "?")
                    + "). Lives left: " + (state != null ? state.getShields() : "?"));
        }

        if ("fire".equalsIgnoreCase(this.lastCommand) && this.robotName != null && this.oldResponse.getState() != null && data != null) {
            int dist = Math.max(1, data.getDistance());
            boolean hit = "Hit".equalsIgnoreCase(message);
            gui.flashBullet(this.robotName, this.oldResponse.getState().getDirection(), dist, hit);
        }

        if ("look".equalsIgnoreCase(this.lastCommand) && data != null && data.getObjects() != null) {
            renderLook(gui, data.getObjects());
        }
    }

    private void handleDeath(ClientGui gui, String reason, Position deathPos) {
        String wasName = this.robotName;
        if (wasName != null) {
            gui.appendLog("[*] " + wasName + " " + reason
                    + (deathPos != null ? " at (" + deathPos.getX() + "," + deathPos.getY() + ")" : "")
                    + ". Launch again with: <name> launch");
            gui.removeRobot(wasName);
        }
        this.robotName = null;
        this.oldResponse.setState(null);
        gui.setSelfName(null);
        gui.setFog(null, false);
        gui.setStatus("not launched — type <name> launch");
    }

    private void renderLook(ClientGui gui, List<ServerResponseObject> objects) {
        if (objects.isEmpty()) {
            gui.appendLog("   (nothing in sight within " + za.co.wethinkcode.robots.server.world.Iworld.lookRange + " cells)");
            return;
        }
        // Sort by direction then distance for readability
        java.util.Map<za.co.wethinkcode.robots.models.Directions, java.util.List<za.co.wethinkcode.robots.models.transitmodels.ServerResponseObject>> grouped = new java.util.EnumMap<>(za.co.wethinkcode.robots.models.Directions.class);
        for (var o : objects) grouped.computeIfAbsent(o.getDirection(), d -> new java.util.ArrayList<>()).add(o);
        for (var dir : za.co.wethinkcode.robots.models.Directions.values()) {
            var list = grouped.get(dir);
            if (list == null) continue;
            list.sort(java.util.Comparator.comparingInt(za.co.wethinkcode.robots.models.transitmodels.ServerResponseObject::getDistance));
            for (var o : list) {
                String label = o.getName() != null ? o.getSubtype() + " " + o.getName() : o.getSubtype();
                String posStr = o.getPosition() != null
                        ? " (" + o.getPosition().getX() + "," + o.getPosition().getY() + ")"
                        : "";
                gui.appendLog(String.format("   %-5s %2d cells -> %s%s", dir, o.getDistance(), label, posStr));
            }
        }
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

            
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                System.out.print("\nshutting down please be patient ...");
                ServerRequest req= new ServerRequest(robotName, "quit");
               String quitReq = new Protocol().encodeRequest(req).toString();
               serverOut.println(quitReq);
            }));
          while (run) {
            //TODO rather call interaction into print 
            System.out.println("Type: <robotName> <command> [arguments....] (example: HAL launch)");
            //TODO rather call interaction into print
             System.out.println("Type: <robotName> quite to exit");

            commandLoop(scan);}
        }catch (java.net.UnknownHostException e){
            System.out.println("Unknown host: '" + host + "' could not be resolved. Try 'localhost' or a valid IP/hostname.");
        }catch (java.net.ConnectException e){
            System.out.println("Could not connect to " + host + ":" + port + " — is the server running on that port? (" + e.getMessage() + ")");
        }catch (IOException e){
            System.out.println("Failed to connect to " + host + ":" + port + " — " + e.getClass().getSimpleName() + ": " + e.getMessage());
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
                
            
                if ( this.oldResponse !=null && this.robotName!=null &&this.oldResponse.getState()!=null){
                    //TODO rather call interaction into print
                 
                    System.out.printf("{%s}[%s,%s] %s > ",this.oldResponse.getState().getDirection(),this.oldResponse.getState().getPosition().getX(),this.oldResponse.getState().getPosition().getY(),this.robotName);
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
        Protocol parser = new Protocol();
        ServerResponse response =parser.decodeResponse(responseJson);
    
       if (oldResponse==null){ oldResponse=response ; }
    
       String message;
        if (response.getData() !=null)
      { if((message = response.getData().getMessage())!=null && response.getData().getMessage() !="DONE"){
        String widget=response.getResult()==StatusCode.OK?ConsoleInteraction.ANSI_GREEN+"[I] "+ConsoleInteraction.ANSI_RESET:ConsoleInteraction.ANSI_RED+"[x] "+ConsoleInteraction.ANSI_RESET;
        System.out.println(widget+message);
       }}
         if(response.getState()!=null)
       {
         if (response.getState().getStatus() == OperationalMode.DEAD){
        robotName =null;
        oldResponse=null;
        return;
       }}
        parser.updatResponse(oldResponse, response);
        if(response == null){
            //TODO rather call interaction into print
            System.out.println("Received non-JSON/invalid response: " + responseJson);
            return;
        }
        this.log.info("client side got "+responseJson);
        String result = (response.getResult() == null ? "UNKNOWN" : response.getResult().toString());
     
       
        
    }

    private void shutDown() {
        try{if (serverIn != null) serverIn.close();}catch (IOException e){}
        if(serverOut != null) serverOut.close();
        try{if (socket != null) socket.close();}catch (IOException e){}
    }
   
}

