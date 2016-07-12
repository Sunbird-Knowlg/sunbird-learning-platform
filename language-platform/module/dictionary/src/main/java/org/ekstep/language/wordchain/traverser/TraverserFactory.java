package org.ekstep.language.wordchain.traverser;


public class TraverserFactory {

	@SuppressWarnings("unchecked")
	public static ITraverser getTraverser(String traverserClass) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<ITraverser> clazz = (Class<ITraverser>) Class.forName(traverserClass);
		ITraverser traverser= clazz.newInstance();
		return traverser;
	}
}
