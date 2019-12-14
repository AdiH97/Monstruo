package monstruo;

public class Estado {

	public static final int POSIBLE_MONSTRUO = 0;
	public static final int MONSTRUO = 1;
	public static final int OK_MONSTRUO = 2;
	public static final int POSIBLE_PRECIPICIO = 3;
	public static final int PRECIPICIO = 4;
	public static final int OK_PRECIPICIO = 5;
	public static final int TESORO = 6;
	public static final int MURO = 7;
	public static final int DISPARADO_NORTE = 8;
	public static final int DISPARADO_ESTE = 9;
	public static final int DISPARADO_SUR = 10;
	public static final int DISPARADO_OESTE = 11;
	public static final int OK = 12;
	public static final int VISITADA = 13;
	public static final int HEDOR = 14;
	public static final int BRISA = 15;
	public static final int NUM_ESTADOS = 16;

	private final boolean[] vector;

	public Estado() {
		vector = new boolean[NUM_ESTADOS];
	}

	public boolean get(int clave) {
		return vector[clave];
	}

	public void set(int clave) {
		vector[clave] = true;
	}

	public void clear(int clave) {
		vector[clave] = false;
	}
}
