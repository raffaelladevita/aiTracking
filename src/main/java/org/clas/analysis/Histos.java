/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.analysis;

import org.jlab.groot.group.DataGroup;

/**
 *
 * @author devita
 */
public class Histos extends DataGroup{
    
    private DataGroup group = null;
    private String name = null;
    
    public Histos(String str, int col) {
        this.name = str;
        group = this.create(col);
    }
    
    public DataGroup create(int col) {
        DataGroup dg = new DataGroup(2,2);
        return dg;
    }
    
    public void fill(Track track) {
    
    }

    public DataGroup getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }
    
    
    
}
