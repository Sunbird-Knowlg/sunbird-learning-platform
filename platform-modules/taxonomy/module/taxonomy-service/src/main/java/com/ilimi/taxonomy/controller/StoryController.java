package com.ilimi.taxonomy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/story")
public class StoryController extends AbstractContentController {
    
    @Autowired
    private ContentController contentController;
    
    {
        this.objectType = "Story";
    }
    
}
