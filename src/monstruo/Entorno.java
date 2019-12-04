package monstruo;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;
import monstruo.Agente.Movimiento;

public class Entorno extends JPanel implements Ciclico {

	private int ciclos;
	private final Atlas atlas;
	private final int filas, columnas;
	private int integralFactor;

	private enum Elemento {
		MONSTRUO, PRECIPICIO, TESORO, MONSTRUO_MUERTO, PARED, NADA
	}

	protected enum Percepciones {
		HEDOR, BRISA, RESPLANDOR, GEMIDO, GOLPE, NADA, POSIBLE_MONSTRUO, MONSTRUO
	}
	private final Elemento[] mapa;
	private final ArrayList<Agente> agentes;

	public Entorno(Atlas atlas, int filas, int columnas) {
		this.atlas = atlas;
		this.filas = filas;
		this.columnas = columnas;
		agentes = new ArrayList<>();
		integralFactor = 1;
		ciclos = 0;

		mapa = new Elemento[filas * columnas];

		for (int i = 0; i < filas * columnas; i++) {
			mapa[i] = Elemento.NADA;
		}
		for (int i = 0; i < columnas; i++) {
			final int primeraFila = 0 * columnas;
			final int ultimaFila = (filas - 1) * columnas;
			mapa[primeraFila + i] = mapa[ultimaFila + i] = Elemento.PARED;
		}
		for (int i = 0; i < filas; i++) {
			final int primeraColumna = 0;
			final int ultimaColumna = columnas - 1;
			mapa[i * columnas + primeraColumna] = mapa[i * columnas + ultimaColumna] = Elemento.PARED;
		}
		mapa[14] = Elemento.MONSTRUO;
		mapa[33] = Elemento.MONSTRUO;
		mapa[54] = Elemento.MONSTRUO;
		agentes.add(new Agente(atlas, filas, columnas, 1, 1));
	}

	@Override
	public void ciclo() {
		for (Agente agente : agentes) {
			if (ciclos % 32 == 0) {
				// Percepciones
				// [NADA, HEDOR, BRISA, RESPLANDOR, GEMIDO, GOLPE]
				boolean[] s = new boolean[Percepciones.values().length];

				// Posición del agente
				int agenteX = agente.getX();
				int agenteY = agente.getY();

				int posAgente = agenteY * columnas + agenteX;

				// casos hedor y brisa
				s[mapa[posAgente - columnas].ordinal()] = true;
				s[mapa[posAgente + 1].ordinal()] = true;
				s[mapa[posAgente + columnas].ordinal()] = true;
				s[mapa[posAgente - 1].ordinal()] = true;

				s[Percepciones.RESPLANDOR.ordinal()] = (mapa[posAgente] == Elemento.TESORO);

				// no sé que poner aún así que lo pongo a false, modificar luego
				s[Percepciones.GEMIDO.ordinal()] = false;
				s[Percepciones.GOLPE.ordinal()] = false;
				s[Percepciones.NADA.ordinal()] = false;

				// Obtener la última acción del agente
				Movimiento accionp = agente.getAccionp();

				// Comprobar si hay una pared en la dirección del agente
				if (accionp == Movimiento.NORTE && mapa[posAgente - columnas] == Elemento.PARED
						|| accionp == Movimiento.ESTE && mapa[posAgente + 1] == Elemento.PARED
						|| accionp == Movimiento.SUD && mapa[posAgente + columnas] == Elemento.PARED
						|| accionp == Movimiento.OESTE && mapa[posAgente - 1] == Elemento.PARED) {
					s[Percepciones.GOLPE.ordinal()] = true;
				} else {
					s[Percepciones.GOLPE.ordinal()] = false;
				}

				// Enviar percepciones
				agente.setW(s);
				
				agente.calcularAccion();
			}

			agente.ciclo();
		}
		ciclos++;
	}

	@Override
	public void paintComponent(Graphics g) {
		for (int i = 0; i < filas * columnas; i++) {
			int indice;
			switch (mapa[i]) {
				case MONSTRUO:
					atlas.pintarTexturaEscala(g, (i % columnas) * atlas.getSubancho(), (i / columnas) * atlas.getSubalto(), 16, integralFactor);
					indice = 15;
					break;
				case PRECIPICIO:
					indice = 2;
					break;
				case TESORO:
					indice = 18;
					break;
				case PARED:
					indice = 1;
					break;
				case NADA:
				default:
					indice = 0;
					break;
			}
			atlas.pintarTexturaEscala(g, (i % columnas) * atlas.getSubancho(), (i / columnas) * atlas.getSubalto(), indice, integralFactor);
		}

		for (Agente agente : agentes) {
			agente.pintar(g, integralFactor);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		int width = columnas * atlas.getSubancho() * integralFactor;
		int height = filas * atlas.getSubalto() * integralFactor;
		return new Dimension(width, height);
	}

	public void cambiarAPared(int x, int y) {
		mapa[y * columnas + x] = Elemento.PARED;
	}

	public void cambiarASuelo(int x, int y) {
		mapa[y * columnas + x] = Elemento.NADA;
	}

	public Elemento tipoCasilla(int x, int y) {
		return mapa[y * columnas + x];
	}

	public int getFilas() {
		return filas;
	}

	public int getColumnas() {
		return columnas;
	}

	public void addAgente(Agente agente) {
		agentes.add(agente);
	}

	public void removeAgente(int idx) {
		agentes.remove(idx);
	}

	public int getAgenteIdx(int x, int y) {
		int res = -1;
		boolean found = false;
		int idx = 0;
		while (!found && idx < agentes.size()) {
			if (agentes.get(idx).getX() == x
					&& agentes.get(idx).getY() == y) {
				res = idx;
				found = true;
			}
			idx++;
		}

		return res;
	}

	public ArrayList<Agente> agentes() {
		return agentes;
	}

	public void setIntegralFactor(int usableWidth, int usableHeight) {
		int wf = usableWidth / (columnas * atlas.getSubancho());
		int hf = usableHeight / (filas * atlas.getSubalto());
		integralFactor = Math.min(wf, hf);
	}

	public int getIntegralFactor() {
		return integralFactor;
	}

}
