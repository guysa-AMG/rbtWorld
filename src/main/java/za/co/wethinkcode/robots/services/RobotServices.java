package za.co.wethinkcode.robots.services;

import java.util.logging.Logger;

public class RobotServices {
   private static RobotServices instance = new RobotServices();
   private Logger log;

    private RobotServices(){
        this.log =  Logger.getLogger("Robot Service");
    }

    public static  RobotServices getInstance(){
        return instance;
    }
    public addRobot(Robot){
        
    }


    public void  getAllPlayers(){}
    //must implement
    public Boolean isValid(){

        return true;
    }
    public void execute(){

    }

}
