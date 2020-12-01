/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.analysis;

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
public class HistoDistribution extends DataGroup{
    
    
    public HistoDistribution(String str, int col) {
        super(str, 3, 2);
        this.create(col);
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
        this.addDataSet(hi_p,     0);
        this.addDataSet(hi_theta, 1);
        this.addDataSet(hi_phi,   2);
        this.addDataSet(hi_chi2,  3);
        this.addDataSet(hi_vz,    4);
        this.addDataSet(hi_xy,    5);
    }
    
    public void fill(Track track) {
        if(track.isValid()) {
            this.getH1F("p_"     + this.getName()).fill(track.p());
            this.getH1F("theta_" + this.getName()).fill(Math.toDegrees(track.theta()));
            this.getH1F("phi_"   + this.getName()).fill(Math.toDegrees(track.phi()));
            this.getH1F("chi2_"  + this.getName()).fill(track.chi2());
            this.getH2F("xy_"    + this.getName()).fill(track.r3().x(),track.r3().y());
        }
        this.getH1F("vz_" + this.getName()).fill(track.vz());
    }

    public DataGroup diff(HistoDistribution histo) {
        DataGroup dg = new DataGroup(this.getColumns(),this.getRows());
        dg.addDataSet(this.diff(histo.getHisto1("p_")),     0);
        dg.addDataSet(this.diff(histo.getHisto1("theta_")), 1);
        dg.addDataSet(this.diff(histo.getHisto1("phi_")),   2);
        dg.addDataSet(this.diff(histo.getHisto1("chi2_")),  3);
        dg.addDataSet(this.diff(histo.getHisto1("vz_")),    4);
        dg.addDataSet(this.diff(histo.getHisto2("xy_")),    5);
        return dg;
    }

   
    private H1F diff(H1F histo) {
        String hname = histo.getName().split("_")[0]+"_"+this.getName();
        H1F h = this.getH1F(hname).histClone(hname);
        h.sub(histo);
        h.divide(histo);
        h.setTitleY("(" + this.getName() + " - " + histo.getName().split("_")[1] + ")/" + histo.getName().split("_")[1]);
        return h;
    }

    private H2F diff(H2F histo) {
        String hname = histo.getName().split("_")[0]+"_"+this.getName();
        H2F h = this.getH2F(hname).histClone(hname);
        h.setTitleX(this.getH2F(hname).getTitleX());
        h.setTitleY(this.getH2F(hname).getTitleY());
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

    private H1F getHisto1(String prefix) {
        String hname = prefix + this.getName();
        return this.getH1F(hname);
    }
        
    private H2F getHisto2(String prefix) {
        String hname = prefix + this.getName();
        return this.getH2F(hname);
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
                System.out.println("\t --> " + ds.getName());
                dir.addDataSet(ds);
            }
        }
    }
    
}
