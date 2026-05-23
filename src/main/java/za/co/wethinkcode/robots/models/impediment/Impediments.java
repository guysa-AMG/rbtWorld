package za.co.wethinkcode.robots.models.impediment;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import za.co.wethinkcode.robots.models.Position;

@Data
@RequiredArgsConstructor
 public abstract class  Impediments extends JPanel {

  @NonNull
  protected Position position;
  @NonNull
  public String type;
  
  protected ImageIcon image;
  
  

  public abstract void draw();

  
 

}
