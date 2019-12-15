package monstruo;

public class Percepciones {

	public static final int HEDOR = 0;
	public static final int HEDOR_FALSO = 1;
	public static final int BRISA = 2;
	public static final int RESPLANDOR = 3;
	public static final int GOLPE = 4;
	public static final int GEMIDO = 5;
	public static final int NUM_PERCEPCIONES = 6;

	private final boolean[] vector;

	public Percepciones() {
		vector = new boolean[NUM_PERCEPCIONES];
	}

	public boolean get(int clave) {
		return vector[clave];
	}

	public void set(int clave, boolean valor) {
		vector[clave] = valor;
	}
}
