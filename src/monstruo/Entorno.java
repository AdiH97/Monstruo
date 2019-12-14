package monstruo;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;

public class Entorno extends JPanel implements Ciclico {

	private static final int[] G_INDICES_AGENTES = {4, 7, 10, 13};
	private static final int[] X_OFFSET = {0, 1, 0, -1};
	private static final int[] Y_OFFSET = {-1, 0, 1, 0};

	private int ciclos;

	// gráficos //
	private final Atlas gAtlas;
	private int gFactorEscalado;

	// mapa //
	public static final int NADA = -1;
	public static final int MONSTRUO = 0;
	public static final int PRECIPICIO = 1;
	public static final int TESORO = 2;
	public static final int MURO = 3;
	private final int ancho;
	private final int alto;
	private final int[][] mapa;

	// agentes //
	private static final int MAX_AGENTES = 4;
	private final int numAgentes;
	private final Agente[] agentes;

	// contadores //
	private int numTesoros;
	private int numMonstruos;

	public Entorno(Atlas gAtlas, int ancho, int alto) {
		ciclos = 0;
		this.gAtlas = gAtlas;
		gFactorEscalado = 1;
		this.ancho = ancho;
		this.alto = alto;

		mapa = new int[ancho][alto];

		final int muroNorte = 0;
		final int muroSur = alto - 1;
		final int muroOeste = 0;
		final int muroEste = ancho - 1;
		int[] baseX = {muroOeste + 1, muroEste - 1, muroEste - 1, muroOeste + 1};
		int[] baseY = {muroNorte + 1, muroNorte + 1, muroSur - 1, muroSur - 1};

		for (int i = 0; i < ancho; i++) {
			mapa[i][muroNorte] = mapa[i][muroSur] = MURO;
		}

		for (int i = 0; i < alto; i++) {
			mapa[muroOeste][i] = mapa[muroEste][i] = MURO;
		}

		for (int i = 1; i < ancho - 1; i++) {
			for (int j = 1; j < alto - 1; j++) {
				mapa[i][j] = NADA;
			}
		}

		agentes = new Agente[MAX_AGENTES];

		for (int i = 0; i < agentes.length; i++) {
			agentes[i] = new Agente(gAtlas, G_INDICES_AGENTES[i], ancho, alto, baseX[i], baseY[i]);
		}

		numAgentes = 1;
		numMonstruos = 0;
		numTesoros = 0;
	}

	private int get(int x, int y) {
		if (x >= 0 && x < ancho && y >= 0 && y < alto) {
			return mapa[x][y];
		} else {
			return NADA;
		}
	}

	private ArrayList<Integer> getAdyacentes(int x, int y) {
		ArrayList<Integer> adyacentes = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			adyacentes.add(this.get(x + X_OFFSET[i], y + Y_OFFSET[i]));
		}
		return adyacentes;
	}

	private int getColindante(int x, int y, int accion) {
		return mapa[x + X_OFFSET[accion]][y + Y_OFFSET[accion]];
	}
	
	private void ganador () {
		boolean todos_base = true;
		for(Agente a : agentes) {
			if(!(a.getX() == a.getStartX() && a.getY() == a.getStartY())) {
				todos_base = false;
			}
		}
		
		if(todos_base) {
			int agente = 0;
			int num_tesoros = 0;
			for(int i = 0; i < agentes.length; i++) {
				if(agentes[i].getNumTesorosEncontrados() > num_tesoros) {
					agente = i;
					num_tesoros = agentes[i].getNumTesorosEncontrados();
				}
			}
			
			System.out.println("Ganador " + agente + ", con " + num_tesoros + " encontrados");
		}
	}

	@Override
	public void ciclo() {
		for (int i = 0; i < numAgentes; i++) {
			Agente a = agentes[i];
			int x = a.getX();
			int y = a.getY();

			// CÓDIGO ENTORNO //
			if (ciclos % 32 == 0) {

				// posición del agente y última acción del agente //
				int accionp = a.getAccionp();

				if (accionp == Acciones.RECOGER_TESORO) {
					set(x, y, NADA);
				} // processar disparos
				/*else if (accionp >= 4) {
					int balaX = x;
					int balaY = y;
					while (balaX > 0 && balaX < ancho - 1 && balaY > 0 && balaY < alto - 1
							&& mapa[balaX][balaY] != MONSTRUO) {
						balaX += X_OFFSET[accionp % 4];
						balaY += Y_OFFSET[accionp % 4];
						if (mapa[balaX][balaY] == MONSTRUO) {
							mapa[balaX][balaY] = NADA;
							break;
						}
					}
				}*/

				Percepciones p = a.getPercepciones();
				boolean h, b, r, g;
				h = this.getAdyacentes(x, y).contains(MONSTRUO);
				b = this.getAdyacentes(x, y).contains(PRECIPICIO);
				r = (this.get(x, y) == TESORO);
				g = accionp >= 0 && accionp < 4 && (getColindante(x, y, accionp) == MURO);
				p.set(Percepciones.HEDOR, h);
				p.set(Percepciones.BRISA, b);
				p.set(Percepciones.RESPLANDOR, r);
				p.set(Percepciones.GOLPE, g);

				a.calcularAccion();
				
				ganador();
			}
			// CÓDIGO ENTORNO (FIN) //

			a.ciclo();
		}
		ciclos++;
	}

	@Override
	public void paintComponent(Graphics g) {
		for (int i = 0; i < ancho; i++) {
			for (int j = 0; j < alto; j++) {
				int x = i * gAtlas.getSubancho();
				int y = j * gAtlas.getSubalto();
				int indice;
				switch (mapa[i][j]) {
					case NADA:
						indice = 0;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						break;
					case MONSTRUO:
						indice = 16;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						indice = 17;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						break;
					case PRECIPICIO:
						indice = 32;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						break;
					case TESORO:
						indice = 48;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						break;
					case MURO:
						indice = 1;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						break;
				}
			}
		}

		for (int i = 0; i < numAgentes; i++) {
			agentes[i].pintar(g, gFactorEscalado);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		int width = ancho * gAtlas.getSubancho() * gFactorEscalado;
		int height = alto * gAtlas.getSubalto() * gFactorEscalado;
		return new Dimension(width, height);
	}

	public void set(int x, int y, int elemento) {
		mapa[x][y] = elemento;
	}

	public int getgFactorEscalado() {
		return gFactorEscalado;
	}

	public void setgFactorEscalado(int anchoDisponible, int altoDisponible) {
		int factorAncho = anchoDisponible / (ancho * gAtlas.getSubancho());
		int factorAlto = altoDisponible / (alto * gAtlas.getSubalto());
		gFactorEscalado = Math.min(factorAncho, factorAlto);
	}

	public int getAncho() {
		return ancho;
	}

	public int getAlto() {
		return alto;
	}

	public Agente[] getAgentes() {
		return agentes;
	}

	public int getNumAgentes() {
		return numAgentes;
	}

	public void addElemento(int x, int y, int elemento) {
		if (mapa[x][y] == NADA) {
			mapa[x][y] = elemento;

			// Incremenetar contadores
			if (elemento == TESORO) {
				numTesoros++;
			}
			if (elemento == MONSTRUO) {
				numMonstruos++;
				for (Agente a : agentes) {
					a.setNumProyectiles(numMonstruos);
				}
			}
		}
	}

	public void removeElemento(int x, int y, int elemento) {
		if (mapa[x][y] == elemento) {
			mapa[x][y] = NADA;

			// Decrementar contadores
			if (elemento == TESORO) {
				numTesoros--;
			}
			if (elemento == MONSTRUO) {
				numMonstruos--;
				for (Agente a : agentes) {
					a.setNumProyectiles(numMonstruos);
				}
			}
		}
	}

	public void verPercepciones(boolean b) {
		for (Agente a : agentes) {
			a.setVerPercepciones(b);
		}
	}

	public int getCiclos() {
		return ciclos;
	}

}
