package com.ilimi.taxonomy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/v1/worksheet")
public class WorksheetController extends AbstractContentController {
    
    {
        this.objectType = "Worksheet";
    }
    
}
