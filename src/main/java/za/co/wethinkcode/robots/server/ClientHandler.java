// # Runnable/Thread to manage each connected robot
package za.co.wethinkcode.robots.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import za.co.wethinkcode.robots.models.transitmodels.ServerRequest;
import za.co.wethinkcode.robots.services.ITCService;
import za.co.wethinkcode.robots.shared.Protocol;

class ClientHandler implements Runnable {
    private Socket specificSock;
    private Logger log;
    private String client;
    private String robotName;
    private PrintWriter writer;

    public ClientHandler(Socket sock) {
        this.client = sock.getLocalAddress().getHostAddress();
        this.log = LoggerFactory.getLogger(ClientHandler.class);
        this.log.info("new connection to -> " + client);
        this.specificSock = sock;
    
    }

    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.specificSock.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new PrintWriter(new OutputStreamWriter(this.specificSock.getOutputStream(), StandardCharsets.UTF_8), true);
            Protocol protocol = new Protocol();
            String data;

            while ((data = br.readLine()) != null) {
                this.log.info("read => " + data + " from " + client);

                // Peek at the request to know when a launch happens so we can register for push events.
                try {
                    ServerRequest req = protocol.decodeRequest(data);
                    if (req != null && "launch".equalsIgnoreCase(req.getCommand()) && req.getRobot() != null) {
                        this.robotName = req.getRobot();
                        ITCService.getInstance().registerClient(this.robotName, this.writer);
                    }
                } catch (Exception ignore) { /* keep going — server still handles bad requests */ }

                String sendableData = ITCService.getInstance().doThisCommand(data);
                if ("off".equals(sendableData)) {
                    this.specificSock.close();
                    ITCService.getInstance().terminateServerThread(specificSock);
                    break;
                }
                this.log.warn("sending=> " + sendableData);
                synchronized (this.writer) {
                    this.writer.println(sendableData);
                    this.writer.flush();
                }
            }
        } catch (IOException e) {
            this.log.error(e.getMessage() + "[" + client + "]");
        } finally {
            if (this.robotName != null) {
                ITCService.getInstance().unregisterClient(this.robotName);
            }
        }
    }
}