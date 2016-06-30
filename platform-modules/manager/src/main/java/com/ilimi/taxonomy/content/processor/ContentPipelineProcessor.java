package com.ilimi.taxonomy.content.processor;

import java.util.ArrayList;
import java.util.List;

import com.ilimi.taxonomy.content.entity.Plugin;

public class ContentPipelineProcessor extends AbstractProcessor{
	
	protected List<AbstractProcessor> lstProcessor = new ArrayList<AbstractProcessor>();

	@Override
	protected Plugin process(Plugin content) {
		for(AbstractProcessor processor: lstProcessor)
			content = processor.execute(content); 
		return content;
	}
	
	public void registerProcessor(AbstractProcessor processor) {
		if (null != processor)
			lstProcessor.add(processor);
	}

}
