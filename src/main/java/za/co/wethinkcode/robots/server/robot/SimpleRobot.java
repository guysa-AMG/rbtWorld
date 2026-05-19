package za.co.wethinkcode.robots.server.robot;

import za.co.wethinkcode.robots.models.Position;

public class SimpleRobot extends BaseRobot {
    private String type="ROBOT";
    private Position pos;
    
    
    public SimpleRobot(String name, int x, int y, int shield, int FRate) {
        super(name, x, y, shield, FRate);
        //TODO Auto-generated constructor stub
    }

    @Override
    public void setPosition(Position pos) {
        this.pos=pos;
    }

    @Override
    public String getType() {
       return this.type;
    }
    
}
