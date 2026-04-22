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
}

