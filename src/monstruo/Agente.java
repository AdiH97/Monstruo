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
	private boolean w[];

	protected enum Movimiento {
		NORTE, ESTE, SUD, OESTE
	}
	private Movimiento accion, accionp, accionpp;

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
		w = new boolean[6];
		accion = accionp = Movimiento.NORTE;
	}

	public void calcularAccion() {
		int posAgente = getY() * columnas + getX();

		if (w[Percepciones.GOLPE.ordinal()] || w[Percepciones.HEDOR.ordinal()]) {
			switch (accionp) {
				case NORTE:
					accion = Movimiento.ESTE;
					break;
				case ESTE:
					accion = Movimiento.SUD;
					break;
				case SUD:
					accion = Movimiento.OESTE;
					break;
				case OESTE:
					accion = Movimiento.NORTE;
					break;
			}
		} else { // Recorrer las casillas vacías
			if (getY() - 1 > 0 && mapa[posAgente - columnas] == null) {
				accion = Movimiento.NORTE;
			} else if (getX() + 1 < filas - 1 && mapa[posAgente + 1] == null) {
				accion = Movimiento.ESTE;
			} else if (getY() + 1 < columnas - 1 && mapa[posAgente + columnas] == null) {
				accion = Movimiento.SUD;
			} else if (getX() - 1 > 1 && mapa[posAgente - 1] == null) {
				accion = Movimiento.OESTE;
			}
		}
		accionpp = accionp;
		accionp = accion;
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

		
		for (int i = 0; i < mapa.length; i++) {
			g.setColor(new Color(0, 128, 255, 100));
			int casillax = (i % columnas) * atlas.getSubancho();
			int casillay = (i / columnas) * atlas.getSubalto();
			if (mapa[(i % columnas) + columnas * (i / columnas)] != null) {
				if (mapa[(i % columnas) + columnas * (i / columnas)][Percepciones.HEDOR.ordinal()]) {
					g.setColor(new Color(32, 128, 0, 100));
				}
				g.fillRect(casillax * escala, casillay * escala, atlas.getSubancho() * escala, atlas.getSubalto() * escala);
			}
		}

		if (w[Percepciones.GOLPE.ordinal()]) {
			int[][] offset = {{0, -16}, {16, 0}, {0, 16}, {-16, 0}};
			atlas.pintarTexturaEscala(g,
									  getX() * atlas.getSubancho() + offset[accionpp.ordinal()][0],
									  getY() * atlas.getSubalto() + offset[accionpp.ordinal()][1],
									  2, escala);
		}
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
		System.arraycopy(w, 0, this.w, 0, this.w.length);
		System.arraycopy(w, 0, mapa[posAgente], 0, this.w.length);
	}

	public Movimiento getAccion() {
		return accion;
	}

	public Movimiento getAccionp() {
		return accionp;
	}

}
