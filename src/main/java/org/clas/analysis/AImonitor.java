/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.graphics.EmbeddedPad;
import org.jlab.groot.graphics.Histogram2DPlotter;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.io.HipoWriterSorted;
import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.options.OptionParser;

/**
 *
 * @author devita
 */
public class AImonitor {    
        
    private HistoDistribution[] tr          = {new HistoDistribution("trNeg",1),  new HistoDistribution("trPos",1)};
    private HistoDistribution[] ai          = {new HistoDistribution("aiNeg",2),  new HistoDistribution("aiPos",2)};
    private HistoDistribution[] trMatched   = {new HistoDistribution("trNegM",4), new HistoDistribution("trPosM",4)};
    private HistoDistribution[] aiMatched   = {new HistoDistribution("aiNegM",4), new HistoDistribution("aiPosM",4)};
    private HistoDistribution[] trUnmatched = {new HistoDistribution("trNegN",3), new HistoDistribution("trPosN",3)};
    private HistoResolution[]   resol       = {new HistoResolution("negRes",1),   new HistoResolution("posRes",2)};
    
    private Bank trBank = null;
    private Bank aiBank = null;
    
    private String[] charges = {"neg", "pos"};
        
    
    public AImonitor(){
    }
    
    
    public void initBanks(Bank trb, Bank aib){
        trBank = trb;
        aiBank = aib;        
    }

    public EmbeddedCanvasTabbed plotHistos() {
        EmbeddedCanvasTabbed canvas  = null;
        String cname = null;
        for(int i=0; i<2; i++) {
            for(String key : tr[i].keySet()) {
                cname = charges[i] + " " + key;
                if(canvas==null) canvas = new EmbeddedCanvasTabbed(cname);
                else             canvas.addCanvas(cname);
                canvas.getCanvas(cname).draw(tr[i].get(key));
                canvas.getCanvas(cname).draw(ai[i].get(key));
                canvas.getCanvas(cname).draw(trMatched[i].get(key));
                canvas.getCanvas(cname).draw(trUnmatched[i].get(key));
            }
            cname = charges[i] + " resolution";
            canvas.addCanvas(cname);
            canvas.getCanvas(cname).draw(resol[i]);
            cname = charges[i] + " differences";
            canvas.addCanvas(cname);
            canvas.getCanvas(cname).draw(ai[i].diff(tr[i]).get("summary"));
            canvas.getCanvas(cname).draw(aiMatched[i].diff(tr[i]).get("summary"));
            this.setRange(canvas.getCanvas(cname), 0.1);
        }
        return canvas;
    }
    
    private void setRange(EmbeddedCanvas canvas, double range) {
        int nx = canvas.getNColumns();
        int ny = canvas.getNRows();
        for(EmbeddedPad pad : canvas.getCanvasPads()) {
            if(pad.getDatasetPlotters().get(0) instanceof Histogram2DPlotter) pad.getAxisZ().setRange(1-range, 1+range);
            else                                                              pad.getAxisY().setRange( -range,  +range);
        }
    }
        
    public EventStatus processEvent(Event event) {
        EventStatus status = new EventStatus();
        ArrayList<Track> aiTracks = null;
        ArrayList<Track> trTracks = null;
        event.read(trBank);
        event.read(aiBank);
        trTracks = this.read(trBank);            
        aiTracks = this.read(aiBank);
        
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
        }
        if(trTracks!=null) {
            for(Track track : trTracks) {
                tr[(track.charge()+1)/2].fill(track);
                if(track.isMatched()) {
                    trMatched[(track.charge()+1)/2].fill(track);
                }
                else {
                    trUnmatched[(track.charge()+1)/2].fill(track);
                    if(track.isValid()) status.setAiMissing();
                }
            }
        }
        if(aiTracks!=null) {
            for(Track track : aiTracks) {
                ai[(track.charge()+1)/2].fill(track);
                if(track.isMatched()) {
                    aiMatched[(track.charge()+1)/2].fill(track);
                }
                else {
                    if(track.isValid()) status.setCvMissing();
                }
            }
        }
        return status;
    }
        
    public ArrayList<Track> read(Bank bank) {
        ArrayList<Track> tracks = null;
        if(bank.getRows()>0) {
            tracks = new ArrayList();
            for(int loop = 0; loop < bank.getRows(); loop++){
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

    public void readHistos(String fileName) {
        // TXT table summary FILE //
        System.out.println("Opening file: " + fileName);
        TDirectory dir = new TDirectory();
        dir.readFile(fileName);
        System.out.println(dir.getDirectoryList());
        dir.cd();
        dir.pwd();
        for(int i=0; i<2; i++) {
            tr[i].readDataGroup(dir);
            ai[i].readDataGroup(dir);
            trMatched[i].readDataGroup(dir);
            trUnmatched[i].readDataGroup(dir);
            aiMatched[i].readDataGroup(dir);
            resol[i]=resol[i].readDataGroup(dir);
        }
    }

    public void saveHistos(String fileName) {
        TDirectory dir = new TDirectory();
        for(int i=0; i<2; i++) {
            tr[i].writeDataGroup(dir);
            ai[i].writeDataGroup(dir);
            trMatched[i].writeDataGroup(dir);
            trUnmatched[i].writeDataGroup(dir);
            aiMatched[i].writeDataGroup(dir);
            resol[i].writeDataGroup(dir);
        }
        System.out.println("Saving histograms to file " + fileName);
        dir.writeFile(fileName);
    }    
 
    public void printHistos(EmbeddedCanvasTabbed canvas) {
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
            canvas.getCanvas(cname).save(figures + "/" + cname + ".png");
            cname = charges[i] + " resolution";
            canvas.getCanvas(cname).save(figures + "/" + cname + ".png");
            for(String key : tr[i].keySet()) {
                cname = charges[i] + " " + key;
                canvas.getCanvas(cname).save(figures + "/" + cname + ".png");
            }
        }
    }
    
    public static void setWriter(HipoWriterSorted writer, SchemaFactory schema, String filename) {
        System.out.println("\nOpening output file: " + filename);
        writer.getSchemaFactory().copy(schema);
        writer.setCompressionType(2);
        writer.open(filename);    
    }
        
    public class EventStatus {
        private boolean aiMissing=false;
        private boolean cvMissing=false;

        public EventStatus() {
        }

        public boolean isAiMissing() {
            return aiMissing;
        }

        public void setAiMissing() {
            this.aiMissing = true;
        }

        public boolean isCvMissing() {
            return cvMissing;
        }

        public void setCvMissing() {
            this.cvMissing = true;
        }      
    }
    
    public static void main(String[] args){

        OptionParser parser = new OptionParser("aiTracking");
        parser.setRequiresInputList(false);
        parser.addOption("-o"    ,"",     "output file name prefix");
        parser.addOption("-n"    ,"-1",   "maximum number of events to process");
        parser.addOption("-b"    ,"TB",   "tracking level: TB or HB");
        parser.addOption("-r"    ,"",     "histogram file to be read");
        parser.addOption("-w"    ,"true", "display histograms");
        parser.parse(args);
        
        int   maxEvents  = parser.getOption("-n").intValue();

        String namePrefix  = parser.getOption("-o").stringValue();        
        String histoName  = "histo.hipo";
        String eventName1 = "missing_ai.hipo";
        String eventName2 = "missing_cv.hipo";
        if(!namePrefix.isEmpty()) {
            histoName  = namePrefix + "_" + histoName;
            eventName1 = namePrefix + "_" + eventName1;
            eventName2 = namePrefix + "_" + eventName2;
        }        
        String type     = parser.getOption("-b").stringValue();        
        String readName = parser.getOption("-r").stringValue();        
        boolean window  = Boolean.parseBoolean(parser.getOption("-w").stringValue());
        
        if(!window) System.setProperty("java.awt.headless", "true");
        
        SchemaFactory schema = null;           
        
        AImonitor analysis = new AImonitor();
        
        HipoWriterSorted writer1 = new HipoWriterSorted();
        HipoWriterSorted writer2 = new HipoWriterSorted();

        if(readName.isEmpty()) {
            
            List<String> inputList = parser.getInputList();
            if(inputList.isEmpty()==true){
                parser.printUsage();
                System.out.println("\n >>>> error: no input file is specified....\n");
                System.exit(0);
            }

            ProgressPrintout progress = new ProgressPrintout();
        
            int counter=-1;
            Event event = new Event();

            for(String inputFile : inputList){
                HipoReader reader = new HipoReader();
                reader.open(inputFile);

                if(schema==null) {
                    schema = reader.getSchemaFactory();
                    AImonitor.setWriter(writer1, schema, eventName1);
                    AImonitor.setWriter(writer2, schema, eventName2);

                    Bank tr = new Bank(schema.getSchema("TimeBasedTrkg::TBTracks"));
                    Bank ai = new Bank(schema.getSchema("TimeBasedTrkg::AITracks"));
                    if(type.equals("HB")) {
                        tr = new Bank(schema.getSchema("HitBasedTrkg::HBTracks"));
                        ai = new Bank(schema.getSchema("HitBasedTrkg::AITracks"));
                    }
                    analysis.initBanks(tr,ai);
                }

                while (reader.hasNext()) {

                    counter++;

                    reader.nextEvent(event);

                    EventStatus status = analysis.processEvent(event);

                    if(status.isAiMissing()) writer1.addEvent(event);
                    if(status.isCvMissing()) writer2.addEvent(event);

                    progress.updateStatus();
                    if(maxEvents>0){
                        if(counter>=maxEvents) break;
                    }
                }
                progress.showStatus();
                reader.close();
            }    

            writer1.close();
            writer2.close();
            analysis.saveHistos(histoName);
        }
        else{
            analysis.readHistos(readName);
        }
        
        if(window) {
            JFrame frame = new JFrame(type);
            frame.setSize(1200, 800);
            EmbeddedCanvasTabbed canvas = analysis.plotHistos();
            frame.add(canvas);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            analysis.printHistos(canvas);
        }
    }
    
}
