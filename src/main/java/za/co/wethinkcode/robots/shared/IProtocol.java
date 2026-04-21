package za.co.wethinkcode.robots.shared;

import com.fasterxml.jackson.databind.ObjectMapper;

import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;

public interface IProtocol {
    
     
    public ServerRequest decodeRequest(String data);

    public ServerResponse decodeResponse(String data);

    public String encodeRequest(ServerRequest req);

    public String encodeResponse(ServerResponse res);
}
