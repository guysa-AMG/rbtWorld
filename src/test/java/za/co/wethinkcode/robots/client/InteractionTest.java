package za.co.wethinkcode.robots.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.errors.InvalidCommandException;
import za.co.wethinkcode.robots.models.ServerRequest;

public class InteractionTest {
    
    @Nested
    @DisplayName(value = "command parser test")
    class ParserTest{


    @Test
    @DisplayName(value = "regular forward command test")
    void commandParser(){
        assertDoesNotThrow(()->{
       ServerRequest req = RobotClient.toRequest("Mark Forward 10");
        assertEquals("ServerRequest(robot=Mark, command=forward, arguments=[10])",
                    req.toString());});
    }

    @Test
    @DisplayName(value = "multiple arged command test like Launch with all of it's parameter")
    void testMultiArgedCommandParser(){
        assertDoesNotThrow(()->{
       ServerRequest req = RobotClient.toRequest("Mark  Launch Speed 45 60 92");
       assertEquals("ServerRequest(robot=Mark, command=launch, arguments=[Speed, 45, 60, 92])",
                        req.toString());});
    }


    @Test
    void testMultiWhiteSpacedCommandParser(){
        assertDoesNotThrow(()->{
       ServerRequest req = RobotClient.toRequest("Mark            Turn            right               ");  
        assertEquals("ServerRequest(robot=Mark, command=turn, arguments=[right])",
                    req.toString());});
    }

     @Test
    @DisplayName(value = "Command")
    void testNoArgCommandParser(){
        assertDoesNotThrow(()->{
        ServerRequest req = RobotClient.toRequest("Mark  Quit");
        assertEquals("ServerRequest(robot=Mark, command=quit, arguments=[])",
            req.toString());});
    }
}


    @DisplayName(value = "invalid Command Test")
    @Test
    void nonExistingCommands(){
        assertThrows(InvalidCommandException.class, 
            ()->{
                RobotClient.toRequest("Mark fly up 20");
            });
    }
}
