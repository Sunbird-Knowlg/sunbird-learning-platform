package org.ekstep.language.util;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.ekstep.language.cache.GradeComplexityCache;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageObjectTypes;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.graph.model.node.MetadataDefinition;

// TODO: Auto-generated Javadoc
/**
 * The Class GradeLevelComplexityUtil, provides functionality to validate
 * complexity range and others
 *
 * @author karthik
 */
public class GradeLevelComplexityUtil extends BaseLanguageManager {

	/**
	 * The ordered grade level information stored in list from the range values
	 * defined in the definition of GradeLevelComplexity
	 */
	private List<String> orderedGradeLevel = null;

	/**
	 * The ordered language level. information stored in list from the range
	 * values defined in the definition of GradeLevelComplexity
	 */
	private List<String> orderedLanguageLevel = null;

	/** The language graph name. */
	private static String languageGraphName = "language";

	/**
	 * Validate new complexity of any GradeLevelComplexity is fallen within
	 * existing range as per its gradeLevel and languageLevel 
	 * 
	 * 		  First Second Third (Language Level) 
	 * Grade1 X1 	X2 		X3 
	 * Grade2 X4 	X5 		X6 
	 * Grade3 X7 	X8 		X9
	 *
	 * X1,X2,... represents AverageComplexity of that GradeLevelComplexity node
	 *
	 * X5 should be as below
	 *  1) X4 > X5 > X6 (X5 should be lesser than X4 and
	 * greater then X6)
	 *  2) X2 < X5 < X8 (X5 should be greater than X2 and lesser
	 * than X8)
	 *
	 * @param languageId
	 *            the language id
	 * @param gradeLevelComplexity
	 *            the grade level complexity
	 * @throws Exception
	 *             the exception
	 */
	public void validateComplexityRange(String languageId, Node gradeLevelComplexity) throws Exception {
		if (orderedGradeLevel == null || orderedLanguageLevel == null)
			loadGradeAndLanguageLevel();
		String higherGrade, lowerGrade, nextLanguageLevel, previousLanguageLevel;
		Double averageComplexity = (Double) gradeLevelComplexity.getMetadata().get("averageComplexity");
		String gradeLevel = (String) gradeLevelComplexity.getMetadata().get("gradeLevel");
		String languageLevel = (String) gradeLevelComplexity.getMetadata().get("languageLevel");
		higherGrade = nextHigher(gradeLevel, orderedGradeLevel);
		lowerGrade = nextLower(gradeLevel, orderedGradeLevel);
		nextLanguageLevel = nextHigher(languageLevel, orderedLanguageLevel);
		previousLanguageLevel = nextLower(languageLevel, orderedLanguageLevel);

		if (higherGrade != null) {
			Double higherGradeComplexity = GradeComplexityCache.getInstance().getGradeLevelComplexity(languageId,
					higherGrade, languageLevel);
			if (higherGradeComplexity != null && higherGradeComplexity < averageComplexity)
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_COMPLEXITY_RANGE.name(),
						"Complexity of given text(" + averageComplexity + ") is greater than its " + higherGrade
								+ " grader level complexity(" + higherGradeComplexity + ")");
		}

		if (lowerGrade != null) {
			Double lowerGradeComplexity = GradeComplexityCache.getInstance().getGradeLevelComplexity(languageId,
					lowerGrade, languageLevel);
			if (lowerGradeComplexity != null && lowerGradeComplexity > averageComplexity)
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_COMPLEXITY_RANGE.name(),
						"Complexity of given text(" + averageComplexity + ") is lesser than its " + lowerGrade
								+ " language level complexity(" + lowerGradeComplexity + ")");
		}

		if (nextLanguageLevel != null) {
			Double nextLanguageComplexity = GradeComplexityCache.getInstance().getGradeLevelComplexity(languageId,
					gradeLevel, nextLanguageLevel);
			if (nextLanguageComplexity != null && nextLanguageComplexity > averageComplexity)
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_COMPLEXITY_RANGE.name(),
						"Complexity of given text(" + averageComplexity + ") is lesser than its " + nextLanguageLevel
								+ " language level complexity(" + nextLanguageComplexity + ")");
		}

		if (previousLanguageLevel != null) {
			Double previousLanguageComplexity = GradeComplexityCache.getInstance().getGradeLevelComplexity(languageId,
					gradeLevel, previousLanguageLevel);
			if (previousLanguageComplexity != null && previousLanguageComplexity < averageComplexity)
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_COMPLEXITY_RANGE.name(),
						"Complexity of given text(" + averageComplexity + ") is greater than its "
								+ previousLanguageLevel + " language level complexity(" + previousLanguageComplexity
								+ ")");
		}

	}

	/**
	 * Load default grade and language level in order based on range values in
	 * definition of GradeLevelComplexity
	 *
	 * @throws Exception
	 *             the exception
	 */
	private void loadGradeAndLanguageLevel() throws Exception {
		DefinitionDTO wordComplexityDefinition = DefinitionDTOCache
				.getDefinitionDTO(LanguageObjectTypes.GradeLevelComplexity.name(), languageGraphName);
		if (wordComplexityDefinition == null)
			throw new ResourceNotFoundException(LanguageErrorCodes.ERR_DEFINITION_NOT_FOUND.name(),
					"Definition not found for " + LanguageObjectTypes.GradeLevelComplexity.name());
		List<MetadataDefinition> properties = wordComplexityDefinition.getProperties();
		for (MetadataDefinition property : properties) {
			if (property.getPropertyName().equalsIgnoreCase("gradeLevel")) {
				List<Object> gradeLevelRange = property.getRange();
				// convert List<Object> into List<String>
				orderedGradeLevel = gradeLevelRange.stream().map(object -> Objects.toString(object, null))
						.collect(Collectors.toList());
			}
			if (property.getPropertyName().equalsIgnoreCase("languageLevel")) {
				List<Object> languageLevelRange = property.getRange();
				// convert List<Object> into List<String>
				orderedLanguageLevel = languageLevelRange.stream().map(object -> Objects.toString(object, null))
						.collect(Collectors.toList());
			}
		}
	}

	/**
	 * get next higher from ordered list for the given property value
	 *
	 * @param propValue
	 *            the prop value
	 * @param orderedProp
	 *            the ordered prop
	 * @return the string
	 */
	private String nextHigher(String propValue, List<String> orderedProp) {
		if (orderedProp.contains(propValue)) {
			int index = orderedProp.indexOf(propValue);
			int next = index + 1;
			if (next < orderedProp.size())
				return orderedProp.get(next);
		}
		return null;
	}

	/**
	 * get next lower from order list for the given property value
	 *
	 * @param propValue
	 *            the prop value
	 * @param orderedProp
	 *            the ordered prop
	 * @return the string
	 */
	private String nextLower(String propValue, List<String> orderedProp) {
		if (orderedProp.contains(propValue)) {
			int index = orderedProp.indexOf(propValue);
			int previous = index - 1;
			if (previous >= 0)
				return orderedProp.get(previous);
		}
		return null;
	}
}
