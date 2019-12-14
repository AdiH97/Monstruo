package monstruo;

public class Estado {

	public static final int POSIBLE_MONSTRUO = 0;
	public static final int MONSTRUO = 1;
	public static final int POSIBLE_PRECIPICIO = 2;
	public static final int PRECIPICIO = 3;
	public static final int DISPARADO_NORTE = 4;
	public static final int DISPARADO_ESTE = 5;
	public static final int DISPARADO_SUR = 6;
	public static final int DISPARADO_OESTE = 7;
	public static final int NUM_ESTADOS = 8;

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
