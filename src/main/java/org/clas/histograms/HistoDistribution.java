/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.histograms;

import org.clas.analysis.Track;
import org.jlab.detector.base.DetectorType;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;

/**
 *
 * @author devita
 */
public class HistoDistribution extends Histos {
    
    public HistoDistribution(String str, int col) {
        super(str,col);
    }
    
    @Override
    public void init() {
        this.put("summary",  new DataGroup("summary",3,2));
        this.put("p",        new DataGroup("p",3,2));
        this.put("theta",    new DataGroup("theta",3,2));
        this.put("vz",       new DataGroup("vz",3,2));        
    }
    
    @Override
    public void create(int col) {
        String name = this.getName();
        H1F hi_p = new H1F("p_" + name, "p", 100, 0.0, 8.0);     
        hi_p.setTitleX("p (GeV)");
        hi_p.setTitleY("Counts");
        hi_p.setLineColor(col);
        H1F hi_theta = new H1F("theta_" + name, "theta", 100, 0.0, 40.0); 
        hi_theta.setTitleX("#theta (deg)");
        hi_theta.setTitleY("Counts");
        hi_theta.setLineColor(col);
        H1F hi_phi = new H1F("phi_" + name, "phi", 100, -180.0, 180.0);   
        hi_phi.setTitleX("#phi (deg)");
        hi_phi.setTitleY("Counts");
        hi_phi.setLineColor(col);
        H1F hi_chi2 = new H1F("chi2_" + name, "chi2", 100, 0.0, 15.0);   
        hi_chi2.setTitleX("#chi2");
        hi_chi2.setTitleY("Counts");
        hi_chi2.setLineColor(col);
        H1F hi_vz = new H1F("vz_" + name, "vz", 180, -50.0, 40.0);   
        hi_vz.setTitleX("Vz (cm)");
        hi_vz.setTitleY("Counts");
        hi_vz.setLineColor(col);
        H2F hi_xy = new H2F("xy_" + name, "xy", 200, -500.0, 500.0, 200, -500.0, 500.0);   
        hi_xy.setTitleX("R3 x (cm)");
        hi_xy.setTitleY("R3 y (cm)");
        this.get("summary").addDataSet(hi_p,     0);
        this.get("summary").addDataSet(hi_theta, 1);
        this.get("summary").addDataSet(hi_phi,   2);
        this.get("summary").addDataSet(hi_chi2,  3);
        this.get("summary").addDataSet(hi_vz,    4);
        this.get("summary").addDataSet(hi_xy,    5);
        for(int i=0; i<6; i++) {
            int sector = i+1;
            H1F hi_p_sec = new H1F("psec" + sector + "_" + name, "p", 100, 0.0, 8.0);     
            hi_p_sec.setTitleX("p (GeV) - sector " + sector);
            hi_p_sec.setTitleY("Counts");
            hi_p_sec.setLineColor(col);
            this.get("p").addDataSet(hi_p_sec, i);
            H1F hi_theta_sec = new H1F("thetasec" + sector + "_" + name, "theta", 100, 0.0, 40.0);   
            hi_theta_sec.setTitleX("#theta (deg) - sector " + sector);
            hi_theta_sec.setTitleY("Counts");
            hi_theta_sec.setLineColor(col);
            this.get("theta").addDataSet(hi_theta_sec, i);
            H1F hi_vz_sec = new H1F("vzsec" + sector + "_" + name, "vz", 180, -50.0, 40.0);    
            hi_vz_sec.setTitleX("vz (cm) - sector " + sector);
            hi_vz_sec.setTitleY("Counts");
            hi_vz_sec.setLineColor(col);
            this.get("vz").addDataSet(hi_vz_sec, i);
        }
    }
    
    @Override
    public void fill(Track track) {
        int sector = track.sector();
        if(track.isValid()) {
            this.get("summary").getH1F("p_"     + this.getName()).fill(track.p());
            this.get("summary").getH1F("theta_" + this.getName()).fill(Math.toDegrees(track.theta()));
            this.get("summary").getH1F("phi_"   + this.getName()).fill(Math.toDegrees(track.phi()));
            this.get("summary").getH1F("chi2_"  + this.getName()).fill(track.chi2());
            this.get("summary").getH2F("xy_"    + this.getName()).fill(track.trajectory(DetectorType.DC.getDetectorId(), 36).x()
                                                                      ,track.trajectory(DetectorType.DC.getDetectorId(), 36).y());
            this.get("p").getH1F("psec"         + sector + "_" + this.getName()).fill(track.p());
            this.get("theta").getH1F("thetasec" + sector + "_" +  this.getName()).fill(Math.toDegrees(track.theta()));
        }
        this.get("summary").getH1F("vz_" + this.getName()).fill(track.vz());
        this.get("vz").getH1F("vzsec"    + sector + "_" +  this.getName()).fill(track.vz());
    }    
    
    @Override
    public int getEntries() {
        return (int) this.get("summary").getH1F("phi_" + this.getName()).getIntegral();
    }
}
