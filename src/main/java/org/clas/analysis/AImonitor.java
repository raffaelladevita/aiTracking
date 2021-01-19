package org.clas.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import org.jlab.detector.base.DetectorType;
import org.jlab.groot.base.GStyle;
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
    private HistoDistribution[] aiUnmatched = {new HistoDistribution("aiNegN",3), new HistoDistribution("aiPosN",3)};
    private HistoResolution[]   resol       = {new HistoResolution("negRes",1),   new HistoResolution("posRes",2)};
    private HistoEvent          trEvent     = null;
    private HistoEvent          aiEvent     = new HistoEvent("ai",2,10.604,2212);
     
    private Banks banks = null;
    
    private String[] charges = {"neg", "pos"};
        
    
    public AImonitor(double beamEnergy, int targetPDG){
        trEvent = new HistoEvent("tr", 1, beamEnergy, targetPDG);
        aiEvent = new HistoEvent("ai", 2, beamEnergy, targetPDG);
    }
    
    
    public void initBanks(Banks banks){
        this.banks = banks;        
    }

    public EmbeddedCanvasTabbed plotHistos() {
        
//        GStyle.getH1FAttributes().setOptStat("1111");
        GStyle.getAxisAttributesX().setTitleFontSize(24);
        GStyle.getAxisAttributesX().setLabelFontSize(18);
        GStyle.getAxisAttributesY().setTitleFontSize(24);
        GStyle.getAxisAttributesY().setLabelFontSize(18);
        GStyle.getAxisAttributesZ().setLabelFontSize(14);
        GStyle.getAxisAttributesX().setLabelFontName("Arial");
        GStyle.getAxisAttributesY().setLabelFontName("Arial");
        GStyle.getAxisAttributesZ().setLabelFontName("Arial");
        GStyle.getAxisAttributesX().setTitleFontName("Arial");
        GStyle.getAxisAttributesY().setTitleFontName("Arial");
        GStyle.getAxisAttributesZ().setTitleFontName("Arial");
        GStyle.setGraphicsFrameLineWidth(1);
        GStyle.getH1FAttributes().setLineWidth(2);

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
        cname = "2pi";
        canvas.addCanvas(cname);
        canvas.getCanvas(cname).draw(trEvent.get("2pi"));
        canvas.getCanvas(cname).draw(aiEvent.get("2pi"));                    
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
        trTracks = this.read(0, event);            
        aiTracks = this.read(1, event);

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
            trEvent.fill(trTracks);
        }
        if(aiTracks!=null) {
            for(Track track : aiTracks) {
                ai[(track.charge()+1)/2].fill(track);
                if(track.isMatched()) {
                    aiMatched[(track.charge()+1)/2].fill(track);
                }
                else {
                    aiUnmatched[(track.charge()+1)/2].fill(track);
                    if(track.isValid()) status.setCvMissing();
                }
            }
            aiEvent.fill(aiTracks);
        }
        return status;
    }


    public ArrayList<Track> read(int type, Event event) {	
        ArrayList<Track> tracks = null;
	Bank runConfig = banks.getRunConfig();
        Bank particleBank   = banks.getRecParticleBank(type);
        Bank trajectoryBank = banks.getRecTrajectoryBank(type);
        Bank trackBank      = banks.getRecTrackBank(type);
        Bank trackingBank   = banks.getTrackingBank(type);
	if(runConfig!=null) event.read(runConfig);
	if(particleBank!=null)   event.read(particleBank);
        if(trajectoryBank!=null) event.read(trajectoryBank);
        if(trackBank!=null)      event.read(trackBank);
        if(trackingBank!=null)   event.read(trackingBank);
        if(trackingBank!=null && trackingBank.getRows()>0) {
            // create tracks list from track bank
            tracks = new ArrayList();
            for(int loop = 0; loop < trackingBank.getRows(); loop++){
                Track track = new Track(trackingBank.getByte("q", loop),
                                        trackingBank.getFloat("p0_x", loop),
                                        trackingBank.getFloat("p0_y", loop),
                                        trackingBank.getFloat("p0_z", loop),
                                        trackingBank.getFloat("Vtx0_x", loop),
                                        trackingBank.getFloat("Vtx0_y", loop),
                                        trackingBank.getFloat("Vtx0_z", loop));
                track.sector(trackingBank.getByte("sector", loop));
                track.NDF(trackingBank.getShort("ndf", loop));
                track.chi2(trackingBank.getFloat("chi2", loop)/trackingBank.getShort("ndf", loop));
                for(int i=0; i<2; i++) {
                    track.trajectory(trackingBank.getFloat("c" + (i*2+1) + "_x", loop),
                                     trackingBank.getFloat("c" + (i*2+1) + "_y", loop),
                                     trackingBank.getFloat("c" + (i*2+1) + "_z", loop),
                                     DetectorType.DC.getDetectorId(),12+24*i);
                }
                track.clusters(trackingBank.getShort("Cluster1_ID", loop),
                               trackingBank.getShort("Cluster2_ID", loop),
                               trackingBank.getShort("Cluster3_ID", loop),
                               trackingBank.getShort("Cluster4_ID", loop),
                               trackingBank.getShort("Cluster5_ID", loop),
                               trackingBank.getShort("Cluster6_ID", loop));
                tracks.add(track);
            }
	    //add information from run config bank
	    if(trackingBank!=null && runConfig!=null) {
		for(int loop = 0; loop < trackingBank.getRows(); loop++){
		    //		    int index = trackingBank.getShort("index", loop);
		    //		    System.out.println("number of track bank rows = " + trackBank.getRows());
		    //		    //		    System.out.println("number of tracking bank rows = " + trackingBank.getRows());
		    //		    System.out.println("size = " + tracks.size());
		    //		    System.out.println("index = " + index);
		    //		    System.out.println("loop = " + loop);
		    Track track  = tracks.get(loop);
		    track.polarity(runConfig.getFloat("torus",0));
		}
	    }
            // add information from particle bank
            if(trackBank!=null && particleBank!=null) {
                for(int loop = 0; loop < trackBank.getRows(); loop++){
                    int pindex = trackBank.getShort("pindex", loop);
                    int status = particleBank.getShort("status", pindex);
                    // Forward Detector only
                    if(((int) Math.abs(status)/1000)==2) { 
                        int index = trackBank.getShort("index", loop);
                        Track track  = tracks.get(index); 
                        track.status(status);
                        track.pid(particleBank.getInt("pid", pindex));                
                        track.chi2pid(particleBank.getFloat("chi2pid", pindex));                
                    }
                }
            }
            // add information from trajectory bank
            if(trajectoryBank!=null) {
                for(int loop = 0; loop < trajectoryBank.getRows(); loop++){
                    int pindex = trajectoryBank.getShort("pindex", loop);
                    int status = particleBank.getShort("status", pindex);
                    // Forward Detector only
                    if(((int) Math.abs(status)/1000)==2) { 
                        int index = trajectoryBank.getShort("index", loop);
                        Track track  = tracks.get(index); 
                        track.trajectory(trajectoryBank.getFloat("x", loop),
                                         trajectoryBank.getFloat("y", loop),
                                         trajectoryBank.getFloat("z", loop),
                                         trajectoryBank.getByte("detector", loop),
                                         trajectoryBank.getByte("layer", loop));
                    }
                }
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
        trEvent.readDataGroup(dir);
        aiEvent.readDataGroup(dir);
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
        trEvent.writeDataGroup(dir);
        aiEvent.writeDataGroup(dir);
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
        parser.addOption("-e"    ,"10.6", "beam energy");
        parser.addOption("-t"    ,"2212", "target PDG");
        parser.parse(args);
        
        int   maxEvents  = parser.getOption("-n").intValue();

        String namePrefix  = parser.getOption("-o").stringValue();        
        String histoName   = "histo.hipo";
        String eventName1  = "missing_ai.hipo";
        String eventName2  = "missing_cv.hipo";
        if(!namePrefix.isEmpty()) {
            histoName  = namePrefix + "_" + histoName;
            eventName1 = namePrefix + "_" + eventName1;
            eventName2 = namePrefix + "_" + eventName2;
        }        
        String  type     = parser.getOption("-b").stringValue();        
        String  readName = parser.getOption("-r").stringValue();        
        boolean window   = Boolean.parseBoolean(parser.getOption("-w").stringValue());
        double  beam     = parser.getOption("-e").doubleValue();
        int     target   = parser.getOption("-t").intValue();
        
        if(!window) System.setProperty("java.awt.headless", "true");
        
        SchemaFactory schema = null;           
        
        AImonitor analysis = new AImonitor(beam,target);
        
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

                    Banks banks = new Banks(type,schema);
                    analysis.initBanks(banks);
                }

                while (reader.hasNext()) {

                    counter++;

                    reader.nextEvent(event);
		    //		    Bank conf = new Bank(reader.getSchemaFactory().getSchema("RUN::config"));

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
