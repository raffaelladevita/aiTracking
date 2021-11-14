package org.clas.analysis;

import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.SchemaFactory;

/**
 *
 * @author devita
 */
public class Banks {

    private int mode =1;
    
    private Bank runConfig;
    // conventional banks
    private Bank cvParticleBank;
    private Bank cvTrajectoryBank;
    private Bank cvTrackBank;
    private Bank cvTrackingBank;
    // ai banks
    private Bank aiParticleBank;
    private Bank aiTrajectoryBank;
    private Bank aiTrackBank;
    private Bank aiTrackingBank;
    private Bank aiCandidates;
    
    private boolean clusterIds;

    public Banks(String mode, SchemaFactory schema) {
        if(schema.hasSchema("RUN::config"))
            this.runConfig        = new Bank(schema.getSchema("RUN::config"));
        if(schema.hasSchema("ai::tracks"))
            this.aiCandidates     = new Bank(schema.getSchema("ai::tracks"));
        if(schema.hasSchema("REC::Particle"))
            this.cvParticleBank   = new Bank(schema.getSchema("REC::Particle"));
        if(schema.hasSchema("REC::Traj"))
            this.cvTrajectoryBank = new Bank(schema.getSchema("REC::Traj"));;
        if(schema.hasSchema("REC::Track"))
            this.cvTrackBank      = new Bank(schema.getSchema("REC::Track"));;
        if(schema.hasSchema("TimeBasedTrkg::TBTracks")) {
            this.cvTrackingBank   = new Bank(schema.getSchema("TimeBasedTrkg::TBTracks"));
            if(schema.getSchema("TimeBasedTrkg::TBTracks").hasEntry("Cluster1_ID"))
               clusterIds = true;
        }
        if(schema.hasSchema("RECAI::Particle"))
            this.aiParticleBank   = new Bank(schema.getSchema("RECAI::Particle"));
        if(schema.hasSchema("RECAI::Traj"))
            this.aiTrajectoryBank = new Bank(schema.getSchema("RECAI::Traj"));;
        if(schema.hasSchema("RECAI::Track"))
            this.aiTrackBank      = new Bank(schema.getSchema("RECAI::Track"));;
        if(schema.hasSchema("TimeBasedTrkg::AITracks")) {
            this.aiTrackingBank   = new Bank(schema.getSchema("TimeBasedTrkg::AITracks"));
            if(schema.getSchema("TimeBasedTrkg::AITracks").hasEntry("Cluster1_ID"))
                clusterIds = true;
        }
        if(mode.equals("HB")) {
            this.mode = 0;
            this.cvTrajectoryBank = null;
            this.aiTrajectoryBank = null;
            if(schema.hasSchema("RECHB::Particle"))
                this.cvParticleBank   = new Bank(schema.getSchema("RECHB::Particle"));
            if(schema.hasSchema("RECHB::Track"))
                this.cvTrackBank      = new Bank(schema.getSchema("RECHB::Track"));;
            if(schema.hasSchema("HitBasedTrkg::HBTracks")) {
                this.cvTrackingBank   = new Bank(schema.getSchema("HitBasedTrkg::HBTracks"));
                if(schema.getSchema("HitBasedTrkg::HBTracks").hasEntry("Cluster1_ID"))
                   clusterIds = true;
            }
            if(schema.hasSchema("RECHBAI::Particle"))
                this.aiParticleBank   = new Bank(schema.getSchema("RECHBAI::Particle"));
            if(schema.hasSchema("RECHBAI::Track"))
                this.aiTrackBank      = new Bank(schema.getSchema("RECHBAI::Track"));;
            if(schema.hasSchema("HitBasedTrkg::AITracks")) {
                this.aiTrackingBank   = new Bank(schema.getSchema("HitBasedTrkg::AITracks"));
                if(schema.getSchema("HitBasedTrkg::AITracks").hasEntry("Cluster1_ID"))
                    clusterIds = true;
            }
        }
    }

    public int getMode() {
        return mode;
    }

    public Bank getRunConfig() {
	return runConfig;
    }

    public Bank getRecParticleBank(int type) {
        if(type==0) return cvParticleBank;
        else        return aiParticleBank;
    }

    public Bank getRecTrajectoryBank(int type) {
        if(type==0) return cvTrajectoryBank;
        else        return aiTrajectoryBank;
    }

    public Bank getRecTrackBank(int type) {
        if(type==0) return cvTrackBank;
        else        return aiTrackBank;
    }

    public Bank getTrackingBank(int type) {
        if(type==0) return cvTrackingBank;
        else        return aiTrackingBank;
    }
    
    public Bank getAICandidateBank() {
	return aiCandidates;
    }

    public boolean hasClusterId() {
        return this.clusterIds;
    }
}
