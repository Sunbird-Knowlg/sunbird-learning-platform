package org.ekstep.language.wordchain.traverser;

public class TraverserFactory {

	public static ITraverserInterface getTraverser(String ruleId) {
		switch (ruleId) {
		case "rule_1":
			return new AksharaTraverser();
		case "rule_2":
			return new RhymingWordsTraverser();
		default:
			return null;
		}
	}
}
