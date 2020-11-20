/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.analysis;

import org.jlab.clas.physics.LorentzVector;
import org.jlab.clas.physics.Vector3;

/**
 *
 * @author devita
 */

public class Track implements Comparable<Track>{

    
    private LorentzVector trackVector = new LorentzVector(0.0,0.0,0.0,0);
    private Vector3 trackVertex = new Vector3(0.0,0.0,0.0);
    private int trackCharge = 0;
    private int trackStatus = 0 ;
    private double trackChi2 = 0;
    private int trackNDF = 0;
    private int[] trackClusters = new int[6];
    private boolean trackMatch = false;
    
    public Track() {
        this.initTrack(0, 0., 0., 0., 0., 0., 0.);
    }

    public Track(Track t) {
        this.initTrack(t.charge(), t.px(), t.py(), t.pz(), t.vertex().x(), t.vertex().y(), t.vertex().z());
    }
    
    public Track(int charge, double px, double py, double pz, double vx, double vy, double vz) {
        this.initTrack(charge, px, py, pz, vx, vy, vz);
    }
        
    
    public static Track copyFrom(Track p){
        Track newp = new Track();
        newp.charge(p.charge());
        newp.vector().copy(p.vector());
        newp.vertex().copy(p.vertex());
        return newp;
    }
    
    public void copy(Track track) {
        this.trackVector.setPxPyPzM(track.vector().px(), track.vector().py(), track.vector().pz(), track.vector().mass());
        this.trackVertex.setXYZ(track.vertex().x(), track.vertex().y(), track.vertex().z());        
        trackCharge   = track.charge();        
    }
    
    public void reset(){
        this.trackCharge = 0;
        this.trackStatus = 0;
        this.trackChi2 = 0;
        this.trackNDF = 0;
        this.vector().setPxPyPzE(0.0, 0.0, 0.0, 0.0);
    }
    
    public final void initTrack(int charge, double px, double py, double pz, double vx, double vy, double vz) {
        this.trackCharge = charge;
        this.trackVector.setPxPyPzM(px, py, pz, 0);
        this.trackVertex.setXYZ(vx, vy, vz);
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
    
    public void status(int status){
        this.trackStatus = status;
    }
    
    public int  status(){
        return this.trackStatus;
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
    }    

    public void setMatch(boolean match) {
        this.trackMatch = match;
    }
    
    public boolean isMatched() {
        return trackMatch;
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
    
    public boolean isValid() {
        boolean value = false;
        if(Math.abs(this.vz())<15) value=true;
        return value;
    }
    
    public void show(){
        System.out.println(this.toString());
    }
    
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
        for(int i=0; i<6; i++) str.append(String.format("clus%1d: %4d  ", (i+1), this.clusters()[i]));
        str.append("\n");
        str.append(String.format("\tchi2: %9.3f",  this.chi2()));
        str.append(String.format("\tNDF: %4d",      this.NDF()));            
        str.append(String.format("\tstatus: %4d\n", this.status()));            
        return str.toString();
    }
    
    /**
     * Compares two particles complying with Comparable interface.
     * The priority is given to charged particles over neutral.
     * In case of two particles have same momentum priority is given
     * to one with higher momentum.
     * a negative int - if this lt that
     * 0              - if this == that
     * a positive int - if this gt that
     * @param o object that this class is being compared to
     * @return -1,0,1 depending how the object are compared
     */
    @Override
    public int compareTo(Track o) {
        /**
         * Always make sure that electron is set in the first position
         */
        if(this.charge()==-1 && o.charge()!=-1) return -1;
        if(o.charge()==-1 && this.charge()!=-1) return  1;
        /**
         * For particles with same PID, sorting will happen on the basis
         * of their momentum.
         */
        if(this.charge()==o.charge()){
            if(o.clusters()[0]==this.clusters()[0] &&
               o.clusters()[1]==this.clusters()[1] &&
               o.clusters()[2]==this.clusters()[2] &&
               o.clusters()[3]==this.clusters()[3] &&
               o.clusters()[4]==this.clusters()[4] &&
               o.clusters()[5]==this.clusters()[5]) return 0;
            return (o.p()>this.p())?-1:1;
        }
        return 0;
    }
    
}