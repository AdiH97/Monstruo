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

		// Posiciones de las percepciones
		int posPercepGolpe = Percepcion.GOLPE.ordinal();
		int posPercepMonstruo = Percepcion.MONSTRUO.ordinal();
		int posPercepPMonstruo = Percepcion.POSIBLE_MONSTRUO.ordinal();
		int posBrisa = Percepcion.BRISA.ordinal();
		int posResplandor = Percepcion.RESPLANDOR.ordinal();
		int posGemido = Percepcion.GEMIDO.ordinal();
		int posHedor = Percepcion.HEDOR.ordinal();

		// Percepciones captadas del ambiente
		boolean entorno_hedor = w[Percepcion.HEDOR.ordinal()];
		boolean entorno_golpe = w[posPercepGolpe];
		boolean entorno_brisa = w[posBrisa];
		boolean entorno_resplandor = w[posResplandor];
		boolean entorno_gemido = w[posGemido];

		// norte, este, sud, oeste [y, x]
		int offset[][] = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

		/**
		 * 1. INFERIR CASILLAS ENVOLVENTES CON LAS PERCEPCIONES ACTUALES
		 *
		 */
		// Movimiento que lleva hacia una pared
		// TODO: Usar el movimiento prohibido en lugar de comprobar las paredes
		Movimiento prohibido = null;

		// Por cada casilla que envuelve al agente
		for (int i = 0; i < offset.length; i++) {
			int casilla_y = getY() + offset[i][0];
			int casilla_x = getX() + offset[i][1];

			// GOLPE
			if (entorno_golpe) {
				// Indicar al agente que no siga avanzando en la misma dirección
				prohibido = accionp;
			}
			
			// HEDOR
			if (entorno_hedor) {
				// Si la casilla no ha sido visitada, posible monstruo
				if (mapa[casilla_y][casilla_x] == null) {
					// Y si la casilla no es una pared
					if (casilla_y != 0 && casilla_y != filas - 1 && casilla_x != 0 && casilla_x != columnas - 1) {
						// Indicar que hay un posible monstruo
						mapa[casilla_y][casilla_x] = new boolean[Percepcion.values().length];
						mapa[casilla_y][casilla_x][posPercepPMonstruo] = true;
					}
				} else { // si no, monstruo "seguro"
					// Si en la casilla hay la percepción de un posible monstruo, es que antes se
					// había detectado hedor en
					// otra casilla que la envuelve. Por lo tanto, en esta casilla hay un monstruo.
					if (mapa[casilla_y][casilla_x][posPercepPMonstruo] == true) {
						mapa[casilla_y][casilla_x][posPercepMonstruo] = true;
					}
				}
			} else {
				// Si no se ha percibido hedor en la casilla actual, evaluar las casillas con posibles monstruos
				if (mapa[casilla_y][casilla_x] != null) {
					// Si se he detectado un posible monstruo y no hay hedor en las casillas adyacentes, es seguro decir
					// que no hay monstruo
					mapa[casilla_y][casilla_x][posPercepPMonstruo] = false;
				}
			}

			// BRISA
			if (entorno_brisa) {
				// Marcar casilla como brisa
				// Intentar inferir la posición del precipicio
				// Si no se ha podido, marcar las posibles posiciones del percipicio
			}

			// RESPLANDOR
			if (entorno_resplandor) {

			}
		}

		/**
		 * 2. REALIZAR ACCIÓN CON LA BC Y LAS PERCEPCIONES ACTUALES
		 *
		 */
		Movimiento posible_accion = null;
		for (int i = 0; i < offset.length; i++) {
			int casilla_y = getY() + offset[i][0];
			int casilla_x = getX() + offset[i][1];

			// Comprobar que la casilla no sea una pared
			if (casilla_y != 0 && casilla_y != filas - 1 && casilla_x != 0 && casilla_x != columnas - 1) {
				if (mapa[casilla_y][casilla_x] == null) {
					// Ir hacia la casilla vacía
					posible_accion = Movimiento.values()[i];
				}
			}
		}

		// Ir hacia la casilla vacía
		if (posible_accion != null) {
			accion = posible_accion;
			// Guardar en la pila la acción contraria
			pila_mov.push(Movimiento.values()[(accion.ordinal() + 2) % Movimiento.values().length]);
		} else {
			// Volver atrás
			accion = pila_mov.pop();
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
			default:
				gfxY = 0;
				gfxX = 0;
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
