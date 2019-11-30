package monstruo;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import monstruo.Entorno.Percepciones;

public class Agente implements Ciclico {

	private int ciclos;
	private final Atlas atlas;
	private int x, y;
	private int direccion;
	private boolean piernaAire, alternaPierna;
	private final int filas, columnas;

	private final boolean[][] mapa;

	protected enum Movimiento {
		NORTE, ESTE, SUD, OESTE
	}
	private Movimiento accion, accionp;

	public Agente(Atlas atlas, int filas, int columnas, int X, int Y) {
		this.atlas = atlas;
		x = X * atlas.getSubancho();
		y = Y * atlas.getSubalto();
		ciclos = 0;
		direccion = 3;
		piernaAire = alternaPierna = false;
		this.filas = filas;
		this.columnas = columnas;
		mapa = new boolean[filas * columnas][];
		accion = Movimiento.NORTE;
	}

	public void calcularAccion() {
	}

	@Override
	public void ciclo() {
		switch (ciclos % 32) {
			case 0: // un ciclo propiamente dicho en el entorno, el resto de casos son intraciclo
				// animación
				switch (accion) {
					case NORTE:
						direccion = 6;
						break;
					case ESTE:
						direccion = 9;
						break;
					case SUD:
						direccion = 3;
						break;
					case OESTE:
						direccion = 12;
						break;
				}
				alternaPierna = !alternaPierna;
				break;
			case 8:
			case 24:
				// animación
				piernaAire = !piernaAire;
				break;
		}
		// esto se hace siempre
		// animación
		switch (accion) {
			case NORTE:
				y -= 1;
				break;
			case ESTE:
				x += 1;
				break;
			case SUD:
				y += 1;
				break;
			case OESTE:
				x -= 1;
				break;
		}
		//System.out.println(x + " " + y);
		ciclos++;
	}

	public void pintar(Graphics g, int escala) {
		int indice = direccion + (piernaAire ? 1 : 0) + (piernaAire && alternaPierna ? 1 : 0);
		atlas.pintarTexturaEscala(g, x, y, indice, escala);
	}

	public int getX() {
		return (x + (accion == Movimiento.OESTE ? 31 : 0)) / atlas.getSubancho();
	}

	public int getY() {
		return (y + (accion == Movimiento.NORTE ? 31 : 0)) / atlas.getSubalto();
	}

	public void setW(boolean[] w) {
		int posAgente = getY() * columnas + getX();
		mapa[posAgente] = new boolean[w.length];
		System.arraycopy(w, 0, mapa[posAgente], 0, mapa[posAgente].length);
	}

	public Movimiento getAccion() {
		return accion;
	}
	
}
