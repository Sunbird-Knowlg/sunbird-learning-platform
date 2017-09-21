/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import java.util.List;

/**
 *
 * @author feroz
 */
public interface Destination {
    public void process(List<Record> data, ProgressCallback callback) throws Exception ;
}
