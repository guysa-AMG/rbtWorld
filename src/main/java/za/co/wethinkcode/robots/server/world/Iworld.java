// # Interface for the world


package za.co.wethinkcode.robots.server.world;

import java.util.Map;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.robot.BaseRobot;

public interface Iworld {
    public final static float reloadTime=5f;
    public final static float repairTime=3f;
    public final static int visibleDistance=3;
    
    public boolean addRobot(String rbt);

    public void moveRobot(String RobotName,Position pos);
    
    public Map<String,BaseRobot> getAllRobots();
    
    public boolean removeRobot(String rbt);

    public ServerResponse perform(Command com);

}