package org.ekstep.language.wordchain.traverser;

public class TraverserFactory {

	public static ITraverserInterface getTraverser(int ruleId) {
		switch (ruleId) {
		case 1:
			return new AksharaTraverser();
		case 2:
			return new ConsonantTraverser();
		case 3:
			return new RhymingWordsTraverser();
		default:
			return null;
		}
	}
}
