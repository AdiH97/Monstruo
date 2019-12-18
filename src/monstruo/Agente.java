package monstruo;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;
import java.util.Stack;

public class Agente implements Ciclico {

	int X_HOFFSET[][][] = {
		{{+1, 0}, {+1, -1}, {-1, 0}},
		{{+1, +2}, {+1, +1}, {-1, +2}},
		{{-1, 0}, {-1, +1}, {+1, 0}},
		{{-1, -2}, {-1, -1}, {-1, -2}}
	};

	int Y_HOFFSET[][][] = {
		{{-1, -2}, {-1, -1}, {-1, -2}},
		{{+1, 0}, {+1, -1}, {-1, 0}},
		{{+1, +2}, {+1, +1}, {+1, +2}},
		{{-1, 0}, {-1, +1}, {+1, 0}}
	};

	private static final int OPACIDAD_DEBUG = 192;
	private static final Color COLOR_MONSTRUO = new Color(255, 0, 0, OPACIDAD_DEBUG);
	private static final Color COLOR_POSIBLEMONSTRUO = new Color(255, 128, 0, OPACIDAD_DEBUG);
	private static final Color COLOR_POSIBLEPRECIPICIO = new Color(0, 255, 255, OPACIDAD_DEBUG);

	private int ciclos;
	private boolean verPercepciones;

	// gráficos //
	private final Atlas gAtlas;
	private final int gIndiceTextura;
	protected int gX, gY;
	private int gDireccion;
	private boolean gPaso, gAlternaPaso;

	private final int ancho;
	private final int alto;

	private final Percepciones percepciones;
	private final Estado[][] mapa;

	private int num_proyectiles;
	private int num_tesoros_encontrados;

	private final int STARTX, STARTY;

	// pila de movimientos inversos //
	private final Stack<Integer> pilaAcciones;
	// contador de casillas por consumir //
	int sinConsumir;

	private int X, Y;

	private int accion, accionp, accionpp;

	private static final int MAX_BOMBAS = 3;
	private static final int MIN_CICLOS = 4;
	private static final int MAX_CICLOS = 8;
	private int bombas_restantes;
	private int ciclos_restantes;

	private Random r;

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
		for (int i = 0; i < ancho; i++) {
			for (int j = 0; j < alto; j++) {
				mapa[i][j] = new Estado();
			}
		}
		accionp = Acciones.NINGUNA;
		accion = Acciones.DESPLAZARSE_NORTE;
		pilaAcciones = new Stack<>();
		num_proyectiles = 0;
		num_tesoros_encontrados = 0;
		STARTX = x;
		STARTY = y;
		X = x;
		Y = y;
		sinConsumir = 1;
		bombas_restantes = MAX_BOMBAS;
		r = new Random();
		ciclos_restantes = r.nextInt(MAX_CICLOS) + MIN_CICLOS;
	}

	private void set(int x, int y, int clave) {
		mapa[x][y].set(clave);
	}

	private void clear(int x, int y, int clave) {
		mapa[x][y].clear(clave);
	}

	private boolean get(int x, int y, int clave) {
		return mapa[x][y].get(clave);
	}

	public void calcularAccion() {

		final int[] X_OFFSET = {0, 1, 0, -1};
		final int[] Y_OFFSET = {-1, 0, 1, 0};
		X = getX();
		Y = getY();
		Percepciones p = getPercepciones();
		boolean H = p.get(Percepciones.HEDOR);
		boolean B = p.get(Percepciones.BRISA);
		boolean R = p.get(Percepciones.RESPLANDOR);
		boolean G = p.get(Percepciones.GOLPE);
		boolean S = p.get(Percepciones.GEMIDO);
		accion = Acciones.NINGUNA;

		if (!get(X, Y, Estado.VISITADA)) {
			sinConsumir--;
		}
		set(X, Y, Estado.VISITADA);

		// *************** //
		// PARTE DEDUCTIVA //
		// *************** //
		// 
		// Si se ha oído un grito en la dirección en que se ha disparado, podemos asegurar que el monstruo en la
		// casilla adyacente no hay monstruo
		if (S) {
			switch (accionp) {
				case Acciones.DISPARAR_NORTE:
					set(X, Y - 1, Estado.OK_MONSTRUO);
					break;
				case Acciones.DISPARAR_ESTE:
					set(X + 1, Y, Estado.OK_MONSTRUO);
					break;
				case Acciones.DISPARAR_SUR:
					set(X, Y + 1, Estado.OK_MONSTRUO);
					break;
				case Acciones.DISPARAR_OESTE:
					set(X - 1, Y, Estado.OK_MONSTRUO);
					break;
			}
		} else {
			// Si se ha disparado un bala y no se ha oido un grito, en toda la fila/columna no hay monstruo
			switch (accionp) {
				case Acciones.DISPARAR_NORTE:
					for (int i = Y - 1; i > 0; i--) {
						set(X, i, Estado.OK_MONSTRUO);
					}
					break;
				case Acciones.DISPARAR_ESTE:
					for (int i = X + 1; i < ancho - 1; i++) {
						set(i, Y, Estado.OK_MONSTRUO);
					}
					break;
				case Acciones.DISPARAR_SUR:
					for (int i = Y + 1; i < alto - 1; i++) {
						set(X, i, Estado.OK_MONSTRUO);
					}
					break;
				case Acciones.DISPARAR_OESTE:
					for (int i = X - 1; i > 0; i--) {
						set(i, Y, Estado.OK_MONSTRUO);
					}
					break;
			}
		}

		// Ri,j => Ti,j
		if (R) {
			set(X, Y, Estado.TESORO);
		}

		// ¬Ri,j => ¬Ti,j
		if (!R) {
			clear(X, Y, Estado.TESORO);
		}

		if (G) {
			pilaAcciones.pop();
		}

		// Gi,j && At-1 = NORTE => M0,j-1 && M1,j-1 … Mn,j-1
		if (G && accionp == Acciones.DESPLAZARSE_NORTE) {
			for (int j = 0; j < ancho; j++) {
				if (get(j, Y - 1, Estado.SIN_CONSUMIR)) {
					sinConsumir--;
				}
				set(j, Y - 1, Estado.MURO);
			}
		}

		// Gi,j && At-1 = ESTE => Mi+1,0 && Mi+1,1 … Mi+1,m
		if (G && accionp == Acciones.DESPLAZARSE_ESTE) {
			for (int j = 0; j < alto; j++) {
				if (get(X + 1, j, Estado.SIN_CONSUMIR)) {
					sinConsumir--;
				}
				set(X + 1, j, Estado.MURO);
			}
		}

		// Gi,j && At-1 = SUR => M0,j+1 && M1,j+1 … Mn,j+1
		if (G && accionp == Acciones.DESPLAZARSE_SUR) {
			for (int j = 0; j < ancho; j++) {
				if (get(j, Y + 1, Estado.SIN_CONSUMIR)) {
					sinConsumir--;
				}
				set(j, Y + 1, Estado.MURO);
			}
		}

		// Gi,j && At-1 = OESTE => Mi-1,0 && Mi-1,1 … Mi-1,m
		if (G && accionp == Acciones.DESPLAZARSE_OESTE) {
			for (int j = 0; j < alto; j++) {
				if (get(X - 1, j, Estado.SIN_CONSUMIR)) {
					sinConsumir--;
				}
				set(X - 1, j, Estado.MURO);
			}
		}

		// H => Hi,j
		if (H) {
			set(X, Y, Estado.HEDOR);
		}

		for (int i = 0; i < 4; i++) {
			int XX = X + X_OFFSET[i];
			int YY = Y + Y_OFFSET[i];
			if (H && !get(XX, YY, Estado.MURO) && !get(XX, YY, Estado.PRECIPICIO) && !get(XX, YY, Estado.VISITADA) && !get(XX, YY, Estado.OK_MONSTRUO)) {
				set(XX, YY, Estado.POSIBLE_MONSTRUO);
			}
		}

		// INFERIR W (CONSULTAR DOCUMENTACIÓN)
		if (H) {
			for (int i = 0; i < 4; i++) {
				int XX = X + X_OFFSET[i];
				int YY = Y + Y_OFFSET[i];
				if (get(XX, YY, Estado.POSIBLE_MONSTRUO)) {
					for (int j = 0; j < 3; j++) {
						int HX1 = X + X_HOFFSET[i][j][0];
						int HY1 = Y + Y_HOFFSET[i][j][0];
						int HX2 = X + X_HOFFSET[i][j][1];
						int HY2 = Y + Y_HOFFSET[i][j][1];
						if (HX1 > 0 && HX1 < ancho - 1
							&& HX2 > 0 && HX2 < ancho - 1
							&& HY1 > 0 && HY1 < alto - 1
							&& HY2 > 0 && HY2 < alto - 1
							&& get(HX1, HY1, Estado.HEDOR)
							&& get(HX2, HY2, Estado.HEDOR)) {
							set(XX, YY, Estado.MONSTRUO);
						}
					}
				}
			}
		}

		// B => Bi,j
		if (B) {
			set(X, Y, Estado.BRISA);
		}

		for (int i = 0; i < 4; i++) {
			int XX = X + X_OFFSET[i];
			int YY = Y + Y_OFFSET[i];
			if (B && !get(XX, YY, Estado.MURO) && !get(XX, YY, Estado.MONSTRUO) && !get(XX, YY, Estado.VISITADA) && !get(XX, YY, Estado.OK_PRECIPICIO)) {
				set(XX, YY, Estado.POSIBLE_PRECIPICIO);
			}
		}

		// INFERIR B (CONSULTAR DOCUMENTACIÓN)
		if (B) {
			for (int i = 0; i < 4; i++) {
				int XX = X + X_OFFSET[i];
				int YY = Y + Y_OFFSET[i];
				if (get(XX, YY, Estado.POSIBLE_PRECIPICIO)) {
					for (int j = 0; j < 3; j++) {
						int BX1 = X + X_HOFFSET[i][j][0];
						int BY1 = Y + Y_HOFFSET[i][j][0];
						int BX2 = X + X_HOFFSET[i][j][1];
						int BY2 = Y + Y_HOFFSET[i][j][1];
						if (BX1 > 0 && BX1 < ancho - 1
							&& BX2 > 0 && BX2 < ancho - 1
							&& BY1 > 0 && BY1 < alto - 1
							&& BY2 > 0 && BY2 < alto - 1
							&& get(BX1, BY1, Estado.BRISA) && get(BX2, BY2, Estado.BRISA)) {
							set(XX, YY, Estado.PRECIPICIO);
						}
					}
				}
			}
		}

		// ¬H => (¬M* => OK_MONSTRUO*)
		if (!H) {
			for (int i = 0; i < 4; i++) {
				int XX = X + X_OFFSET[i];
				int YY = Y + Y_OFFSET[i];
				if (!get(XX, YY, Estado.MURO)) {
					set(XX, YY, Estado.OK_MONSTRUO);
				}
			}
		}

		// ¬B => (¬M* => OK_PRECIPICIO*)
		if (!B) {
			for (int i = 0; i < 4; i++) {
				int XX = X + X_OFFSET[i];
				int YY = Y + Y_OFFSET[i];
				if (!get(XX, YY, Estado.MURO)) {
					set(XX, YY, Estado.OK_PRECIPICIO);
				}
			}
		}

		// ¬V* => sinConsumir++
		for (int i = 0; i < 4; i++) {
			int XX = X + X_OFFSET[i];
			int YY = Y + Y_OFFSET[i];
			if (get(XX, YY, Estado.OK) && !get(XX, YY, Estado.VISITADA) && !get(XX, YY, Estado.SIN_CONSUMIR)) {
				sinConsumir++;
				set(XX, YY, Estado.SIN_CONSUMIR);
			}
		}

		// ************** //
		// PARTE REACTIVA //
		// ************** //
		// Ti,j => RECOGER_TESORO
		if (get(X, Y, Estado.TESORO)) {
			num_tesoros_encontrados++;
			accion = Acciones.RECOGER_TESORO;
		} else if (ciclos_restantes == 0 && bombas_restantes > 0) {
			bombas_restantes--;
			accion = Acciones.PRODUCIR_HEDOR;
		} else {
			// Monstruo seguro
			for (int i = 0; i < 4; i++) {
				int XX = X + X_OFFSET[i];
				int YY = Y + Y_OFFSET[i];

				if (get(XX, YY, Estado.MONSTRUO) && num_proyectiles > 0) {
					accion = 4 + i;
					num_proyectiles--;
					set(XX, YY, Estado.DISPARADO_NORTE + i);
					break;
				}
			}

			// Visitar casilla segura
			for (int i = 0; i < 4 && accion == Acciones.NINGUNA; i++) {
				int XX = X + X_OFFSET[i];
				int YY = Y + Y_OFFSET[i];
				if (!get(XX, YY, Estado.MURO) && !get(XX, YY, Estado.VISITADA) && get(XX, YY, Estado.OK)) {
					accion = i;
					pilaAcciones.push((accion + 2) % 4);
					break;
				}
			}

			// Posible monstruos y no hay salida
			for (int i = 0; i < 4 && accion == Acciones.NINGUNA; i++) {
				int XX = X + X_OFFSET[i];
				int YY = Y + Y_OFFSET[i];
				if (get(XX, YY, Estado.POSIBLE_MONSTRUO) && !get(X, Y, Estado.DISPARADO_NORTE + i) && num_proyectiles > 0) {
					if (sinConsumir == 0) {
						accion = 4 + i;
						num_proyectiles--;
						set(X, Y, Estado.DISPARADO_NORTE + i);
						break;
					}
				}
			}

			if (accion == Acciones.NINGUNA) {
				// **** !!!!!!! APAÑO !!!!!!!!!!!!! //
				if (!pilaAcciones.empty()) {
					accion = pilaAcciones.pop();
				}
			}
		}

		tirarBomba();
		accionp = accion;
	}

	private void tirarBomba() {
		if (getX() != STARTX && getY() != STARTY) {
			if (ciclos_restantes == 0) {
				ciclos_restantes = r.nextInt(MAX_CICLOS) + MIN_CICLOS;
			} else {
				if (ciclos % 32 == 0) {
					ciclos_restantes--;
				}
			}
		}
	}

	@Override
	public void ciclo() {
		switch (ciclos % 32) {
			case 0: // un ciclo propiamente dicho en el entorno, el resto de casos son intraciclo
				// animación
				switch (accion) {
					case Acciones.DESPLAZARSE_NORTE:
					case Acciones.DISPARAR_NORTE:
						gDireccion = 1 * 608 / gAtlas.getSubancho();
						break;
					case Acciones.DESPLAZARSE_ESTE:
					case Acciones.DISPARAR_ESTE:
						gDireccion = 2 * 608 / gAtlas.getSubancho();
						break;
					case Acciones.DESPLAZARSE_SUR:
					case Acciones.DISPARAR_SUR:
						gDireccion = 0 * 608 / gAtlas.getSubancho();
						break;
					case Acciones.DESPLAZARSE_OESTE:
					case Acciones.DISPARAR_OESTE:
						gDireccion = 3 * 608 / gAtlas.getSubancho();
						break;
					case Acciones.NINGUNA:
						gDireccion = 0;
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
		ciclos++;
	}

	public void pintar(Graphics g, int escala) {

		if (verPercepciones) {
			for (int i = 0; i < ancho; i++) {
				for (int j = 0; j < alto; j++) {
					//g.setColor(new Color(0, 128, 255, OPACIDAD_DEBUG));
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
						} else if (mapa[i][j].get(Estado.VISITADA)) {
							g.setColor(new Color(0, 128, 255, OPACIDAD_DEBUG));
							g.fillRect(i * gAtlas.getSubancho() * escala, j * gAtlas.getSubalto() * escala, gAtlas.getSubancho() * escala, gAtlas.getSubalto() * escala);
						}
					}
				}
			}
		}

		boolean quieto = (accion == Acciones.NINGUNA);
		int indice = gIndiceTextura + gDireccion + (!quieto && gPaso ? 1 : 0) + (!quieto && gPaso && gAlternaPaso ? 1 : 0);
		gAtlas.pintarTexturaEscala(g, gX, gY, indice, escala);

		if (percepciones.get(Percepciones.GOLPE) && accion >= 0 && accion < 4) {
			int[][] offset = {{0, -16}, {16, 0}, {0, 16}, {-16, 0}};
			gAtlas.pintarTexturaEscala(g, getX() * gAtlas.getSubancho() + offset[accion][0],
									   getY() * gAtlas.getSubalto() + offset[accion][1], 2, escala);
		}
		if (accion >= 4 && accion < 8) {
			int[][] offset = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
			int cc = (ciclos % 32) * 4;
			cc = cc > 32 ? 32 : cc;
			gAtlas.pintarTexturaEscala(g, getX() * gAtlas.getSubancho() + offset[accion % 4][0] * cc,
									   getY() * gAtlas.getSubalto() + offset[accion % 4][1] * cc, 2 + 19, escala);
		}
	}

	public int getX() {
		switch (accion) {
			case Acciones.DESPLAZARSE_NORTE:
				return gX / gAtlas.getSubancho();
			case Acciones.DESPLAZARSE_ESTE:
				return (gX - 0) / gAtlas.getSubancho();
			case Acciones.DESPLAZARSE_SUR:
				return gX / gAtlas.getSubancho();
			case Acciones.DESPLAZARSE_OESTE:
				return (gX + 31) / gAtlas.getSubancho();
			default:
				return gX / gAtlas.getSubancho();
		}
	}

	public int getY() {
		switch (accion) {
			case Acciones.DESPLAZARSE_NORTE:
				return (gY + 31) / gAtlas.getSubalto();
			case Acciones.DESPLAZARSE_ESTE:
				return gY / gAtlas.getSubalto();
			case Acciones.DESPLAZARSE_SUR:
				return (gY - 0) / gAtlas.getSubalto();
			case Acciones.DESPLAZARSE_OESTE:
				return gY / gAtlas.getSubalto();
			default:
				return gY / gAtlas.getSubalto();
		}
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

	public boolean getVerPercepciones() {
		return verPercepciones;
	}

	public void clearPercepciones() {
		verPercepciones = false;
	}

	public void toggleVerPercepciones() {
		verPercepciones = !verPercepciones;
	}

	public void setNumProyectiles(int proyectiles) {
		num_proyectiles = proyectiles;
	}

	public int getNumTesorosEncontrados() {
		return num_tesoros_encontrados;
	}

	public int getSinConsumir() {
		return sinConsumir;
	}

	public int getNumProyectiles() {
		return num_proyectiles;
	}

	public int getNumBombasRestantes() {
		return bombas_restantes;
	}

	public int getCiclosRestantes() {
		return ciclos_restantes;
	}
}
