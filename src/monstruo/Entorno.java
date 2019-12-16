package monstruo;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Entorno extends JPanel implements Ciclico {

	private static final int[] G_INDICES_AGENTES = {4, 7, 10, 13};
	private static final int[] X_OFFSET = {0, 1, 0, -1};
	private static final int[] Y_OFFSET = {-1, 0, 1, 0};

	private static final int DURACION_BOMBAS = 69; // 9 ciclos (-1 del agente)
	private ArrayList<int[]> bombas;
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
	public static final int HEDOR_FALSO = 4;
	public static final int BASE_AMARILLO = 8;
	public static final int BASE_ROJO = 9;
	public static final int BASE_VERDE = 10;
	public static final int BASE_AZUL = 11;
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
			mapa[baseX[i]][baseY[i]] = BASE_AMARILLO + i;
		}

		bombas = new ArrayList<>();

		numAgentes = 4;
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

	private void ganador() {
		if (numTesoros != 0) {
			boolean todos_base = true;
			boolean todos_terminado = true;
			for (int i = 0; i < numAgentes; i++) {
				Agente a = agentes[i];
				if (!(a.getX() == a.getStartX() && a.getY() == a.getStartY())) {
					todos_base = false;
				}
				if (a.getSinConsumir() != 0) {
					todos_terminado = false;
				}
			}

			if (todos_base && todos_terminado) {
				int agente = 0;
				int num_tesoros = 0;
				for (int i = 0; i < numAgentes; i++) {
					Agente a = agentes[i];
					if (a.getNumTesorosEncontrados() > num_tesoros) {
						agente = i;
						num_tesoros = a.getNumTesorosEncontrados();
					}
				}
				JOptionPane.showMessageDialog(null,
						"Número de tesoros recogidos: " + num_tesoros,
						"Ganador",
						JOptionPane.DEFAULT_OPTION,
						new ImageIcon(gAtlas.getSubImagen(G_INDICES_AGENTES[agente])));
				System.exit(0);
			}
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

				for (int j = 0; j < bombas.size(); j++) {
					bombas.get(j)[2]--;

					if (bombas.get(j)[2] == 0) {
						int bx = bombas.get(j)[0];
						int by = bombas.get(j)[1];
						mapa[bx][by] = NADA;
						bombas.remove(j);
					}
				}

				// posición del agente y última acción del agente //
				int accionp = a.getAccionp();

				Percepciones p = a.getPercepciones();
				p.set(Percepciones.GEMIDO, false);

				if (accionp == Acciones.RECOGER_TESORO) {
					set(x, y, NADA);
				} // processar disparos
				else if (accionp >= 4) {
					int balaX = x;
					int balaY = y;
					while (balaX > 0 && balaX < ancho - 1 && balaY > 0 && balaY < alto - 1
						   && mapa[balaX][balaY] != MONSTRUO) {
						balaX += X_OFFSET[accionp % 4];
						balaY += Y_OFFSET[accionp % 4];
						if (mapa[balaX][balaY] == MONSTRUO) {
							p.set(Percepciones.GEMIDO, true);
							mapa[balaX][balaY] = NADA;
							break;
						}
					}
				}

				boolean h, b, r, g;
				h = this.getAdyacentes(x, y).contains(MONSTRUO) || mapa[x][y] == HEDOR_FALSO;
				b = this.getAdyacentes(x, y).contains(PRECIPICIO);
				r = (this.get(x, y) == TESORO);
				p.set(Percepciones.HEDOR, h);
				p.set(Percepciones.BRISA, b);
				p.set(Percepciones.RESPLANDOR, r);

				a.calcularAccion();

				int accion = a.getAccion();

				g = accion >= 0 && accion < 4 && (getColindante(x, y, accion) == MURO);
				p.set(Percepciones.GOLPE, g);

				if (accion == Acciones.PRODUCIR_HEDOR) {
					bombas.add(new int[]{x, y, DURACION_BOMBAS, i});
					set(x, y, HEDOR_FALSO);
				}

				ganador();
			}
			// CÓDIGO ENTORNO (FIN) //

			a.ciclo();

			if (a.getAccion() >= 0 && a.getAccion() < 4 && getColindante(x, y, a.getAccion()) != MURO) {
				switch (a.getAccion()) {
					case Acciones.DESPLAZARSE_NORTE:
						a.gY -= 1;
						break;
					case Acciones.DESPLAZARSE_ESTE:
						a.gX += 1;
						break;
					case Acciones.DESPLAZARSE_SUR:
						a.gY += 1;
						break;
					case Acciones.DESPLAZARSE_OESTE:
						a.gX -= 1;
						break;
				}
			}

			if (a.getAccionp() <= 4 && a.getAccionp() < 8) {

			}
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
						indice = 19;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						indice = 38;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						break;
					case PRECIPICIO:
						indice = 20;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						break;
					case TESORO:
						indice = 0;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						indice = 57;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						break;
					case MURO:
						indice = 1;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						break;
					case HEDOR_FALSO:
						indice = 0;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						break;
					case BASE_AMARILLO:
						indice = 16 + 19 * 0;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						break;
					case BASE_ROJO:
						indice = 16 + 19 * 1;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						break;
					case BASE_VERDE:
						indice = 16 + 19 * 2;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						break;
					case BASE_AZUL:
						indice = 16 + 19 * 3;
						gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado);
						break;
				}
			}

		}

		for (int i = 0; i < numAgentes; i++) {
			agentes[i].pintar(g, gFactorEscalado);
		}

		for (int i = 0; i < ancho; i++) {
			for (int j = 0; j < alto; j++) {
				int x = i * gAtlas.getSubancho();
				int y = j * gAtlas.getSubalto();
				int indice;
				switch (mapa[i][j]) {
					case MONSTRUO:
						for (int k = 0; k < 4; k++) {
							int XX = i + X_OFFSET[k];
							int YY = j + Y_OFFSET[k];
							int xx = XX * gAtlas.getSubancho();
							int yy = YY * gAtlas.getSubalto();
							if (get(XX, YY) != MURO && get(XX, YY) != PRECIPICIO && get(XX, YY) != MONSTRUO) {
								int cc = ciclos % 32;
								if ((cc >= 0 && cc < 16)) {
									indice = 41;
								} else {
									indice = 60;
								}
								gAtlas.pintarTexturaEscala(g, xx, yy, indice, gFactorEscalado); // 37, 54
							}
						}
						break;
					case PRECIPICIO:
						for (int k = 0; k < 4; k++) {
							int XX = i + X_OFFSET[k];
							int YY = j + Y_OFFSET[k];
							int xx = XX * gAtlas.getSubancho();
							int yy = YY * gAtlas.getSubalto();
							if (get(XX, YY) != MURO && get(XX, YY) != PRECIPICIO && get(XX, YY) != MONSTRUO) {
								int cc = ciclos % 32;
								if ((cc >= 0 && cc < 16)) {
									indice = 3;
								} else {
									indice = 22;
								}
								gAtlas.pintarTexturaEscala(g, xx, yy, indice, gFactorEscalado); // 3, 20
							}
						}
						break;
					case HEDOR_FALSO:
						ArrayList<Integer> hedoresFalsos = new ArrayList<>();
						for (int[] foo : bombas) {
							if (foo[0] == i && foo[1] == j) {
								hedoresFalsos.add(foo[3]);
							}
						}
						for (int a : hedoresFalsos) {
							int cc = ciclos % 32;
							indice = 0;
							if ((cc >= 0 && cc < 16)) {
								switch (a) {
									case 0:
										indice = 17;
										break;
									case 1:
										indice = 18;
										break;
									case 2:
										indice = 55;
										break;
									case 3:
										indice = 56;
										break;
								}
							} else {
								switch (a) {
									case 0:
										indice = 17 + 19;
										break;
									case 1:
										indice = 18 + 19;
										break;
									case 2:
										indice = 55 + 19;
										break;
									case 3:
										indice = 56 + 19;
										break;
								}
							}
							gAtlas.pintarTexturaEscala(g, x, y, indice, gFactorEscalado); // 3, 20
						}
				}
			}
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
