package org.ekstep.language.measures;

public class VectorUtil {

	public static Integer[] addVector(Integer[] v1, Integer[] v2) {
		if (null != v1 && null != v2) {
			if (v1.length == v2.length) {
				for (int i = 0; i < v1.length; i++) {
					if (null == v1[i])
						v1[i] = 0;
					if (null == v2[i])
						v2[i] = 0;
					v1[i] = v1[i] + v2[i];
				}
				return v1;
			}
		}
		if ((null == v1 || v1.length == 0) && (null != v2 && v2.length > 0)) {
			return copy(v2);
		}
		return v1;
	}
	
	public static void incrementVectorValues() {
		
	}

	public static Double sum(Double[] vec) {
		if (null != vec && vec.length > 0) {
			double sum = 0.0;
			for (Double d : vec) {
				if (null != d)
					sum += d;
			}
			return sum;
		}
		return 0.0;
	}

	public static Double[] difference(Double[] v1, Double[] v2) {
		if (null != v1 && null != v2) {
			if (v1.length == v2.length) {
				Double[] arr = new Double[v1.length];
				for (int i = 0; i < v1.length; i++) {
					if (null == v1[i])
						v1[i] = 0.0;
					if (null == v2[i])
						v2[i] = 0.0;
					arr[i] = Math.abs(v1[i] - v2[i]);
				}
				return arr;
			}
		}
		if ((null == v1 || v1.length == 0) && (null != v2 && v2.length > 0))
			return copy(v2);
		return v1;
	}
	
	public static Double[] dotProduct(Double[] v1, Double[] v2) {
		if (null != v1 && null != v2) {
			if (v1.length == v2.length) {
				Double[] arr = new Double[v1.length];
				for (int i = 0; i < v1.length; i++) {
					if (null == v1[i])
						v1[i] = 0.0;
					if (null == v2[i])
						v2[i] = 0.0;
					arr[i] = v1[i] * v2[i];
				}
				return arr;
			}
		}
		if ((null == v1 || v1.length == 0) && (null != v2 && v2.length > 0))
			return copy(v2);
		return v1;
	}

	public static Double[] dotMatrix(Integer[] vec, Double[] weights) {
		if (null != vec && null != weights) {
			if (vec.length == weights.length) {
				Double[] arr = new Double[vec.length];
				for (int i = 0; i < vec.length; i++) {
					if (null == vec[i])
						vec[i] = 0;
					if (null == weights[i])
						weights[i] = 0.0;
					arr[i] = weights[i] * vec[i];
				}
				return arr;
			}
		}
		if ((null == vec || vec.length == 0) && (null != weights && weights.length > 0))
			return copy(weights);
		return weights;
	}
	
	public static Double[] dotMatrix(Integer[] vec, Double[] weights, int incr) {
		if (null != vec && null != weights) {
			if (vec.length == weights.length) {
				Double[] arr = new Double[vec.length];
				for (int i = 0; i < vec.length; i++) {
					if (null == vec[i])
						vec[i] = 0;
					if (null == weights[i])
						weights[i] = 0.0;
					arr[i] = weights[i] * vec[i];
					if (incr > 0) {
						arr[i] = arr[i] + (arr[i] * (incr/100));
					}
				}
				return arr;
			}
		}
		if ((null == vec || vec.length == 0) && (null != weights && weights.length > 0))
			return copy(weights);
		return weights;
	}

	public static Double dotProduct(Integer[] vec, Double[] weights) {
		double dotProduct = 0.0;
		if (null != vec && null != weights && vec.length == weights.length) {
			for (int i = 0; i < vec.length; i++) {
				dotProduct += vec[i] * weights[i];
			}
		}
		return dotProduct;
	}

	public static String getBinaryString(Integer[] vec) {
		String s = "";
		if (null != vec && vec.length > 0) {
			for (Integer i : vec) {
				s += (i + ":");
			}
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	private static Integer[] copy(Integer[] v) {
		Integer[] arr = new Integer[v.length];
		for (int i = 0; i < v.length; i++) {
			arr[i] = v[i];
		}
		return arr;
	}

	private static Double[] copy(Double[] v) {
		Double[] arr = new Double[v.length];
		for (int i = 0; i < v.length; i++) {
			arr[i] = v[i];
		}
		return arr;
	}

}
