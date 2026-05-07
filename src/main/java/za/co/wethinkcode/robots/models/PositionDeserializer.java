package za.co.wethinkcode.robots.models;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class PositionDeserializer extends JsonDeserializer<Position>{

    @Override
    public Position deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        
        JsonToken token = p.currentToken();
        if (token == JsonToken.START_ARRAY){
            p.nextToken();
            int x = p.getIntValue();
            p.nextToken();
            int y = p.getIntValue();
            p.nextToken();
            return new Position(x, y);
        }

        if (token == JsonToken.START_OBJECT){
            int x = 0;
            int y = 0;
            while(p.nextToken()!=JsonToken.END_OBJECT){
                String field = p.getCurrentName();
                p.nextToken();

                switch (field) {
                    case "x":
                        x=p.getIntValue();
                        break;
                    case "y":
                        y=p.getIntValue();
                        break;
                
                    default:
                        break;
                }

            }
            return new Position(x, y);
        }
        throw ctxt.mappingException("Expected an array or object.");
    }
    
    
}
