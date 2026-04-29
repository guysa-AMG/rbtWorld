package za.co.wethinkcode.robots.server.robot;

import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.impediment.Impediments;

public abstract class Robot extends Impediments {
     private String name;
     private Position position;
       
     private Directions direction;

     private int shield;
     private int fireRate;

   
     public Robot(String name,int x, int y,int shield,int FRate) {
       
        this.position= new Position(x, y);
        this.direction = Directions.NORTH;
        this.name = name;
        this.fireRate = FRate;
        this.shield = 20;
    }
   
     protected boolean absorbDamage(int shots){
     if (this.shield>0){

          this.shield-=shots;

          return true;
     }
          else{
               return false;
          }
    }
    
     public boolean shootRobot(Robot robot){
         if (robot.absorbDamage(this.fireRate)){
          return true;
         }
         else{
          return false;
         }
     }
   
     public String getName(){
          return this.name;
      }
    
     public Position getPosition(){
          return this.position;
     }

     public boolean updatePosition(Position pos){
          this.position = pos;
          return true;
     }


    public Directions getDirection() {
         return this.direction;
    }

    public int getShields() {
         return this.shield;
    }

    public int getFireRate() {
         return this.fireRate;
    }

    public void sendMessage(String string) {
    }
}
