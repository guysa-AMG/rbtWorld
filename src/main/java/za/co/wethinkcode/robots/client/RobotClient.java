package za.co.wethinkcode.robots.client;

/*
Iteration 1: simple CLI client that sends JSON strings to the server over a socket.
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RobotClient {

    private final String host;
    private final int port;

    private final ObjectMapper mapper = new ObjectMapper();

    private Socket socket;
    private BufferedReader serverIn;
    private PrintWriter serverOut;


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
        try{
            socket = new Socket(host, port);
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            serverOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            System.out.println("Connected to : " + host + ":" + port);
            System.out.println("Type: <robotName> <command> [arguments....] (example: HAL launch)");
            System.out.println("Type: <robotName> quite to exit");

            commandLoop();
        }catch (IOException e){
            System.out.println("Failed to connect to : " + host + ":" + port + "\n" + e.getMessage());
        }finally {
            shutDown();
        }
    }

    private void commandLoop() {
        try(BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))){
            String userLine;
            while((userLine = console.readLine()) != null){
                userLine = userLine.trim();
                if(userLine.isBlank()){continue;}

                Request request = toRequest(userLine);
                if(request == null){continue;};

                String json = toJson(request);
                if(json == null){continue;};

                serverOut.println(json);

                String responseJson = serverIn.readLine(); //here am assuming that the server sends one JSON object per line
                if(responseJson == null){
                    System.out.println("Server Disconnected");
                    return;
                }
                //System.out.println(responseJson);
                handleResponse(responseJson);

                if("quit".equalsIgnoreCase(request.getCommand())){
                    return;
                }
            }
        }catch (IOException e){
            System.out.println("I/O error in client loop ("+ e.getMessage()+")");
        }
    }

    private Request toRequest(String userLine) {
        String[] parts = userLine.split("\\s+");
        if(parts.length < 2){
            System.out.println("Invalid input. Use: <robotName> <command> [arguments....] (example: HAL launch)>");
            return null;
        }
        String robotName = parts[0];
        String command = parts[1];
        String[] arguments = (parts.length > 2) ? Arrays.copyOfRange(parts, 2, parts.length) : new String[0];

        return new Request(robotName, command, arguments);
    }

    private String toJson(Request request) {
        try {
            return mapper.writeValueAsString(request);
        }catch (JsonProcessingException e){
            System.out.println("Failed to serialize request to Json(" + e.getMessage() + ")");
            return null;
        }
    }

    private Response fromJson(String json) {
        try{
            return mapper.readValue(json, Response.class);
        }catch (Exception e){
            return null;
        }
    }

    public static final class Request{
        private String robot;
        private String command;
        private String[] arguments;

        public  Request(){}

        public  Request(String robot, String command, String[] arguments){
            this.robot = robot;
            this.command = command;
            this.arguments = arguments;
        }

        public String getRobot() { return robot; }
        public void setRobot(String robot) { this.robot = robot; }

        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }

        public String[] getArguments() { return arguments; }
        public void setArguments(String[] arguments) { this.arguments = arguments; }
    }

    public static final class Response{
        private String result;
        private String message;
        private Object data;
        private Object state;

        public Response(){}
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }

        public Object getState() { return state; }
        public void setState(Object state) { this.state = state; }

    }

    private void handleResponse(String responseJson) {
        Response response = fromJson(responseJson);
        if(response == null){
            System.out.println("Received non-JSON/invalid response: " + responseJson);
            return;
        }
        String result = (response.getResult() == null ? "UNKNOWN" : response.getResult());
        String message = (response.getMessage() == null ? "" : response.getMessage());
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

    public static void main(String[] args) {
        String host = "localhost";
        int port = 2146;
        // id = 20.20.18.206

        if (args != null && args.length >= 1 && args[0] != null && !args[0].isBlank()) {
            host = args[0];
        }
        if (args != null && args.length >= 2 && args[1] != null && !args[1].isBlank()) {
            port = Integer.parseInt(args[1]);
        }

        RobotClient client = new RobotClient(host, port);
        client.start();
    }
}

// mvn --% -DskipTests compile exec:java -Dexec.mainClass=za.co.wethinkcode.robots.client.RobotClient -Dexec.args="20.20.18.206 2146"