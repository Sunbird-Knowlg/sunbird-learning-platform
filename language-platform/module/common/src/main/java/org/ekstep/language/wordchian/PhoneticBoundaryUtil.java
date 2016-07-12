package org.ekstep.language.wordchian;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.util.WordUtil;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class PhoneticBoundaryUtil extends BaseManager{
	
	private WordUtil wordUtil = new WordUtil();

	private IPhoneticBoundary getPhoneticBoundary(String languageId, Node node, WordComplexity wc){
		if(languageId.equalsIgnoreCase("en")){
			return new EnglishPhoneticBoundary(languageId, node, wc);
		}else{
			return new IndianLanguagePhoneticBoundary(languageId, node, wc);
		}
	}
		
	public void updateWordChainRelations(String languageId, Node node, WordComplexity wc) throws Exception{
		String status = (String) node.getMetadata().get(LanguageParams.status.name());
		if(status == null)
			return;
		if("Live".equalsIgnoreCase(status)){
			IPhoneticBoundary phoneticBoundary = getPhoneticBoundary(languageId, node, wc);
			List<Relation> outRelations = new ArrayList<Relation>();
			List<Relation> inRelations = new ArrayList<Relation>();
			Relation startsWithRelation = phoneticBoundary.getStartsWithAksharaRelation();
			List<Relation> endsWithRelation = phoneticBoundary.getEndsWithAksharaRelation();
			List<Relation> rhymingSoundRelations = phoneticBoundary.getRhymingSoundRelation();
			outRelations.add(startsWithRelation);
			outRelations.addAll(endsWithRelation);
			if(rhymingSoundRelations != null && rhymingSoundRelations.size() > 0){
				outRelations.add(rhymingSoundRelations.get(0));
				inRelations.add(rhymingSoundRelations.get(1));
			}
			
			updateWordWithNewRelation(languageId, node, inRelations, outRelations);
		}else{
			List<Relation> outRelation = node.getOutRelations();
			if(outRelation != null ){
				Iterator<Relation> rItr =outRelation.iterator();
				while(rItr.hasNext()){
					Relation rel = rItr.next();
					if(rel.getRelationType().equalsIgnoreCase(RelationTypes.STARTS_WITH_AKSHARA.relationName()) ||
							rel.getRelationType().equalsIgnoreCase(RelationTypes.ENDS_WITH_AKSHARA.relationName()) ||
							rel.getRelationType().equalsIgnoreCase(RelationTypes.RYMING_SOUNDS.relationName())){
						rItr.remove();
					}
				}
				node.setOutRelations(outRelation);				
			}
			
			List<Relation> inRelation = node.getInRelations();
			if(inRelation != null){
				Iterator<Relation> irItr =inRelation.iterator();
				while(irItr.hasNext()){
					Relation rel = irItr.next();
					if(rel.getRelationType().equalsIgnoreCase(RelationTypes.RYMING_SOUNDS.relationName())){
						irItr.remove();
					}
				}
				node.setInRelations(inRelation);				
			}

			
			Response wordResponse = wordUtil.updateWord(node, languageId, node.getIdentifier());
			if (checkError(wordResponse)) {
				throw new ServerException(LanguageErrorCodes.ERR_UPDATE_WORD.name(),
						getErrorMessage(wordResponse));
			}
		}

	}

	private void updateWordWithNewRelation(String languageId, Node wordNode, List<Relation> newInRelations, List<Relation> newOutRelations){
		List<Relation> inRelation = wordNode.getInRelations();
		List<Relation> outRelation = wordNode.getOutRelations();
				
		if(inRelation == null){
			wordNode.setInRelations(newInRelations);
		}else{
			for(Iterator<Relation> iRel = inRelation.iterator(); iRel.hasNext();) {
			       Relation relation = iRel.next();
			       if(!relation.getRelationType().equalsIgnoreCase(RelationTypes.RYMING_SOUNDS.name())){
			    	   newInRelations.add(relation);
			       }
			 }
			wordNode.setInRelations(newInRelations);
		}
		
		if(outRelation == null){
			wordNode.setOutRelations(newOutRelations);
		}else{
			for(Iterator<Relation> iRel = outRelation.iterator(); iRel.hasNext();) {
			       Relation relation = iRel.next();
			       if(!relation.getRelationType().equalsIgnoreCase(RelationTypes.RYMING_SOUNDS.name())&&
			    		   !relation.getRelationType().equalsIgnoreCase(RelationTypes.STARTS_WITH_AKSHARA.name())&&
			    		   !relation.getRelationType().equalsIgnoreCase(RelationTypes.ENDS_WITH_AKSHARA.name())){
			    	   newOutRelations.add(relation);
			       }
			 }
			wordNode.setOutRelations(newOutRelations);
		}
		
		wordUtil.updateWord(wordNode, languageId, wordNode.getIdentifier());
	}
}
