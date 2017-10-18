/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.destination;

import java.util.List;

import org.ekstep.tools.loader.service.ProgressCallback;
import org.ekstep.tools.loader.service.Record;

/**
 *
 * @author feroz
 */
public interface Destination {
    public void process(List<Record> data, ProgressCallback callback) throws Exception ;
}
