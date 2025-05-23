package org.clas.analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.jlab.groot.data.DataLine;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.groot.ui.LatexText;
/**
 *
 * @author devita
 */
public class LumiAnalysis {
    
    private ArrayList<LumiDatum> lumies = null;

    LinkedHashMap<String,DataGroup> aiDGs   = new LinkedHashMap<>();
    LinkedHashMap<String,DataGroup> lumiDGs = new LinkedHashMap<>();
    LinkedHashMap<String,DataGroup> normDGs = new LinkedHashMap<>();
    LinkedHashMap<String,DataGroup> gainDGs = new LinkedHashMap<>();
    
    private Charges[] charges = {Charges.POS, Charges.NEG, Charges.ELE};
    private String[]  titles  = {"Positives", "Negatives", "Electrons"};
    private String[]  inputs  = {"data", "bg", "mc"};
    private int[]     symbols = {0, 3, 1};

    private boolean scale = true;
    
    public LumiAnalysis(ArrayList<LumiDatum> lumies, boolean scale) {
        this.lumies = lumies;
        this.scale  = scale;
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
                
                if(input.equals("bg") && charges[i]==Charges.NEG) {
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
                                                                                           lumen.getRatio(charges[i],Type.MATCHED,0), 
                                                                                           0, 
                                                                                           lumen.getRatioError(charges[i],Type.MATCHED,0));
                aiDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "gain").addPoint(lumen.getCurrent(), 
                                                                                           lumen.getRatio(charges[i],Type.AI,0), 
                                                                                           0, 
                                                                                           lumen.getRatioError(charges[i],Type.AI,0));
            
                lumiDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "conventional").addPoint(lumen.getCurrent(), 
                                                                                                     lumen.getLumi(Type.CONVENTIONAL, charges[i]), 
                                                                                                     0, 
                                                                                                     lumen.getLumiErr(Type.CONVENTIONAL, charges[i]));
                lumiDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "ai").addPoint(lumen.getCurrent(), 
                                                                                           lumen.getLumi(Type.AI, charges[i]), 
                                                                                           0, 
                                                                                           lumen.getLumiErr(Type.AI, charges[i]));
            }
            aiDGs.get(lumen.getRun()).getGraph(charges[2].getName() + "eff" ).addPoint(lumen.getCurrent(), 
                                                                                       lumen.getRatio(charges[2],Type.MATCHED,0), 
                                                                                       0, 
                                                                                       lumen.getRatioError(charges[2],Type.MATCHED,0));
            aiDGs.get(lumen.getRun()).getGraph(charges[2].getName() + "gain" ).addPoint(lumen.getCurrent(), 
                                                                                        lumen.getRatio(charges[2],Type.AI,0), 
                                                                                        0, 
                                                                                        lumen.getRatioError(charges[2],Type.AI,0));
        }
        for(int i=0; i<2; i++) {
            this.fit(aiDGs.get("data").getGraph(charges[i].getName() + "eff" ));
            this.fit(aiDGs.get("data").getGraph(charges[i].getName() + "gain"));

            this.fit(lumiDGs.get("data").getGraph(charges[i].getName() + "conventional"));
            this.fit(lumiDGs.get("data").getGraph(charges[i].getName() + "ai"));
            
            this.fit(lumiDGs.get("bg").getGraph(charges[i].getName() + "conventional"));
            this.fit(lumiDGs.get("bg").getGraph(charges[i].getName() + "ai"));
        }
        for(int j=0; j< this.lumies.size(); j++) {
            LumiDatum lumen = this.lumies.get(j);
            for(int i=0; i<2; i++) {
                if(!lumen.getRun().equals("mc")) {
                    lumen.setNorm(charges[i], Type.CONVENTIONAL, lumiDGs.get("data").getGraph(charges[i].getName() + "conventional").getFunction().getParameter(0));
                    lumen.setNorm(charges[i], Type.AI,           lumiDGs.get("data").getGraph(charges[i].getName() + "ai").getFunction().getParameter(0));
                }
                Type primary   = Type.AI;
                Type secondary = Type.CONVENTIONAL;
                if(!this.scale) {
                    primary   = Type.CONVENTIONAL;
                    secondary = Type.AI;
                }
                
                if(lumen.getNorm(charges[i], primary)>0)
                    normDGs.get(lumen.getRun()).getGraph(charges[i].getName() + secondary.getName()).addPoint(lumen.getCurrent(), 
                                                lumen.getLumiNorm(secondary, charges[i], lumen.getNorm(charges[i], primary)), 
                                                0, 
                                                lumen.getLumiNormErr(secondary, charges[i], lumen.getNorm(charges[i], primary)));
                else
                    normDGs.get(lumen.getRun()).getGraph(charges[i].getName() + secondary.getName()).addPoint(lumen.getCurrent(), 
                                                lumen.getLumiNorm(secondary, charges[i]), 
                                                0, 
                                                lumen.getLumiNormErr(secondary, charges[i]));
                
//                if(lumen.getNorm(charges[i], Type.CONVENTIONAL)>0)
//                    normDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "ai").addPoint(lumen.getCurrent(), 
//                                                lumen.getLumiNorm(Type.AI, charges[i], lumen.getNorm(charges[i], Type.CONVENTIONAL)), 
//                                                0, 
//                                                lumen.getLumiNormErr(Type.AI, charges[i], lumen.getNorm(charges[i], Type.CONVENTIONAL)));
//                else
//                    normDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "ai").addPoint(lumen.getCurrent(), 
//                                                lumen.getLumiNorm(Type.AI, charges[i]), 
//                                                0, 
//                                                lumen.getLumiNormErr(Type.AI, charges[i]));
                    
                normDGs.get(lumen.getRun()).getGraph(charges[i].getName() + primary.getName()).addPoint(lumen.getCurrent(), 
                                                                                           lumen.getLumiNorm(primary, charges[i]), 
                                                                                           0, 
                                                                                           lumen.getLumiNormErr(primary, charges[i]));
                gainDGs.get(lumen.getRun()).getGraph(charges[i].getName()).addPoint(lumen.getCurrent(),lumen.getLumiRatio(charges[i]),0,lumen.getLumiRatioErr(charges[i]));
                if(lumen.getRun().equals("bg") && charges[i]==Charges.NEG) {
                    normDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "conventional" + "ele").addPoint(lumen.getCurrent(), lumen.getEH(Type.CONVENTIONAL, Charges.ELE), 0, 0.001);
                    normDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "ai" + "ele").addPoint(lumen.getCurrent(), lumen.getEH(Type.AI, Charges.ELE), 0, 0.001);
                    gainDGs.get(lumen.getRun()).getGraph(charges[i].getName() + "ele").addPoint(lumen.getCurrent(),lumen.getLumiRatio(Charges.ELE),0,lumen.getLumiRatioErr(Charges.ELE));                    
                }
            }
        }
        for(int i=0; i<2; i++) {
            this.fit(normDGs.get("data").getGraph(charges[i].getName() + "conventional"));
            this.fit(normDGs.get("data").getGraph(charges[i].getName() + "ai"));
            
            this.fit(gainDGs.get("data").getGraph(charges[i].getName()));
            
            removeref(normDGs.get("bg").getGraph(charges[i].getName() + "conventional"));
            removeref(normDGs.get("bg").getGraph(charges[i].getName() + "ai"));
            removeref(gainDGs.get("bg").getGraph(charges[i].getName()));
//            normalize(normDGs.get("mc").getGraph(charges[i].getName() + "conventional"), normDGs.get("mc").getGraph(charges[i].getName() + "ai").getDataY(0));
//            normalize(normDGs.get("mc").getGraph(charges[i].getName() + "ai"), normDGs.get("mc").getGraph(charges[i].getName() + "ai").getDataY(0));
////            normalize(gainDGs.get("mc").getGraph(charges[i].getName()));
            if(charges[i]==Charges.NEG) {
                normalize(normDGs.get("bg").getGraph(charges[i].getName() + "conventional" + "ele"));
                normalize(normDGs.get("bg").getGraph(charges[i].getName() + "ai" + "ele"));
                normalize(gainDGs.get("bg").getGraph(charges[i].getName() + "ele"));
            }
        }
    }

    private void fit(GraphErrors g) {
        F1D f = funct("f"+g.getName(), g.getMarkerColor());
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
    
    public EmbeddedCanvasTabbed plotGraphs(boolean fitPar){
 
        EmbeddedCanvasTabbed canvas = new EmbeddedCanvasTabbed("AI","Lumi","Norm");

        for(String type : aiDGs.keySet()) {
            if(aiDGs.get(type).getGraph("poseff").getDataSize(0)>0) {
                canvas.getCanvas("AI").draw(aiDGs.get(type));
            }
        }
        canvas.getCanvas("AI").setGridX(false);
        canvas.getCanvas("AI").setGridY(false);
        for(int i=0; i<2; i++) {
            F1D fune = (F1D) aiDGs.get("data").getGraph(charges[i].getName() + "eff" ).getFunction();
            F1D fung = (F1D) aiDGs.get("data").getGraph(charges[i].getName() + "gain" ).getFunction();
            canvas.getCanvas("AI").cd(i);
            if(fitPar) {
                canvas.getCanvas("AI").draw(text(fune.getParameter(0),fune.getParameter(1),120,400,4));
                canvas.getCanvas("AI").draw(text(fung.getParameter(0),fung.getParameter(1),120,150,2));
            }
            canvas.getCanvas("AI").getPad(i).getAxisY().setRange(0.8,1.4);
            DataLine line= new DataLine(0,1,fune.getRange().getMax(),1);
            line.setLineWidth(2);
            canvas.getCanvas("AI").getPad(i).draw(line);
        }
        
        if(lumiDGs.get("data").getGraph("posai").getDataSize(0)>0) {
                canvas.getCanvas("Lumi").draw(lumiDGs.get("data"));
                canvas.getCanvas("Lumi").draw(lumiDGs.get("bg"));
        }
        canvas.getCanvas("Lumi").setGridX(false);
        canvas.getCanvas("Lumi").setGridY(false);

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
            F1D func = (F1D) normDGs.get("data").getGraph(charges[i].getName() + "conventional" ).getFunction();
            F1D funa = (F1D) normDGs.get("data").getGraph(charges[i].getName() + "ai" ).getFunction();
            F1D fung = (F1D) gainDGs.get("data").getGraph(charges[i].getName()).getFunction();
            canvas.getCanvas("Norm").cd(i);
            if(fitPar) {
                if(func.getParameter(0)!=0)
                    canvas.getCanvas("Norm").draw(text(func.getParameter(0),func.getParameter(1),120,370,1));
                if(funa.getParameter(0)!=0)
                    canvas.getCanvas("Norm").draw(text(funa.getParameter(0),funa.getParameter(1),120,330,2));
                if(fung.getParameter(0)!=0)
                    canvas.getCanvas("Norm").draw(text(fung.getParameter(0),fung.getParameter(1),120,40,3));
            }
            canvas.getCanvas("Norm").getPad(i).getAxisY().setRange(0.5,1.1);
            DataLine line= new DataLine(0,1,func.getRange().getMax(),1);
            line.setLineWidth(2);
            canvas.getCanvas("Norm").getPad(i).draw(line);
        }
        this.printResults();
        return canvas;
    }
    
    private void printResults() {
        for(String type : normDGs.keySet()) {
            for(int i=0; i<2; i++) {
                GraphErrors uconv = lumiDGs.get(type).getGraph(charges[i].getName() + "conventional");
                GraphErrors uai   = lumiDGs.get(type).getGraph(charges[i].getName() + "ai");
                GraphErrors nconv = normDGs.get(type).getGraph(charges[i].getName() + "conventional");
                GraphErrors nai   = normDGs.get(type).getGraph(charges[i].getName() + "ai");
                GraphErrors gain  = gainDGs.get(type).getGraph(charges[i].getName());
                if(nconv!=null && nai!=null && gain!=null && nconv.getDataSize(0)>0 && nai.getDataSize(0)>0) {
                    System.out.println();
                    System.out.println(type + " " + charges[i].getName());
                    System.out.println("       \t      unnormalized values   \t\t      normalized values     \t\t   ratios");
                    System.out.println("current\tconv\terror\tai\terror\t\tconv\terror\tai\terror\t\tgain\terror");
                    for(int j=0; j<nconv.getDataSize(0); j++)
                        System.out.println(String.format("%.0f\t%.4f\t%.4f\t%.4f\t%.4f\t\t%.4f\t%.4f\t%.4f\t%.4f\t\t%.4f\t%.4f", 
                                                         nconv.getDataX(j),
                                                         uconv.getDataY(j),uconv.getDataEY(j),
                                                         uai.getDataY(j),  uai.getDataEY(j),
                                                         nconv.getDataY(j),nconv.getDataEY(j),
                                                         nai.getDataY(j),  nai.getDataEY(j),
                                                         gain.getDataY(j),gain.getDataEY(j)));
                    F1D funuc = (F1D) uconv.getFunction();
                    F1D funua = (F1D) uai.getFunction();
                    F1D funnc = (F1D) nconv.getFunction();
                    F1D funna = (F1D) nai.getFunction();
                    F1D fung  = (F1D) gain.getFunction();
                    if(funuc!=null)
                        System.out.println(String.format("unnormalized cv: intercept=(%.5f \u00b1 %.5f) slope=(%.5f \u00b1 %.5f)", 
                                                          funuc.getParameter(0), funuc.parameter(0).error(),funuc.getParameter(1), funuc.parameter(1).error()));
                    if(funua!=null)
                        System.out.println(String.format("unnormalized ai: intercept=(%.5f \u00b1 %.5f) slope=(%.5f \u00b1 %.5f)", 
                                                          funua.getParameter(0), funua.parameter(0).error(),funua.getParameter(1), funua.parameter(1).error()));
                    if(funnc!=null)
                        System.out.println(String.format("normalized cv:   intercept=(%.5f \u00b1 %.5f) slope=(%.5f \u00b1 %.5f)", 
                                                          funnc.getParameter(0), funnc.parameter(0).error(),funnc.getParameter(1), funnc.parameter(1).error()));
                    if(funna!=null)
                        System.out.println(String.format("normalized ai:   intercept=(%.5f \u00b1 %.5f) slope=(%.5f \u00b1 %.5f)", 
                                                          funna.getParameter(0), funna.parameter(0).error(),funna.getParameter(1), funna.parameter(1).error()));
                }
            }
        }
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
        
    private void normalize(GraphErrors graph, double norm) {
        int n = graph.getDataSize(0);
        double[] x  = new double[n];
        double[] y  = new double[n];
        double[] ex = new double[n];
        double[] ey = new double[n];
        for(int i=0; i<n; i++) {
            x[i]  = graph.getDataX(i);
            y[i]  = graph.getDataY(i);
            ex[i] = graph.getDataEX(i);
            ey[i] = graph.getDataEY(i);
        }
        graph.reset();
        for(int i=0; i<n; i++) {
            graph.addPoint(x[i], y[i]/norm, ex[i], ey[i]/norm);
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
