# AI tracking validation

The package is designed to analyze and compare results from conventional and AI-assisted tracking. Comparison include:
- kinematic distributions for positive, negative and electron tracks, for both 6- and 5-superlayers tracks;
- event-by-event track parameters to identify matched and missed tracks from the two alogirthms;
- mass distributions from one and two pions final states.

It also provides the functionality for luminosity scan analyses to estimate the tracking efficiency.


### Prerequisites
* Software:
  * A Linux or Mac computer
  * Java Development Kit 11 or newer
  * maven 
* Input data:
  * hipo files with data processed with the data-aicv.yaml provided with the coatjava distributions and the following bank:
    * ```RUN::config, ai::tracks, REC::Particle, REC::Track, REC::Traj, TimeBasedTrkg::TBTracks, RECAI::Particle, RECAI::Track, RECAI::Traj, TimeBasedTrkg::AITracks``` for time-base tracking analysis,
    *```RUN::config, ai::tracks, RECHB::Particle, RECHB::Track, RECHB::Traj, HitBasedTrkg::HBTracks, RECHBAI::Particle, RECHBAI::Track, RECHBAI::Traj, HitBasedTrkg::AITracks``` for hit base tracking analysis.
  
  
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
   -energy : beam energy (default = 10.6)
    -histo : read histogram file (0/1) (default = 0)
     -lumi : (comma-separated) luminosity scan currents, e.g. "5:data,20:data,40:data,40:bg;40:mc" (default = )
        -n : maximum number of events to process (default = -1)
        -o : output file name prefix (default = )
     -plot : display histograms (0/1) (default = 1)
    -stats : histogram stat option (default = )
-superlayers : number of superlayers (5 or 6, 0=any) (default = 0)
   -target : target PDG (default = 2212)
-threshold : minimum number of entries for histogram differences (default = 0)
   -vertex : vertex range (min:max) (default = -15:5)
    -write : save events with missing tracks (0/1) (default = 0)
```  


### Luminosity scan analysis
To analyze a luminosity scan:
* run the code on the data files for each luminosity setting, separately,
* save the histogram files,
* run the code with the ````-lumi``` option. For example:
```
./bin/aiTracking -histo 1 -lumi "2:data,5:data,10:data,20:data,40:data" 2nA_histo_file.hipo 5nA_histo_file.hipo 10nA_histo_file.hipo 20nA_histo_file.hipo 40nA_histo_file.hipo

The cuts used in this analysis are defined at https://github.com/raffaelladevita/aiTracking/blob/master/src/main/java/org/clas/analysis/Track.java#L431-L439 and can be easily modified as needed.

Note that the luminosity analysis can be performed also if only conventional or only AI-assisted tracking results are available in the data files.
