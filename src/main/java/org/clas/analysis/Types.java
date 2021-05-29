/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.analysis;

/**
 *
 * @author devita
 */
public enum Types {
      
    UNDEFINED    ( -1, "undef"),
    CONVENTIONAL (  0, "conventional"),    
    AI           (  1, "ai"),
    MATCHED      (  2, "matched"),
    UNMATCHED    (  3, "unmatched"),
    CANDIDATES   (  4, "candidates");
    
    
    private final int typeId;
    private final String typeName;
    
    Types(){
        typeId = -1;
        typeName = "undef";
    }
    
    Types(int id, String name){
        typeId = id;
        typeName = name;
    }
    
    public String getName() {
        return typeName;
    }
    
    public int getId() {
        return typeId;
    }
    
    public static Types getType(String name) {
        name = name.trim();
        for(Types id: Types.values())
            if (id.getName().equalsIgnoreCase(name)) 
                return id;
        return UNDEFINED;
    }
    public static Types getType(Integer detId) {

        for(Types id: Types.values())
            if (id.getId() == detId) 
                return id;
        return UNDEFINED;
    }
}