/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syntaxit;

/**
 *
 * @author Enache
 * @param <T>
 */


public class Variable<T extends Object> {
    public final TYPES.VARS type;
    public T value;
    public final String name;
    
    public Variable(TYPES.VARS type, String name, T Value){
        this.type=type;
        this.name = name;
        this.value =  Value;
    }
    
   public T getValue(){
       return value;
   }
   
   public void setValue(T value){
       this.value = value;
   }
   
   public String getName(){
       return name;
   }

}

