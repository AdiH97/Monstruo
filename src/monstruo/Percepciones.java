package monstruo;

public class Percepciones {

	public static final int HEDOR = 0;
	public static final int BRISA = 1;
	public static final int RESPLANDOR = 2;
	public static final int GOLPE = 3;
	public static final int GEMIDO = 4;
	public static final int NUM_PERCEPCIONES = 5;

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
