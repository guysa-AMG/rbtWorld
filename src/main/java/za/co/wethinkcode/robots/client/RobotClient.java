package za.co.wethinkcode.robots.client;

/*
Iteration 1: simple CLI client that sends JSON strings to the server over a socket.
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import za.co.wethinkcode.robots.models.IpAddr;
import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;
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

public class RobotClient {

    private final String host;
    private final int port;

    private final ObjectMapper mapper = new ObjectMapper();

    private Socket socket;
    private BufferedReader serverIn;
    private PrintWriter serverOut;

    public RobotClient(IpAddr addr){
        host=addr.ip();
        port=addr.port();
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

            System.out.println("Connected to : " + host + ":" + port);
          while (run) {
             System.out.println("Type: <robotName> <command> [arguments....] (example: HAL launch)");
            System.out.println("Type: <robotName> quite to exit");

            commandLoop();}
        }catch (IOException e){
            System.out.println("Failed to connect to : " + host + ":" + port + "\n" + e.getMessage());
        }finally {
            shutDown();
        }
    }

    private void commandLoop() {
        try(
            Scanner console = new Scanner(System.in)){
            String userLine;
            while((userLine = console.nextLine()) != null){
                userLine = userLine.trim();
                if(userLine.isBlank()){continue;}

                ServerRequest request = toRequest(userLine);
                if(request == null){continue;};

                String json = new Protocol().encodeRequest(request).toString();
                if(json == null){continue;};

                serverOut.println(json);

                String responseJson = serverIn.readLine(); //here am assuming that the server sends one JSON object per line
               System.out.print(responseJson);
                if(responseJson == null){
                    System.out.println("Server Disconnected");
                    return;
                }
                //System.out.println(responseJson);
                handleResponse(responseJson);

                if("quit".equalsIgnoreCase(request.getCommandInstance().getCommandName())){
                    return;
                }
            }
        }catch (IOException e){
            System.out.println("I/O error in client loop ("+ e.getMessage()+")");
        }
    }

    private ServerRequest toRequest(String userLine) {
        String[] parts = userLine.split("\\s+");
        if(parts.length < 2){
            System.out.println("Invalid input. Use: <robotName> <command> [arguments....] (example: HAL launch)>");
            return null;
        }
        String robotName = parts[0];
        String command = parts[1];
        String[] arguments = (parts.length > 2) ? Arrays.copyOfRange(parts, 2, parts.length) : new String[0];

        return new ServerRequest(robotName, command, arguments);
    }

   
    private void handleResponse(String responseJson) {
        ServerResponse response =new Protocol().decodeResponse(responseJson);
        if(response == null){
            System.out.println("Received non-JSON/invalid response: " + responseJson);
            return;
        }
        String result = (response.getResult() == null ? "UNKNOWN" : response.getResult().toString());
        String message = (response.getData().get("message") == null ? "" : response.getData().get("message"));
        System.out.println(result + (message.isBlank() ? "" : message));

        try{
            if(response.getData() != null){
                System.out.println("DATA: " + mapper.writeValueAsString(response.getData()));
            }
            if(response.getState() != null){
                System.out.println("STATE: " + mapper.writeValueAsString(response.getState()));
            }
        }catch (JsonProcessingException ignored){
            // if data/state isn't JSON-serializable, i will just skip
        }
    }

    private void shutDown() {
        try{if (serverIn != null) serverIn.close();}catch (IOException e){}
        if(serverOut != null) serverOut.close();
        try{if (socket != null) socket.close();}catch (IOException e){}
    }
   
}

