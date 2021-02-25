package org.clas.analysis;

import org.clas.histograms.HistoResolution;
import org.clas.histograms.HistoEvent;
import org.clas.histograms.HistoDistribution;
import org.clas.fiducials.Fiducial;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import org.jlab.detector.base.DetectorType;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.DataLine;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.graphics.EmbeddedPad;
import org.jlab.groot.graphics.Histogram2DPlotter;
import org.jlab.groot.graphics.IDataSetPlotter;
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
        
    private Fiducial fiducial = new Fiducial();
    private HistoDistribution[] tr          = new HistoDistribution[2];
    private HistoDistribution[] ai          = new HistoDistribution[2];
    private HistoDistribution[] trMatched   = new HistoDistribution[2];
    private HistoDistribution[] aiMatched   = new HistoDistribution[2];
    private HistoDistribution[] trUnmatched = new HistoDistribution[2];
    private HistoDistribution[] aiUnmatched = new HistoDistribution[2];
    private HistoResolution[]   resol       = new HistoResolution[2];
    private HistoEvent          trEvent     = null;
    private HistoEvent          aiEvent     = null;
     
    private Banks banks = null;
    
    private String[] charges = {"neg", "pos"};
      
    private String opts = "";
    
    public AImonitor(double beamEnergy, int targetPDG, String opts){
        this.opts = opts;
        this.createHistos(beamEnergy, targetPDG);
    }
    
    
    public void initBanks(Banks banks){
        this.banks = banks;        
    }

    private void createHistos(double beamEnergy, int targetPDG) {
        
        GStyle.getH1FAttributes().setOptStat(opts);
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
        for(int i=0; i<2; i++) {
            tr[i] = new HistoDistribution("tr"+charges[i],1);
            ai[i] = new HistoDistribution("ai"+charges[i],2);
            trMatched[i] = new HistoDistribution("tr"+charges[i]+"M",4);
            aiMatched[i] = new HistoDistribution("ai"+charges[i]+"M",4);
            trUnmatched[i] = new HistoDistribution("tr"+charges[i]+"N",3);
            aiUnmatched[i] = new HistoDistribution("ai"+charges[i]+"N",3);
            resol[i] = new HistoResolution(charges[i]+"Res",i+1);   
        }
        trEvent = new HistoEvent("tr", 1, beamEnergy, targetPDG);
        aiEvent = new HistoEvent("ai", 2, beamEnergy, targetPDG);        
    }
    
    private void setHistoStats(String opts) {
        GStyle.getH1FAttributes().setOptStat(opts);
        for(int i=0; i<2; i++) {
            tr[i].setStats(opts);
            ai[i].setStats(opts);
            trMatched[i].setStats(opts);
            aiMatched[i].setStats(opts);
            trUnmatched[i].setStats(opts);
            aiUnmatched[i].setStats(opts);           
        }  
        trEvent.setStats(opts);
        aiEvent.setStats(opts);
    }
    
    public EmbeddedCanvasTabbed plotHistos() {
        this.setHistoStats(opts);
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
            canvas.getCanvas(cname).draw(trMatched[i].diff(tr[i]).get("summary"));
            if(tr[i].getEntries()>0) {
                System.out.println(cname + " gain =       " +  String.format("%6.4f", (double) ai[i].getEntries()/tr[i].getEntries()));
                System.out.println(cname + " efficiency = " +  String.format("%6.4f", (double) trMatched[i].getEntries()/tr[i].getEntries()));
            }
            this.drawLines(canvas.getCanvas(cname));
            this.setRange(canvas.getCanvas(cname), 0.2);
        }
        cname = "2pi";
        canvas.addCanvas(cname);
        canvas.getCanvas(cname).draw(trEvent.get("2pi"));
        canvas.getCanvas(cname).draw(aiEvent.get("2pi"));                    
        cname = "2pidifferences";
        canvas.addCanvas(cname);
        canvas.getCanvas(cname).draw(aiEvent.diff(trEvent).get("2pi"));
        this.plotErrors(canvas.getCanvas(cname));
        this.drawLines(canvas.getCanvas(cname));
        this.setRange(canvas.getCanvas(cname), 0.2);
        cname = "1pi";
        canvas.addCanvas(cname);
        canvas.getCanvas(cname).draw(trEvent.get("1pi"));
        canvas.getCanvas(cname).draw(aiEvent.get("1pi"));                    
        cname = "1pidifferences";
        canvas.addCanvas(cname);
        canvas.getCanvas(cname).draw(aiEvent.diff(trEvent).get("1pi"));
        this.plotErrors(canvas.getCanvas(cname));
        this.drawLines(canvas.getCanvas(cname));
        this.setRange(canvas.getCanvas(cname), 0.2);
        return canvas;
    }
    
    private void drawLines(EmbeddedCanvas canvas) {
        for(EmbeddedPad pad : canvas.getCanvasPads()) {
            if(pad.getDatasetPlotters().get(0).getDataSet() instanceof H1F) {
                H1F h1 = (H1F) pad.getDatasetPlotters().get(0).getDataSet();
                DataLine line= new DataLine(h1.getXaxis().min(),1,h1.getXaxis().max(),1);
                line.setLineWidth(1);
                pad.draw(line);
            }
        }
    }
    
    private void plotErrors(EmbeddedCanvas canvas) {
        for(EmbeddedPad pad : canvas.getCanvasPads()) {
            if(pad.getDatasetPlotters().get(0).getDataSet() instanceof H1F) {
                List<IDataSetPlotter> plots = pad.getDatasetPlotters();
                for(int i=0; i<plots.size(); i++) {
                    H1F h1 = (H1F) plots.get(i).getDataSet();
                    if(i==0) pad.draw(h1,"E");
                    else     pad.draw(h1, "Esame");
                }
            }
        }
    }
    
    private void setRange(EmbeddedCanvas canvas, double range) {
        int nx = canvas.getNColumns();
        int ny = canvas.getNRows();
        for(EmbeddedPad pad : canvas.getCanvasPads()) {
            if(pad.getDatasetPlotters().get(0) instanceof Histogram2DPlotter) pad.getAxisZ().setRange(1-range, 1);
            else {
                pad.getAxisY().setRange(1-range, 1+range);
                pad.getAxisY().setTitle("Ratio");
            }
        }
    }
        
    public EventStatus processEvent(Event event, int superlayers) {
	EventStatus status = new EventStatus();
        ArrayList<Track> aiTracks = null;
        ArrayList<Track> trTracks = null;
        trTracks = this.read(0, superlayers, event);            
        aiTracks = this.read(1, superlayers, event);

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
		    if(trTracks.size()==1 && track.isValid()) status.setAiMissing();
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
                    if(aiTracks.size()==1 && track.isValid()) status.setCvMissing();
                }
            }
            aiEvent.fill(aiTracks);
        }
        return status;
    }


    public ArrayList<Track> read(int type, int superlayers, Event event) {	
        ArrayList<Track> tracks = null;
	Bank runConfig      = banks.getRunConfig();
        Bank particleBank   = banks.getRecParticleBank(type);
        Bank trajectoryBank = banks.getRecTrajectoryBank(type);
        Bank trackBank      = banks.getRecTrackBank(type);
        Bank trackingBank   = banks.getTrackingBank(type);
	if(runConfig!=null)      event.read(runConfig);
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
                track.clusters(superlayers,
                               trackingBank.getShort("Cluster1_ID", loop),
                               trackingBank.getShort("Cluster2_ID", loop),
                               trackingBank.getShort("Cluster3_ID", loop),
                               trackingBank.getShort("Cluster4_ID", loop),
                               trackingBank.getShort("Cluster5_ID", loop),
                               trackingBank.getShort("Cluster6_ID", loop));
                if(runConfig!=null && runConfig.getRows()>0) track.polarity(runConfig.getFloat("torus",0));
                tracks.add(track);
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
            // add fiducial information
            for(Track tr : tracks) {
                tr.isInFiducial(fiducial.inFiducial(tr));
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
        parser.addOption("-m"    ,"false","save events with missing tracks");
        parser.addOption("-b"    ,"TB",   "tracking level: TB or HB");
        parser.addOption("-l"    ,"0",    "number of superlayers: default 5 or 6");
        parser.addOption("-r"    ,"",     "histogram file to be read");
        parser.addOption("-w"    ,"true", "display histograms");
        parser.addOption("-s"    ,"",     "histogram stat option");
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
        boolean write    = Boolean.parseBoolean(parser.getOption("-m").stringValue());
        String  type     = parser.getOption("-b").stringValue();        
        int     slayers  = parser.getOption("-l").intValue();
        String  readName = parser.getOption("-r").stringValue();        
        boolean window   = Boolean.parseBoolean(parser.getOption("-w").stringValue());
        String  optStats = parser.getOption("-s").stringValue();        
        double  beam     = parser.getOption("-e").doubleValue();
        int     target   = parser.getOption("-t").intValue();
        
        if(!window) System.setProperty("java.awt.headless", "true");
        
        SchemaFactory schema = null;           
        
        AImonitor analysis = new AImonitor(beam,target,optStats);
        
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
                    if(write) {
                        AImonitor.setWriter(writer1, schema, eventName1);
                        AImonitor.setWriter(writer2, schema, eventName2);
                    }
                    
                    Banks banks = new Banks(type,schema);
                    analysis.initBanks(banks);
                }

                while (reader.hasNext()) {

                    counter++;

                    reader.nextEvent(event);

                    EventStatus status = analysis.processEvent(event,slayers);

                    if(write && status.isAiMissing()) writer1.addEvent(event);
                    if(write && status.isCvMissing()) writer2.addEvent(event);

                    progress.updateStatus();
                    if(maxEvents>0){
                        if(counter>=maxEvents) break;
                    }
                }
                progress.showStatus();
                reader.close();
            }    
            if(write) {
                writer1.close();
                writer2.close();
            }
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
