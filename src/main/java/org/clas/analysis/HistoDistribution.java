/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.analysis;

import java.util.HashMap;
import java.util.List;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.group.DataGroup;

/**
 *
 * @author devita
 */
public class HistoDistribution extends HashMap<String,DataGroup> {
    
    private String name = null;
    
    public HistoDistribution(String str, int col) {
        super();
        this.init(str);        
        this.create(col);
    }
    
    private void init(String str) {
        this.name = str;
        this.put("summary",  new DataGroup("summary",3,2));
        this.put("p",        new DataGroup("p",3,2));
        this.put("theta",    new DataGroup("theta",3,2));
        this.put("vz",       new DataGroup("vz",3,2));        
    }
    
    private void create(int col) {
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
    
    public void fill(Track track) {
        int sector = track.sector();
        if(track.isValid()) {
            this.get("summary").getH1F("p_"     + this.getName()).fill(track.p());
            this.get("summary").getH1F("theta_" + this.getName()).fill(Math.toDegrees(track.theta()));
            this.get("summary").getH1F("phi_"   + this.getName()).fill(Math.toDegrees(track.phi()));
            this.get("summary").getH1F("chi2_"  + this.getName()).fill(track.chi2());
            this.get("summary").getH2F("xy_"    + this.getName()).fill(track.r3().x(),track.r3().y());
            this.get("p").getH1F("psec"         + sector + "_" + this.getName()).fill(track.p());
            this.get("theta").getH1F("thetasec" + sector + "_" +  this.getName()).fill(Math.toDegrees(track.theta()));
        }
        this.get("summary").getH1F("vz_" + this.getName()).fill(track.vz());
        this.get("vz").getH1F("vzsec"    + sector + "_" +  this.getName()).fill(track.vz());
    }

    
    public HashMap<String,DataGroup> diff(HistoDistribution histo) {
        HashMap<String,DataGroup> diffs = new HashMap<String,DataGroup>();
        for(String key : this.keySet()) {
            diffs.put(key, this.diff(key,histo));
        }
        return diffs;
    }
    
    private DataGroup diff(String key, HistoDistribution histo) {
        DataGroup dg = new DataGroup(this.get(key).getColumns(),this.get(key).getRows());
        int nrows = dg.getRows();
        int ncols = dg.getColumns();
        int nds   = nrows*ncols;
        for(int i = 0; i < nds; i++){
            List<IDataSet> dsList = histo.get(key).getData(i);
            for(IDataSet ds : dsList){
                dg.addDataSet(this.diff(key, ds),i);
            }
        }
        return dg;
    }

    private IDataSet diff(String key, IDataSet ds) {
        if(ds instanceof H1F) {
            return this.diffH1(key, (H1F) ds);
        }
        else if(ds instanceof H2F) {
            return this.diffH2(key, (H2F) ds);
        }
        else {
            return null;
        }
    }    
   
    private H1F diffH1(String key ,H1F h1) {
        String hname = this.getPrefix(h1) + "_" + this.getName();
        int   icolor = this.get(key).getH1F(hname).getLineColor();
        H1F h = this.get(key).getH1F(hname).histClone(hname);
        h.sub(h1);
        h.divide(h1);
        h.setTitleY("(" + this.getName() + " - " + h1.getName().split("_")[1] + ")/" + h1.getName().split("_")[1]);
        h.setLineColor(icolor);
        return h;
    }

    private H2F diffH2(String key ,H2F histo) {
        String hname = this.getPrefix(histo) + "_" + this.getName();
        H2F h = this.get(key).getH2F(hname).histClone(hname);
        h.setTitleX(this.get(key).getH2F(hname).getTitleX());
        h.setTitleY(this.get(key).getH2F(hname).getTitleY());
        h.divide(histo);
//        int nx = h.getDataSize(0);
//        int ny = h.getDataSize(1);
//        for(int ix=0; ix<nx; ix++) {
//            for(int iy=0; iy<ny; iy++) {
//                double h1 = h.getBinContent(ix, iy);
//                double h2 = histo.getBinContent(ix, iy);
//                if(h2>0) {
//                    double w = (h1-h2)/h2;
//                    if     (w> range) w =  range;
//                    else if(w<-range) w = -range;
//                    h.setBinContent(ix, iy, w);
//                }
//            }
//        }
        return h;
    }

    public String getName() {
        return name;
    }
        
    private String getPrefix(IDataSet ds) {
        String prefix = ds.getName().split("_")[0];
        return prefix;
    }
    
    public void writeDataGroup(TDirectory dir) {
        String folder = "/" + this.getName();
        dir.mkdir(folder);
        for(String key : this.keySet()) {
            String subfolder = folder + "/" + key;
            dir.mkdir(subfolder);
            dir.cd(subfolder);        
            int nrows = this.get(key).getRows();
            int ncols = this.get(key).getColumns();
            int nds   = nrows*ncols;
            for(int i = 0; i < nds; i++){
                List<IDataSet> dsList = this.get(key).getData(i);
                for(IDataSet ds : dsList){
//                    System.out.println("\t --> " + ds.getName());
                    dir.addDataSet(ds);
                }
            }
        }
    }
    
}
