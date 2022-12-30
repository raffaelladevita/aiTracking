package org.clas.histograms;

import java.util.ArrayList;
import org.clas.analysis.Constants;
import org.clas.analysis.Track;
import org.clas.analysis.Type;
import org.jlab.clas.physics.Particle;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;

/**
 *
 * @author devita
 */
public class HistoEvent extends Histos {
    
    private String   name   = null;
    private Particle beam   = null;
    private Particle target = null;
    
    private Type type = null;
    
    public HistoEvent(String str, Type type, int col) {
        super(str,type,col);
        this.type   = type;
        this.beam   = new Particle(11, 0,0,Constants.BEAMENERGY, 0,0,0);
        this.target = Particle.createWithPid(Constants.TARGETPID, 0,0,0, 0,0,0);
    }
    
    @Override
    public void init() {
        this.put("2pi",  new DataGroup("2pi",3,2));       
        this.put("1pi",  new DataGroup("1pi",2,2));       
        this.put("eh",   new DataGroup("eh",3,1));       
    }
    
    @Override
    public void create(int col) {
        String name = this.getName();
        // 2 pi
        String[] a2pimx = {"M_X(ep#rarrow e'^#pi+^#pi-X) (GeV)","M_^X2(ep#rarrow e'p^#pi+X) (Ge^V2)","M_^X2(ep#rarrow e'p^#pi-X) (Ge^V2)"};
        for(int i=1; i<=3; i++) {
            double rmin = 0;
            double rmax = 3;
            if(i>1) rmin = -1;
            H1F hi_mmass = new H1F("mxt" + i + "_" + name, "", 100, rmin, rmax);     
            hi_mmass.setTitleX(a2pimx[i-1]);
            hi_mmass.setTitleY("Counts");
            hi_mmass.setLineColor(col);
            H1F hi_imass = new H1F("mit" + i + "_" + name, "", 100, 0.0, 3.0);     
            hi_imass.setTitleX("M(#pi#pi) (GeV)");
            hi_imass.setTitleY("Counts");
            hi_imass.setLineColor(col);
            this.get("2pi").addDataSet(hi_mmass, i-1);
            this.get("2pi").addDataSet(hi_imass, i+2);
        }
        // 1 pi
        String[] atitle = {"M_X(ep#rarrow e'^#pi+X) (GeV)","M_^X2(ep#rarrow e'pX) (Ge^V2)"};
        for(int i=1; i<=2; i++) {
            double rmin = 0;
            double rmax = 3;
            if(i>1) rmin = -1;
            H1F hi_w = new H1F("W" + i + "_" + name, "", 100, 0.5, 3.5);     
            hi_w.setTitleX("W (GeV)");
            hi_w.setTitleY("Counts");
            hi_w.setLineColor(col);
            H1F hi_mmass = new H1F("mxt" + i + "_" + name, "", 100, rmin, rmax);      
            hi_mmass.setTitleX(atitle[i-1]);
            hi_mmass.setTitleY("Counts");
            hi_mmass.setLineColor(col);
            this.get("1pi").addDataSet(hi_w,     i-1);
            this.get("1pi").addDataSet(hi_mmass, i+1);
        }
        // eh+/-
        String[] aehW = {"W(ep#rarrow e'X) (GeV)","W(ep#rarrow e'X) (GeV)","W(ep#rarrow e'h^+X) (GeV)","W(ep#rarrow e'h^-X) (GeV)"};
        double rmin = 0.5;
        double rmax = 4.0;
        H1F hi_we = new H1F("We" + "_" + name, "", 100, rmin, rmax);     
        hi_we.setTitleX("W(ep#rarrow e'X) (GeV)");
        hi_we.setTitleY("Counts");
        hi_we.setLineColor(col);
        H1F hi_wehp = new H1F("Wehp" + "_" + name, "", 100, rmin, rmax);     
        hi_wehp.setTitleX("W(ep#rarrow e'h^+X) (GeV)");
        hi_wehp.setTitleY("Counts");
        hi_wehp.setLineColor(col);
        H1F hi_wehm = new H1F("Wehm" + "_" + name, "", 100, rmin, rmax);     
        hi_wehm.setTitleX("W(ep#rarrow e'h^-X) (GeV)");
        hi_wehm.setTitleY("Counts");
        hi_wehm.setLineColor(col);
        this.get("eh").addDataSet(hi_we,   0);
        this.get("eh").addDataSet(hi_wehp, 1);
        this.get("eh").addDataSet(hi_wehm, 2);
    }
    
    
    @Override
    public boolean fill(ArrayList<Track> tracks) {
        Track electron = null;
        Track piplus   = null;
        Track piminus  = null;  
        Track proton   = null;  
        Track trigger = null;
        ArrayList<Track> hadpos   = new ArrayList<>();  
        ArrayList<Track> hadneg   = new ArrayList<>();  
        for(Track track : tracks) {
            if(track.isValid()) {
                if(electron==null && track.pid()==11 && track.status()<0) {
                    electron= track;
                }
                else if(piplus==null && track.pid()==211)  {
                    piplus= track;
                }
                else if(piminus==null && track.pid()==-211)  {
                    piminus= track;
                }
                else if(proton==null && track.pid()==2212)  {
                    proton= track;
                }
            }
            if(track.isForLumiScan()) {
                if(trigger==null && track.pid()==11 && track.status()<0) {
                    trigger= track;
                }
                if(track.charge()>0 && track.status()>0)  {
                    hadpos.add(track);
                }
                if(track.charge()<0 && track.status()>0)  {
                    hadneg.add(track);
                }
            }
        }
        if(trigger!=null) {
            Particle W = new Particle();
            W.copy(target);
            W.combine(beam, +1);
            W.combine(trigger.particle(), -1);
            this.get("eh").getH1F("We_" + this.getName()).fill(W.mass());
            for(int i=0; i<hadpos.size(); i++) this.get("eh").getH1F("Wehp_" + this.getName()).fill(W.mass());
            for(int i=0; i<hadneg.size(); i++) this.get("eh").getH1F("Wehm_" + this.getName()).fill(W.mass());
        }
        if(this.isEventType(electron, proton, piplus, piminus)) {
            if(electron!=null && piplus!=null && piminus!=null) {
                Particle missing = new Particle();
                missing.copy(target);
                missing.combine(beam, +1);
                missing.combine(electron.particle(), -1);
                missing.combine(piplus.particle(),   -1);
                missing.combine(piminus.particle(),  -1);
                Particle rho = new Particle();
                rho.copy(piplus.particle());
                rho.combine(piminus.particle(), +1);
                this.get("2pi").getH1F("mxt1_" + this.getName()).fill(missing.mass());
                this.get("2pi").getH1F("mit1_" + this.getName()).fill(rho.mass());  
                return true;
            }
            else if(electron!=null && piplus!=null && proton!=null && piminus==null) {
                Particle missing = new Particle();
                missing.copy(target);
                missing.combine(beam, +1);
                missing.combine(electron.particle(), -1);
                missing.combine(piplus.particle(),   -1);
                missing.combine(proton.particle(),  -1);
                Particle rho = new Particle();
                rho.copy(piplus.particle());
                rho.combine(missing, +1);
                this.get("2pi").getH1F("mxt2_" + this.getName()).fill(missing.mass2());
                this.get("2pi").getH1F("mit2_" + this.getName()).fill(rho.mass());  
                return true;
            }
            else if(electron!=null && piminus!=null && proton!=null && piplus==null) {
                Particle missing = new Particle();
                missing.copy(target);
                missing.combine(beam, +1);
                missing.combine(electron.particle(), -1);
                missing.combine(piminus.particle(),   -1);
                missing.combine(proton.particle(),  -1);
                Particle rho = new Particle();
                rho.copy(missing);
                rho.combine(piminus.particle(), +1);
                this.get("2pi").getH1F("mxt3_" + this.getName()).fill(missing.mass2());
                this.get("2pi").getH1F("mit3_" + this.getName()).fill(rho.mass()); 
                return true;
            }
            else if(electron!=null && piplus!=null && piminus==null && proton==null) {
                Particle neutron = new Particle();
                neutron.copy(target);
                neutron.combine(beam, +1);
                neutron.combine(electron.particle(), -1);
                neutron.combine(piplus.particle(),   -1);
                Particle W = new Particle();
                W.copy(target);
                W.combine(beam, +1);
                W.combine(electron.particle(), -1);
                this.get("1pi").getH1F("W1_" + this.getName()).fill(W.mass());
                this.get("1pi").getH1F("mxt1_" + this.getName()).fill(neutron.mass());                    
                return true;
            }
            else if(electron!=null && proton!=null && piminus==null && piplus==null) {
                Particle pizero = new Particle();
                pizero.copy(target);
                pizero.combine(beam, +1);
                pizero.combine(electron.particle(), -1);
                pizero.combine(proton.particle(),   -1);
                Particle W = new Particle();
                W.copy(target);
                W.combine(beam, +1);
                W.combine(electron.particle(), -1);
                this.get("1pi").getH1F("W2_" + this.getName()).fill(W.mass());
                this.get("1pi").getH1F("mxt2_" + this.getName()).fill(pizero.mass2());                    
                return true;
            }
        }
        return false;
    }
    
    public int getNe() {
        return (int) this.get("eh").getH1F("We_" + this.getName()).getIntegral();
    }

    public int getNehp() {
        return (int) this.get("eh").getH1F("Wehp_" + this.getName()).getIntegral();
    }

    public int getNehm() {
        return (int) this.get("eh").getH1F("Wehm_" + this.getName()).getIntegral();
    }

    private boolean isEventType(Track p1, Track p2, Track p3, Track p4) {
        if(this.type==Type.MATCHED) {
            boolean value = true;
            if(p1!=null && !p1.isMatched()) value = false;
            if(p2!=null && !p2.isMatched()) value = false;
            if(p3!=null && !p3.isMatched()) value = false;
            if(p4!=null && !p4.isMatched()) value = false;
            return value;
        }
        else if(this.type==Type.UNMATCHED) {
            boolean value = false;
            if(p1!=null && !p1.isMatched()) value = true;
            if(p2!=null && !p2.isMatched()) value = true;
            if(p3!=null && !p3.isMatched()) value = true;
            if(p4!=null && !p4.isMatched()) value = true;
            return value;
        }
        else
            return true;
    }
}
