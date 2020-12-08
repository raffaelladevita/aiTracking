/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.analysis;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JFrame;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.graphics.EmbeddedPad;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.options.OptionParser;

/**
 *
 * @author devita
 */
public class AImonitor {    
    
    private EmbeddedCanvasTabbed canvas  = null;
    
    private HistoDistribution[] tr          = {new HistoDistribution("trNeg",1),  new HistoDistribution("trPos",1)};
    private HistoDistribution[] ai          = {new HistoDistribution("aiNeg",2),  new HistoDistribution("aiPos",2)};
    private HistoDistribution[] trMatched   = {new HistoDistribution("trNegM",4), new HistoDistribution("trPosM",4)};
    private HistoDistribution[] aiMatched   = {new HistoDistribution("aiNegM",4), new HistoDistribution("aiPosM",4)};
    private HistoDistribution[] trUnmatched = {new HistoDistribution("trNegN",3), new HistoDistribution("trPosN",3)};
    private HistoResolution[]   resol       = {new HistoResolution("negRes",1),   new HistoResolution("posRes",2)};
    
    private String trBank = null;
    private String aiBank = null;
    
    private String[] charges = {"neg", "pos"};
        
    
    public AImonitor(String trb, String aib){
        trBank = trb;
        aiBank = aib;        
    }
    
    public void plotHistos() {
        String cname = null;
        for(int i=0; i<2; i++) {
            for(String key : tr[i].keySet()) {
                cname = charges[i] + " " + key;
                if(canvas==null) canvas = new EmbeddedCanvasTabbed(cname);
                else             canvas.addCanvas(cname);
                canvas.getCanvas(cname).draw(tr[i].get(key));
                canvas.getCanvas(cname).draw(ai[i].get(key));
//                canvas.getCanvas(cname).draw(trMatched[i].get(key));
//                canvas.getCanvas(cname).draw(trUnmatched[i].get(key));
            }
            cname = charges[i] + " resolution";
            canvas.addCanvas(cname);
            canvas.getCanvas(cname).draw(resol[i]);
            cname = charges[i] + " differences";
            canvas.addCanvas(cname);
            canvas.getCanvas(cname).draw(ai[i].diff(tr[i]).get("summary"));
//            canvas.getCanvas(cname).draw(aiMatched[i].diff(tr[i]).get("summary"));
            this.setRange(canvas.getCanvas(cname), 0.1);
        }
    }
    
    private void setRange(EmbeddedCanvas canvas, double range) {
        int nx = canvas.getNColumns();
        int ny = canvas.getNRows();
        for(EmbeddedPad pad : canvas.getCanvasPads()) {
            pad.getAxisZ().setRange(1-range, 1+range);
        }
    }
    
    public void processEvent(DataEvent event) {
        ArrayList<Track> aiTracks = null;
        ArrayList<Track> trTracks = null;
        if(event.hasBank(aiBank)) {
            aiTracks = this.read(event.getBank(aiBank));
            for(Track track: aiTracks) {
                ai[(track.charge()+1)/2].fill(track);
            }
        }
        if(event.hasBank(trBank)) {
            trTracks = this.read(event.getBank(trBank));
            for(Track track: trTracks) {
                tr[(track.charge()+1)/2].fill(track);
            }
        }
        if(trTracks!=null && aiTracks!=null) {
            for(Track tr : trTracks) {
                for(Track ai : aiTracks) {
                    if(tr.compareTo(ai)==0) {
                        tr.setMatch(true);
                        ai.setMatch(true);
                        resol[(tr.charge()+1)/2].fill(tr, ai);
                    }
                }
            }
            for(Track track : trTracks) {
                if(track.isMatched()) {
                    trMatched[(track.charge()+1)/2].fill(track);
                }
                else {
                    trUnmatched[(track.charge()+1)/2].fill(track);
                    
                }
            }
            for(Track track : aiTracks) {
                if(track.isMatched()) {
                    aiMatched[(track.charge()+1)/2].fill(track);
                }
            }
        }
    }
        
    public ArrayList<Track> read(DataBank bank) {
        ArrayList<Track> tracks = null;
        if(bank.rows()>0) {
            tracks = new ArrayList();
            for(int loop = 0; loop < bank.rows(); loop++){
                Track track = new Track(bank.getByte("q", loop),
                                        bank.getFloat("p0_x", loop),
                                        bank.getFloat("p0_y", loop),
                                        bank.getFloat("p0_z", loop),
                                        bank.getFloat("Vtx0_x", loop),
                                        bank.getFloat("Vtx0_y", loop),
                                        bank.getFloat("Vtx0_z", loop));
                track.sector(bank.getByte("sector", loop));
                track.NDF(bank.getShort("ndf", loop));
                track.chi2(bank.getFloat("chi2", loop)/bank.getShort("ndf", loop));
                track.r3(bank.getFloat("c3_x", loop),bank.getFloat("c3_y", loop),bank.getFloat("p0_x", loop));
                track.clusters(bank.getShort("Cluster1_ID", loop),
                               bank.getShort("Cluster2_ID", loop),
                               bank.getShort("Cluster3_ID", loop),
                               bank.getShort("Cluster4_ID", loop),
                               bank.getShort("Cluster5_ID", loop),
                               bank.getShort("Cluster6_ID", loop));
                tracks.add(track);
            }
        }
        return tracks;
    }

    public EmbeddedCanvasTabbed getCanvas() {
        return canvas;
    }
    
    public void saveHistos(String fileName) {
        TDirectory dir = new TDirectory();
        for(int i=0; i<2; i++) {
            tr[i].writeDataGroup(dir);
            ai[i].writeDataGroup(dir);
            trMatched[i].writeDataGroup(dir);
            trUnmatched[i].writeDataGroup(dir);
            aiMatched[i].writeDataGroup(dir);
        }
        System.out.println("Saving histograms to file " + fileName);
        dir.writeFile(fileName);
    }    
 
    public void printHistos() {
        String figures = "plots"; 
        File theDir = new File(figures);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            boolean result = false;
            try{
                theDir.mkdir();
                result = true;
            } 
            catch(SecurityException se){
                //handle it
            }        
            if(result) {    
            System.out.println("Created directory: " + figures);
            }
        }
        for(int i=0; i<2; i++) {
            String cname = charges[i] + " differences";
            this.canvas.getCanvas(cname).save(figures + "/" + cname + ".png");
            for(String key : tr[i].keySet()) {
                cname = charges[i] + " " + key;
                this.canvas.getCanvas(cname).save(figures + "/" + cname + ".png");
            }
        }
    }
    
    public static void main(String[] args){

        OptionParser parser = new OptionParser("aiTracking");
        parser.setRequiresInputList(false);
        parser.addOption("-o"    ,"histo.hipo", "output histogram file name");
        parser.addOption("-n"    ,"-1", "maximum number of events to process");
        parser.addOption("-b"    ,"TB", "tracking level: TB or HB");
        parser.parse(args);
        
        List<String> inputList = parser.getInputList();
        if(inputList.isEmpty()==true){
            parser.printUsage();
            System.out.println("\n >>>> error: no input file is specified....\n");
            System.exit(0);
        }

        int     maxEvents  = parser.getOption("-n").intValue();

        String fileName = parser.getOption("-o").stringValue();
        
        String  type  = parser.getOption("-b").stringValue();
        String tr = "TimeBasedTrkg::TBTracks";
        String ai = "TimeBasedTrkg::AITracks";
        if(type.equals("HB")) {
            tr = "HitBasedTrkg::HBTracks";
            ai = "HitBasedTrkg::AITracks";
        }
        
//        List<String> inputList = new ArrayList<String>();
//        inputList.add("/Users/devita/out_clas_005038.1231.ai.hipo");
//        int maxEvents = -1;
        
        AImonitor analysis = new AImonitor(tr,ai);
        
        ProgressPrintout  progress = new ProgressPrintout();
        
        int counter=-1;
        for(String inputFile : inputList){
            HipoDataSource reader = new HipoDataSource();
            reader.open(inputFile);
            
            while (reader.hasEvent()) {

                counter++;

                DataEvent event = reader.getNextEvent();
                analysis.processEvent(event);
            
                progress.updateStatus();
                if(maxEvents>0){
                    if(counter>=maxEvents) break;
                }
            }
            progress.showStatus();
            reader.close();
        }    
        analysis.plotHistos();
        analysis.saveHistos(fileName);
        analysis.printHistos();
        
        JFrame frame = new JFrame(type);
        frame.setSize(1200, 800);
        frame.add(analysis.getCanvas());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
    }
    
}
