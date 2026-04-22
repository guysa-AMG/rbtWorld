package za.co.wethinkcode.robots.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;

public class Protocol implements IProtocol {

    private final ObjectMapper mapper;

    public Protocol() {
        this.mapper = new ObjectMapper();
        this.mapper.findAndRegisterModules();
        this.mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
        );
    }

    @Override
    public ServerRequest decodeRequest(String data) {
        try {
            ServerRequest req = mapper.readValue(data, ServerRequest.class);
            validateRequest(req);
            return req;
        } catch (Exception e) {
            throw new UnsupportedOperationException("Unimplemented method 'decodeRequest'");
        }
    }

    @Override
    public ServerResponse decodeResponse(String data) {
        try {
            return mapper.readValue(data, ServerResponse.class);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Unimplemented method 'decodeResponse'");
        }
    }

    @Override
    public String encodeRequest(ServerRequest req) {
        try {
            return mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(req);
        } catch (JsonProcessingException e) {
            throw new UnsupportedOperationException("Unimplemented method 'encodeRequest'");
        }
    }

    @Override
    public String encodeResponse(ServerResponse res) {
        try {
            return mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(res);
        } catch (JsonProcessingException e) {
            throw new UnsupportedOperationException("Unimplemented method 'encodeResponse'");
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
