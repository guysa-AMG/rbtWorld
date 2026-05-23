package za.co.wethinkcode.robots.models.impediment;
import za.co.wethinkcode.robots.models.Position;

public class EmptySpot extends Impediments {

    public EmptySpot( Position position) {
        super(position, "EMPTYSPOT");
    }

    @Override
    public void draw() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'draw'");
    }

    
}
