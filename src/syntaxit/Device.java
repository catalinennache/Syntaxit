/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syntaxit;



import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 * @author Enache
 */


public class Device  {
    public ArrayList<Variable> environment_vars;
    public final TYPES.DEVICES  type;
    public String name;
    public Device( String name,TYPES.DEVICES type){
        System.out.println("Device created -- "+name+" -- "+type);
        this.type = type;
        this.name = name;
        environment_vars= new ArrayList<>();
        Variable tmp = new Variable<String>(TYPES.VARS.SIMPLE,"host",name);
        environment_vars.add(tmp);
    }
    public void addEnvVariable(Variable var){
        Variable evar = this.getEnvVariable(var.getName());
        
        if(evar != null && evar.type.equals(TYPES.VARS.KEY_VALUE)){
          HashMap<String,String> value =  (HashMap)evar.getValue();
          value.putAll((HashMap)var.getValue());
        }else{
             this.environment_vars.removeIf( _var->{ return _var.getName().equals(var.getName());});
             this.environment_vars.add(var);
        }
    }
    
    public  Variable getEnvVariable(String name){
        for (Variable environment_var : environment_vars) {
            if (environment_var.getName().equals(name)) {
                return environment_var;
            }
        }
       return null;
    }

    
}
