/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.analysis;

import java.util.ArrayList;
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
        this.put("2pi",  new DataGroup("2pi",2,1));       
    }
    
    @Override
    public void create(int col) {
        String name = this.getName();
        H1F hi_mmass = new H1F("mx_" + name, "mx", 100, 0.0, 3.0);     
        hi_mmass.setTitleX("Mx (GeV)");
        hi_mmass.setTitleY("Counts");
        hi_mmass.setLineColor(col);
        H1F hi_imass = new H1F("mi_" + name, "mx", 100, 0.0, 3.0);     
        hi_imass.setTitleX("Mi (GeV)");
        hi_imass.setTitleY("Counts");
        hi_imass.setLineColor(col);
        this.get("2pi").addDataSet(hi_mmass, 0);
        this.get("2pi").addDataSet(hi_imass, 1);
    }
    
    
    @Override
    public void fill(ArrayList<Track> tracks) {
        Particle electron = null;
        Particle piplus   = null;
        Particle piminus  = null;  
        for(Track track : tracks) {
            if(!track.isValid()) continue;
            if(electron==null && track.pid()==11 && track.status()<0 ) {
                electron= new Particle(11, track.px(),track.py(),track.pz(), track.vx(), track.vy(), track.vz());
            }
            else if(piplus==null && track.pid()==211)  {
                piplus= new Particle(211, track.px(),track.py(),track.pz(), track.vx(), track.vy(), track.vz());
            }
            else if(piminus==null && track.pid()==-211)  {
                piminus= new Particle(211, track.px(),track.py(),track.pz(), track.vx(), track.vy(), track.vz());
            }
        }
        if(electron!=null && piplus!=null && piminus!=null) {
            Particle proton = new Particle();
            proton.copy(target);
            proton.combine(beam, +1);
            proton.combine(electron, -1);
            proton.combine(piplus,   -1);
            proton.combine(piminus,  -1);
            Particle rho = new Particle();
            rho.copy(piplus);
            rho.combine(piminus, +1);
            this.get("2pi").getH1F("mx_" + this.getName()).fill(proton.mass());
            this.get("2pi").getH1F("mi_" + this.getName()).fill(rho.mass());                    
        }
    }

}
