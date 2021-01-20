package org.clas.analysis;

import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.SchemaFactory;

/**
 *
 * @author devita
 */
public class Banks {

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

    public Banks(String mode, SchemaFactory schema) {
	this.runConfig        = new Bank(schema.getSchema("RUN::config"));
        this.cvParticleBank   = new Bank(schema.getSchema("REC::Particle"));
        this.cvTrajectoryBank = new Bank(schema.getSchema("REC::Traj"));;
        this.cvTrackBank      = new Bank(schema.getSchema("REC::Track"));;
        this.cvTrackingBank   = new Bank(schema.getSchema("TimeBasedTrkg::TBTracks"));
        this.aiParticleBank   = new Bank(schema.getSchema("RECAI::Particle"));
        this.aiTrajectoryBank = new Bank(schema.getSchema("RECAI::Traj"));;
        this.aiTrackBank      = new Bank(schema.getSchema("RECAI::Track"));;
        this.aiTrackingBank   = new Bank(schema.getSchema("TimeBasedTrkg::AITracks"));
        if(mode.equals("HB")) {
            this.cvParticleBank   = new Bank(schema.getSchema("RECHB::Particle"));
            this.cvTrajectoryBank = null;
            this.cvTrackBank      = new Bank(schema.getSchema("RECHB::Track"));;
            this.cvTrackingBank   = new Bank(schema.getSchema("HitBasedTrkg::HBTracks"));
            this.aiParticleBank   = new Bank(schema.getSchema("RECHBAI::Particle"));
            this.aiTrajectoryBank = null;
            this.aiTrackBank      = new Bank(schema.getSchema("RECHBAI::Track"));;
            this.aiTrackingBank   = new Bank(schema.getSchema("HitBasedTrkg::AITracks"));            
        }
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
    
     
}
