package org.clas.analysis;

import org.clas.histograms.HistoResolution;
import org.clas.histograms.HistoEvent;
import org.clas.histograms.HistoDistribution;
import org.clas.fiducials.Fiducial;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JFrame;
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
    private HistoDistribution[] tr          = new HistoDistribution[3];
    private HistoDistribution[] ai          = new HistoDistribution[3];
    private HistoDistribution[] trMatched   = new HistoDistribution[3];
    private HistoDistribution[] aiMatched   = new HistoDistribution[3];
    private HistoDistribution[] trUnmatched = new HistoDistribution[3];
    private HistoDistribution[] aiUnmatched = new HistoDistribution[3];
    private HistoDistribution[] trCands     = new HistoDistribution[3];
    private HistoResolution[]   resol       = new HistoResolution[3];
    private HistoEvent          trEvent     = null;
    private HistoEvent          aiEvent     = null;
    private HistoEvent          trEventM    = null;
    private HistoEvent          trEventN    = null;
     
    private Banks banks = null;
    
    private String[] charges = {Charges.NEG.getName(), Charges.POS.getName(), Charges.ELE.getName()};
      
    private int minentries = 0;
    private String opts = "";
    private double yRange = 0.1;
    
    public AImonitor(int nMin, String opts, double range){       
        this.minentries = nMin;
        this.opts = opts;
        this.yRange = range;
        this.createHistos();
    }
    
    
    public void initBanks(Banks banks){
        this.banks = banks;        
    }

    private void createHistos() {
        
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
        for(int i=0; i<charges.length; i++) {
            tr[i] = new HistoDistribution("tr"+charges[i],Type.CONVENTIONAL,1);
            ai[i] = new HistoDistribution("ai"+charges[i],Type.AI,2);
            trMatched[i] = new HistoDistribution("tr"+charges[i]+"M",Type.MATCHED,4);
            aiMatched[i] = new HistoDistribution("ai"+charges[i]+"M",Type.MATCHED,4);
            trUnmatched[i] = new HistoDistribution("tr"+charges[i]+"N",Type.UNMATCHED,3);
            aiUnmatched[i] = new HistoDistribution("ai"+charges[i]+"N",Type.UNMATCHED,3);
            trCands[i]     = new HistoDistribution("tr"+charges[i]+"C",Type.CANDIDATES,5);
            resol[i] = new HistoResolution(charges[i]+"Res",i+1);   
        }
        trEvent  = new HistoEvent("tr", Type.CONVENTIONAL, 1);
        aiEvent  = new HistoEvent("ai", Type.AI, 2);        
        trEventM = new HistoEvent("trM", Type.MATCHED, 4);
        trEventN = new HistoEvent("trN", Type.UNMATCHED, 3);
    }
    
    private void setHistoStats(String opts) {
        GStyle.getH1FAttributes().setOptStat(opts);
        for(int i=0; i<charges.length; i++) {
            tr[i].setStats(opts);
            ai[i].setStats(opts);
            trMatched[i].setStats(opts);
            aiMatched[i].setStats(opts);
            trUnmatched[i].setStats(opts);
            aiUnmatched[i].setStats(opts); 
            trCands[i].setStats(opts);
        }  
        trEvent.setStats(opts);
        aiEvent.setStats(opts);
        trEventM.setStats(opts);
        trEventN.setStats(opts);
    }
    
    public LumiDatum loadStatistics(String run, double current) {
        LumiDatum lumi = new LumiDatum(run, current);
        for(int i=0; i<charges.length; i++) {
           lumi.initTracks(run, Charges.getCharge(charges[i]), tr[i], ai[i], trMatched[i], trCands[i]);
        }
        lumi.initEH(aiEvent, trEvent);
        lumi.show();
        return lumi;
    }
    
    public EmbeddedCanvasTabbed plotHistos() {
        this.setHistoStats(opts);
        EmbeddedCanvasTabbed canvas  = null;
        String cname = null;
        for(int i=0; i<charges.length; i++) {
            for(String key : tr[i].keySet()) {
                cname = charges[i] + " " + key;
                if(charges[i].equals(Charges.POS.getName()) && key.equals("e-")) continue;
                if(canvas==null) canvas = new EmbeddedCanvasTabbed(cname);
                else             canvas.addCanvas(cname);
                canvas.getCanvas(cname).draw(tr[i].get(key));
                canvas.getCanvas(cname).draw(ai[i].get(key));
                canvas.getCanvas(cname).draw(trMatched[i].get(key));
                canvas.getCanvas(cname).draw(trUnmatched[i].get(key));
                for(EmbeddedPad pad : canvas.getCanvas(cname).getCanvasPads())
                    if(pad.getDatasetPlotters().get(0).getDataSet() instanceof H2F) {
                        H2F h2 = (H2F) pad.getDatasetPlotters().get(0).getDataSet();
                        if(h2.getTitleX().contains("x") && h2.getTitleY().contains("y"))
                            this.drawRegion(pad,2);
                    }                                   
            }
            cname = charges[i] + " resolution";
            canvas.addCanvas(cname);
            canvas.getCanvas(cname).draw(resol[i]);
            this.drawDifferences(canvas, charges[i] + " differences",     ai[i].diff(tr[i],minentries).get("summary"),        this.yRange, false);
            this.drawDifferences(canvas, charges[i] + " differences",     trMatched[i].diff(tr[i],minentries).get("summary"), this.yRange, false);
            this.drawDifferences(canvas, charges[i] + " 2D differences",  ai[i].diff(tr[i],minentries).get("2D"),             this.yRange, false);
            this.drawDifferences(canvas, charges[i] + " 2D differences",  trMatched[i].diff(tr[i],minentries).get("2D"),      this.yRange, false);
            this.drawDifferences(canvas, charges[i] + " 6SL differences", ai[i].diff(tr[i],minentries).get("6SL"),            this.yRange, false);
            this.drawDifferences(canvas, charges[i] + " 6SL differences", trMatched[i].diff(tr[i],minentries).get("6SL"),     this.yRange, false);
            this.drawDifferences(canvas, charges[i] + " 5SL differences", ai[i].diff(tr[i],minentries).get("5SL"),            0.8, false);
            this.drawDifferences(canvas, charges[i] + " 5SL differences", trMatched[i].diff(tr[i],minentries).get("5SL"),     0.8, false);
        }
        cname = "2pi";
        canvas.addCanvas(cname);
        canvas.getCanvas(cname).draw(trEvent.get("2pi"));
        canvas.getCanvas(cname).draw(aiEvent.get("2pi"));                    
//        canvas.getCanvas(cname).draw(trEventM.get("2pi"));
        canvas.getCanvas(cname).draw(trEventN.get("2pi"));
        this.drawDifferences(canvas, "2pidifferences", aiEvent.diff(trEvent,minentries).get("2pi"), 0.7, true);
        cname = "1pi";
        canvas.addCanvas(cname);
        canvas.getCanvas(cname).draw(trEvent.get("1pi"));
        canvas.getCanvas(cname).draw(aiEvent.get("1pi"));                    
//        canvas.getCanvas(cname).draw(trEventM.get("1pi"));
        canvas.getCanvas(cname).draw(trEventN.get("1pi"));
        this.drawDifferences(canvas, "1pidifferences", aiEvent.diff(trEvent,minentries).get("1pi"), 0.4, true);
        cname = "eh+/-";
        canvas.addCanvas(cname);
        canvas.getCanvas(cname).draw(trEvent.get("eh"));
        canvas.getCanvas(cname).draw(aiEvent.get("eh"));                    
        this.drawDifferences(canvas, "ehdifferences", aiEvent.diff(trEvent,minentries).get("eh"), 0.4, true);
        return canvas;
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
                if(pad.getDatasetPlotters().get(0).getDataSet() instanceof H2F) {
                    H2F h2 = (H2F) pad.getDatasetPlotters().get(0).getDataSet();
                    if(h2.getTitleX().contains("x") && h2.getTitleY().contains("y"))
                        this.drawRegion(pad,2);
                }   
            }
        }
    }
    
    private void drawRegion(EmbeddedPad pad, int col) {
        int region = 1;
        String name = pad.getDatasetPlotters().get(0).getDataSet().getName().replaceAll("[^1-3]", "");
        if(!name.equals("")) region = Integer.parseInt(name);
                        
        if(region!=1) return;
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
        trTracks = this.readTracks(0, event);            
        aiTracks = this.readTracks(1, event);

        if(trTracks!=null) {
            for(Track tr : trTracks) {
                if(aiTracks!=null) {
                    for(Track ai : aiTracks) {
                        if(tr.equals(ai)) {
                            tr.setMatch(true);
                            ai.setMatch(true);
                            resol[(tr.charge()+1)/2].fill(tr, ai);
                            if(tr.pid()==11) resol[2].fill(tr,ai);
                            if(tr.diff(ai) && tr.isValid() && ai.isValid() && trTracks.size()==1 && aiTracks.size()==1) 
                                status.setMismatch();
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
                if(track.pid()==11) tr[2].fill(track);
                if(track.isMatched()) {
                    trMatched[(track.charge()+1)/2].fill(track);
                    if(track.pid()==11) trMatched[2].fill(track);
                }
                else {
                    trUnmatched[(track.charge()+1)/2].fill(track);
                    if(track.pid()==11) trUnmatched[2].fill(track);
            	    if(trTracks.size()==1 && track.isValid(true)) status.setAiMissing();
                    }
                if(track.isPredicted()) {
                    trCands[(track.charge()+1)/2].fill(track);
                    if(track.pid()==11) trCands[2].fill(track);
                }
                else if(trTracks.size()==1 && track.isValid()) status.setCdMissing();
            }
            trEvent.fill(trTracks);
            trEventM.fill(trTracks);
            if(trEventN.fill(trTracks)) status.setPiMissing();
        }
        if(aiTracks!=null) {
            for(Track track : aiTracks) {
                ai[(track.charge()+1)/2].fill(track);
                if(track.pid()==11) ai[2].fill(track);
                if(track.isMatched()) {
                    aiMatched[(track.charge()+1)/2].fill(track);
                    if(track.pid()==11) aiMatched[2].fill(track);
                }
                else {
                    aiUnmatched[(track.charge()+1)/2].fill(track);
                    if(track.pid()==11) aiUnmatched[2].fill(track);
                    if(aiTracks.size()==1 && track.isValid()) status.setCvMissing();
                }
            }
            aiEvent.fill(aiTracks);
        }
        return status;
    }


    public ArrayList<Track> readTracks(int type, Event event) {	
        ArrayList<Track> tracks = null;
        Bank runConfig      = banks.getRunConfig();
        Bank particleBank   = banks.getRecParticleBank(type);
        Bank trajectoryBank = banks.getRecTrajectoryBank(type);
        Bank trackBank      = banks.getRecTrackBank(type);
        Bank trackingBank   = banks.getTrackingBank(type);
        Bank hitBank        = banks.getHitBank(type);
        if(runConfig!=null)      event.read(runConfig);
        if(particleBank!=null)   event.read(particleBank);
        if(trajectoryBank!=null) event.read(trajectoryBank);
        if(trackBank!=null)      event.read(trackBank);
        if(trackingBank!=null)   event.read(trackingBank);
        if(hitBank!=null)        event.read(hitBank);

        double torusScale = -1;
        if(runConfig!=null && runConfig.getRows()>0) torusScale = runConfig.getFloat("torus",0);
        
        // use tracking back if available 
        if(trackingBank!=null && trackingBank.getRows()>0) {
            // create tracks list from track bank
            tracks = new ArrayList();
            for(int it = 0; it < trackingBank.getRows(); it++){
                Track track = new Track(banks.getMode(),
                                        trackingBank.getByte("q", it),
                                        trackingBank.getFloat("p0_x", it),
                                        trackingBank.getFloat("p0_y", it),
                                        trackingBank.getFloat("p0_z", it),
                                        trackingBank.getFloat("Vtx0_x", it),
                                        trackingBank.getFloat("Vtx0_y", it),
                                        trackingBank.getFloat("Vtx0_z", it));
                track.index(it);
                track.sector(trackingBank.getByte("sector", it));
                track.NDF(trackingBank.getShort("ndf", it));
                track.chi2(trackingBank.getFloat("chi2", it)/trackingBank.getShort("ndf", it));
                for(int i=0; i<2; i++) {
                    track.cross(trackingBank.getFloat("c" + (i*2+1) + "_x", it),
                                trackingBank.getFloat("c" + (i*2+1) + "_y", it),
                                trackingBank.getFloat("c" + (i*2+1) + "_z", it),
                                (i*2+1));
                }
                if(banks.hasClusterId())
                    track.clusters(trackingBank.getShort("Cluster1_ID", it),
                                   trackingBank.getShort("Cluster2_ID", it),
                                   trackingBank.getShort("Cluster3_ID", it),
                                   trackingBank.getShort("Cluster4_ID", it),
                                   trackingBank.getShort("Cluster5_ID", it),
                                   trackingBank.getShort("Cluster6_ID", it));
                if(hitBank.getRows()>0) {
 //                   int tid = trackingBank.getShort("id", it);
                    for(int ih=0; ih<hitBank.getRows(); ih++) {
                        int superlayer = hitBank.getByte("superlayer", ih);
                        int clusterId  = hitBank.getShort("clusterID", ih);
                        if(clusterId>0 && clusterId==track.clusters()[superlayer-1]) {
 //                       if(hitBank.getInt("trkID", ih)==tid) {
                            int layer = (superlayer-1)*6+hitBank.getByte("layer", ih);
                            int wire  = hitBank.getShort("wire", ih);
                            track.hit(layer, wire);
                        }
                    }
                }
                track.polarity(torusScale);
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
        }
        else if(trackBank!=null && particleBank!=null) {
            tracks = new ArrayList<>();
            for(int it = 0; it < trackBank.getRows(); it++){
                int pindex = trackBank.getShort("pindex", it);
                int status = particleBank.getShort("status", pindex);
                // Forward Detector only
                if(((int) Math.abs(status)/1000)==2) { 
                    int index = trackBank.getShort("index", it);
                    Track track = new Track(banks.getMode(),
                                        particleBank.getByte("charge", pindex),
                                        particleBank.getFloat("px", pindex),
                                        particleBank.getFloat("py", pindex),
                                        particleBank.getFloat("pz", pindex),
                                        particleBank.getFloat("vx", pindex),
                                        particleBank.getFloat("vy", pindex),
                                        particleBank.getFloat("vz", pindex));
                    track.index(index);
                    track.sector(trackBank.getByte("sector", it));
                    track.NDF(trackBank.getShort("NDF", it));
                    track.chi2(trackBank.getFloat("chi2", it)/trackBank.getShort("NDF", it));
                    track.status(status);
                    track.pid(particleBank.getInt("pid", pindex));                
                    track.chi2pid(particleBank.getFloat("chi2pid", pindex));    
                    track.polarity(torusScale);
                    tracks.add(track);
                }
            }
            Collections.sort(tracks);
        }
        if(tracks!=null) {
            // add information from trajectory bank
            if(trajectoryBank!=null) {
                for(int loop = 0; loop < trajectoryBank.getRows(); loop++){
                    int pindex = trajectoryBank.getShort("pindex", loop);
                    int status = particleBank.getShort("status", pindex);
                    // Forward Detector only
                    if(((int) Math.abs(status)/1000)==2) { 
                        int index = trajectoryBank.getShort("index", loop);
                        Track track  = tracks.get(index); 
                        if(trajectoryBank.getSchema().hasEntry("edge"))
                            track.trajectory(trajectoryBank.getFloat("x", loop),
                                             trajectoryBank.getFloat("y", loop),
                                             trajectoryBank.getFloat("z", loop),
                                             trajectoryBank.getFloat("edge", loop),
                                             trajectoryBank.getByte("detector", loop),
                                             trajectoryBank.getByte("layer", loop));
                        else
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
                Track track = new Track(banks.getMode(),charge,0,0,0,0,0,0);
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
        for(int i=0; i<charges.length; i++) {
            tr[i].readDataGroup(dir);
            ai[i].readDataGroup(dir);
            trMatched[i].readDataGroup(dir);
            trUnmatched[i].readDataGroup(dir);
            aiMatched[i].readDataGroup(dir);
            trCands[i].readDataGroup(dir);
            resol[i]=resol[i].readDataGroup(dir);
        }
        trEvent.readDataGroup(dir);
        aiEvent.readDataGroup(dir);
        trEventM.readDataGroup(dir);
        trEventN.readDataGroup(dir);
    }

    public void saveHistos(String fileName) {
        TDirectory dir = new TDirectory();
        for(int i=0; i<charges.length; i++) {
            tr[i].writeDataGroup(dir);
            ai[i].writeDataGroup(dir);
            trMatched[i].writeDataGroup(dir);
            trUnmatched[i].writeDataGroup(dir);
            aiMatched[i].writeDataGroup(dir);
            trCands[i].writeDataGroup(dir);
            resol[i].writeDataGroup(dir);
        }
        trEvent.writeDataGroup(dir);
        aiEvent.writeDataGroup(dir);
        trEventM.writeDataGroup(dir);
        trEventN.writeDataGroup(dir);
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
        for(int i=0; i<charges.length; i++) {
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
        private boolean cdMissing=false;
        private boolean cvMissing=false;
        private boolean mismatched=false;
        private boolean piMissing=false;

        public EventStatus() {
        }

        public boolean isAiMissing() {
            return aiMissing;
        }

        public void setAiMissing() {
            this.aiMissing = true;
        }

        public boolean isCdMissing() {
            return cdMissing;
        }

        public void setCdMissing() {
            this.cdMissing = true;
        }

        public boolean isCvMissing() {
            return cvMissing;
        }

        public void setCvMissing() {
            this.cvMissing = true;
        } 

        public boolean isMismatched() {
            return mismatched;
        }

        public void setMismatch() {
            this.mismatched = true;
        }

        public boolean isPiMissing() {
            return piMissing;
        }

        public void setPiMissing() {
            this.piMissing = true;
        }
                
    }
    
    public static void main(String[] args){

        OptionParser parser = new OptionParser("aiTracking");
        parser.setRequiresInputList(false);
        // valid options for event-base analysis
        parser.addOption("-o"          ,"",     "output file name prefix");
        parser.addOption("-n"          ,"-1",   "maximum number of events to process");
        parser.addOption("-banks"      ,"TB",   "tracking level: TB or HB");
        parser.addOption("-superlayers","0",    "number of superlayers (5 or 6, 0=any)");
        parser.addOption("-sector"     ,"0",    "sector (1-6, 0=any)");
        parser.addOption("-match"      ,"0",    "match based on clusters or hits (0/1)");
        parser.addOption("-pmin"       ,"0.5",  "minimum momentum (GeV)");
        parser.addOption("-vertex"     ,"-15:5","vertex range (min:max)");
        parser.addOption("-edge"       ,"",     "colon-separated DC, FTOF, ECAL edge cuts in cm (e.g. 5:10:5)");
        parser.addOption("-wiremin"    ,"1",    "min DC wire");
        parser.addOption("-chi2"       ,"-1",   "max track reduced chi2 (-1 = infinity");
        parser.addOption("-write"      ,"0",    "save events with missing tracks (0/1)");
        parser.addOption("-energy"     ,"10.6", "beam energy");
        parser.addOption("-target"     ,"2212", "target PDG");
        // histogram based analysis
        parser.addOption("-histo"      ,"0",    "read histogram file (0/1)");
        parser.addOption("-plot"       ,"1",    "display histograms (0/1)");
        parser.addOption("-range"      ,"0.1",  "set y-axis range for efficiency and gain plots");
        parser.addOption("-stats"      ,"",     "histogram stat option");
        parser.addOption("-threshold"  ,"0",    "minimum number of entries for histogram differences");
        // luminosity analysis
        parser.addOption("-lumi"       ,"",     "(comma-separated) luminosity scan currents, e.g. \"5:data,20:data,40:data,40:bg;40:mc\"");
        parser.addOption("-fit"        ,"1",    "display fit parameters in luminosity analyses (0/1)");
        parser.addOption("-scale"      ,"1",    "set luminosity dependence scale factor according to conventional (0) or AI-assisted tracking (1)");
        
        parser.parse(args);
        
        int   maxEvents  = parser.getOption("-n").intValue();

        String namePrefix  = parser.getOption("-o").stringValue();        
        String histoName   = "histo.hipo";
        String eventName1  = "missing_ai.hipo";
        String eventName2  = "missing_cd.hipo";
        String eventName3  = "missing_cv.hipo";
        String eventName4  = "mismatched.hipo";
        String eventName5  = "missing_pi.hipo";
        if(!namePrefix.isEmpty()) {
            histoName  = namePrefix + "_" + histoName;
            eventName1 = namePrefix + "_" + eventName1;
            eventName2 = namePrefix + "_" + eventName2;
            eventName3 = namePrefix + "_" + eventName3;
            eventName4 = namePrefix + "_" + eventName4;
            eventName5 = namePrefix + "_" + eventName5;
        }        
        boolean writeMissing = parser.getOption("-write").intValue()!=0;
        String  trackingType = parser.getOption("-banks").stringValue();        
        int     sector       = parser.getOption("-sector").intValue();
        int     superLayers  = parser.getOption("-superlayers").intValue();
        boolean hitMatch     = parser.getOption("-match").intValue()!=0;
        String[] vertex      = parser.getOption("-vertex").stringValue().split(":");
        if(vertex.length != 2) {
            System.out.println("\n >>>> error: incorrect vertex parameters...\n");
            System.exit(0);
        }
        else {
            Constants.ZMIN = Double.parseDouble(vertex[0]);
            Constants.ZMAX = Double.parseDouble(vertex[1]);
        }
        Constants.PMIN = parser.getOption("-pmin").doubleValue();
        String[] edge  = parser.getOption("-edge").stringValue().split(":");
        if(!parser.getOption("-edge").stringValue().isBlank() && edge.length != 3) {
            System.out.println("\n >>>> error: incorrect edge parameters...\n");
            System.exit(0);
        }
        else if(edge.length==3) {
            Constants.EDGE[0] = Double.parseDouble(edge[0]);
            Constants.EDGE[1] = Double.parseDouble(edge[1]);
            Constants.EDGE[2] = Double.parseDouble(edge[2]);
        }
        Constants.WIREMIN    = parser.getOption("-wiremin").intValue();         
        if(parser.getOption("-chi2").doubleValue()>0)
            Constants.CHI2MAX = parser.getOption("-chi2").doubleValue();
        Constants.BEAMENERGY = parser.getOption("-energy").doubleValue();
        Constants.TARGETPID  = parser.getOption("-target").intValue();         
        
        boolean readHistos   = (parser.getOption("-histo").intValue()!=0);            
        boolean openWindow   = (parser.getOption("-plot").intValue()!=0);
        String  optStats     = parser.getOption("-stats").stringValue();        
        double  yRange       = parser.getOption("-range").doubleValue();        
        int     minCounts    = parser.getOption("-threshold").intValue();
        boolean lumiFit      = (parser.getOption("-fit").intValue()!=0);
        boolean lumiScale    = (parser.getOption("-scale").intValue()!=0);
 
        List<Double> lumiCurrent = new ArrayList<>();
        List<String> lumiType    = new ArrayList<>();
        if(!parser.getOption("-lumi").stringValue().isEmpty()) {
            readHistos = true;
            String[] lumiParameters = parser.getOption("-lumi").stringValue().split(",");         
            for(int i=0; i<lumiParameters.length; i++) {
                String[] pars = lumiParameters[i].split(":");
                if(pars.length!=2 || !(pars[1].trim().equals("data") || pars[1].trim().equals("bg") || pars[1].trim().equals("mc"))) {
                    System.out.println("\n >>>> error: incorrect lumi parameters...\n");
                    System.exit(0);
                }
                lumiCurrent.add(Double.parseDouble(lumiParameters[i].split(":")[0]));
                lumiType.add(lumiParameters[i].split(":")[1]);
            }
        }
        if(superLayers!=0 && superLayers!=5 && superLayers!=6) 
            Constants.NSUPERLAYERS=0;
        else 
            Constants.NSUPERLAYERS=superLayers;
        
        if(sector<0 || sector>6)
            Constants.SECTOR = 0;
        else
            Constants.SECTOR = sector;
        
        Constants.HITMATCH = hitMatch;
        
        if(!openWindow) System.setProperty("java.awt.headless", "true");
        
        SchemaFactory schema = null;           
        
        AImonitor analysis = new AImonitor(minCounts,optStats, yRange);
        
        ArrayList<LumiDatum> lumis = new ArrayList<>();
        
        HipoWriterSorted writer1 = new HipoWriterSorted();
        HipoWriterSorted writer2 = new HipoWriterSorted();
        HipoWriterSorted writer3 = new HipoWriterSorted();
        HipoWriterSorted writer4 = new HipoWriterSorted();
        HipoWriterSorted writer5 = new HipoWriterSorted();

        List<String> inputList = parser.getInputList();
        if(inputList.isEmpty()==true){
            parser.printUsage();
            System.out.println("\n >>>> error: no input file is specified....\n");
            System.exit(0);
        }

        if(readHistos) {
            if(lumiCurrent.size()!=0 && lumiCurrent.size()!=inputList.size()) {
                System.out.println("\n >>>> error: number of lumi parameters provided doesn't match the list of input files: " + lumiCurrent.size() + " versus " + inputList.size() + "\n");
                System.exit(0);
            }
            
            for(int i=0; i<inputList.size(); i++){
                analysis.readHistos(inputList.get(i));
                if(lumiCurrent.size()!=0) {
                    lumis.add(analysis.loadStatistics(lumiType.get(i),lumiCurrent.get(i)));
                }
                else {
                    analysis.loadStatistics("0",0);
                }
            }  
        }
        else{

            ProgressPrintout progress = new ProgressPrintout();

            int counter=-1;
            Event event = new Event();

            for(String inputFile : inputList){
                HipoReader reader = new HipoReader();
                reader.open(inputFile);

                if(schema==null) {
                    schema = reader.getSchemaFactory();
                    if(writeMissing) {
                        AImonitor.setWriter(writer1, schema, eventName1);
                        AImonitor.setWriter(writer2, schema, eventName2);
                        AImonitor.setWriter(writer3, schema, eventName3);
                        AImonitor.setWriter(writer4, schema, eventName4);
                        AImonitor.setWriter(writer5, schema, eventName5);
                    }

                    Banks banks = new Banks(trackingType,schema);
                    analysis.initBanks(banks);
                }

                while (reader.hasNext()) {

                    counter++;

                    reader.nextEvent(event);

                    EventStatus status = analysis.processEvent(event);

                    if(writeMissing && status.isAiMissing())  writer1.addEvent(event);
                    if(writeMissing && status.isCdMissing())  writer2.addEvent(event);
                    if(writeMissing && status.isCvMissing())  writer3.addEvent(event);
                    if(writeMissing && status.isMismatched()) writer4.addEvent(event);
                    if(writeMissing && status.isPiMissing())  writer5.addEvent(event);

                    progress.updateStatus();
                    if(maxEvents>0){
                        if(counter>=maxEvents) break;
                    }
                }
                progress.showStatus();
                reader.close();
            }    
            if(writeMissing) {
                writer1.close();
                writer2.close();
                writer3.close();
                writer4.close();
                writer5.close();
            }
            analysis.saveHistos(histoName);
            analysis.loadStatistics("0",0);
        }

        if(openWindow) {
            JFrame frame = new JFrame(trackingType);
            EmbeddedCanvasTabbed canvas = null;
            if(readHistos && lumis.size()>1) {
                LumiAnalysis luminosity = new LumiAnalysis(lumis, lumiScale);
                canvas = luminosity.plotGraphs(lumiFit);
                frame.setSize(1000, 600);
            }
            else {
                canvas = analysis.plotHistos();
                frame.setSize(1200, 750);
            }
            frame.add(canvas);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
      
        
    }
    
}
