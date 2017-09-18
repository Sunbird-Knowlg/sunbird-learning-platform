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
public interface ContentService {
    public String create(JsonObject content, ExecutionContext context) throws Exception ;
    public String update(JsonObject content, ExecutionContext context) throws Exception ;
    public String retire(JsonArray contentIds, ExecutionContext context) throws Exception ;
    
    public String uploadArtifact(JsonObject content, ExecutionContext context) throws Exception ;
    public String uploadAppIcon(JsonObject content, ExecutionContext context) throws Exception ;
    
    public String submitForReview(JsonObject content, ExecutionContext context) throws Exception ;
    public String publish(JsonObject content, ExecutionContext context) throws Exception ;
}
