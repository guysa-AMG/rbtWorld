// # Interface for the world


package za.co.wethinkcode.robots.server.world;

import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.commands.Command;

public interface Iworld {

    public boolean addRobot(String rbt);

    public boolean removeRobot(String rbt);

    public ServerResponse perform(Command com);

}