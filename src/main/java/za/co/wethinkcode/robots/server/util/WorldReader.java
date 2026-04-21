package za.co.wethinkcode.robots.server.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.slf4j.LoggerFactoryFriend;

import za.co.wethinkcode.robots.server.Server;

public class WorldReader {
private Properties prop;
  public  WorldReader(){
          try {
    this.prop=new Properties();
    InputStream worldConfigStream =Server.class.getResourceAsStream("/world.properties");
  
   prop.load(worldConfigStream);
 
  
   } catch (FileNotFoundException e) {
  
    e.printStackTrace();
   } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
}

    }

public String[] getWorldMap(){
 
    String rawMap = (String) this.prop.get("robotWorld");
    String[] map = rawMap.split(",");
    return map;
}
}
