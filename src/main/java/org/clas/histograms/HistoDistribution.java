/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.histograms;

import java.util.LinkedHashMap;
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
    
    private LinkedHashMap<String,String> summaries;
                    
    public HistoDistribution(String str, String title, int col) {
        super(str,title,col);
    }
    
    @Override
    public void init() {
        this.summaries = new LinkedHashMap<>();
        this.summaries.put("summary", "");
        this.summaries.put("e-",      "e-");
        this.summaries.put("6SL",     "6SL");
        this.summaries.put("5SL",     "5SL");
        for(String key :this.summaries.keySet()) this.put(key, new DataGroup(key,3,2));
        this.put("2D",       new DataGroup("2D",3,2));
        this.put("p",        new DataGroup("p",3,2));
        this.put("theta",    new DataGroup("theta",3,2));
        this.put("vz",       new DataGroup("vz",3,2));        
    }
    
    @Override
    public void create(int col) {
        String name = this.getName();
        for(String dg :this.summaries.keySet()) {
            String title = this.summaries.get(dg);
            String sname = title + "_" + this.getName();
            H1F hi_p = new H1F("p" + sname, "p", 100, 0.0, 8.0);
            hi_p.setTitleX("p (GeV)");
            hi_p.setTitleY("Counts");
            hi_p.setLineColor(col);
            H1F hi_theta = new H1F("theta" + sname, "theta", 100, 0.0, 40.0); 
            hi_theta.setTitleX("#theta (deg)");
            hi_theta.setTitleY("Counts");
            hi_theta.setLineColor(col);
            H1F hi_phi = new H1F("phi" + sname, "phi", 100, -180.0, 180.0);   
            hi_phi.setTitleX("#phi (deg)");
            hi_phi.setTitleY("Counts");
            hi_phi.setLineColor(col);
            H1F hi_chi2 = new H1F("chi2" + sname, "chi2", 100, 0.0, 15.0);   
            hi_chi2.setTitleX("#chi2");
            hi_chi2.setTitleY("Counts");
            hi_chi2.setLineColor(col);
            H1F hi_vz = new H1F("vz" + sname, "vz", 180, -50.0, 40.0);   
            hi_vz.setTitleX("Vz (cm)");
            hi_vz.setTitleY("Counts");
            hi_vz.setLineColor(col);
            H2F hi_xy = new H2F("xy" + sname, "xy", 200, -200.0, 200.0, 200, -200.0, 200.0);   
            hi_xy.setTitleX("R1 x (cm)");
            hi_xy.setTitleY("R1 y (cm)");
            this.get(dg).addDataSet(hi_p,     0);
            this.get(dg).addDataSet(hi_theta, 1);
            this.get(dg).addDataSet(hi_phi,   2);
            this.get(dg).addDataSet(hi_chi2,  3);
            this.get(dg).addDataSet(hi_vz,    4);
            this.get(dg).addDataSet(hi_xy,    5);
        }
        for(int i=0; i<3; i++) {
            int region = i+1;
            double size = 200.0+i*100;
            H2F hi_traj = new H2F("traj"   + region + "_" + name, "R" + region + " trajectory", 200, -size, size, 200, -size, size);   
            hi_traj.setTitleX("R" + region + " x (cm)");
            hi_traj.setTitleY("R" + region + " y (cm)");
            H2F hi_cross = new H2F("cross" + region + "_" + name, "R" + region + " crosses"   , 200, -size*1.2, size*1.2, 200, -size*1.2, size*1.2);   
            hi_cross.setTitleX("R" + region + " x (cm)");
            hi_cross.setTitleY("R" + region + " y (cm)");
            this.get("2D").addDataSet(hi_traj,  i);            
            this.get("2D").addDataSet(hi_cross, i+3);            
        }
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
        this.fillSummaries(track, "summary");
        if(track.SL()==6)      this.fillSummaries(track, "6SL");
        else if(track.SL()==5) this.fillSummaries(track, "5SL");
        if(track.pid()==11)    this.fillSummaries(track, "e-");
        this.fill2D(track);
        this.fillSectors(track);
    }  
    
    private void fillSummaries(Track track, String dg) {
        String title = this.summaries.get(dg);
        String sname = title + "_" + this.getName();
        if(track.isValid()) {
            this.get(dg).getH1F("p"     + sname).fill(track.p());
            this.get(dg).getH1F("theta" + sname).fill(Math.toDegrees(track.theta()));
            this.get(dg).getH1F("phi"   + sname).fill(Math.toDegrees(track.phi()));
            this.get(dg).getH1F("chi2"  + sname).fill(track.chi2());
            this.get(dg).getH2F("xy"    + sname).fill(track.trajectory(DetectorType.DC.getDetectorId(), 6).x()
                                                     ,track.trajectory(DetectorType.DC.getDetectorId(), 6).y());
        }
        this.get(dg).getH1F("vz" + sname).fill(track.vz());
    }
     
    private void fill2D(Track track) {
        if(track.isValid()) {
            for(int i=0; i<3; i++) {
                int region = i+1;
                this.get("2D").getH2F("traj"   + region + "_" + this.getName()).fill(track.trajectory(DetectorType.DC.getDetectorId(),6+12*i).x()
                                                                                    ,track.trajectory(DetectorType.DC.getDetectorId(),6+12*i).y());
                this.get("2D").getH2F("cross"  + region + "_" + this.getName()).fill(track.cross(region).x()
                                                                                    ,track.cross(region).y());
            }
        }
    }
    
    private void fillSectors(Track track) {
        int sector = track.sector();
        if(track.isValid()) {
            this.get("p").getH1F("psec"         + sector + "_" + this.getName()).fill(track.p());
            this.get("theta").getH1F("thetasec" + sector + "_" + this.getName()).fill(Math.toDegrees(track.theta()));
        }
        this.get("vz").getH1F("vzsec"    + sector + "_" +  this.getName()).fill(track.vz());       
    }
    
    @Override
    public int getEntries() {
        return this.getEntries("summary");
    }

    @Override
    public int getEntries(String dg) {
        if(this.containsKey(dg)) {
            return (int) this.get(dg).getH1F("phi" + this.summaries.get(dg) + "_" + this.getName()).getIntegral();
        }
        else {
            return 0;
        }
    }
}
