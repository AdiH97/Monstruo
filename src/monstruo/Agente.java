package monstruo;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Stack;

public class Agente implements Ciclico {

	private static final int OPACIDAD_DEBUG = 192;
	private static final Color COLOR_MONSTRUO = new Color(255, 0, 0, OPACIDAD_DEBUG);
	private static final Color COLOR_POSIBLEMONSTRUO = new Color(255, 128, 0, OPACIDAD_DEBUG);
	private static final Color COLOR_POSIBLEPRECIPICIO = new Color(0, 255, 255, OPACIDAD_DEBUG);

	private int ciclos;
	private boolean verPercepciones;

	// gráficos //
	private final Atlas gAtlas;
	private final int gIndiceTextura;
	private int gX, gY;
	private int gDireccion;
	private boolean gPaso, gAlternaPaso;

	private final int ancho;
	private final int alto;

	private final Percepciones percepciones;
	private final Estado[][] mapa;

	private int num_proyectiles;
	private int num_tesoros_encontrados;
	private int max_tesoros;

	private final int STARTX, STARTY;

	// pila de movimientos inversos //
	private final Stack<Integer> pilaAcciones;

	private int accion, accionp, accionpp;

	public Agente(Atlas gAtlas, int gIndiceTextura, int ancho, int alto, int x, int y) {
		ciclos = 0;
		verPercepciones = false;
		this.gAtlas = gAtlas;
		this.gIndiceTextura = gIndiceTextura;
		gX = x * gAtlas.getSubancho();
		gY = y * gAtlas.getSubalto();
		gDireccion = 0;
		gPaso = gAlternaPaso = false;
		this.ancho = ancho;
		this.alto = alto;
		percepciones = new Percepciones();
		mapa = new Estado[ancho][alto];
		accion = accionp = Acciones.MOVERSE_NORTE;
		pilaAcciones = new Stack<>();
		num_proyectiles = 0;
		num_tesoros_encontrados = 0;
		max_tesoros = 0;
		STARTX = x;
		STARTY = y;
	}

	public boolean tesoros_encontrados() {
		return num_tesoros_encontrados == max_tesoros
				&& // apaño temporal //
				max_tesoros != 0;
	}

	public void calcularAccion() {
		// crear la casilla
		mapa[getX()][getY()] = new Estado();

		// Percepciones captadas del ambiente
		boolean entorno_hedor = percepciones.get(Percepciones.HEDOR);
		boolean entorno_brisa = percepciones.get(Percepciones.BRISA);
		boolean entorno_resplandor = percepciones.get(Percepciones.RESPLANDOR);
		boolean entorno_golpe = percepciones.get(Percepciones.GOLPE);

		// norte, este, sud, oeste [y, x]
		int offset[][] = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

		/**
		 * 1. INFERIR CASILLAS ENVOLVENTES CON LAS PERCEPCIONES ACTUALES
		 *
		 */
		// Accion que lleva hacia una pared
		// TODO: Usar el movimiento prohibido en lugar de comprobar las paredes
		int prohibido = Acciones.NINGUNA;

		// GOLPE
		if (entorno_golpe) {
			// Indicar al agente que no siga avanzando en la misma dirección
			prohibido = accionp;
		}

		// Por cada casilla que envuelve al agente
		for (int i = 0; i < offset.length; i++) {
			int casilla_y = getY() + offset[i][0];
			int casilla_x = getX() + offset[i][1];

			// HEDOR
			if (entorno_hedor) {
				// Si la casilla no ha sido visitada, posible monstruo
				if (mapa[casilla_x][casilla_y] == null) {
					// Y si la casilla no es una pared
					if (casilla_y != 0 && casilla_y != ancho - 1 && casilla_x != 0 && casilla_x != alto - 1) {
						// Indicar que hay un posible monstruo
						mapa[casilla_x][casilla_y] = new Estado();
						mapa[casilla_x][casilla_y].set(Estado.POSIBLE_MONSTRUO);
					}
				} else { // si no, monstruo "seguro"
					// Si en la casilla hay la percepción de un posible monstruo, es que antes se
					// había detectado hedor en
					// otra casilla que la envuelve. Por lo tanto, en esta casilla hay un monstruo.
					if (mapa[casilla_x][casilla_y].get(Estado.POSIBLE_MONSTRUO)) {
						mapa[casilla_x][casilla_y].set(Estado.MONSTRUO);
					}
				}
			} else {
				// Si no se ha percibido hedor en la casilla actual, evaluar las casillas con posibles monstruos
				if (mapa[casilla_x][casilla_y] != null) {
					// Si se he detectado un posible monstruo y no hay hedor en las casillas adyacentes, es seguro decir
					// que no hay monstruo
					if (mapa[casilla_x][casilla_y].get(Estado.POSIBLE_MONSTRUO)) {
						mapa[casilla_x][casilla_y] = null;
					}
				}
			}

			// BRISA (Igual que el monstruo)
			if (entorno_brisa) {
				if (mapa[casilla_x][casilla_y] == null) {
					if (casilla_y != 0 && casilla_y != ancho - 1 && casilla_x != 0 && casilla_x != alto - 1) {
						mapa[casilla_x][casilla_y] = new Estado();
						mapa[casilla_x][casilla_y].set(Estado.POSIBLE_PRECIPICIO);
					}
				} else {
					if (mapa[casilla_x][casilla_y].get(Estado.POSIBLE_PRECIPICIO)) {
						mapa[casilla_x][casilla_y].set(Estado.PRECIPICIO);
					}
				}
			} else {
				// Si no se ha percibido hedor en la casilla actual, evaluar las casillas con posibles monstruos
				if (mapa[casilla_x][casilla_y] != null) {
					// Si se he detectado un posible monstruo y no hay hedor en las casillas adyacentes, es seguro decir
					// que no hay monstruo
					if (mapa[casilla_x][casilla_y].get(Estado.POSIBLE_PRECIPICIO)) {
						mapa[casilla_x][casilla_y] = null;
					}
				}
			}
		}

		/**
		 * 2. REALIZAR ACCIÓN CON LA BC Y LAS PERCEPCIONES ACTUALES
		 *
		 */
		// Si se se ha encontrado el tesoro volver hacia atrás, ya que el camino de vuelta es seguro
		if (tesoros_encontrados()) {
			System.out.println("Todos los tesoros encontrados!");
			accion = pilaAcciones.pop();
		} else {
			int posible_accion = Acciones.NINGUNA;
			for (int i = 0; i < offset.length; i++) {
				int casilla_y = getY() + offset[i][0];
				int casilla_x = getX() + offset[i][1];

				// Comprobar que la casilla no sea una pared
				if (casilla_y != 0 && casilla_y != ancho - 1 && casilla_x != 0 && casilla_x != alto - 1) {
					if (mapa[casilla_x][casilla_y] == null) {
						// Ir hacia la casilla vacía
						posible_accion = i; // ya que el índice del offset coincide con el enum del movimiento correspondiente
					}
				}
			}

			// Ir hacia la casilla vacía
			if (posible_accion != Acciones.NINGUNA) {
				accion = posible_accion;
				// Guardar en la pila la acción contraria
				if (accion < 4) {
					pilaAcciones.push((accion + 2) % 4);
				}
			} else {
				// Intentamos disparar
				boolean disparado = false;
				for (int ii = 0; ii < 4; ii++) {
					// pos Monstruo y PosibleMonstruo
					int row = getY() + offset[ii][0];
					int col = getX() + offset[ii][1];

					// si no hemos disparado ya en esa dirección y hay un M. o P.M. allí pues
					// disparamos
					if (mapa[getY()][getX()] != null
							&& !mapa[getY()][getX()].get(Estado.DISPARADO_NORTE + ii)
							&& mapa[row][col] != null
							&& (mapa[row][col].get(Estado.MONSTRUO)
							|| mapa[row][col].get(Estado.POSIBLE_MONSTRUO))
							&& num_proyectiles > 0) {
						accion = 4 + ii;
						mapa[getY()][getX()].set(Estado.DISPARADO_NORTE + ii);
						disparado = true;
						num_proyectiles--;
						break;
					}
				}

				if (!disparado) {
					// Volver atrás
					if (!pilaAcciones.empty()) {
						accion = pilaAcciones.pop();
					} else {
						accion = Acciones.NINGUNA;
					}
				}
			}
		}

		// RESPLANDOR
		if (entorno_resplandor) {
			num_tesoros_encontrados++;
			// Apaño para que no tenga en cuenta el ciclo extra de quedarse quieto
			pilaAcciones.pop();
			accion = Acciones.RECOGER_TESORO;
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
					case Acciones.MOVERSE_NORTE:
					case Acciones.DISPARAR_NORTE:
						gDireccion = 1 * 512 / gAtlas.getSubancho();
						break;
					case Acciones.MOVERSE_ESTE:
					case Acciones.DISPARAR_ESTE:
						gDireccion = 2 * 512 / gAtlas.getSubancho();
						break;
					case Acciones.MOVERSE_SUR:
					case Acciones.DISPARAR_SUR:
						gDireccion = 0 * 512 / gAtlas.getSubancho();
						break;
					case Acciones.MOVERSE_OESTE:
					case Acciones.DISPARAR_OESTE:
						gDireccion = 3 * 512 / gAtlas.getSubancho();
						break;
				}
				gAlternaPaso = !gAlternaPaso;
				break;
			case 8:
			case 24:
				// animación
				gPaso = !gPaso;
				break;
		}
		// esto se hace siempre
		// animación
		switch (accion) {
			case Acciones.MOVERSE_NORTE:
				gY -= 1;
				break;
			case Acciones.MOVERSE_ESTE:
				gX += 1;
				break;
			case Acciones.MOVERSE_SUR:
				gY += 1;
				break;
			case Acciones.MOVERSE_OESTE:
				gX -= 1;
				break;
		}
		ciclos++;
	}

	public void pintar(Graphics g, int escala) {

		if (verPercepciones) {
			for (int i = 0; i < ancho; i++) {
				for (int j = 0; j < alto; j++) {
					g.setColor(new Color(0, 128, 255, OPACIDAD_DEBUG));
					if (mapa[i][j] != null) {
						if (mapa[i][j].get(Estado.MONSTRUO)) {
							g.setColor(COLOR_MONSTRUO);
							g.fillRect(i * gAtlas.getSubancho() * escala, j * gAtlas.getSubalto() * escala, gAtlas.getSubancho() * escala, gAtlas.getSubalto() * escala);
						} else if (mapa[i][j].get(Estado.POSIBLE_MONSTRUO)) {
							g.setColor(COLOR_POSIBLEMONSTRUO);
							g.fillRect(i * gAtlas.getSubancho() * escala, j * gAtlas.getSubalto() * escala, gAtlas.getSubancho() * escala, gAtlas.getSubalto() * escala);
						} else if (mapa[i][j].get(Estado.POSIBLE_PRECIPICIO)) {
							g.setColor(COLOR_POSIBLEPRECIPICIO);
							g.fillRect(i * gAtlas.getSubancho() * escala, j * gAtlas.getSubalto() * escala, gAtlas.getSubancho() * escala, gAtlas.getSubalto() * escala);
						} else if (mapa[i][j].get(Estado.PRECIPICIO)) {
							g.setColor(new Color(0, 0, 0, 255));
							g.fillRect(i * gAtlas.getSubancho() * escala, j * gAtlas.getSubalto() * escala, gAtlas.getSubancho() * escala, gAtlas.getSubalto() * escala);
						} else {
							g.setColor(new Color(0, 128, 255, OPACIDAD_DEBUG));
							g.fillRect(i * gAtlas.getSubancho() * escala, j * gAtlas.getSubalto() * escala, gAtlas.getSubancho() * escala, gAtlas.getSubalto() * escala);
						}
					}
				}
			}
		}

		int indice = gIndiceTextura + gDireccion + (gPaso ? 1 : 0) + (gPaso && gAlternaPaso ? 1 : 0);
		gAtlas.pintarTexturaEscala(g, gX, gY, indice, escala);
		if (percepciones.get(Percepciones.GOLPE)) {
			int[][] offset = {{0, -16}, {16, 0}, {0, 16}, {-16, 0}};
			gAtlas.pintarTexturaEscala(g, getX() * gAtlas.getSubancho() + offset[accionpp][0],
					getY() * gAtlas.getSubalto() + offset[accionpp][1], 2, escala);
		}
	}

	public int getX() {
		return (gX + (accion == Acciones.MOVERSE_OESTE ? 31 : 0)) / gAtlas.getSubancho();
	}

	public int getY() {
		return (gY + (accion == Acciones.MOVERSE_SUR ? 31 : 0)) / gAtlas.getSubalto();
	}

	public int getAccion() {
		return accion;
	}

	public int getAccionp() {
		return accionp;
	}

	public Percepciones getPercepciones() {
		return percepciones;
	}

	public Estado[][] getMapa() {
		return mapa;
	}

	public int getStartX() {
		return STARTX;
	}

	public int getStartY() {
		return STARTY;
	}

	public void setVerPercepciones(boolean b) {
		verPercepciones = b;
	}

	public void setMaxTesoros(int max) {
		max_tesoros = max;
	}
	
	public void setNumProyectiles(int proyectiles) {
		num_proyectiles = proyectiles;
	}
}
