package monstruo;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Stack;

public class Agente implements Ciclico {

	private int ciclos;

	private final Atlas gfxAtlas;
	private int gfxX, gfxY;
	private final int gfxBaseTextura;
	private int gfxDireccion;
	private boolean gfxPaso, gfxAlternaPaso;

	private final int filas;
	private final int columnas;

	private final boolean[][][] mapa;
	private boolean w[];

	protected enum Percepcion {
		HEDOR, BRISA, RESPLANDOR, GOLPE, POSIBLE_MONSTRUO, MONSTRUO, POSIBLE_PRECIPICIO, PRECIPICIO, GEMIDO
	}

	// 
	// Pila de movimientos inversos
	private Stack<Movimiento> pila_mov;
	private static final int OPACIDAD_DEBUG = 192;
	private static final Color COLOR_MONSTRUO = new Color(255, 0, 0, OPACIDAD_DEBUG);
	private static final Color COLOR_POSIBLEMONSTRUO = new Color(255, 128, 0, OPACIDAD_DEBUG);

	protected enum Movimiento {
		NORTE, ESTE, SUD, OESTE
	}
	private Movimiento accion, accionp, accionpp;

	public Agente(Atlas atlas, int gfxBaseTextura, int filas, int columnas, int x, int y) {
		ciclos = 0;
		gfxAtlas = atlas;
		gfxX = x * atlas.getSubancho();
		gfxY = y * atlas.getSubalto();
		this.gfxBaseTextura = gfxBaseTextura;
		gfxDireccion = 0;
		gfxPaso = gfxAlternaPaso = false;
		this.filas = filas;
		this.columnas = columnas;

		mapa = new boolean[filas][columnas][];
		w = new boolean[Percepcion.values().length];
		accion = accionp = Movimiento.NORTE;
		pila_mov = new Stack();
	}

	public void calcularAccion() {

		int posPercepGolpe = Percepcion.GOLPE.ordinal();
		int posPercepMonstruo = Percepcion.MONSTRUO.ordinal();
		int posPercepPMonstruo = Percepcion.POSIBLE_MONSTRUO.ordinal();
		int posBrisa = Percepcion.BRISA.ordinal();
		int posResplandor = Percepcion.RESPLANDOR.ordinal();
		int posGemido = Percepcion.GEMIDO.ordinal();

		boolean hedor = w[Percepcion.HEDOR.ordinal()];
		boolean golpe = w[posPercepGolpe];
		boolean monstruo = mapa[getY()][getX()][posPercepMonstruo];
		boolean posibleMonstruo = w[posPercepPMonstruo];
		boolean brisa = w[posBrisa];
		boolean resplandor = w[posResplandor];
		boolean gemido = w[posGemido];

		// norte, este, sud, oeste [y, x]
		int offset[][] = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

		// Actualizar posición del monstruo y precipicios
		if (hedor) {
			for (int i = 0; i < offset.length; i++) {
				int casilla_y = getY() + offset[i][0];
				int casilla_x = getX() + offset[i][1];

				// Si la casilla no ha sido visitada, posible monstruo
				if (mapa[casilla_y][casilla_x] == null) {
					if (casilla_y != 0 && casilla_y != filas - 1 && casilla_x != 0 && casilla_x != columnas - 1) {
						// Indicar que hay un posible monstruo
						mapa[casilla_y][casilla_x] = new boolean[Percepcion.values().length];
						mapa[casilla_y][casilla_x][posPercepPMonstruo] = true;
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

		Movimiento prohibido = null;
		if (golpe) {
			prohibido = accionp;
		}

		/**
		 * *********
		 */
		if (mapa[getY() + offset[accion.ordinal()][0]][getX() + offset[accion.ordinal()][1]] != null
			&& (mapa[getY() + offset[accion.ordinal()][0]][getX() + offset[accion.ordinal()][1]][Percepcion.POSIBLE_MONSTRUO.ordinal()]
				|| mapa[getY() + offset[accion.ordinal()][0]][getX() + offset[accion.ordinal()][1]][Percepcion.MONSTRUO.ordinal()])) {
			accion = accionp;
			while (mapa[getY() + offset[accion.ordinal()][0]][getX() + offset[accion.ordinal()][1]] != null
				   && (mapa[getY() + offset[accion.ordinal()][0]][getX() + offset[accion.ordinal()][1]][Percepcion.POSIBLE_MONSTRUO.ordinal()]
					   || mapa[getY() + offset[accion.ordinal()][0]][getX() + offset[accion.ordinal()][1]][Percepcion.MONSTRUO.ordinal()])) {
				accion = Movimiento.values()[(accion.ordinal() + 1) % Movimiento.values().length];
			}
			System.err.println("Test");
		}
		/**
		 * ********
		 */

		if (hedor) {
			accion = pila_mov.pop();
			while (prohibido != null && prohibido == accion) {
				accion = pila_mov.pop();
			}
		} else {
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

		if (accion == prohibido) {
			while (prohibido != null && prohibido == accion) {
				accion = Movimiento.values()[(accion.ordinal() + 1) % Movimiento.values().length];
			}
		}

		boolean alguna_no_visitada = false;
		for (int i = 0; i < offset.length; i++) {
			if (mapa[getY() + offset[i][0]][getX() + offset[i][1]] == null) {
				alguna_no_visitada = true;
				break;
			}
		}

		if (!alguna_no_visitada) {
			accion = pila_mov.pop();
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
						gfxDireccion = 1 * 512 / gfxAtlas.getSubancho();
						break;
					case ESTE:
						gfxDireccion = 2 * 512 / gfxAtlas.getSubancho();
						break;
					case SUD:
						gfxDireccion = 0 * 512 / gfxAtlas.getSubancho();
						break;
					case OESTE:
						gfxDireccion = 3 * 512 / gfxAtlas.getSubancho();
						break;
				}
				gfxAlternaPaso = !gfxAlternaPaso;
				break;
			case 8:
			case 24:
				// animación
				gfxPaso = !gfxPaso;
				break;
		}
		// esto se hace siempre
		// animación
		switch (accion) {
			case NORTE:
				gfxY -= 1;
				break;
			case ESTE:
				gfxX += 1;
				break;
			case SUD:
				gfxY += 1;
				break;
			case OESTE:
				gfxX -= 1;
				break;
		}
		// System.out.println(x + " " + y);
		ciclos++;
	}

	public void pintar(Graphics g, int escala) {

		for (int i = 0; i < mapa.length; i++) {
			for (int j = 0; j < mapa[0].length; j++) {
				g.setColor(new Color(0, 128, 255, OPACIDAD_DEBUG));
				if (mapa[i][j] != null) {
					if (mapa[i][j][Percepcion.HEDOR.ordinal()]) {
						g.setColor(new Color(32, 128, 0, OPACIDAD_DEBUG));
						g.fillRect(j * gfxAtlas.getSubancho() * escala, i * gfxAtlas.getSubalto() * escala, gfxAtlas.getSubancho() * escala, gfxAtlas.getSubalto() * escala);
					} else if (mapa[i][j][Percepcion.MONSTRUO.ordinal()]) {
						g.setColor(COLOR_MONSTRUO);
						g.fillRect(j * gfxAtlas.getSubancho() * escala, i * gfxAtlas.getSubalto() * escala, gfxAtlas.getSubancho() * escala, gfxAtlas.getSubalto() * escala);
					} else if (mapa[i][j][Percepcion.POSIBLE_MONSTRUO.ordinal()]) {
						g.setColor(COLOR_POSIBLEMONSTRUO);
						g.fillRect(j * gfxAtlas.getSubancho() * escala, i * gfxAtlas.getSubalto() * escala, gfxAtlas.getSubancho() * escala, gfxAtlas.getSubalto() * escala);
					} else {
						g.setColor(new Color(0, 128, 255, OPACIDAD_DEBUG));
						g.fillRect(j * gfxAtlas.getSubancho() * escala, i * gfxAtlas.getSubalto() * escala, gfxAtlas.getSubancho() * escala, gfxAtlas.getSubalto() * escala);
					}
				}
			}
		}

		int indice = gfxBaseTextura + gfxDireccion + (gfxPaso ? 1 : 0) + (gfxPaso && gfxAlternaPaso ? 1 : 0);
		gfxAtlas.pintarTexturaEscala(g, gfxX, gfxY, indice, escala);
		if (w[Percepcion.GOLPE.ordinal()]) {
			int[][] offset = {{0, -16}, {16, 0}, {0, 16}, {-16, 0}};
			gfxAtlas.pintarTexturaEscala(g, getX() * gfxAtlas.getSubancho() + offset[accionpp.ordinal()][0],
										 getY() * gfxAtlas.getSubalto() + offset[accionpp.ordinal()][1], 2, escala);
		}
	}

	public int getX() {
		return (gfxX + (accion == Movimiento.OESTE ? 31 : 0)) / gfxAtlas.getSubancho();
	}

	public int getY() {
		return (gfxY + (accion == Movimiento.NORTE ? 31 : 0)) / gfxAtlas.getSubalto();
	}

	public void setW(boolean[] w) {
		if (mapa[getY()][getX()] == null) {
			mapa[getY()][getX()] = new boolean[Percepcion.values().length];
		}

		for (int i = 0; i < w.length; i++) {
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
