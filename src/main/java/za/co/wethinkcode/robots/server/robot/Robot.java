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
     private OperationalMode status;

     private int worldWidth;
     private int worldHeight;

   
     public Robot(String name,int x, int y,int shield,int FRate) {
       
        this.position= new Position(x, y);
        this.direction = Directions.NORTH;
        this.name = name;
        this.fireRate = FRate;
        this.shield = shield;
        this.status = OperationalMode.NORMAL;
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

    public OperationalMode getStatus() {
        return this.status;
    }

    public void sendMessage(String string) {
    }

    public void setWorldBounds(int width, int height) {
        this.worldWidth = width;
        this.worldHeight = height;
    }

    public void setStatus(OperationalMode status) {
        this.status = status;
    }

    public void turnLeft() {
        switch (this.direction) {
            case NORTH -> this.direction = Directions.WEST;
            case WEST  -> this.direction = Directions.SOUTH;
            case SOUTH -> this.direction = Directions.EAST;
            case EAST  -> this.direction = Directions.NORTH;
        }
    }

    public void turnRight() {
        switch (this.direction) {
            case NORTH -> this.direction = Directions.EAST;
            case EAST  -> this.direction = Directions.SOUTH;
            case SOUTH -> this.direction = Directions.WEST;
            case WEST  -> this.direction = Directions.NORTH;
        }
    }

    public boolean moveForward(int steps) {
        return move(steps, this.direction);
    }

    public boolean moveBack(int steps) {
        return move(steps, getOppositeDirection(this.direction));
    }

    private boolean move(int steps, Directions direction) {
        int x = this.position.getX();
        int y = this.position.getY();

        for (int i = 0; i < steps; i++) {
            int newX = x;
            int newY = y;

            switch (direction) {
                case NORTH -> newY += 1;
                case SOUTH -> newY -= 1;
                case EAST  -> newX += 1;
                case WEST  -> newX -= 1;
            }

            if (isOutOfBounds(newX, newY)) {
                break;
            }

            x = newX;
            y = newY;
        }

        this.position = new Position(x, y);
        return true;
    }

    private boolean isOutOfBounds(int x, int y) {
        return x < -(worldWidth / 2) || x > (worldWidth / 2)
                || y < -(worldHeight / 2) || y > (worldHeight / 2);
    }

    private Directions getOppositeDirection(Directions dir) {
        return switch (dir) {
            case NORTH -> Directions.SOUTH;
            case SOUTH -> Directions.NORTH;
            case EAST  -> Directions.WEST;
            case WEST  -> Directions.EAST;
        };
    }

    protected boolean absorbDamage(int shots){
        if (this.shield>0){
            this.shield-=shots;
            if (this.shield <= 0) {
                this.shield = 0;
                this.status = OperationalMode.DEAD;
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean shootRobot(Robot robot){
        if (this.fireRate <= 0){
            sendMessage("No shots remaining, please reload.");
            return false;
        }
        this.fireRate--;
        return robot.absorbDamage(this.fireRate);

    }

    public void reload(int maxFireRate) {
        this.fireRate = maxFireRate;
        this.status = OperationalMode.NORMAL;
    }

    public void repair(int maxShield) {
        this.shield = maxShield;
        this.status = OperationalMode.NORMAL;
    }

    public boolean isDead() {
        return this.status == OperationalMode.DEAD;
    }
}
