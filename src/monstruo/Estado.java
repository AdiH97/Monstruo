package monstruo;

public class Estado {

	public static final int VISITADA = 0;
	public static final int OK = 1;
	public static final int OK_MONSTRUO = 2;
	public static final int POSIBLE_MONSTRUO = 3;
	public static final int MONSTRUO = 4;
	public static final int OK_PRECIPICIO = 5;
	public static final int POSIBLE_PRECIPICIO = 6;
	public static final int PRECIPICIO = 7;
	public static final int HEDOR = 8;
	public static final int BRISA = 9;
	public static final int TESORO = 10;
	public static final int MURO = 11;
	public static final int DISPARADO_NORTE = 12;
	public static final int DISPARADO_ESTE = 13;
	public static final int DISPARADO_SUR = 14;
	public static final int DISPARADO_OESTE = 15;
	public static final int SIN_CONSUMIR = 16;
	public static final int NUM_ESTADOS = 17;

	private final boolean[] v;

	public Estado() {
		v = new boolean[NUM_ESTADOS];
	}

	public boolean get(int clave) {
		return v[clave];
	}

	public void set(int clave) {
		// VISITADA => OK
		if (clave == VISITADA) {
			v[VISITADA] = true;
			v[OK] = true;
			v[OK_MONSTRUO] = true;
			v[OK_PRECIPICIO] = true;
			v[POSIBLE_MONSTRUO] = false;
			v[MONSTRUO] = false;
			v[POSIBLE_PRECIPICIO] = false;
			v[PRECIPICIO] = false;
			v[SIN_CONSUMIR] = false;
		}
		// OK <=> OK_MONSTRUO && OK_PRECIPICIO
		if (clave == OK || (clave == OK_MONSTRUO && v[OK_PRECIPICIO]) || (clave == OK_PRECIPICIO && v[OK_MONSTRUO])) {
			v[OK] = true;
			v[OK_MONSTRUO] = true;
			v[OK_PRECIPICIO] = true;
			v[POSIBLE_MONSTRUO] = false;
			v[MONSTRUO] = false;
			v[POSIBLE_PRECIPICIO] = false;
			v[PRECIPICIO] = false;
		}
		// OK_MONSTRUO => ¬POSIBLE_MONSTRUO && ¬MONSTRUO
		if (clave == OK_MONSTRUO) {
			v[OK_MONSTRUO] = true;
			v[POSIBLE_MONSTRUO] = false;
			v[MONSTRUO] = false;
		}
		// OK_PRECIPICIO => ¬POSIBLE_PRECIPICIO && ¬PRECIPICIO
		if (clave == OK_PRECIPICIO) {
			v[OK_PRECIPICIO] = true;
			v[POSIBLE_PRECIPICIO] = false;
			v[PRECIPICIO] = false;
		}
		// PRECIPICIO => ¬POSIBLE_PRECIPICIO && ¬POSIBLE_MONSTRUO && ¬MONSTRUO
		if (clave == PRECIPICIO) {
			v[PRECIPICIO] = true;
			v[POSIBLE_PRECIPICIO] = false;
			v[POSIBLE_MONSTRUO] = false;
			v[MONSTRUO] = false;
		}
		// MONSTRUO => ¬POSIBLE_MONSTRUO && ¬POSIBLE_PRECIPICIO && ¬PRECIPICIO
		if (clave == MONSTRUO) {
			v[MONSTRUO] = true;
			v[POSIBLE_MONSTRUO] = false;
			v[POSIBLE_PRECIPICIO] = false;
			v[PRECIPICIO] = false;
		}
		// MURO => ¬VISITADA
		if (clave == MURO) {
			v[MURO] = true;
			v[VISITADA] = false;
			v[OK] = false;
			v[OK_MONSTRUO] = false;
			v[OK_PRECIPICIO] = false;
			v[POSIBLE_MONSTRUO] = false;
			v[MONSTRUO] = false;
			v[POSIBLE_PRECIPICIO] = false;
			v[PRECIPICIO] = false;
			v[SIN_CONSUMIR] = false;
		}
		v[clave] = true;
	}

	public void clear(int clave) {
		v[clave] = false;
	}
}
