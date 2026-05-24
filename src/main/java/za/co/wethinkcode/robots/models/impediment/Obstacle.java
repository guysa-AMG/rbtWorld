package za.co.wethinkcode.robots.models.impediment;

import java.awt.Graphics;

import za.co.wethinkcode.robots.models.Position;

public  class Obstacle extends Impediments {
        
        private  int x1, y1, x2, y2;
        private final String type;

        public Obstacle(int x1, int y1, int x2, int y2, String type) {
            super(new Position(x2, y2), type,"cuterbt.gif");
            this.x1 = x1; this.y1 = y1;
            this.x2 = x2; this.y2 = y2;
            this.type = type;
            
        }

        public boolean isAt(int x, int y) {
            return (x >= x1 && x <= x2) && (y <= y1 && y >= y2);
        }
        public Position getPos(){
            return new Position(x1, y1);
        } 
        

        public String getType() { return type; }

       
        public Position getPosition() {
            // TODO Auto-generated method stub
                   return new Position(x1, y1);
        }

  
        public void setPosition(Position pos) {
            // TODO Auto-generated method stub
            this.x1=pos.getX();
            this.y1=pos.getY();
        }

        @Override
        public void draw(Graphics g) {
      
        }

     
    }
