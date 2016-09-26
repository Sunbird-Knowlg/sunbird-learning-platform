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

public class GradeLevelComplexityUtil extends BaseLanguageManager {

	private List<String> orderedGradeLevel = null;
	private List<String> orderedLanguageLevel = null;
	private static String languageGraphName = "language";

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

	private String nextHigher(String propValue, List<String> orderedProp) {
		if (orderedProp.contains(propValue)) {
			int index = orderedProp.indexOf(propValue);
			int next = index + 1;
			if (next < orderedProp.size())
				return orderedProp.get(next);
		}
		return null;
	}

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
