package org.ekstep.language.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.server.quorum.LeaderElectionMXBean;

import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class WordnetUtil implements IWordnetConstants {
	
	private static WordUtil wordUtil=new WordUtil();
	
    public static String getPosValue(String posTag) {
        return getPosValue(posTag, true);
    }

    public static String getPosValue(String posTag, boolean returnDefault) {
        if (StringUtils.isNotBlank(posTag)) {
            switch (posTag.trim().toLowerCase()) {
            case POS_TAG_NN:
            case POS_TAG_NST:
            case POS_TAG_NNP:
            case POS_CATEGORY_N:
                return POS_NOUN;
            case POS_TAG_PRP:
            case POS_CATEGORY_PN:
                return POS_PRONOUN;
            case POS_TAG_VM:
            case POS_TAG_VAUX:
            case POS_CATEGORY_V:
                return POS_VERB;
            case POS_TAG_JJ:
            case POS_CATEGORY_ADJ:
                return POS_ADJECTIVE;
            case POS_TAG_RB:
                return POS_ADVERB;
            case POS_TAG_CC:
                return POS_CONJUNCTION;
            case POS_TAG_INJ:
                return POS_INTERJECTION;
            case POS_TAG_UNK:
                return null;
            }
        }
        if (returnDefault)
            return posTag;
        else
            return null;
    }

    public static boolean isStandardPOS(String posTag) {
        if (StringUtils.isNotBlank(posTag)) {
            switch (posTag.trim().toLowerCase()) {
            case POS_NOUN:
            case POS_PRONOUN:
            case POS_VERB:
            case POS_ADVERB:
            case POS_ADJECTIVE:
            case POS_CONJUNCTION:
            case POS_PREPOSITION:
            case POS_INTERJECTION:
            case POS_ARTICLE:
                return true;
            }
        }
        return false;
    }

    public static void updatePOS(Node node) {
        Set<String> posList = new HashSet<String>();
        try {
            Object posTags = (Object) node.getMetadata().get(ATTRIB_POS_TAGS);
            boolean updatePosTags = false;
            if (null == posTags || StringUtils.isBlank(posTags.toString()))
                updatePosTags = true;
            else {
                if (posTags instanceof String[]) {
                    String[] arr = (String[]) posTags;
                    if (null != arr && arr.length > 0) {
                        for (String str : arr) {
                            String pos = WordnetUtil.getPosValue(str);
                            if (StringUtils.isNotBlank(pos))
                                posList.add(pos.toLowerCase());
                        }
                    }
                } else if (posTags instanceof String) {
                    if (StringUtils.isNotBlank(posTags.toString())) {
                        String pos = WordnetUtil.getPosValue(posTags.toString());
                        if (StringUtils.isNotBlank(pos))
                            posList.add(pos.toLowerCase());
                    }
                }
            }
            Set<String> posTagList = new HashSet<String>();
            Object value = node.getMetadata().get(ATTRIB_POS);
            if (null != value) {
                if (value instanceof String[]) {
                    String[] arr = (String[]) value;
                    if (null != arr && arr.length > 0) {
                        for (String str : arr) {
                            String pos = WordnetUtil.getPosValue(str);
                            if (StringUtils.isNotBlank(pos))
                                posList.add(pos.toLowerCase());
                            if (updatePosTags)
                                posTagList.add(str.toLowerCase());
                        }
                    }
                } else if (value instanceof String) {
                    if (StringUtils.isNotBlank(value.toString())) {
                        String pos = WordnetUtil.getPosValue(value.toString());
                        if (StringUtils.isNotBlank(pos))
                            posList.add(pos.toLowerCase());
                        if (updatePosTags)
                            posTagList.add(value.toString().toLowerCase());
                    }
                }
            }
            Object posCategories = node.getMetadata().get(ATTRIB_POS_CATEGORIES);
            if (null != posCategories) {
                if (posCategories instanceof String[]) {
                    String[] arr = (String[]) posCategories;
                    if (null != arr && arr.length > 0) {
                        for (String str : arr) {
                            String pos = WordnetUtil.getPosValue(str, false);
                            if (StringUtils.isNotBlank(pos))
                                posList.add(pos.toLowerCase());
                        }
                    }
                } else if (posCategories instanceof String) {
                    if (StringUtils.isNotBlank(posCategories.toString())) {
                        String pos = WordnetUtil.getPosValue(posCategories.toString(), false);
                        if (StringUtils.isNotBlank(pos))
                            posList.add(pos.toLowerCase());
                    }
                }
            }
            List<Relation> inRels = node.getInRelations();
            if (null != inRels && !inRels.isEmpty()) {
                for (Relation rel : inRels) {
                    if (StringUtils.equalsIgnoreCase(rel.getRelationType(), RelationTypes.SYNONYM.relationName())
                            && StringUtils.equalsIgnoreCase(rel.getStartNodeObjectType(), OBJECTTYPE_SYNSET)) {
                        Map<String, Object> metadata = rel.getStartNodeMetadata();
                        if (null != metadata && !metadata.isEmpty()) {
                            String pos = (String) metadata.get(ATTRIB_POS);
                            if (StringUtils.isNotBlank(pos) && !posList.contains(pos.toLowerCase()))
                                posList.add(pos.toLowerCase());
                        }
                    }
                }
            }
            if (!posList.isEmpty())
                node.getMetadata().put(ATTRIB_POS, new ArrayList<String>(posList));
            if (updatePosTags && !posTagList.isEmpty())
                node.getMetadata().put(ATTRIB_POS_TAGS, new ArrayList<String>(posTagList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void updateSyllables(Node node) {
    	String word=(String) node.getMetadata().get(ATTRIB_LEMMA);
    	List<String> syllables=wordUtil.buildSyllables("en", word);
    	node.getMetadata().put(ATTRIB_SYLLABLES,syllables);
    }
}
