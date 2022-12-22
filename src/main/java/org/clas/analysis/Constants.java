package org.clas.analysis;

import org.jlab.detector.base.DetectorType;

/**
 *
 * @author devita
 */
public class Constants {
  
    public static double CHI2MAX = Double.POSITIVE_INFINITY;
    public static double ZMIN = -15;
    public static double ZMAX = 5;
    public static double PMIN = 0.5;
    public static double[] EDGE = {-99, -99, -99};
    public static int    WIREMIN = 0;
    public static int    SECTOR = 0;
    
    public static double BEAMENERGY = 10.6;
    public static int    TARGETPID = 2212;
    
    public static int NSUPERLAYERS = 0;
    
    public static boolean HITMATCH = false;
    
    
    public static double getEdge(DetectorType type)            {
        if(null==type)
            return -99;
        else switch (type) {
            case DC:
                return EDGE[0];
            case FTOF:
                return EDGE[1];
            case ECAL:
                return EDGE[2];
            default:
                return -99;
        }
    }
}
