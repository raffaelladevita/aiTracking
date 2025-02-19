package org.clas.analysis;

/**
 *
 * @author devita
 */
public enum Charges {
      
    UNDEFINED ( -1, "undef"),
    POS       (  0, "pos"),    
    NEG       (  1, "neg"),
    ELE       (  2, "ele");
    
    
    private final int chargeId;
    private final String chargeName;
    
    Charges(){
        chargeId = -1;
        chargeName = "undef";
    }
    
    Charges(int id, String name){
        chargeId = id;
        chargeName = name;
    }
    
    public String getName() {
        return chargeName;
    }
    
    public int getId() {
        return chargeId;
    }
    
    public static Charges getCharge(String name) {
        name = name.trim();
        for(Charges id: Charges.values())
            if (id.getName().equalsIgnoreCase(name)) 
                return id;
        return UNDEFINED;
    }
    public static Charges getCharge(Integer detId) {

        for(Charges id: Charges.values())
            if (id.getId() == detId) 
                return id;
        return UNDEFINED;
    }
}