/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 *
 * @author feroz
 */
public interface ConceptService {
    public String create(JsonObject concept, ExecutionContext context) throws Exception ;
    public String update(JsonObject concept, ExecutionContext context) throws Exception ;
    public String retire(JsonArray conceptIds, ExecutionContext context) throws Exception ;
}
