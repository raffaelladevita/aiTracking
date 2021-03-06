/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.analysis;

import org.jlab.clas.physics.LorentzVector;
import org.jlab.clas.physics.Vector3;
import org.jlab.detector.base.DetectorType;

/**
 *
 * @author devita
 */

public class Track {
    
    // from tracking bank
    private LorentzVector trackVector = new LorentzVector(0.0,0.0,0.0,0);
    private Vector3 trackVertex = new Vector3(0.0,0.0,0.0);
    private int trackCharge = 0;
    private int trackSector = 0;
    private double trackPolarity = 0;
    private double trackChi2 = 0;
    private int trackNDF = 0;
    private Vector3[] trackCrosses = new Vector3[3];
    private int[] trackClusters = new int[6];
    private int   trackSL = 0;
    
    // from particle bank
    private int    trackStatus  = 0;
    private double trackChi2pid = 0;
    private int    trackPid = 0;
    
    // from trajectory bank
    private Vector3[] trackTrajectory = new Vector3[9]; // size set to contain 3 DC reegions, 3 FTOF and ECAL layers
    private boolean inFiducial = true;
    
    // flag to select on number of superlayers
    private int nSL = 0;
    
    // HB (0) or TB (0)
    private int trackMode = 1;
    
    private boolean trackMatch   = false;
    private boolean trackPredict = false;
    
    public Track() {
        this.initTrack(1, 0, 0., 0., 0., 0., 0., 0.);
    }

    public Track(Track t) {
        this.initTrack(t.mode(), t.charge(), t.px(), t.py(), t.pz(), t.vertex().x(), t.vertex().y(), t.vertex().z());
    }
    
    public Track(int mode, int charge, double px, double py, double pz, double vx, double vy, double vz) {
        this.initTrack(mode, charge, px, py, pz, vx, vy, vz);
    }
        
    
    public static Track copyFrom(Track p){
        Track newp = new Track();
        newp.charge(p.charge());
        newp.vector().copy(p.vector());
        newp.vertex().copy(p.vertex());
        newp.mode(p.mode());
        return newp;
    }
    
    public void copy(Track track) {
        this.trackVector.setPxPyPzM(track.vector().px(), track.vector().py(), track.vector().pz(), track.vector().mass());
        this.trackVertex.setXYZ(track.vertex().x(), track.vertex().y(), track.vertex().z());        
        this.trackCharge = track.charge(); 
        this.trackMode = track.mode();
    }
    
    public void reset(){
        this.trackMode= 1;
        this.trackCharge = 0;
        this.trackStatus = 0;
        this.trackChi2 = 0;
        this.trackNDF = 0;
        this.vector().setPxPyPzE(0.0, 0.0, 0.0, 0.0);
    }
    
    public final void initTrack(int mode, int charge, double px, double py, double pz, double vx, double vy, double vz) {
        this.trackMode   = mode;
        this.trackCharge = charge;
        this.trackVector.setPxPyPzM(px, py, pz, 0);
        this.trackVertex.setXYZ(vx, vy, vz);
        for(int i=0; i<this.trackTrajectory.length; i++) {
            this.trackTrajectory[i] = new Vector3(0,0,0);
        }
        for(int i=0; i<this.trackCrosses.length; i++) {
            this.trackCrosses[i] = new Vector3(0,0,0);
        }
    }

    public int mode() {
        return trackMode;
    }
    
    public void mode(int m) {
        this.trackMode=m;
    }
    
    public void setP(double mom) {
        double mag = this.vector().p();
        double factor = mom / mag;
        this.vector().setPxPyPzM(this.vector().vect().x() * factor, this.vector().vect().y() * factor, this.vector().vect().z() * factor, 0);
    }
    
    public void setTheta(double theta) {
        this.vector().vect().setMagThetaPhi(this.vector().p(), theta, this.vector().phi());
    }
    
    
    public void setVector(int charge, double px, double py, double pz, double vx, double vy, double vz) {
        trackVector.setPxPyPzM(px, py, pz, 0);
        trackVertex.setXYZ(vx, vy, vz);
        trackCharge = charge;
        
    }
    
    public double px() {
        return this.vector().px();
    }
    
    public double py() {
        return this.vector().py();
    }
    
    public double pz() {
        return this.vector().pz();
    }
    
    public double p() {
        return this.vector().p();
    }
    
    public double theta() {
        return this.vector().theta();
    }
    
    public double phi() {
        return this.vector().phi();
    }
    
    public double e() {
        return this.vector().e();
    }
    
    public double vx() {
        return this.trackVertex.x();
    }
    
    public double vy() {
        return this.trackVertex.y();
    }
    
    public double vz() {
        return this.trackVertex.z();
    }
    
    public int sector() {
        return trackSector;
    }

    public void sector(int sec) {
        this.trackSector = sec;
    }

    public double x(int detector, int layer) {
	int i = this.trajIndex(detector, layer);
        return this.trackTrajectory[i].x();
    }

    public double y(int detector, int layer) {
        int i = this.trajIndex(detector, layer);
	return this.trackTrajectory[i].y();
    }

    public double z(int detector, int layer) {
        int i = this.trajIndex(detector, layer);
	return this.trackTrajectory[i].z();
    }

    public void setVector(int charge, Vector3 nvect, Vector3 nvert) {
        trackVector.setVectM(nvect, 0);
        trackVertex.setXYZ(nvert.x(), nvert.y(), nvert.z());
        trackCharge = charge;
        
    }
    
    public double euclideanDistance(Track part) {
        double xx = (this.vector().px() - part.vector().px());
        double yy = (this.vector().py() - part.vector().py());
        double zz = (this.vector().pz() - part.vector().pz());
        return Math.sqrt(xx * xx + yy * yy + zz * zz);
    }
    
    public double cosTheta(Track part) {
        if (part.vector().p() == 0 || this.vector().p() == 0)
            return -1;
        return part.vector().vect().dot(trackVector.vect()) / (part.vector().vect().mag() * trackVector.vect().mag());
    }
    
    
    public void setVector(LorentzVector nvec, Vector3 nvert) {
        trackVector = nvec;
        trackVertex = nvert;
    }

    public int charge() {
        return (int) trackCharge;
    }
    
    public void charge(int charge){
        this.trackCharge = charge;
    }

    public double chi2() {
        return trackChi2;
    }

    public void chi2(double trackChi2) {
        this.trackChi2 = trackChi2;
    }

    public int NDF() {
        return trackNDF;
    }

    public void NDF(int trackNDF) {
        this.trackNDF = trackNDF;
    }

    public int[] clusters() {
        return this.trackClusters;
    }
    
    public void clusters(int i1, int i2, int i3, int i4, int i5, int i6) {
        this.trackClusters[0] = i1;
        this.trackClusters[1] = i2;
        this.trackClusters[2] = i3;
        this.trackClusters[3] = i4;
        this.trackClusters[4] = i5;
        this.trackClusters[5] = i6;
        for(int i=0; i<6; i++) {
            if(this.trackClusters[i]<=0) this.trackClusters[i]=-1; //change 0 to -1 to allow matching of candidates to tracks
            if(this.trackClusters[i]>0)  this.trackSL++;
        }
    }    

    public int SL() {
        return trackSL;
    }

    public void SL(int n) {
        this.nSL = n;
    }

    private boolean hasSL(){
       if(this.nSL==0) return true;
       else return (this.SL()==this.nSL);
    }
    
    public void polarity(double polarity){
	this.trackPolarity = polarity;
    }

    public double polarity(){
	return this.trackPolarity;
    }

    public void status(int status){
        this.trackStatus = status;
    }
    
    public int status(){
        return this.trackStatus;
    }
    
    public void chi2pid(double chi2pid) {
        this.trackChi2pid = chi2pid;
    }
    
    public double chi2pid() {
        return this.trackChi2pid;
    }
    
    public void pid(int pid) {
        this.trackPid = pid;
    }
    
    public int pid() {
        return this.trackPid;
    }
    
    public void cross(double x, double y, double z, int region) {
        this.trackCrosses[region-1] = new Vector3(x, y, z);
    }
    
    public Vector3 cross(int region) {
        return this.trackCrosses[region-1];
    }
    
    public void trajectory(double x, double y, double z, int detector, int layer) {
        int i = this.trajIndex(detector, layer);
        if(i>=0) this.trackTrajectory[i] = new Vector3(x, y, z);
    }
    
    public Vector3 trajectory(int detector, int layer) {
        int i = this.trajIndex(detector, layer);
        if(i>=0) return this.trackTrajectory[i];
        else     return null;
    }
    
    private int trajIndex(int detector, int layer) {
        int i=-1;
        if(detector == DetectorType.DC.getDetectorId()) {
            i = ((int) (layer-1)/12);
        }
        else if(detector == DetectorType.FTOF.getDetectorId()) {
            i = layer-1 + 3;
        }
        else if(detector == DetectorType.ECAL.getDetectorId()) {
            i = ((int) (layer-1)/3) + 6;
        }
        return i;
    }

    public boolean isInFiducial() {
        return inFiducial;
    }

    public void isInFiducial(boolean inFiducial) {
        this.inFiducial = inFiducial;
    }
    
    public void setMatch(boolean match) {
        this.trackMatch = match;
    }
    
    public boolean isMatched() {
        return trackMatch;
    }
    
    public void setPrediction(boolean predict) {
        this.trackPredict = predict;
    }
    
    public boolean isPredicted() {
        return trackPredict;
    }
    
    public final LorentzVector vector() {
        return trackVector;
    }
    
    public final Vector3 vertex() {
        return trackVertex;
    }
    
    public double get(String pname) {
        if (pname.compareTo("theta") == 0)
            return trackVector.theta();
        if (pname.compareTo("phi") == 0)
            return trackVector.phi();
        if (pname.compareTo("p") == 0)
            return trackVector.p();
        if (pname.compareTo("mom") == 0)
            return trackVector.p();
        if (pname.compareTo("px") == 0)
            return trackVector.px();
        if (pname.compareTo("py") == 0)
            return trackVector.py();
        if (pname.compareTo("pz") == 0)
            return trackVector.pz();
        if (pname.compareTo("vx") == 0)
            return trackVertex.x();
        if (pname.compareTo("vy") == 0)
            return trackVertex.y();
        if (pname.compareTo("vz") == 0)
            return trackVertex.z();
        if (pname.compareTo("vertx") == 0)
            return trackVertex.x();
        if (pname.compareTo("verty") == 0)
            return trackVertex.y();
        if (pname.compareTo("vertz") == 0)
            return trackVertex.z();
        
        System.out.println("[Track::get] ERROR ----> variable " + pname + "  is not defined");
        return 0.0;
    }

    public boolean isP(double mean, double sigma){
        return (Math.abs(trackVector.p()-mean) < sigma);
    }
    
    public boolean isThetaDeg(double mean, double sigma){
        return (Math.abs(Math.toDegrees(trackVector.theta())-mean) < sigma);
    }
    
    public boolean isTheta(double mean, double sigma){
        return (Math.abs(trackVector.theta()-mean) < sigma);
    }
    
    public boolean isPhi(double mean, double sigma){
        return (Math.abs(trackVector.phi()-mean) < sigma);
    }
    public boolean isPhiDeg(double mean, double sigma){
        return (Math.abs(Math.toDegrees(trackVector.phi())-mean) < sigma);
    }
    
    public boolean isinbending() {

        if(this.polarity() < 0) {
            return true;
	}
        else {
            return false;
        }
    }

     public boolean isValid() {
        boolean value = false;
        if(Math.abs(this.vz()+5)<10 
        &&  this.chi2()<10  
        && Math.abs(this.chi2pid())<5
        && this.isInFiducial()
        && this.hasSL()
        ) value=true;
        return value;
    }

     public boolean isForLumiScan() {
        boolean value = false;
        if(this.pid()==11 && this.status()<0) value = this.p()>2.5 && this.p()<5.2;
        else                                  value = this.p()>0.4 && Math.abs(this.chi2pid())<3;
        value = value && Math.abs(this.vz()+5)<10 ;
        return value;
    }

    private int matchedClusters(Track t) {
        int nmatch = 0;
        for(int i=0; i<6; i++) {
            if(this.clusters()[i]==t.clusters()[i]) nmatch++;            
        }
        return nmatch;
    }
    
    private int nClusters() {
        int nclus = 0;
        for(int i=0; i<6; i++) {
            if(this.clusters()[i]>=0) nclus++;            
        }
        return nclus;
    }
    
    public boolean diff(Track t) {
        if(Math.abs(this.p()-t.p())>0.001) return true;
        if(Math.abs(this.theta()-t.theta())>0.01) return true;
        if(Math.abs(this.phi()-t.phi())>0.05) return true;
        return false;
    }
    
    
    public void show(){
        System.out.println(this.toString());
    }
    
    @Override
    public String  toString(){
        StringBuilder str = new StringBuilder();
        
        str.append(String.format("\tcharge: %4d\n", this.trackCharge));            
        str.append(String.format("\tpx: %9.5f",   this.px()));
        str.append(String.format("\tpy: %9.5f",   this.py()));
        str.append(String.format("\tpz: %9.5f\n", this.pz()));
        str.append(String.format("\tpx: %9.5f",   this.vx()));
        str.append(String.format("\tvx: %9.5f",   this.vy()));
        str.append(String.format("\tvz: %9.5f\n", this.vz()));
        str.append("\t");
        for(int i=0; i<this.clusters().length; i++) str.append(String.format("clus%1d:%3d\t", (i+1), this.clusters()[i]));
        str.append("\n");
        str.append(String.format("\tchi2: %7.3f",   this.chi2()));
        str.append(String.format("\tNDF:  %4d\n",   this.NDF()));            
        str.append(String.format("\tpid: %4d",      this.pid()));            
        str.append(String.format("\tchi2pid: %.1f", this.chi2pid()));            
        str.append(String.format("\tstatus: %4d\n", this.status()));            
        str.append(String.format("\tmatch:  %b\t",  this.isMatched()));            
        str.append(String.format("\tvalid:  %b\n",  this.isValid()));            
        for(int i=0; i<this.trackTrajectory.length; i++) {
        str.append("\t");
            str.append(String.format("traj%1d: ", (i+1)));
            if(this.trackTrajectory[i]!=null) str.append(this.trackTrajectory[i].toString());
            str.append("\n");
        }
        return str.toString();
    }
        
    public boolean equals(Track o) {
        if(this.matchedClusters(o)==6) return true;
        else return false;
    }
    
    public boolean isContainedIn(Track o) {
        boolean value = true;
        for(int i=0; i<6; i++) {
            if(this.clusters()[i]!=-1 && this.clusters()[i]!=o.clusters()[i]) value=false;           
        }
        return value;
    }
    
}
