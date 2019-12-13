package monstruo;

public class Mapa<T> {

	private final T[][] mapa;

	private final int filas;
	private final int columnas;

	public Mapa(int filas, int columnas) {
		this.filas = filas;
		this.columnas = columnas;
		mapa = (T[][]) new Object[filas][columnas];
		for (int i = 0; i < filas; i++) {
			for (int j = 0; j < columnas; j++) {
				mapa[i][j] = null;
			}
		}
	}

	public T get(int fila, int columna) {
		if (fila >= 0 && fila < filas && columna >= 0 && columna < columnas) {
			return (T) mapa[fila][columna];
		} else {
			return null;
		}
	}

	public void set(int fila, int columna, T valor) {
		if (fila >= 0 && fila < filas && columna >= 0 && columna < columnas) {
			mapa[fila][columna] = valor;
		}
	}
}
