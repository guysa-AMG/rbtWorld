package za.co.wethinkcode.robots.models.impediment;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;
import lombok.NonNull;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.server.world.WorldGenerator;

@Data
 public abstract class  Impediments extends JPanel {
  @NonNull
  protected Position position;
  @NonNull
  public String type;
  private Logger log ;
  @NonNull
  protected ImageIcon image;
  
  protected int width;
   
  protected int height;
  private Color obj;

  
/**
 * Impediments
 * this is the base for every object in the game.
 * @param pos
 * @param type
 * @param imagepath
 */
  protected Impediments(Position pos,String type,String imagepath){
  this.type=type;

  this.position=pos;
  this.log =LoggerFactory.getLogger(this.getClass());

  }
    protected Impediments(Position pos,String type){
  this.type=type;
  this.position=pos;
  this.log =LoggerFactory.getLogger(this.getClass());
 
  }


  public abstract void draw(Graphics g);

  
 

}
