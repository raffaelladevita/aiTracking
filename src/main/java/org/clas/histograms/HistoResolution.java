package org.clas.histograms;

import java.util.List;
import org.clas.analysis.Track;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.group.DataGroup;

/**
 *
 * @author devita
 */
public class HistoResolution extends DataGroup{
    
    
    public HistoResolution(String str) {
        super(str, 3 ,2);
    }
    
    public HistoResolution(String str, int col) {
        super(str, 3 ,2);
        this.create(col);
    }
    
    private void create(int col) {
        String name = this.getName();
        H1F hi_p = new H1F("p_" + name, "p", 100, -0.01, 0.01);     
        hi_p.setTitleX("#Deltap/p");
        hi_p.setTitleY("Counts");
        hi_p.setLineColor(col);
        H1F hi_theta = new H1F("theta_" + name, "theta", 100, -0.1, 0.1); 
        hi_theta.setTitleX("#Delta#theta (deg)");
        hi_theta.setTitleY("Counts");
        hi_theta.setLineColor(col);
        H1F hi_phi = new H1F("phi_" + name, "phi", 100, -0.5, 0.5);  
        hi_phi.setTitleX("#Delta#phi (deg)");
        hi_phi.setTitleY("Counts");
        hi_phi.setLineColor(col);
        H1F hi_chi2 = new H1F("chi2_" + name, "chi2", 100, -1, 1);  
        hi_chi2.setTitleX("#Delta#chi2");
        hi_chi2.setTitleY("Counts");
        hi_chi2.setLineColor(col);
        H1F hi_vz = new H1F("vz_" + name, "vz", 180, -0.5, 0.5);  
        hi_vz.setTitleX("#DeltaVz (cm)");
        hi_vz.setTitleY("Counts");
        hi_vz.setLineColor(col);
        this.addDataSet(hi_p, 0);
        this.addDataSet(hi_theta, 1);
        this.addDataSet(hi_phi, 2);
        this.addDataSet(hi_chi2, 3);
        this.addDataSet(hi_vz, 4);
    }
    
    public void fill(Track track1, Track track2) {
        this.getH1F("p_"     + this.getName()).fill((track1.p()-track2.p())/track1.p());
        this.getH1F("theta_" + this.getName()).fill(Math.toDegrees(track1.theta()-track2.theta()));
        this.getH1F("phi_"   + this.getName()).fill(Math.toDegrees(track1.phi()-track2.phi()));
        this.getH1F("chi2_"  + this.getName()).fill(track1.chi2()-track2.chi2());
        this.getH1F("vz_"    + this.getName()).fill(track1.vz()-track2.vz());
    }

    
    public HistoResolution readDataGroup(TDirectory dir) {
        String folder = this.getName();
        int nrows = this.getRows();
        int ncols = this.getColumns();
        int nds   = nrows*ncols;
        boolean replace = true;
        HistoResolution newGroup = new HistoResolution(this.getName());
        for(int i = 0; i < nds; i++){
            List<IDataSet> dsList = this.getData(i);
            for(IDataSet ds : dsList){
//                    System.out.println("\t --> " + ds.getName());
                if(dir.getObject(folder, ds.getName())!=null) {
                    newGroup.addDataSet(dir.getObject(folder, ds.getName()),i);
                }
            }
        }
        return newGroup;
    }

    public void writeDataGroup(TDirectory dir) {
        String folder = "/" + this.getName();
        dir.mkdir(folder);
        dir.cd(folder);        
        int nrows = this.getRows();
        int ncols = this.getColumns();
        int nds   = nrows*ncols;
        for(int i = 0; i < nds; i++){
            List<IDataSet> dsList = this.getData(i);
            for(IDataSet ds : dsList){
//                    System.out.println("\t --> " + ds.getName());
                dir.addDataSet(ds);
            }          
        }
    }
 
}
