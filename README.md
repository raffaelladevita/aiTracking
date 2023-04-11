# AI tracking validation

The package is designed to analyze and compare results from conventional and AI-assisted tracking (or from undenoised and denoised tracking). Comparison include:
- kinematic distributions for positive, negative and electron tracks, for both 6- and 5-superlayers tracks;
- event-by-event track parameters to identify matched and missed tracks from the two alogirthms;
- mass distributions from one and two pions final states.

It also provides the functionality for luminosity scan analyses to estimate the tracking efficiency.


### Prerequisites
* Software:
  * A Linux or Mac computer
  * Java Development Kit 11 or newer
  * maven 
  
  
### Build and run
Clone this repository:
```  
  git clone https://github.com/raffaelladevita/aiTracking
```
Go to the folder aiTracking and compile with maven:
```
  cd aiTracking
  mvn install
```

Run the code with:
```
  ./bin/aiTracking
  
     Usage : aiTracking 

   Options :
    -banks : tracking level: TB or HB (default = TB)
     -chi2 : max track reduced chi2 (-1 = infinity (default = -1)
     -edge : colon-separated DC, FTOF, ECAL edge cuts in cm (e.g. 5:10:5) (default = )
   -energy : beam energy (default = 10.6)
    -histo : read histogram file (0/1) (default = 0)
     -lumi : (comma-separated) luminosity scan currents, e.g. "5:data,20:data,40:data,40:bg;40:mc" (default = )
    -match : match based on clusters or hits (0/1) (default = 0)
        -n : maximum number of events to process (default = -1)
        -o : output file name prefix (default = )
     -plot : display histograms (0/1) (default = 1)
     -pmin : minimum momentum (GeV) (default = 0.5)
   -sector : sector (1-6, 0=any) (default = 0)
    -stats : histogram stat option (default = )
-superlayers : number of superlayers (5 or 6, 0=any) (default = 0)
   -target : target PDG (default = 2212)
-threshold : minimum number of entries for histogram differences (default = 0)
   -vertex : vertex range (min:max) (default = -15:5)
  -wiremin : min DC wire (default = 1)
    -write : save events with missing tracks (0/1) (default = 0)
```  

### Input data:
For AI-assisted tracking validation, input files can be produced as follows:
* use the yaml file data-aicv.yaml from the coatjava distribution of your choice (supported starting from coatjava-7.1.0): this will be in the plugins/clas12/config subfolder of the Clara installation;
* update the services configuration as needed for your data set; in case of doubts, consult the analysis coordinator of the data run group;
* in the MLTD service configuration section:
** update the run number to match the data to be processed; 
** if using a new network file, choose the run number to match the one used when creating and saving the network to the archive file and add the following setting:
   ```network: “absolute-path-to-your-network-archive-file”```
   specifying the path to your network file.
* run reconstruction saving the following banks as a minimum ```RUN::config, ai::tracks, REC::Particle, REC::Track, REC::Traj, TimeBasedTrkg::TBTracks, RECAI::Particle, RECAI::Track, RECAI::Traj, TimeBasedTrkg::AITracks```;
* typically a few hipo files are sufficient but ideally a full production run should be processed.


Similarly, for denoising validation, input files can be produced as follows:
* use the yaml file denoise.yaml from the coatjava distribution of your choice (supported starting from coatjava-8.7.1): this will be in the plugins/clas12/config subfolder of the Clara installation;
* update the services configuration (variation, timestamp, AI neural network) as needed for your data set;
* setup a custom schema directory for saving the following banks as a minimum ```RUN::config, ai::tracks, aid::tracks, REC::Particle, REC::Track, REC::Traj, TimeBasedTrkg::TBTracks, TimeBasedTrkg::TBHits, RECAI::Particle, RECAI::Track, RECAI::Traj, TimeBasedTrkg::AITracks, TimeBasedTrkg::AIHits```;
* run reconstruction configuring a workflow with the --denoise” option.


The same procedure can be used to produce input files for validation at the Hit-based tracking level. The only difference is in the banks to save:
* ```RUN::config, ai::tracks, RECHB::Particle, RECHB::Track, RECHB::Traj, HitBasedTrkg::HBTracks, RECHBAI::Particle, RECHBAI::Track, RECHBAI::Traj, HitBasedTrkg::AITracks``` for AI-assisted tracking validation,
* ```RUN::config, ai::tracks, RECHB::Particle, RECHB::Track, RECHB::Traj, HitBasedTrkg::HBTracksj, HitBasedTrkg::HBHits, RECHBAI::Particle, RECHBAI::Track, RECHBAI::Traj, HitBasedTrkg::AITracks, HitBasedTrkg::AIHits``` for denoising validation.


### Luminosity scan analysis
To analyze a luminosity scan:
* run the code on the data files for each luminosity setting, separately. For denoising validation use the option ```-match 1``` to match tracks at the hit level.
* save the histogram files,
* run the code with the ````-lumi``` option. For example:
```
./bin/aiTracking -histo 1 -lumi "2:data,5:data,10:data,20:data,40:data" 2nA_histo_file.hipo 5nA_histo_file.hipo 10nA_histo_file.hipo 20nA_histo_file.hipo 40nA_histo_file.hipo
```

The cuts used in this analysis are defined at https://github.com/raffaelladevita/aiTracking/blob/master/src/main/java/org/clas/analysis/Track.java#L431-L439 and can be easily modified as needed.

Note that the luminosity analysis can be performed also if only conventional or only AI-assisted tracking results are available in the data files.
