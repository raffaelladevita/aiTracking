package org.clas.analysis;

import org.clas.histograms.HistoResolution;
import org.clas.histograms.HistoEvent;
import org.clas.histograms.HistoDistribution;
import org.clas.fiducials.Fiducial;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import org.clas.histograms.Histos;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.DataLine;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.graphics.EmbeddedPad;
import org.jlab.groot.graphics.Histogram2DPlotter;
import org.jlab.groot.graphics.IDataSetPlotter;
import org.jlab.groot.group.DataGroup;
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
    private HistoDistribution[] trCands     = new HistoDistribution[2];
    private HistoResolution[]   resol       = new HistoResolution[2];
    private HistoEvent          trEvent     = null;
    private HistoEvent          aiEvent     = null;
     
    private Banks banks = null;
    
    private String[] charges = {"neg", "pos"};
      
    private int nsuperlayers = 0;
    private int minentries = 0;
    private String opts = "";
    
    public AImonitor(double beamEnergy, int targetPDG, int nSL, int nMin, String opts){       
        this.nsuperlayers = nSL;
        this.minentries = nMin;
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
            trCands[i]     = new HistoDistribution("tr"+charges[i]+"C",5);
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
    
    public void printStatistics() {
        for(int i=0; i<2; i++) {
            this.printStatistics(ai[i], tr[i], trMatched[i], trCands[i], charges[i]);
        }
        this.printStatistics(aiEvent, trEvent);
    }
    
    public EmbeddedCanvasTabbed plotHistos() {
        this.setHistoStats(opts);
        EmbeddedCanvasTabbed canvas  = null;
        String cname = null;
        for(int i=0; i<2; i++) {
            for(String key : tr[i].keySet()) {
                cname = charges[i] + " " + key;
                if(charges[i].equals("pos") && key.equals("e-")) continue;
                if(canvas==null) canvas = new EmbeddedCanvasTabbed(cname);
                else             canvas.addCanvas(cname);
                canvas.getCanvas(cname).draw(tr[i].get(key));
                canvas.getCanvas(cname).draw(ai[i].get(key));
                canvas.getCanvas(cname).draw(trMatched[i].get(key));
                canvas.getCanvas(cname).draw(trUnmatched[i].get(key));
                for(EmbeddedPad pad : canvas.getCanvas(cname).getCanvasPads())
                    if(pad.getDatasetPlotters().get(0).getDataSet() instanceof H2F) this.drawRegion1(pad,2);                                   
            }
            cname = charges[i] + " resolution";
            canvas.addCanvas(cname);
            canvas.getCanvas(cname).draw(resol[i]);
            this.drawDifferences(canvas, charges[i] + " differences",     ai[i].diff(tr[i],minentries).get("summary"),        0.3, false);
            this.drawDifferences(canvas, charges[i] + " differences",     trMatched[i].diff(tr[i],minentries).get("summary"), 0.3, false);
            if(charges[i].equals("neg")) {
                this.drawDifferences(canvas, charges[i] + " e- differences",  ai[i].diff(tr[i],minentries).get("e-"),         0.3, false);
                this.drawDifferences(canvas, charges[i] + " e- differences",  trMatched[i].diff(tr[i],minentries).get("e-"),  0.3, false);
            }
            this.drawDifferences(canvas, charges[i] + " 6SL differences", ai[i].diff(tr[i],minentries).get("6SL"),            0.3, false);
            this.drawDifferences(canvas, charges[i] + " 6SL differences", trMatched[i].diff(tr[i],minentries).get("6SL"),     0.3, false);
            this.drawDifferences(canvas, charges[i] + " 5SL differences", ai[i].diff(tr[i],minentries).get("5SL"),            0.8, false);
            this.drawDifferences(canvas, charges[i] + " 5SL differences", trMatched[i].diff(tr[i],minentries).get("5SL"),     0.8, false);
        }
        cname = "2pi";
        canvas.addCanvas(cname);
        canvas.getCanvas(cname).draw(trEvent.get("2pi"));
        canvas.getCanvas(cname).draw(aiEvent.get("2pi"));                    
        this.drawDifferences(canvas, "2pidifferences", aiEvent.diff(trEvent,minentries).get("2pi"), 0.7, true);
        cname = "1pi";
        canvas.addCanvas(cname);
        canvas.getCanvas(cname).draw(trEvent.get("1pi"));
        canvas.getCanvas(cname).draw(aiEvent.get("1pi"));                    
        this.drawDifferences(canvas, "1pidifferences", aiEvent.diff(trEvent,minentries).get("1pi"), 0.4, true);
        cname = "eh+/-";
        canvas.addCanvas(cname);
        canvas.getCanvas(cname).draw(trEvent.get("eh"));
        canvas.getCanvas(cname).draw(aiEvent.get("eh"));                    
        this.drawDifferences(canvas, "ehdifferences", aiEvent.diff(trEvent,minentries).get("eh"), 0.4, true);
        return canvas;
    }

    private void printStatistics(Histos ai, Histos tr, Histos trMatched, Histos trCands, String charge) {
        System.out.println("+-------------------------------------------------------------------------------------------------------------------------------+");
        System.out.println("|     charge |       type | conventional |           ai |       matched |    predicted |       gain |  efficiency |   inference |");
        System.out.println("+-------------------------------------------------------------------------------------------------------------------------------+");
        this.printStatistics(ai, tr, trMatched, trCands, "summary", charge);        
        this.printStatistics(ai, tr, trMatched, trCands, "6SL", charge);        
        this.printStatistics(ai, tr, trMatched, trCands, "5SL", charge);        
        System.out.println("+-------------------------------------------------------------------------------------------------------------------------------+");
    }
    
    private void printStatistics(Histos ai, Histos tr, Histos trMatched, Histos trCands, String dg, String charge) {
        if(tr.getEntries(dg)>0) {
            System.out.println(String.format("| %10s | %10s | %12d | % 12d | %12d  | %12d | %10.4f |  %10.4f |  %10.4f |"
                                                                                                , charge, dg 
                                                                                                , tr.getEntries(dg) 
                                                                                                , ai.getEntries(dg)
                                                                                                , trMatched.getEntries(dg)
                                                                                                , trCands.getEntries(dg)
                                                                                                , (double) ai.getEntries(dg)/tr.getEntries(dg)
                                                                                                , (double) trMatched.getEntries(dg)/tr.getEntries(dg)
                                                                                                , (double) trCands.getEntries(dg)/tr.getEntries(dg)));
        }        
    }
    
    private void printStatistics(HistoEvent ai, HistoEvent tr) {
        System.out.println("+--------------------------------------------------------------------------------------+");
        System.out.println("|         type |            e |          eh+ |          eh- |      eh+/e |       eh-/e |");
        System.out.println("+--------------------------------------------------------------------------------------+");
        this.printStatistics(tr, "conventional");        
        this.printStatistics(ai, "ai");        
        System.out.println("+--------------------------------------------------------------------------------------+");
    }
    
    private void printStatistics(HistoEvent he, String type) {
        System.out.println(String.format("| %12s | %12d | % 12d | %12d | %10.4f |  %10.4f |", type 
                                                                                            , he.getNe()
                                                                                            , he.getNehp()
                                                                                            , he.getNehm()
                                                                                            , (double) he.getNehp()/he.getNe()
                                                                                            , (double) he.getNehm()/he.getNe()));        
    }
    
    private void drawDifferences(EmbeddedCanvasTabbed canvas, String cname, DataGroup dg, double range, boolean errors) {
        if(canvas.getCanvas(cname)==null) canvas.addCanvas(cname);
        canvas.getCanvas(cname).draw(dg);
        canvas.getCanvas(cname).draw(dg);
        if(errors) this.plotErrors(canvas.getCanvas(cname));
        this.drawLines(canvas.getCanvas(cname));
        this.setRange(canvas.getCanvas(cname), range);
    }
    
    private void drawLines(EmbeddedCanvas canvas) {
        for(EmbeddedPad pad : canvas.getCanvasPads()) {
            if(pad.getDatasetPlotters().get(0).getDataSet() instanceof H1F) {
                H1F h1 = (H1F) pad.getDatasetPlotters().get(0).getDataSet();
                DataLine line= new DataLine(h1.getXaxis().min(),1,h1.getXaxis().max(),1);
                line.setLineWidth(1);
                pad.draw(line);
            }
            else if(pad.getDatasetPlotters().get(0).getDataSet() instanceof H2F) {
                this.drawRegion1(pad,1);
            }
        }
    }
    
    private void drawRegion1(EmbeddedPad pad, int col) {
        Line3D inner   = new Line3D( -5.197,  18.657, 0,  5.197,  18.657, 0);
        Line3D outer   = new Line3D(-46.086, 151.061, 0, 46.086, 151.061, 0);
        Line3D lcorner = new Line3D(-73.104, 138.852, 0,-46.086, 151.061, 0);
        Line3D rcorner = new Line3D( 46.086, 151.061, 0, 73.104, 138.852, 0);
        Line3D left    = new Line3D( -5.197,  18.657, 0,-73.104, 138.852, 0);
        Line3D right   = new Line3D(  5.197,  18.657, 0, 73.104, 138.852, 0);
        Line3D[] r1 = {inner,outer,lcorner,rcorner,left,right};
        for(int sector=0; sector<=6; sector++) {
            for(int iline=0; iline<6; iline++) {
                Line3D side = new Line3D(r1[iline]);
                side.rotateZ(Math.toRadians(-90+(sector-1)*60));
                DataLine line= new DataLine(side.origin().x(),side.origin().y(),side.end().x(),side.end().y());
                line.setLineWidth(2);
                line.setLineColor(col);
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
        
    public EventStatus processEvent(Event event) {
        EventStatus status = new EventStatus();
        ArrayList<Track> aiCands  = null;
        ArrayList<Track> aiTracks = null;
        ArrayList<Track> trTracks = null;
        aiCands  = this.readCandidates(event);
        trTracks = this.readTracks(0, this.nsuperlayers, event);            
        aiTracks = this.readTracks(1, this.nsuperlayers, event);

        if(trTracks!=null) {
            for(Track tr : trTracks) {
                if(aiTracks!=null) {
                    for(Track ai : aiTracks) {
                        if(tr.equals(ai)) {
                            tr.setMatch(true);
                            ai.setMatch(true);
                            resol[(tr.charge()+1)/2].fill(tr, ai);
                        }
                    }
                }
                if(aiCands!=null) {
                    for(Track ai : aiCands) {
                        if(tr.isContainedIn(ai)) {
                            tr.setPrediction(true);
                        }
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
		    if(trTracks.size()==1 && track.isValid() ) status.setAiMissing();
                }
                if(track.isPredicted()) trCands[(track.charge()+1)/2].fill(track);
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


    public ArrayList<Track> readTracks(int type, int nsuperlayers, Event event) {	
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
                track.clusters(trackingBank.getShort("Cluster1_ID", loop),
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
            // add fiducial information and selection on number of superlayers
            for(Track tr : tracks) {
                tr.isInFiducial(fiducial.inFiducial(tr));
                tr.SL(nsuperlayers);
            }
        }
        return tracks;
    }

    public ArrayList<Track> readCandidates(Event event) {	
        ArrayList<Track> tracks = null;
        Bank aiCandidateBank = banks.getAICandidateBank();
        if(aiCandidateBank!=null) event.read(aiCandidateBank);
        if(aiCandidateBank!=null && aiCandidateBank.getRows()>0) {
            // create tracks list from track bank
            tracks = new ArrayList();
            for(int loop = 0; loop < aiCandidateBank.getRows(); loop++){
                int charge = -1;
                if(aiCandidateBank.getByte("charge", loop)==22) charge = 1;
                Track track = new Track(charge,0,0,0,0,0,0);
                track.clusters(aiCandidateBank.getShort("c1", loop),
                               aiCandidateBank.getShort("c2", loop),
                               aiCandidateBank.getShort("c3", loop),
                               aiCandidateBank.getShort("c4", loop),
                               aiCandidateBank.getShort("c5", loop),
                               aiCandidateBank.getShort("c6", loop));
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
                if(charges[i].equals("pos") && key.equals("e-")) continue;
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
        parser.addOption("-r"    ,"",     "histogram file to be read");
        parser.addOption("-w"    ,"true", "display histograms");
        parser.addOption("-s"    ,"",     "histogram stat option");
        parser.addOption("-d"    ,"0",    "minimum number of entries for histogram differences");
        parser.addOption("-l"    ,"0",    "number of superlayers (5 or 6, 0=any)");
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
        String  readName = parser.getOption("-r").stringValue();        
        boolean window   = Boolean.parseBoolean(parser.getOption("-w").stringValue());
        String  optStats = parser.getOption("-s").stringValue();        
        int     layers   = parser.getOption("-l").intValue();
        int     nmin     = parser.getOption("-d").intValue();
        double  beam     = parser.getOption("-e").doubleValue();
        int     target   = parser.getOption("-t").intValue();
        
        if(layers!=0 && layers!=5 && layers!=6) layers=0;

        if(!window) System.setProperty("java.awt.headless", "true");
        
        SchemaFactory schema = null;           
        
        AImonitor analysis = new AImonitor(beam,target,layers,nmin,optStats);
        
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

                    EventStatus status = analysis.processEvent(event);

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
        analysis.printStatistics();
        
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
