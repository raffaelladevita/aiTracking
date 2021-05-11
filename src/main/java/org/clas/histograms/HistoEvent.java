/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.histograms;

import java.util.ArrayList;
import org.clas.analysis.Track;
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
    
    public HistoEvent(String str, int col, double beamEnergy, int targetPDG) {
        super(str,col);
        this.beam   = new Particle(11, 0,0,beamEnergy, 0,0,0);
        this.target = Particle.createWithPid(targetPDG, 0,0,0, 0,0,0);
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
        String[] a2pimx = {"MX(ep->e'#pi+#pi-X) (GeV)","MX2(ep->e'p#pi+X) (GeV2)","MX2(ep->e'p#pi-X) (GeV2)"};
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
        String[] atitle = {"MX(ep->e'#pi+X) (GeV)","MX2(ep->e'pX) (GeV2)"};
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
        String[] aehW = {"W(ep->e'X) (GeV)","W(ep->e'X) (GeV)","W(ep->e'h+X) (GeV)","W(ep->e'h-X) (GeV)"};
        double rmin = 0.5;
        double rmax = 4.0;
        H1F hi_we = new H1F("We" + "_" + name, "", 100, rmin, rmax);     
        hi_we.setTitleX("W(ep->e'X) (GeV)");
        hi_we.setTitleY("Counts");
        hi_we.setLineColor(col);
        H1F hi_wehp = new H1F("Wehp" + "_" + name, "", 100, rmin, rmax);     
        hi_wehp.setTitleX("W(ep->e'h+X) (GeV)");
        hi_wehp.setTitleY("Counts");
        hi_wehp.setLineColor(col);
        H1F hi_wehm = new H1F("Wehm" + "_" + name, "", 100, rmin, rmax);     
        hi_wehm.setTitleX("W(ep->e'h-X) (GeV)");
        hi_wehm.setTitleY("Counts");
        hi_wehm.setLineColor(col);
        this.get("eh").addDataSet(hi_we,   0);
        this.get("eh").addDataSet(hi_wehp, 1);
        this.get("eh").addDataSet(hi_wehm, 2);
    }
    
    
    @Override
    public void fill(ArrayList<Track> tracks) {
        Particle electron = null;
        Particle piplus   = null;
        Particle piminus  = null;  
        Particle proton   = null;  
        Particle trigger = null;
        ArrayList<Particle> hadpos   = new ArrayList<>();  
        ArrayList<Particle> hadneg   = new ArrayList<>();  
        for(Track track : tracks) {
            if(track.isValid()) {
                if(electron==null && track.pid()==11 && track.status()<0) {
                    electron= new Particle(11, track.px(),track.py(),track.pz(), track.vx(), track.vy(), track.vz());
                }
                else if(piplus==null && track.pid()==211)  {
                    piplus= new Particle(211, track.px(),track.py(),track.pz(), track.vx(), track.vy(), track.vz());
                }
                else if(piminus==null && track.pid()==-211)  {
                    piminus= new Particle(211, track.px(),track.py(),track.pz(), track.vx(), track.vy(), track.vz());
                }
                else if(proton==null && track.pid()==2212)  {
                    proton= new Particle(2212, track.px(),track.py(),track.pz(), track.vx(), track.vy(), track.vz());
                }
            }
            if(track.isForLumiScan()) {
                if(trigger==null && track.pid()==11 && track.status()<0) {
                    trigger= new Particle(11, track.px(),track.py(),track.pz(), track.vx(), track.vy(), track.vz());
                }
                if(track.charge()>0 && track.status()>0)  {
                    hadpos.add(new Particle(211, track.px(),track.py(),track.pz(), track.vx(), track.vy(), track.vz()));
                }
                if(track.charge()<0 && track.status()>0)  {
                    hadneg.add(new Particle(-211, track.px(),track.py(),track.pz(), track.vx(), track.vy(), track.vz()));
                }
            }
        }
        if(electron!=null && piplus!=null && piminus!=null) {
            proton = new Particle();
            proton.copy(target);
            proton.combine(beam, +1);
            proton.combine(electron, -1);
            proton.combine(piplus,   -1);
            proton.combine(piminus,  -1);
            Particle rho = new Particle();
            rho.copy(piplus);
            rho.combine(piminus, +1);
            this.get("2pi").getH1F("mxt1_" + this.getName()).fill(proton.mass());
            this.get("2pi").getH1F("mit1_" + this.getName()).fill(rho.mass());                    
        }
        else if(electron!=null && piplus!=null && proton!=null && piminus==null) {
            piminus = new Particle();
            piminus.copy(target);
            piminus.combine(beam, +1);
            piminus.combine(electron, -1);
            piminus.combine(piplus,   -1);
            piminus.combine(proton,  -1);
            Particle rho = new Particle();
            rho.copy(piplus);
            rho.combine(piminus, +1);
            this.get("2pi").getH1F("mxt2_" + this.getName()).fill(piminus.mass2());
            this.get("2pi").getH1F("mit2_" + this.getName()).fill(rho.mass());                                
        }
        else if(electron!=null && piminus!=null && proton!=null && piplus==null) {
            piplus = new Particle();
            piplus.copy(target);
            piplus.combine(beam, +1);
            piplus.combine(electron, -1);
            piplus.combine(piminus,   -1);
            piplus.combine(proton,  -1);
            Particle rho = new Particle();
            rho.copy(piplus);
            rho.combine(piminus, +1);
            this.get("2pi").getH1F("mxt3_" + this.getName()).fill(piplus.mass2());
            this.get("2pi").getH1F("mit3_" + this.getName()).fill(rho.mass());                                
        }
        else if(electron!=null && piplus!=null && piminus==null && proton==null) {
            Particle neutron = new Particle();
            neutron.copy(target);
            neutron.combine(beam, +1);
            neutron.combine(electron, -1);
            neutron.combine(piplus,   -1);
            Particle W = new Particle();
            W.copy(target);
            W.combine(beam, +1);
            W.combine(electron, -1);
            this.get("1pi").getH1F("W1_" + this.getName()).fill(W.mass());
            this.get("1pi").getH1F("mxt1_" + this.getName()).fill(neutron.mass());                    
        }
        else if(electron!=null && proton!=null && piminus==null && piplus==null) {
            Particle pizero = new Particle();
            pizero.copy(target);
            pizero.combine(beam, +1);
            pizero.combine(electron, -1);
            pizero.combine(proton,   -1);
            Particle W = new Particle();
            W.copy(target);
            W.combine(beam, +1);
            W.combine(electron, -1);
            this.get("1pi").getH1F("W2_" + this.getName()).fill(W.mass());
            this.get("1pi").getH1F("mxt2_" + this.getName()).fill(pizero.mass2());                    
        } 
        if(trigger!=null) {
            Particle W = new Particle();
            W.copy(target);
            W.combine(beam, +1);
            W.combine(trigger, -1);
            this.get("eh").getH1F("We_" + this.getName()).fill(W.mass());
            for(int i=0; i<hadpos.size(); i++) this.get("eh").getH1F("Wehp_" + this.getName()).fill(W.mass());
            for(int i=0; i<hadneg.size(); i++) this.get("eh").getH1F("Wehm_" + this.getName()).fill(W.mass());
        }
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

}
