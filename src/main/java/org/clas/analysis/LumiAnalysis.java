package org.clas.analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.jlab.groot.data.DataLine;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.graphics.EmbeddedPad;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.groot.ui.LatexText;
/**
 *
 * @author devita
 */
public class LumiAnalysis {
    
    private ArrayList<LumiDatum> lumies = null;

    LinkedHashMap<String,DataGroup> aiDGs   = new LinkedHashMap<String,DataGroup>();
    LinkedHashMap<String,DataGroup> lumiDGs = new LinkedHashMap<String,DataGroup>();
    LinkedHashMap<String,DataGroup> normDGs = new LinkedHashMap<String,DataGroup>();
    LinkedHashMap<String,DataGroup> gainDGs = new LinkedHashMap<String,DataGroup>();
    
    private Charges[] charges = {Charges.POS, Charges.NEG, Charges.ELE};
    private String[]  titles  = {"Positives", "Negatives", "Electrons"};
    private String[]  inputs  = {"data", "bg", "mc"};
    private int[]     symbols = {0, 3, 1};

    public LumiAnalysis(ArrayList<LumiDatum> lumies) {
        this.lumies = lumies;
        this.createGraphs();
        this.fillGraphs();
    }
    
    private void createGraphs() {
        for(int j=0; j<inputs.length; j++) {
            String input = inputs[j];
            int  marker = symbols[j];
            DataGroup dgAI   = new DataGroup(2,1);
            DataGroup dgLumi = new DataGroup(2,1);
            DataGroup dgNorm = new DataGroup(2,1);
            DataGroup dgGain = new DataGroup(2,1);
            for(int i=0; i<2; i++) {
                dgAI.addDataSet(graph(charges[i].getName() + "eff",   "I (nA)", titles[i], 4, marker, 5),i);
                dgAI.addDataSet(graph(charges[i].getName() + "gain",  "I (nA)", titles[i], 2, marker, 5),i);
                
                dgLumi.addDataSet(graph(charges[i].getName() + "conventional", "I (nA)", titles[i], 1, marker, 5),i);
                dgLumi.addDataSet(graph(charges[i].getName() + "ai",           "I (nA)", titles[i], 2, marker, 5),i);

                dgNorm.addDataSet(graph(charges[i].getName() + "conventional", "I (nA)", titles[i], 1, marker, 5),i);
                dgNorm.addDataSet(graph(charges[i].getName() + "ai",           "I (nA)", titles[i], 2, marker, 5),i);

                dgGain.addDataSet(graph(charges[i].getName(), "I (nA)", titles[i], 3, marker, 5),i);
                
                if(input.equals("data")) {
                    dgAI.addDataSet(funct("f" + charges[i].getName() + "eff" , 4),i);
                    dgAI.addDataSet(funct("f" + charges[i].getName() + "gain", 2),i);
                    dgLumi.addDataSet(funct("f" + charges[i].getName() + "conventional", 1),i);
                    dgLumi.addDataSet(funct("f" + charges[i].getName() + "ai",           2),i);
                    dgNorm.addDataSet(funct("f" + charges[i].getName() + "conventional", 1),i);
                    dgNorm.addDataSet(funct("f" + charges[i].getName() + "ai",           2),i);
                    dgGain.addDataSet(funct("f" + charges[i].getName(), 3),i);                    
                }
                else if(input.equals("bg") && charges[i]==Charges.NEG) {
                    dgNorm.addDataSet(graph(charges[i].getName() + "conventional" + "ele", "I (nA)", titles[i], 1, 2, 5),i);
                    dgNorm.addDataSet(graph(charges[i].getName() + "ai" + "ele",           "I (nA)", titles[i], 2, 2, 5),i);                    
                    dgGain.addDataSet(graph(charges[i].getName() + "ele", "I (nA)", titles[i], 3, 2, 5),i);
                }
            }
            dgAI.addDataSet(graph(charges[2].getName() + "eff",  "I (nA)", titles[2], 9, marker, 5),1);
            dgAI.addDataSet(graph(charges[2].getName() + "gain", "I (nA)", titles[2], 7, marker, 5),1);
            aiDGs.put(input, dgAI);
            lumiDGs.put(input, dgLumi);
            normDGs.put(input, dgNorm);
            gainDGs.put(input, dgGain);
        }
    }
    
    private void fillGraphs() {
        for(int j=0; j< this.lumies.size(); j++) {
            LumiDatum lumen = this.lumies.get(j);
            for(int i=0; i<2; i++) {
                aiDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "eff" ).addPoint(lumen.getCurrent(), 
                                                                                           lumen.getRatio(charges[i],Types.MATCHED,0), 
                                                                                           0, 
                                                                                           lumen.getRatioError(charges[i],Types.MATCHED,0));
                aiDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "gain").addPoint(lumen.getCurrent(), 
                                                                                           lumen.getRatio(charges[i],Types.AI,0), 
                                                                                           0, 
                                                                                           lumen.getRatioError(charges[i],Types.AI,0));
            
                lumiDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "conventional").addPoint(lumen.getCurrent(), 
                                                                                                     lumen.getLumi(Types.CONVENTIONAL, charges[i]), 
                                                                                                     0, 
                                                                                                     lumen.getLumiErr(Types.CONVENTIONAL, charges[i]));
                lumiDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "ai").addPoint(lumen.getCurrent(), 
                                                                                           lumen.getLumi(Types.AI, charges[i]), 
                                                                                           0, 
                                                                                           lumen.getLumiErr(Types.AI, charges[i]));
            }
            aiDGs.get(lumen.getRun()).getGraph(charges[2].getName() + "eff" ).addPoint(lumen.getCurrent(), 
                                                                                       lumen.getRatio(charges[2],Types.MATCHED,0), 
                                                                                       0, 
                                                                                       lumen.getRatioError(charges[2],Types.MATCHED,0));
            aiDGs.get(lumen.getRun()).getGraph(charges[2].getName() + "gain" ).addPoint(lumen.getCurrent(), 
                                                                                        lumen.getRatio(charges[2],Types.AI,0), 
                                                                                        0, 
                                                                                        lumen.getRatioError(charges[2],Types.AI,0));
        }
        for(int i=0; i<2; i++) {
            this.fit(aiDGs.get("data").getF1D("f" + charges[i].getName() + "eff" ),aiDGs.get("data").getGraph(charges[i].getName() + "eff" ));
            this.fit(aiDGs.get("data").getF1D("f" + charges[i].getName() + "gain"),aiDGs.get("data").getGraph(charges[i].getName() + "gain"));

            this.fit(lumiDGs.get("data").getF1D("f" + charges[i].getName() + "conventional"),lumiDGs.get("data").getGraph(charges[i].getName() + "conventional"));
            this.fit(lumiDGs.get("data").getF1D("f" + charges[i].getName() + "ai"          ),lumiDGs.get("data").getGraph(charges[i].getName() + "ai"));
            
        }
        for(int j=0; j< this.lumies.size(); j++) {
            LumiDatum lumen = this.lumies.get(j);
            for(int i=0; i<2; i++) {
                if(!lumen.getRun().equals("mc")) {
                    lumen.setNorm(charges[i], Types.CONVENTIONAL, lumiDGs.get("data").getF1D("f" + charges[i].getName() + "conventional").getParameter(0));
                    lumen.setNorm(charges[i], Types.AI,           lumiDGs.get("data").getF1D("f" + charges[i].getName() + "ai").getParameter(0));
                }
                normDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "conventional").addPoint(lumen.getCurrent(), 
                                                                                                     lumen.getLumiNorm(Types.CONVENTIONAL, charges[i]), 
                                                                                                     0, 
                                                                                                     lumen.getLumiNormErr(Types.CONVENTIONAL, charges[i]));
                normDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "ai").addPoint(lumen.getCurrent(), 
                                                                                           lumen.getLumiNorm(Types.AI, charges[i]), 
                                                                                           0, 
                                                                                           lumen.getLumiNormErr(Types.AI, charges[i]));
                gainDGs.get(lumen.getRun()).getGraph(charges[i].getName()).addPoint(lumen.getCurrent(),lumen.getLumiRatio(charges[i]),0,lumen.getLumiRatioErr(charges[i]));
                if(lumen.getRun().equals("bg") && charges[i]==Charges.NEG) {
                    normDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "conventional" + "ele").addPoint(lumen.getCurrent(), lumen.getEH(Types.CONVENTIONAL, Charges.ELE), 0, 0.001);
                    normDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "ai" + "ele").addPoint(lumen.getCurrent(), lumen.getEH(Types.AI, Charges.ELE), 0, 0.001);
                    gainDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "ele").addPoint(lumen.getCurrent(),lumen.getLumiRatio(Charges.ELE),0,lumen.getLumiRatioErr(Charges.ELE));                    
                }
            }
        }
        for(int i=0; i<2; i++) {
            this.fit(normDGs.get("data").getF1D("f" + charges[i].getName() + "conventional"),normDGs.get("data").getGraph(charges[i].getName() + "conventional"));
            this.fit(normDGs.get("data").getF1D("f" + charges[i].getName() + "ai"          ),normDGs.get("data").getGraph(charges[i].getName() + "ai"));
            
            this.fit(gainDGs.get("data").getF1D("f" + charges[i].getName()),gainDGs.get("data").getGraph(charges[i].getName()));
            
            removeref(normDGs.get("bg").getGraph(charges[i].getName() + "conventional"));
            removeref(normDGs.get("bg").getGraph(charges[i].getName() + "ai"));
            removeref(gainDGs.get("bg").getGraph(charges[i].getName()));
            normalize(normDGs.get("mc").getGraph(charges[i].getName() + "conventional"));
            normalize(normDGs.get("mc").getGraph(charges[i].getName() + "ai"));
            normalize(gainDGs.get("mc").getGraph(charges[i].getName()));
            if(charges[i]==Charges.NEG) {
                normalize(normDGs.get("bg").getGraph(charges[i].getName() + "conventional" + "ele"));
                normalize(normDGs.get("bg").getGraph(charges[i].getName() + "ai" + "ele"));
                normalize(gainDGs.get("bg").getGraph(charges[i].getName() + "ele"));
            }
        }
    }

    private void fit(F1D f, GraphErrors g) {
        double min=0;
        double max = g.getVectorX().getMax()*1.1;
        f.setRange(min, max);
        DataFitter.fit(f,g,"Q");
//        g.setFunction(null);
    }
    
    private F1D funct(String title, int col){
        F1D f1 = new F1D(title,  "[p0]+[p1]*x",0,60); 
        f1.setLineColor(col);
        return f1;
    }
    
    private GraphErrors graph(String title, String xTitle, String yTitle, int col, int style, int size) {
        GraphErrors ge = new GraphErrors(title);
        ge.setTitleX(xTitle);
        ge.setTitleY(yTitle);
        ge.setMarkerColor(col);
        ge.setMarkerSize(size);
        ge.setMarkerStyle(style);
        return ge;
    }  
    
    public EmbeddedCanvasTabbed plotGraphs(){
 
        EmbeddedCanvasTabbed canvas = new EmbeddedCanvasTabbed("AI","Lumi","Norm");

        for(String type : aiDGs.keySet()) {
            if(aiDGs.get(type).getGraph("poseff").getDataSize(0)>0) {
                canvas.getCanvas("AI").draw(aiDGs.get(type));
            }
        }
        canvas.getCanvas("AI").setGridX(false);
        canvas.getCanvas("AI").setGridY(false);
        for(int i=0; i<2; i++) {
            F1D fune = aiDGs.get("data").getF1D("f" + charges[i].getName() + "eff" );
            F1D fung = aiDGs.get("data").getF1D("f" + charges[i].getName() + "gain" );
            canvas.getCanvas("AI").cd(i);
            canvas.getCanvas("AI").draw(text(fune.getParameter(0),fune.getParameter(1),120,400,4));
            canvas.getCanvas("AI").draw(text(fung.getParameter(0),fung.getParameter(1),120,150,2));
            canvas.getCanvas("AI").getPad(i).getAxisY().setRange(0.9,1.25);
            DataLine line= new DataLine(0,1,fune.getRange().getMax(),1);
            line.setLineWidth(2);
            canvas.getCanvas("AI").getPad(i).draw(line);
        }
        
        if(lumiDGs.get("data").getGraph("posai").getDataSize(0)>0) {
                canvas.getCanvas("Lumi").draw(lumiDGs.get("data"));
        }
        canvas.getCanvas("Lumi").setGridX(false);
        canvas.getCanvas("Lumi").setGridY(false);
        for(EmbeddedPad pad : canvas.getCanvas("Lumi").getCanvasPads()) {
            pad.getAxisX().setRange(0,60);
        }

        for(String type : normDGs.keySet()) {
            if(normDGs.get(type).getGraph("posai").getDataSize(0)>0) {
                canvas.getCanvas("Norm").draw(normDGs.get(type));
            }
        }
        for(String type : gainDGs.keySet()) {
            if(gainDGs.get(type).getGraph("pos").getDataSize(0)>0) {
                canvas.getCanvas("Norm").draw(gainDGs.get(type));
            }
        }
        canvas.getCanvas("Norm").setGridX(false);
        canvas.getCanvas("Norm").setGridY(false);
        for(int i=0; i<2; i++) {
            F1D func = normDGs.get("data").getF1D("f" + charges[i].getName() + "conventional" );
            F1D funa = normDGs.get("data").getF1D("f" + charges[i].getName() + "ai" );
            F1D fung = gainDGs.get("data").getF1D("f" + charges[i].getName());
            canvas.getCanvas("Norm").cd(i);
            canvas.getCanvas("Norm").draw(text(func.getParameter(0),func.getParameter(1),120,270,1));
            canvas.getCanvas("Norm").draw(text(funa.getParameter(0),funa.getParameter(1),120,230,2));
            canvas.getCanvas("Norm").draw(text(fung.getParameter(0),fung.getParameter(1),120,70,3));
            canvas.getCanvas("Norm").getPad(i).getAxisY().setRange(0.5,1.2);
            DataLine line= new DataLine(0,1,60,1);
            line.setLineWidth(2);
            canvas.getCanvas("Norm").getPad(i).draw(line);
        }
        
        return canvas;
    }

    private void removeref(GraphErrors graph) {
        int n = graph.getDataSize(0);
        double[] x  = new double[n];
        double[] y  = new double[n];
        double[] ex = new double[n];
        double[] ey = new double[n];
        int i0 = 0;
        for(int i=0; i<n; i++) {
            if(graph.getDataX(i)<graph.getDataX(i0)) i0=i;
            x[i]  = graph.getDataX(i);
            y[i]  = graph.getDataY(i);
            ex[i] = graph.getDataEX(i);
            ey[i] = graph.getDataEY(i);
        }
        graph.reset();
        for(int i=0; i<n; i++) {
            if(i!=i0) {
                graph.addPoint(x[i], y[i], ex[i], ey[i]);
            }
        }
    }
    
    private void normalize(GraphErrors graph) {
        int n = graph.getDataSize(0);
        double[] x  = new double[n];
        double[] y  = new double[n];
        double[] ex = new double[n];
        double[] ey = new double[n];
        int i0 = 0;
        for(int i=0; i<n; i++) {
            if(graph.getDataX(i)<graph.getDataX(i0)) i0=i;
            x[i]  = graph.getDataX(i);
            y[i]  = graph.getDataY(i);
            ex[i] = graph.getDataEX(i);
            ey[i] = graph.getDataEY(i);
        }
        graph.reset();
        for(int i=0; i<n; i++) {
            if(i!=i0) {
                graph.addPoint(x[i]-x[i0], y[i]/y[i0], ex[i], ey[i]/y[i0]);
            }
        }
    }
    
    private LatexText text(double p0, double p1, int ix, int iy, int icol) {
        LatexText tt = null;
        if(p1>0) tt = new LatexText(String.format("%.3f +%.5f x", p0, p1), ix, iy);
        else     tt = new LatexText(String.format("%.3f %.5f x", p0, p1), ix, iy);
        tt.setFont("Arial");
        tt.setFontSize(18);
        tt.setColor(icol);
        return tt;
    }

}
