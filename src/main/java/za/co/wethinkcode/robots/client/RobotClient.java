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
            System.out.println("Tyoe: <robotName> quite to exit");

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
        return null;
    }

    private String toJson(Request request) {
        return null;
    }

    private Response fromJson(String json) {
        return null;
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

    private void handleResponse(String responseJson) {}

    private void shutDown() {}

    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;

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

