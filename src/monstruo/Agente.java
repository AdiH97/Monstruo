package monstruo;

import java.awt.Color;
import java.awt.Graphics;
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
	private int gX, gY;
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

	private int X, Y;

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
		for (int i = 0; i < ancho; i++) {
			for (int j = 0; j < alto; j++) {
				mapa[i][j] = new Estado();
			}
		}
		accion = accionp = Acciones.DESPLAZARSE_NORTE;
		pilaAcciones = new Stack<>();
		num_proyectiles = 0;
		num_tesoros_encontrados = 0;
		STARTX = x;
		STARTY = y;
		X = x;
		Y = y;
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

		set(X, Y, Estado.VISITADA);
		accion = Acciones.NINGUNA;

		// *************** //
		// PARTE DEDUCTIVA //
		// *************** //
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
						if (get(HX1, HY1, Estado.HEDOR) && get(HX2, HY2, Estado.HEDOR)) {
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
						if (get(BX1, BY1, Estado.BRISA) && get(BX2, BY2, Estado.BRISA)) {
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

		// Ri,j => Ti,j
		if (R) {
			set(X, Y, Estado.TESORO);
		}

		// ¬Ri,j => ¬Ti,j
		if (!R) {
			clear(X, Y, Estado.TESORO);
		}

		// Gi,j && At-1 = NORTE => M0,j-1 && M1,j-1 … Mn,j-1
		if (G && accionp == Acciones.DESPLAZARSE_NORTE) {
			for (int j = 0; j < ancho; j++) {
				set(j, Y - 1, Estado.MURO);
			}
		}

		// Gi,j && At-1 = ESTE => Mi+1,0 && Mi+1,1 … Mi+1,m
		if (G && accionp == Acciones.DESPLAZARSE_ESTE) {
			for (int j = 0; j < alto; j++) {
				set(X + 1, j, Estado.MURO);
			}
		}

		// Gi,j && At-1 = SUR => M0,j+1 && M1,j+1 … Mn,j+1
		if (G && accionp == Acciones.DESPLAZARSE_SUR) {
			for (int j = 0; j < ancho; j++) {
				set(j, Y + 1, Estado.MURO);
			}
		}

		// Gi,j && At-1 = OESTE => Mi-1,0 && Mi-1,1 … Mi-1,m
		if (G && accionp == Acciones.DESPLAZARSE_OESTE) {
			for (int j = 0; j < alto; j++) {
				set(X - 1, j, Estado.MURO);
			}
		}

		// ************** //
		// PARTE REACTIVA //
		// ************** //
		// Ti,j => RECOGER_TESORO
		if (get(X, Y, Estado.TESORO)) {
			accion = Acciones.RECOGER_TESORO;
		} else {
			for (int i = 0; i < 4; i++) {
				int XX = X + X_OFFSET[i];
				int YY = Y + Y_OFFSET[i];
				if (!get(XX, YY, Estado.MURO) && !get(XX, YY, Estado.VISITADA) && get(XX, YY, Estado.OK)) {
					accion = i;
					pilaAcciones.push((accion + 2) % 4);
					break;
				}
			}

			if (accion == Acciones.NINGUNA) {
				// **** !!!!!!! APAÑO !!!!!!!!!!!!! //
				if (!pilaAcciones.empty()) {
					accion = pilaAcciones.pop();
				}
			}
		}

		accionp = accion;
	}

	@Override
	public void ciclo() {
		switch (ciclos % 32) {
			case 0: // un ciclo propiamente dicho en el entorno, el resto de casos son intraciclo
				// animación
				switch (accion) {
					case Acciones.DESPLAZARSE_NORTE:
					case Acciones.DISPARAR_NORTE:
						gDireccion = 1 * 512 / gAtlas.getSubancho();
						break;
					case Acciones.DESPLAZARSE_ESTE:
					case Acciones.DISPARAR_ESTE:
						gDireccion = 2 * 512 / gAtlas.getSubancho();
						break;
					case Acciones.DESPLAZARSE_SUR:
					case Acciones.DISPARAR_SUR:
						gDireccion = 0 * 512 / gAtlas.getSubancho();
						break;
					case Acciones.DESPLAZARSE_OESTE:
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
		//if (!getPercepciones().get(Percepciones.GOLPE)) {
			switch (accion) {
				case Acciones.DESPLAZARSE_NORTE:
					gY -= 1;
					break;
				case Acciones.DESPLAZARSE_ESTE:
					gX += 1;
					break;
				case Acciones.DESPLAZARSE_SUR:
					gY += 1;
					break;
				case Acciones.DESPLAZARSE_OESTE:
					gX -= 1;
					break;
			}
		//}
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

		int indice = gIndiceTextura + gDireccion + (gPaso ? 1 : 0) + (gPaso && gAlternaPaso ? 1 : 0);
		gAtlas.pintarTexturaEscala(g, gX, gY, indice, escala);
		if (percepciones.get(Percepciones.GOLPE)) {
			int[][] offset = {{0, -16}, {16, 0}, {0, 16}, {-16, 0}};
			gAtlas.pintarTexturaEscala(g, getX() * gAtlas.getSubancho() + offset[accionpp][0],
									   getY() * gAtlas.getSubalto() + offset[accionpp][1], 2, escala);
		}
	}

	public int getX() {
		return (gX + (accion == Acciones.DESPLAZARSE_OESTE ? 31 : 0)) / gAtlas.getSubancho();
	}

	public int getY() {
		return (gY + (accion == Acciones.DESPLAZARSE_SUR ? 31 : 0)) / gAtlas.getSubalto();
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

	public void setNumProyectiles(int proyectiles) {
		num_proyectiles = proyectiles;
	}

	public int getNumTesorosEncontrados() {
		return num_tesoros_encontrados;
	}
}
