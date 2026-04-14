// # Abstract class or Interface
package za.co.wethinkcode.robots.server.commands;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Command {
  
    private String name;
    private String[] argument;

    Command(String name,String[] arguemnt){
        this.name=name;
        this.argument=argument;
    }
    Command(String name){
        this.name = name;
        this.argument = argument;
    }
    
}