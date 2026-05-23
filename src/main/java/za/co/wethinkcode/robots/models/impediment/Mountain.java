package za.co.wethinkcode.robots.models.impediment;

import lombok.NonNull;
import za.co.wethinkcode.robots.models.Position;

public class Mountain extends Impediments {

    public Mountain( Position position) {
        super(position, "MOUNTAIN");
        
       
    }

    @Override
    public void draw() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'draw'");
    }




    
}
