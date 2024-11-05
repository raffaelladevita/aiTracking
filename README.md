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
  
     Usage : aiTracking file1 file2 ... fileN

   Options :
    -banks : tracking level: TB or HB (default = TB)
     -chi2 : max track reduced chi2 (-1 = infinity (default = -1)
     -edge : colon-separated DC, FTOF, ECAL edge cuts in cm (e.g. 5:10:5) (default = )
   -energy : beam energy (default = 10.6)
      -fit : display fit parameters in luminosity analyses (0/1) (default = 1)
    -histo : read histogram file (0/1) (default = 0)
     -lumi : (comma-separated) luminosity scan currents, e.g. "5:data,20:data,40:data,40:bg;40:mc" (default = )
    -match : match based on clusters or hits (0/1) (default = 0)
        -n : maximum number of events to process (default = -1)
        -o : output file name prefix (default = )
     -plot : display histograms (0/1) (default = 1)
     -pmin : minimum momentum (GeV) (default = 0.5)
    -range : set y-axis range for efficiency and gain plots (default = 0.1)
    -scale : set luminosity dependence scale factor according to conventional (0) or AI-assisted tracking (1) (default = 1)
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
* add the swap service if needed: this should be inserted in the service chain right after ```magfield``` and the appropriate configuration (timestamps and detector list) added to the configuration section; do not alter the order of the other services unless specifically instructed;
* update the services configuration as needed for your data set: this typically involves setting the appropriate timestamp and variation; in case of doubts, consult the analysis coordinator of the data run group;
* in the MLTD service configuration section:
  * update the run number to match the data to be processed; 
  * if using a new network file, choose the run number to match the one used when creating and saving the network to the archive file and add the following setting:
    ```
    network: “absolute-path-to-your-network-archive-file”
    ```
    specifying the path to your network file.
* set the output bank schema path to point to the ```dcalign``` folder of your clara installation; this will be under plugins/clas12/etc/bankdefs/hipo4;
* typically a few hipo files are sufficient but ideally, a full production run should be processed. DO NOT include denoising in the data processing workflow.


Similarly, for denoising validation, input files can be produced as follows:
* use the yaml file denoise.yaml from the coatjava distribution of your choice (supported starting from coatjava-8.7.1): this will be in the plugins/clas12/config subfolder of the Clara installation;
* add the swap service if needed: this should be inserted in the service chain right after ```magfield``` and the appropriate configuration (timestamps and detector list added to the configuration section; do not alter the order of the other services unless specifically instructed;
* update the services configuration (variation, timestamp, AI neural network) as needed for your data set;
* set the output bank schema path to point to the ```dcalign``` folder of your clara installation; this will be under plugins/clas12/etc/bankdefs/hipo4;
* run reconstruction configuring a CLAS12 reconstruction workflow with the --denoise” option.


The same procedure can be used to produce input files for validation at the Hit-based tracking level. The only difference is in the banks to save, which will require a custom schema with the following banks:
* ```RUN::config, ai::tracks, RECHB::Particle, RECHB::Track, RECHB::Traj, HitBasedTrkg::HBTracks, RECHBAI::Particle, RECHBAI::Track, RECHBAI::Traj, HitBasedTrkg::AITracks``` for AI-assisted tracking validation,
* ```RUN::config, ai::tracks, RECHB::Particle, RECHB::Track, RECHB::Traj, HitBasedTrkg::HBTracksj, HitBasedTrkg::HBHits, RECHBAI::Particle, RECHBAI::Track, RECHBAI::Traj, HitBasedTrkg::AITracks, HitBasedTrkg::AIHits``` for denoising validation.

### Command line options
Several options can be selected from command line:
* Configuration:
  * ```-banks```: select whether the analysis is be performed on Hit Based or Time Based banks (default is TB)
  * ```-match```: matches tracks reconstructed in the same event with different tracking algorithms based on clusters (0=default) or hits (1). The default mode is recommended for AI-assisted tracking validation; in this case two tracks are considered matched if resulting from the exact same clusters. Matching based on hits should instead be selected for denoising validation; in this case, two tracks are considered matched if they share at least 60% of the hits; 
  * ```-superlayers```: select the number of superlayers a tracks should have to be included in the analysis, 5, 6 or any (0) with default being any
  * ```-sector```: select the sectors to be included in the analysis; values are 1 to 6 or 0 for all sectors (default)
  * ```-lumi```: performs a luminosity scan analysis on existing histogram files (see section below)
* Display:
  * ```-fit```: display the luminosity analyses fit results (1) or no t(0)), default is 1
  * ```-plot```: open histogram GUI (1) or run in batch mode (0), default is 1
  * ```-range```: set the y-axis range to 1 +/- the chosen value
  * ```-plot```: set the normalization scale factor in the luminosity analyses from conventional (0) or AI-assisted (1) tracking, default is 1
  * ```-stats```: set the histogram stat option, for example "1111" to show histogram name, entries, mean and RMS or "" to turn off the statistical box
  * ```-threshold```: minimum number of entries for histogram differences (default = 0), useful to avoid large fluctuations in histogram ratio
* I/O:
  * ```-histo```: read existing histogram file (0/1) (default = 0);
  * ```-n```: maximum number of events to process (default = -1);
  * ```-o```: output file name prefix (default = ), used for the histogram file name and the missing tracks files that can be saved with the option ```-write```;
  * ```-write```: save events with missing tracks (0/1) (default = 0)
* Run-dependent parameters:
  * ```-target```: target PDG (default = 2212)
  * ```-energy```: beam energy (default = 10.6)
* Track selection:
  * ```-chi2```: max track reduced chi2 (-1 = infinity (default = -1)
  * ```-edge```: colon-separated DC, FTOF, ECAL edge cuts in cm (e.g. 5:10:5) (default = )
  * ```-pmin```: minimum momentum (GeV) (default = 0.5)
  * ```-vertex```: vertex range (min:max) (default = -15:5)
  * ```-wiremin```: min DC wire (default = 1)
    
    
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
