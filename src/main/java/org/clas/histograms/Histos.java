/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.histograms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.clas.analysis.Track;
import org.jlab.detector.base.DetectorType;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.group.DataGroup;

/**
 *
 * @author devita
 */
public class Histos extends HashMap<String,DataGroup> {
    
    private String name = null;
    
    public Histos(String str, int col) {
        super();
        this.setName(str);
        this.init();        
        this.create(col);
    }
    
    public void init() {
    }
    
    public void create(int col) {
    }
    
    public void fill(Track track) {
    }

    public void fill(ArrayList<Track> tracks) {
    }

    
    public HashMap<String,DataGroup> diff(Histos histo) {
        HashMap<String,DataGroup> diffs = new HashMap<String,DataGroup>();
        for(String key : this.keySet()) {
            diffs.put(key, this.diff(key,histo));
        }
        return diffs;
    }
    
    private DataGroup diff(String key, Histos histo) {
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
        
    public void setName(String name) {
        this.name = name;
    }
        
    private String getPrefix(IDataSet ds) {
        String prefix = ds.getName().split("_")[0];
        return prefix;
    }
    
    public void readDataGroup(TDirectory dir) {
        String folder = this.getName();
        for(String key : this.keySet()) {
            String subfolder = folder + "_" + key;
            int nrows = this.get(key).getRows();
            int ncols = this.get(key).getColumns();
            int nds   = nrows*ncols;
            DataGroup newGroup = new DataGroup(ncols,nrows);
            for(int i = 0; i < nds; i++){
                List<IDataSet> dsList = this.get(key).getData(i);
                for(IDataSet ds : dsList){
//                    System.out.println("\t --> " + ds.getName());
                    newGroup.addDataSet(dir.getObject(subfolder, ds.getName()),i);
                }
            }
            this.replace(key, newGroup);
        }
    }

    public void writeDataGroup(TDirectory dir) {
        String folder = "/" + this.getName();
        dir.mkdir(folder);
        for(String key : this.keySet()) {
            String subfolder = folder + "_" + key;
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
