/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.analysis;

import java.util.ArrayList;
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
    
    private EmbeddedCanvasTabbed   canvas  = null;
    
    // negative tracks
    private HistoDistribution trNeg  = new HistoDistribution("trNeg",1);
    private HistoDistribution aiNeg  = new HistoDistribution("aiNeg",2);
    private HistoDistribution trNegM = new HistoDistribution("trNeg",3);
    private HistoDistribution aiNegM = new HistoDistribution("aiNeg",4);
    private HistoResolution   negRes = new HistoResolution("negRes",1);
    // positive tracks
    private HistoDistribution trPos  = new HistoDistribution("trPos",1);
    private HistoDistribution aiPos  = new HistoDistribution("aiPos",2);
    private HistoDistribution trPosM = new HistoDistribution("trPosM",3);
    private HistoDistribution aiPosM = new HistoDistribution("aiPosM",4);
    private HistoResolution   posRes = new HistoResolution("posRes",2);
    
    private String trBank = null;
    private String aiBank = null;
    
    
    public AImonitor(String tr, String ai){
        trBank = tr;
        aiBank = ai;
        this.createHistos();
        
    }

    
    private void createHistos() {
        
        
    }
    
    public void plotHistos() {
        canvas = new EmbeddedCanvasTabbed("negatives","positives","negative resolution","positive resolution","negative difference","positive difference");
        canvas.getCanvas("negatives").draw(trNeg);
        canvas.getCanvas("negatives").draw(aiNeg);
//        canvas.getCanvas("negatives").draw(trNegM);
//        canvas.getCanvas("negatives").draw(aiNegM);
        canvas.getCanvas("positives").draw(trPos);
        canvas.getCanvas("positives").draw(aiPos);
//        canvas.getCanvas("positives").draw(trPosM);
//        canvas.getCanvas("positives").draw(aiPosM);
        canvas.getCanvas("negative resolution").draw(negRes);
        canvas.getCanvas("positive resolution").draw(posRes);
        canvas.getCanvas("negative difference").draw(aiNeg.diff(trNeg));
        canvas.getCanvas("positive difference").draw(aiPos.diff(trPos));
        this.setRange(canvas.getCanvas("negative difference"), 0.1);
        this.setRange(canvas.getCanvas("positive difference"), 0.1);
    }
    
    public void setRange(EmbeddedCanvas canvas, double range) {
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
                if(track.charge()<0) aiNeg.fill(track);
                else                 aiPos.fill(track);
            }
        }
        if(event.hasBank(trBank)) {
            trTracks = this.read(event.getBank(trBank));
            for(Track track: trTracks) {
                if(track.charge()<0) trNeg.fill(track);
                else                 trPos.fill(track);
            }
        }
        if(trTracks!=null && aiTracks!=null) {
            for(Track tr : trTracks) {
                for(Track ai : aiTracks) {
                    if(tr.compareTo(ai)==0) {
                        tr.setMatch(true);
                        ai.setMatch(true);
                        if(tr.charge()>0) posRes.fill(tr, ai);
                        else              negRes.fill(tr, ai);
                    }
                }
            }
            for(Track tr : trTracks) {
                if(tr.isMatched()) {
                    if(tr.charge()>0) trPosM.fill(tr);
                    else              trNegM.fill(tr);
                }
            }
            for(Track ai : aiTracks) {
                if(ai.isMatched()) {
                    if(ai.charge()>0) aiPosM.fill(ai);
                    else              aiNegM.fill(ai);
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
        trNeg.writeDataGroup(dir);
        trPos.writeDataGroup(dir);
        aiNeg.writeDataGroup(dir);
        aiPos.writeDataGroup(dir);
//        trNegM.writeDataGroup(dir);
//        trPosM.writeDataGroup(dir);
//        aiNegM.writeDataGroup(dir);
//        aiPosM.writeDataGroup(dir);
        System.out.println("Saving histograms to file " + fileName);
        dir.writeFile(fileName);
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
        
        JFrame frame = new JFrame(type);
        frame.setSize(1200, 800);
        frame.add(analysis.getCanvas());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
    }
    
}
