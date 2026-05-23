package za.co.wethinkcode.robots.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import za.co.wethinkcode.robots.models.transitmodels.ServerRequest;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;

/**
 * Protocol 
 * this class is responsible for the serialization and deserialization of 
 * server Request and response using the jackson lib
 */
public class Protocol implements IProtocol {
    private Logger logger;
    private final ObjectMapper mapper;

    public Protocol() {
        this.logger=LoggerFactory.getLogger(Protocol.class);
        this.mapper = new ObjectMapper();
        this.mapper.findAndRegisterModules();
        this.mapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
    }

    @Override
    public ServerRequest decodeRequest(String data) {
        try {
            ServerRequest req = mapper.readValue(data, ServerRequest.class);
            validateRequest(req);
            return req;
        } catch (Exception e) {
            throw new UnsupportedOperationException(e.getMessage());
        }
    }

    @Override
    public ServerResponse decodeResponse(String data) {
        try {
            this.logger.warn("decoding => "+data);
            return mapper.readValue(data, ServerResponse.class);
        } catch (Exception e) {
            throw new UnsupportedOperationException(e.getMessage());
        }
    }

    @Override
    public String encodeRequest(ServerRequest req) {
        try {
            return mapper
                    .writeValueAsString(req);
        } catch (JsonProcessingException e) {
            throw new UnsupportedOperationException(e.getMessage());
        }
    }

    @Override
    public String encodeResponse(ServerResponse res) {
        try {
            return mapper
                    .writeValueAsString(res);
        } catch (JsonProcessingException e) {
            throw new UnsupportedOperationException(e.getMessage());
        }
    }

     public void updatResponse(ServerResponse old,ServerResponse new_data){
        try {
            mapper.updateValue(old,new_data);
        } catch (JsonMappingException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateRequest(ServerRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (req.getRobot() == null) {
            throw new IllegalArgumentException("Robot name is required");
        }
        if (req.getCommand() == null) {
            throw new IllegalArgumentException("Command is required");
        }
        if (req.getArguments() == null) {
            throw new IllegalArgumentException("Argument is required");
        }
    }
}
