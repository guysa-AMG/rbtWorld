// # Implementation of the 2D world logic
package za.co.wethinkcode.robots.server.world;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.commands.CommandTypeEnum;
import za.co.wethinkcode.robots.server.robot.BaseRobot;

public class RobotWorld implements Iworld{
    public Map<String,BaseRobot> robots;
    private Logger log;

   
   public RobotWorld(){
        this.log=LoggerFactory.getLogger(RobotWorld.class);
        this.robots=new HashMap<>();
    }
    
    @Override
    public boolean addRobot(String rbt) {
     BaseRobot robot= BaseRobot.Builder(rbt, 0, 0, 100,50);
     robots.put(rbt, robot);
     this.log.info("registered "+rbt+" into ROBOT WORLD !!!!");
     return true;
    }

    

    @Override
    public boolean removeRobot(String rbt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeRobot'");
    }

    @Override
    public ServerResponse perform(Command com) {
        this.log.info("executing "+com.getCommandName()+" command for "+com.getRobotName());

        BaseRobot robot = this.robots.get(com.getRobotName());
      return  com.execute(this,robot);

        
    }

    public void moveRobot(String robotName,Position desiredPosition){
        BaseRobot robot = this.robots.get(robotName);

        //TODO: Move to desiredPosition
     
    }



    @Override
    public Map<String, BaseRobot> getAllRobots() {
       return this.robots;
    }

    
}