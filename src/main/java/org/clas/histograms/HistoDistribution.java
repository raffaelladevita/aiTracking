package org.clas.histograms;

import java.util.LinkedHashMap;
import org.clas.analysis.Track;
import org.clas.analysis.Type;
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
                    
    public HistoDistribution(String str, Type type, int col) {
        super(str,type,col);
    }
    
    @Override
    public void init() {
        this.summaries = new LinkedHashMap<>();
        this.summaries.put("summary", "");
        this.summaries.put("6SL",     "6SL");
        this.summaries.put("5SL",     "5SL");
        for(String key :this.summaries.keySet()) this.put(key, new DataGroup(key,4,2));
        this.put("2D",       new DataGroup("2D",3,2));
        this.put("p",        new DataGroup("p",3,2));
        this.put("theta",    new DataGroup("theta",3,2));
        this.put("vz",       new DataGroup("vz",3,2));        
        this.put("ndf",      new DataGroup("ndf",3,2));        
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
            H2F hi_ptheta = new H2F("ptheta" + sname, "ptheta", 100, 0.0, 8.0, 100, 0.0, 40.0);   
            hi_ptheta.setTitleX("p (GeV)");
            hi_ptheta.setTitleY("#theta (deg)");
            H1F hi_chi2 = new H1F("chi2" + sname, "chi2", 100, 0.0, 15.0);   
            hi_chi2.setTitleX("#chi2");
            hi_chi2.setTitleY("Counts");
            hi_chi2.setLineColor(col);
            H1F hi_ndf = new H1F("ndf" + sname, "ndf", 40, 0.0, 40.0);   
            hi_ndf.setTitleX("NDF");
            hi_ndf.setTitleY("Counts");
            hi_ndf.setLineColor(col);
            H1F hi_vz = new H1F("vz" + sname, "vz", 180, -50.0, 40.0);   
            hi_vz.setTitleX("Vz (cm)");
            hi_vz.setTitleY("Counts");
            hi_vz.setLineColor(col);
            H2F hi_xy = new H2F("xy" + sname, "xy", 200, -200.0, 200.0, 200, -200.0, 200.0);   
            hi_xy.setTitleX("R1 x (cm)");
            hi_xy.setTitleY("R1 y (cm)");
            this.get(dg).addDataSet(hi_p,      0);
            this.get(dg).addDataSet(hi_theta,  1);
            this.get(dg).addDataSet(hi_phi,    2);
            this.get(dg).addDataSet(hi_ptheta, 3);
            this.get(dg).addDataSet(hi_chi2,   4);
            this.get(dg).addDataSet(hi_ndf,    5);
            this.get(dg).addDataSet(hi_vz,     6);
            this.get(dg).addDataSet(hi_xy,     7);
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
        H1F hi_ndf0 = new H1F("ndf0_" + name, "ndf0", 40, 0.0, 40.0);   
        hi_ndf0.setTitleX("NDF0");
        hi_ndf0.setTitleY("Counts");
        hi_ndf0.setLineColor(col);
        H1F hi_ndf = new H1F("ndf_" + name, "ndf", 40, 0.0, 40.0);   
        hi_ndf.setTitleX("NDF");
        hi_ndf.setTitleY("Counts");
        hi_ndf.setLineColor(col);
        H1F hi_nsl = new H1F("nsl_" + name, "nsl", 8, 0.0, 8.0);   
        hi_nsl.setTitleX("NSL");
        hi_nsl.setTitleY("Counts");
        hi_nsl.setLineColor(col);
        this.get("ndf").addDataSet(hi_ndf0, 0);
        this.get("ndf").addDataSet(hi_ndf,  1);
        this.get("ndf").addDataSet(hi_nsl,  2);
        for(int i=0; i<3; i++) {
            int region = i+1;
            H1F hi_nhit = new H1F("nhit" + region + "_" + name, "nhit", 12, 0.0, 12.0);   
            hi_nhit.setTitleX("Cluster size - region" + region);
            hi_nhit.setTitleY("Counts");
            hi_nhit.setLineColor(col);
            this.get("ndf").addDataSet(hi_nhit, 2+region);
        }
    }
    
    @Override
    public void fill(Track track) {
        this.fillSummaries(track, "summary");
        if(track.SL()==6)      this.fillSummaries(track, "6SL");
        else if(track.SL()==5) this.fillSummaries(track, "5SL");
        this.fill2D(track);
        this.fillSectors(track);
        this.fillNDF(track);
    }  
    
    private void fillSummaries(Track track, String dg) {
        String title = this.summaries.get(dg);
        String sname = title + "_" + this.getName();
        if(track.isValid()) {
            this.get(dg).getH1F("p"      + sname).fill(track.p());
            this.get(dg).getH1F("theta"  + sname).fill(Math.toDegrees(track.theta()));
            this.get(dg).getH1F("phi"    + sname).fill(Math.toDegrees(track.phi()));
            this.get(dg).getH2F("ptheta" + sname).fill(track.p(),Math.toDegrees(track.theta()));
            this.get(dg).getH1F("chi2"   + sname).fill(track.chi2());
            this.get(dg).getH1F("ndf"    + sname).fill(track.NDF());
            this.get(dg).getH2F("xy"     + sname).fill(track.trajectory(DetectorType.DC.getDetectorId(), 6).x()
                                                     ,track.trajectory(DetectorType.DC.getDetectorId(), 6).y());
        }
        if(track.isValid(false)) this.get(dg).getH1F("vz" + sname).fill(track.vz());
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
        if(track.isValid(false)) this.get("vz").getH1F("vzsec"    + sector + "_" +  this.getName()).fill(track.vz());       
    }
    
    private void fillNDF(Track track) {
        if(track.isValid()) {
            this.get("ndf").getH1F("ndf" + "_" + this.getName()).fill(track.NDF());
            this.get("ndf").getH1F("ndf0" + "_" + this.getName()).fill(track.nHits());
            this.get("ndf").getH1F("nsl" + "_" + this.getName()).fill(track.SL());
            for(int isl=0; isl<6; isl++) {
                int region = isl/2+1;
                this.get("ndf").getH1F("nhit" + region + "_" + this.getName()).fill(track.nHits(isl+1));
            }
        }
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
