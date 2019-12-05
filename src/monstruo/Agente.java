package monstruo;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.Stack;
import monstruo.Entorno.Percepciones;

public class Agente implements Ciclico {

	private int ciclos;
	private final Atlas atlas;
	private int x, y;
	private int direccion;
	private boolean piernaAire, alternaPierna;
	private final int filas, columnas;

	private final boolean[][][] mapa;
	private boolean w[];
	
	// Pila de movimientos inversos
	private Stack pila_mov;

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
		mapa = new boolean[filas][columnas][];
		w = new boolean[Percepciones.values().length];
		accion = accionp = Movimiento.NORTE;
		pila_mov = new Stack();
	}

	public void calcularAccion() {

		boolean hedor = w[Percepciones.HEDOR.ordinal()];
		int posPercepGolpe = Percepciones.GOLPE.ordinal();
		int posPercepMonstruo = Percepciones.MONSTRUO.ordinal();
		int posPercepPMonstruo = Percepciones.POSIBLE_MONSTRUO.ordinal();

		// norte, este, sud, oeste [y, x] 
		int offset[][] = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
		
		if (w[Percepciones.GOLPE.ordinal()]) {
			// Si hay un golpe, elegir la acción a realizar en base de otras percepciones
			for(int i = 0; i < offset.length; i++) {
			}
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
		} 
		else if (hedor) {
			for (int i = 0; i < offset.length; i++) {
				int casilla_y = getY() + offset[i][0];
				int casilla_x = getX() + offset[i][1];

				// Si la casilla no ha sido visitada, posible monstruo
				if (mapa[casilla_y][casilla_x] == null) {
					if (casilla_y != 0 && casilla_y != filas - 1 && casilla_x != 0 && casilla_x != columnas - 1) {
						// Indicar que hay un posible monstruo
						mapa[casilla_y][casilla_x] = new boolean[Percepciones.values().length];
						mapa[casilla_y][casilla_x][posPercepPMonstruo] = true;
						
						// Realizar acción anterior
						accion = (Movimiento)pila_mov.pop();
					}
				} else { // si no, monstruo "seguro"
					// Si en la casilla hay la percepción de un posible monstruo, es que antes se había detectado hedor en
					// otra casilla que la envuelve. Por lo tanto, en esta casilla hay un monstruo.
					if (mapa[casilla_y][casilla_x][posPercepPMonstruo] == true) {
						mapa[casilla_y][casilla_x][posPercepMonstruo] = true;
					}
				}
			}
		}
		else {
			// Recorrer las casillas vacías
			if (getY() - 1 > 0 && mapa[getY() - 1][getX()] == null) {
				accion = Movimiento.NORTE;
			} else if (getX() + 1 < filas - 1 && mapa[getY()][getX() + 1] == null) {
				accion = Movimiento.ESTE;
			} else if (getY() + 1 < columnas - 1 && mapa[getY() + 1][getX()] == null) {
				accion = Movimiento.SUD;
			} else if (getX() - 1 > 0 && mapa[getY()][getX() - 1] == null) {
				accion = Movimiento.OESTE;
			}
		}
		
		// Guardar en la pila la acción contraria
		pila_mov.push(Movimiento.values()[(accion.ordinal() + 2) % Movimiento.values().length]);

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
			for (int j = 0; j < mapa[0].length; j++) {
				g.setColor(new Color(0, 128, 255, 100));
				if (mapa[i][j] != null) {
					if (mapa[i][j][Percepciones.HEDOR.ordinal()]) {
						g.setColor(new Color(32, 128, 0, 100));
						g.fillRect(j * atlas.getSubancho() * escala, i * atlas.getSubalto() * escala, atlas.getSubancho() * escala, atlas.getSubalto() * escala);
					} else if (mapa[i][j][Percepciones.MONSTRUO.ordinal()]) {
						g.setColor(new Color(148, 6, 156, 100));
						g.fillRect(j * atlas.getSubancho() * escala, i * atlas.getSubalto() * escala, atlas.getSubancho() * escala, atlas.getSubalto() * escala);
					} else if (mapa[i][j][Percepciones.POSIBLE_MONSTRUO.ordinal()]) {
						g.setColor(new Color(255, 0, 0, 100));
						g.fillRect(j * atlas.getSubancho() * escala, i * atlas.getSubalto() * escala, atlas.getSubancho() * escala, atlas.getSubalto() * escala);
					} else {
						g.setColor(new Color(0, 128, 255, 100));
						g.fillRect(j * atlas.getSubancho() * escala, i * atlas.getSubalto() * escala, atlas.getSubancho() * escala, atlas.getSubalto() * escala);
					}
				}
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
		if(mapa[getY()][getX()] == null) {
			mapa[getY()][getX()] = new boolean[Percepciones.values().length];
		}
		
		for(int i = 0; i < w.length; i++) {
			this.w[i] = w[i];
			mapa[getY()][getX()][i] = mapa[getY()][getX()][i] || w[i];
		}
	}

	public Movimiento getAccion() {
		return accion;
	}

	public Movimiento getAccionp() {
		return accionp;
	}

}
