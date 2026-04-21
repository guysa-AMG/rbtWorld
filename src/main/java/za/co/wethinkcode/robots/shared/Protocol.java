package za.co.wethinkcode.robots.shared;
// # Constants for JSON keys/values

import com.fasterxml.jackson.databind.ObjectMapper;

import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;

public class Protocol implements IProtocol{

    @Override
    public ServerRequest decodeRequest(String data) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decodeRequest'");
    }

    @Override
    public ServerResponse decodeResponse(String data) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decodeResponse'");
    }

    @Override
    public String encodeRequest(ServerRequest req) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encodeRequest'");
    }

    @Override
    public String encodeResponse(ServerResponse res) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encodeResponse'");
    }
   

}