package monstruo;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;
import monstruo.Agente.Movimiento;
import monstruo.Agente.Percepcion;

public class Entorno extends JPanel implements Ciclico {

	private int ciclos;

	private final Atlas gfxAtlas;
	private int gfxFactorEscaladoIntegral;

	private final int filas;
	private final int columnas;

	protected enum Elemento {
		MONSTRUO, PRECIPICIO, TESORO, MURO
	}

	// lista incompleta de índices del atlas, MEJORAR //
	private static final int ATLAS_SUELO = 0, ATLAS_PARED = 1;
	// índices dónde empiezan las texturas cada agente para cada color //
	private static final int ATLAS_AMARILLO = 4, ATLAS_ROJO = 7, ATLAS_VERDE = 10, ATLAS_AZUL = 13;

	private final Agente[] agentes;
	private final int numAgentes = 1; // número de agentes instanciados, de momento 1
	private final Elemento[][] mapa;

	public Entorno(Atlas atlas, int filas, int columnas) {
		ciclos = 0;
		gfxAtlas = atlas;
		gfxFactorEscaladoIntegral = 1;
		this.filas = filas;
		this.columnas = columnas;
		agentes = new Agente[4];
		mapa = new Elemento[filas][columnas];

		// inicializar muros
		for (int i = 0; i < columnas; i++) {
			final int PRIMERA_FILA = 0;
			final int ULTIMA_FILA = filas - 1;
			mapa[PRIMERA_FILA][i] = mapa[ULTIMA_FILA][i] = Elemento.MURO;
		}
		for (int i = 0; i < filas; i++) {
			final int PRIMERA_COLUMNA = 0;
			final int ULTIMA_COLUMNA = columnas - 1;
			mapa[i][PRIMERA_COLUMNA] = mapa[i][ULTIMA_COLUMNA] = Elemento.MURO;
		}

		// cosas puestas a mano, QUITAR LUEGO
		mapa[1][8] = Elemento.MONSTRUO;
		mapa[8][8] = Elemento.MONSTRUO;
		mapa[8][1] = Elemento.MONSTRUO;
		mapa[5][5] = Elemento.MONSTRUO;
		mapa[3][1] = Elemento.MONSTRUO;
		agentes[0] = new Agente(atlas, ATLAS_AMARILLO, filas, columnas, 1, 1);
	}

	// 3 funciones auxiliares para el mapa que a lo mejor estaría bien meter en una clase //
	private Elemento casilla(Elemento[][] mapa, int X, int Y) {
		if (mapa[Y][X] != null) {
			return mapa[Y][X];
		} else {
			return null;
		}
	}

	private ArrayList<Elemento> casillasAdyacentes(Elemento[][] mapa, int X, int Y) {
		int offset[][] = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
		ArrayList<Elemento> res = new ArrayList<>();
		for (Movimiento m : Movimiento.values()) {
			if (mapa[Y + offset[m.ordinal()][0]][X + offset[m.ordinal()][1]] != null) {
				res.add(mapa[Y + offset[m.ordinal()][0]][X + offset[m.ordinal()][1]]);
			}
		}
		return res;
	}

	private Elemento casillaSiguiente(Elemento[][] mapa, int X, int Y, Movimiento accion) {
		int offset[][] = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
		if (mapa[Y + offset[accion.ordinal()][0]][X + offset[accion.ordinal()][1]] != null) {
			return mapa[Y + offset[accion.ordinal()][0]][X + offset[accion.ordinal()][1]];
		} else {
			return null;
		}
	}

	@Override
	public void ciclo() {
		for (int i = 0; i < numAgentes; i++) {
			Agente agente = agentes[i];
			if (ciclos % 32 == 0) {

				// lista de percepciones a enviar al agente y sus índices //
				boolean[] P = new boolean[4];
				final int HEDOR = Percepcion.HEDOR.ordinal();
				final int BRISA = Percepcion.BRISA.ordinal();
				final int RESPLANDOR = Percepcion.RESPLANDOR.ordinal();
				final int GOLPE = Percepcion.GOLPE.ordinal();

				// posición del agente y última acción del agente //
				int X = agente.getX();
				int Y = agente.getY();
				Movimiento accionp = agente.getAccionp();

				P[HEDOR] = casillasAdyacentes(mapa, X, Y).contains(Elemento.MONSTRUO);
				P[BRISA] = casillasAdyacentes(mapa, X, Y).contains(Elemento.PRECIPICIO);
				P[RESPLANDOR] = (casilla(mapa, X, Y) == Elemento.TESORO);
				P[GOLPE] = (casillaSiguiente(mapa, X, Y, accionp) == Elemento.MURO);

				// Enviar percepciones
				agente.setW(P);

				agente.calcularAccion();
			}

			agente.ciclo();
		}

		ciclos++;
	}

	@Override
	public void paintComponent(Graphics g) {
		for (int i = 0; i < filas; i++) {
			for (int j = 0; j < columnas; j++) {
				int indice;
				if (mapa[i][j] == null) {
					indice = 0;
				} else {
					switch (mapa[i][j]) {
						case MONSTRUO:
							gfxAtlas.pintarTexturaEscala(g, j * gfxAtlas.getSubancho(), i * gfxAtlas.getSubalto(), 16, gfxFactorEscaladoIntegral);
							indice = 17;
							break;
						case PRECIPICIO:
							indice = 2;
							break;
						case TESORO:
							indice = 18;
							break;
						case MURO:
							indice = 1;
							break;
						default:
							indice = 0;
							break;
					}
				}

				gfxAtlas.pintarTexturaEscala(g, j * gfxAtlas.getSubancho(), i * gfxAtlas.getSubalto(), indice, gfxFactorEscaladoIntegral);

			}
		}

		for (int i = 0; i < numAgentes; i++) {
			agentes[i].pintar(g, gfxFactorEscaladoIntegral);
		}

	}

	@Override
	public Dimension getPreferredSize() {
		int width = columnas * gfxAtlas.getSubancho() * gfxFactorEscaladoIntegral;
		int height = filas * gfxAtlas.getSubalto() * gfxFactorEscaladoIntegral;
		return new Dimension(width, height);
	}

	public Elemento tipoCasilla(int x, int y) {
		return mapa[y][x];
	}

	public int getFilas() {
		return filas;
	}

	public int getColumnas() {
		return columnas;
	}

	public void setIntegralFactor(int usableWidth, int usableHeight) {
		int wf = usableWidth / (columnas * gfxAtlas.getSubancho());
		int hf = usableHeight / (filas * gfxAtlas.getSubalto());
		gfxFactorEscaladoIntegral = Math.min(wf, hf);
	}

	public int getGfxFactorEscaladoIntegral() {
		return gfxFactorEscaladoIntegral;
	}

	public int posPercepcion(Percepcion percepcion) {
		return percepcion.ordinal();
	}

	public int posElemento(Elemento elemento) {
		return elemento.ordinal();
	}

}
