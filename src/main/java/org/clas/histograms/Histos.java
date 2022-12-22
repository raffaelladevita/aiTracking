/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.histograms;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.clas.analysis.Track;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.group.DataGroup;

/**
 *
 * @author devita
 */
public class Histos extends LinkedHashMap<String,DataGroup> {
    
    private String name  = null;
    private String title = null;
    
    public Histos(String str, String title, int col) {
        super();
        this.setName(str);
        this.setTitle(title);
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

    public int getEntries() {
        return 0;
    }
    
    public int getEntries(String key) {
        return 0;
    }
    
    public LinkedHashMap<String,DataGroup> diff(Histos histo, int minEntries) {
        LinkedHashMap<String,DataGroup> diffs = new LinkedHashMap<>();
        for(String key : this.keySet()) {
            diffs.put(key, this.diff(key,histo,minEntries));
        }
        return diffs;
    }
    
    private DataGroup diff(String key, Histos histo, int minEntries) {
        DataGroup dg = new DataGroup(this.get(key).getColumns(),this.get(key).getRows());
        int nrows = dg.getRows();
        int ncols = dg.getColumns();
        int nds   = nrows*ncols;
        for(int i = 0; i < nds; i++){
            List<IDataSet> dsList = histo.get(key).getData(i);
            for(IDataSet ds : dsList){
                dg.addDataSet(this.diff(key, ds, minEntries),i);
            }
        }
        return dg;
    }

    private IDataSet diff(String key, IDataSet ds, int minEntries) {
        if(ds instanceof H1F) {
            return this.diffH1(key, (H1F) ds, minEntries);
        }
        else if(ds instanceof H2F) {
            return this.diffH2(key, (H2F) ds, minEntries);
        }
        else {
            return null;
        }
    }    
   
    private H1F diffH1(String key ,H1F h1, int minEntries) {
        String hname = this.getPrefix(h1) + "_" + this.getName();
        int   icolor = this.get(key).getH1F(hname).getLineColor();
        H1F h = this.get(key).getH1F(hname).histClone(hname);
        for(int i=0; i< h.getDataSize(0); i++) {
            double v1 = h.getBinContent(i);
            double v2 = h1.getBinContent(i);
            double ratio = 0;
            double err   = 0;
            if(v2>minEntries && v1>minEntries) {
                ratio = v1/v2;
                err   = (v1/v2)*Math.sqrt(Math.abs(v1-v2)/v1/v2);
            }
            h.setBinContent(i, ratio);
            h.setBinError(i, err);
        }
        h.setTitleY("(" + this.getName() + " - " + h1.getName().split("_")[1] + ")/" + h1.getName().split("_")[1]);
        h.setLineColor(icolor);
        return h;
    }

    private H2F diffH2(String key ,H2F histo, int minEntries) {
        String hname = this.getPrefix(histo) + "_" + this.getName();
        H2F h = this.get(key).getH2F(hname).histClone(hname);
        h.setTitleX(this.get(key).getH2F(hname).getTitleX());
        h.setTitleY(this.get(key).getH2F(hname).getTitleY());
//        h.divide(histo);
        int nx = h.getDataSize(0);
        int ny = h.getDataSize(1);
        for(int ix=0; ix<nx; ix++) {
            for(int iy=0; iy<ny; iy++) {
                double h1 = h.getBinContent(ix, iy);
                double h2 = histo.getBinContent(ix, iy);
                double ratio = 0;
                if(h2>minEntries && h1>minEntries) {
                    ratio = h1/h2;
                }
                h.setBinContent(ix, iy, ratio);
            }
        }
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setStats(String opts) {
        for(String key : this.keySet()) {
            DataGroup dg = this.get(key);
            int nrows = dg.getRows();
            int ncols = dg.getColumns();
            int nds   = nrows*ncols;
            for(int i = 0; i < nds; i++){
                List<IDataSet> dsList = dg.getData(i);
                for(IDataSet ds : dsList){
                    if(ds instanceof H1F) ((H1F) ds).setOptStat(opts);
                }
            }
        }
    }
    
    public void readDataGroup(TDirectory dir) {
        String folder = this.getName();
        for(String key : this.keySet()) {
            String subfolder = folder + "_" + key;
            int nrows = this.get(key).getRows();
            int ncols = this.get(key).getColumns();
            int nds   = nrows*ncols;
            boolean replace = true;
            DataGroup newGroup = new DataGroup(ncols,nrows);
            for(int i = 0; i < nds; i++){
                List<IDataSet> dsList = this.get(key).getData(i);
                for(IDataSet ds : dsList){
//                    System.out.println("\t --> " + ds.getName());
                    if(dir.getObject(subfolder, ds.getName())!=null) {
                        newGroup.addDataSet(dir.getObject(subfolder, ds.getName()),i);
                    }
                    else {
                        replace = false;
                    }
                }
            }
            if(replace) this.replace(key, newGroup);
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
