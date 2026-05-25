package za.co.wethinkcode.robots.models.impediment;
import java.awt.Graphics;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.impediment.ImpedimentsType.CanGoThrough;

@CanGoThrough
public class EmptySpot extends Impediments {

    public EmptySpot( Position position) {
        super(position, "EMPTYSPOT");
    }

    @Override
    public void draw(Graphics g) {
       
    }


    
}
