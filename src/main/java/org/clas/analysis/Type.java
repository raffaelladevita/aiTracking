package org.clas.analysis;

/**
 *
 * @author devita
 */
public enum Type {
      
    UNDEFINED    ( -1, "undef"),
    CONVENTIONAL (  0, "conventional"),    
    AI           (  1, "ai"),
    MATCHED      (  2, "matched"),
    UNMATCHED    (  3, "unmatched"),
    CANDIDATES   (  4, "candidates");
    
    
    private final int typeId;
    private final String typeName;
    
    Type(){
        typeId = -1;
        typeName = "undef";
    }
    
    Type(int id, String name){
        typeId = id;
        typeName = name;
    }
    
    public String getName() {
        return typeName;
    }
    
    public int getId() {
        return typeId;
    }
    
    public static Type getType(String name) {
        name = name.trim();
        for(Type id: Type.values())
            if (id.getName().equalsIgnoreCase(name)) 
                return id;
        return UNDEFINED;
    }
    public static Type getType(Integer detId) {

        for(Type id: Type.values())
            if (id.getId() == detId) 
                return id;
        return UNDEFINED;
    }
}