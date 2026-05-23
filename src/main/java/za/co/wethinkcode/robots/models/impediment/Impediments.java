package za.co.wethinkcode.robots.models.impediment;

import lombok.Data;
import za.co.wethinkcode.robots.models.Position;


 public interface  Impediments {



   Position getPosition();

   void setPosition(Position pos);

   String getType();

}
