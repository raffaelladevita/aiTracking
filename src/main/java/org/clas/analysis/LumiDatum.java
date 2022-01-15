package org.clas.analysis;

import org.clas.histograms.HistoEvent;
import org.clas.histograms.Histos;

/**
 *
 * @author devita
 */
public class LumiDatum {
    private String     run;
    private double     current=0;
    private int[][][]  tracks = new int[3][5][3];
    private int[][]    eh = new int[2][3];
    private double[][] norm = {{1,1},{1,1},{1,1}};

    public LumiDatum(String run, double current) {
       this.run     = run;
       this.current = current;
    }
    
    public void initTracks(String run, Charges charge, Histos...histos) {
        for(Histos histo : histos) {
            this.setTracks(charge, Types.getType(histo.getTitle()), 0, histo.getEntries("summary"));
            this.setTracks(charge, Types.getType(histo.getTitle()), 6, histo.getEntries("6SL"));
            this.setTracks(charge, Types.getType(histo.getTitle()), 5, histo.getEntries("5SL"));
        }
    }

    public void initEH(HistoEvent...histos) {
        for(HistoEvent histo : histos) {
            this.setEH(Types.getType(histo.getTitle()), Charges.ELE, histo.getNe());
            this.setEH(Types.getType(histo.getTitle()), Charges.POS, histo.getNehp());
            this.setEH(Types.getType(histo.getTitle()), Charges.NEG, histo.getNehm());
        }
    }

    private int jsl(int sl) {
        switch (sl) {
            case 0:
                return 0;
            case 6:
                return 1;
            case 5:
                return 2;
            default:
                return -1;
        }
    }

    public double getCurrent() {
       return this.current;
    }

    public String getRun() {
       return this.run;
    }

    public void setRun(String run) {
       this.run=run;
    }

    public double getNorm(Charges charge, Types type) {
        if(charge!=Charges.UNDEFINED && type!=Types.UNDEFINED ) return norm[charge.getId()][type.getId()];
        else return 1;
    }

    public void setNorm(Charges charge, Types type, double norm) {
        if(charge!=Charges.UNDEFINED && type!=Types.UNDEFINED ) this.norm[charge.getId()][type.getId()] = norm;
        else System.out.println("Error setting the normalization factor for charge=" + charge.getName() + " type=" + type.getName());
    }
    

    public int getTracks(Charges charge, Types type, int sl) {
       int il=jsl(sl);
       if(charge!=Charges.UNDEFINED && type!=Types.UNDEFINED && il!=-1) return tracks[charge.getId()][type.getId()][il];
       else return 0;
    }
    
    public void setTracks(Charges charge, Types type, int sl, int ntracks) {
       int il=jsl(sl);
       if(charge!=Charges.UNDEFINED && type!=Types.UNDEFINED && il!=-1) tracks[charge.getId()][type.getId()][il]=ntracks;
       else System.out.println("Error setting number of tracks for charge=" + charge.getName() + " type=" + type.getName() + " SL=" + sl);
    }
    
    public int getEH(Types type, Charges charge) {
       if(charge!=Charges.UNDEFINED && type!=Types.UNDEFINED) return eh[type.getId()][charge.getId()];
       else return 0;
    }
    
    public void setEH(Types type, Charges charge, int tr) {
       if(charge!=Charges.UNDEFINED && type!=Types.UNDEFINED) eh[type.getId()][charge.getId()]=tr;
       else System.out.println("Error setting lumi data for type=" + type.getName() + " charge=" + charge.getName());
    }

    public double getRatio(Charges charge, Types type, int sl) {
       double cv = this.getTracks(charge,Types.CONVENTIONAL,sl);
       double ai = this.getTracks(charge,type,sl);
       return ai/cv;
    }

    public double getRatioError(Charges charge, Types type, int sl) {
       double cv = this.getTracks(charge,Types.CONVENTIONAL,sl);
       double ai = this.getTracks(charge,type,sl);
       return this.getRatioError(ai, cv);
    }
    
    private double getRatioError(double numerator, double denominator){
        double err = (numerator/denominator)*Math.sqrt(Math.abs(numerator-denominator)/numerator/denominator);
        return err;
    }
    
    public double getLumi(Types type, Charges charge) {
       double e  = this.getEH(type,Charges.ELE);
       double eh = this.getEH(type,charge);
       double ratio = eh/e;
       if(charge.equals(Charges.ELE)) ratio = e;
       return ratio;
    }    
    
    public double getLumiErr(Types type, Charges charge) {
       double e  = this.getEH(type,Charges.ELE);
       double eh = this.getEH(type,charge);
       double err = (eh/e)*Math.sqrt(Math.abs(eh-e)/e/eh);
       if(charge.equals(Charges.ELE)) err = Math.sqrt(e);
       return err;
    }

    public double getLumiNorm(Types type, Charges charge) {
       return this.getLumi(type, charge)/this.getNorm(charge,type);
    }

    public double getLumiNormErr(Types type, Charges charge) {
       return this.getLumiErr(type, charge)/this.getNorm(charge,type);
    }

    public double getLumiNorm(Types type, Charges charge, double norm) {
       return this.getLumi(type, charge)/norm;
    }

    public double getLumiNormErr(Types type, Charges charge, double norm) {
       return this.getLumiErr(type, charge)/norm;
    }

    public double getLumiRatio(Charges charge) {
       return this.getLumi(Types.AI, charge)/this.getLumi(Types.CONVENTIONAL,charge);
    }

    public double getLumiRatioErr(Charges charge) {
       double e1  = this.getLumi(Types.CONVENTIONAL,charge);
       double e2  = this.getLumi(Types.AI, charge);
       double ee1 = this.getLumiErr(Types.CONVENTIONAL,charge);
       double ee2 = this.getLumiErr(Types.AI, charge);
       double err = (e2/e1)*Math.sqrt((ee1/e1)*(ee1/e1)+(ee2/e2)*(ee2/e2));
       return err;
    }

    private void printTracks(Charges charge) {
        System.out.println("+-------------------------------------------------------------------------------------------------------------------------------+");
        System.out.println("|     charge |       type | conventional |           ai |       matched |    predicted |       gain |  efficiency |   inference |");
        System.out.println("+-------------------------------------------------------------------------------------------------------------------------------+");
        this.printTracks(charge, 0);        
        this.printTracks(charge, 6);        
        this.printTracks(charge, 5);        
        System.out.println("+-------------------------------------------------------------------------------------------------------------------------------+");
    }
    
    private void printTracks(Charges charge, int sl) {
        System.out.println(String.format("| %10s | %10d | %12d | % 12d | %12d  | %12d | %10.4f |  %10.4f |  %10.4f |"
                                                                                                , charge.getName(), sl 
                                                                                                , this.getTracks(charge, Types.CONVENTIONAL, sl)
                                                                                                , this.getTracks(charge, Types.AI, sl)
                                                                                                , this.getTracks(charge, Types.MATCHED, sl)
                                                                                                , this.getTracks(charge, Types.CANDIDATES, sl)
                                                                                                , this.getRatio(charge, Types.AI, sl)
                                                                                                , this.getRatio(charge, Types.MATCHED, sl)
                                                                                                , this.getRatio(charge, Types.CANDIDATES, sl)));
    }        
    
    private void printLumi() {
        System.out.println("+--------------------------------------------------------------------------------------+");
        System.out.println("|         type |            e |          eh+ |          eh- |      eh+/e |       eh-/e |");
        System.out.println("+--------------------------------------------------------------------------------------+");
        this.printLumi(Types.CONVENTIONAL);        
        this.printLumi(Types.AI);        
        System.out.println("+--------------------------------------------------------------------------------------+");
    }
    
    private void printLumi(Types type) {
        System.out.println(String.format("| %12s | %12d | % 12d | %12d | %10.4f |  %10.4f |", type.getName()
                                                                                            , this.getEH(type, Charges.ELE)
                                                                                            , this.getEH(type, Charges.POS)
                                                                                            , this.getEH(type, Charges.NEG)
                                                                                            , this.getLumi(type, Charges.POS)
                                                                                            , this.getLumi(type, Charges.NEG)));        
    }
    
    public void show() {
       this.printTracks(Charges.NEG);
       this.printTracks(Charges.POS);
       this.printLumi();
    }
}