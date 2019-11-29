package robot;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;

public class Entorno extends JPanel implements Ciclico {

	private final Atlas atlas;
	private final int filas, columnas;
	private int integralFactor;

	private final boolean[] mapa;
	private final ArrayList<Agente> agentes;

	public Entorno(Atlas atlas, int filas, int columnas) {
		this.atlas = atlas;
		this.filas = filas;
		this.columnas = columnas;
		agentes = new ArrayList<>();
		integralFactor = 1;

		mapa = new boolean[filas * columnas];
		for (int i = 0; i < columnas; i++) {
			final int primeraFila = 0 * columnas;
			final int ultimaFila = (filas - 1) * columnas;
			mapa[primeraFila + i] = mapa[ultimaFila + i] = true;
		}
		for (int i = 0; i < filas; i++) {
			final int primeraColumna = 0;
			final int ultimaColumna = columnas - 1;
			mapa[i * columnas + primeraColumna] = mapa[i * columnas + ultimaColumna] = true;
		}
	}

	@Override
	public void ciclo() {
		for (Agente agente : agentes) {
			// Percepciones
			// [ norte, este, sud, oeste]
			boolean[] s = new boolean[4];

			// Posición del agente
			int agenteX = agente.getX();
			int agenteY = agente.getY();

			int posAgente = agenteY * columnas + agenteX;
			s[0] = mapa[posAgente - columnas]; // norte
			s[1] = mapa[posAgente + 1]; // este
			s[2] = mapa[posAgente + columnas]; // sud
			s[3] = mapa[posAgente - 1]; // oeste

			// Enviar percepciones al agente
			agente.setW(s);
			agente.ciclo();
		}

	}

	@Override
	public void paintComponent(Graphics g) {
		for (int i = 0; i < filas * columnas; i++)
			atlas.pintarTexturaEscala(g, (i % columnas) * atlas.getSubancho(), (i / columnas) * atlas.getSubalto(), mapa[i] ? 1 : 0, integralFactor);
		for (Agente agente : agentes)
			agente.pintar(g, integralFactor);
	}

	@Override
	public Dimension getPreferredSize() {
		int width = columnas * atlas.getSubancho() * integralFactor;
		int height = filas * atlas.getSubalto() * integralFactor;
		return new Dimension(width, height);
	}

	public void cambiarAPared(int x, int y) {
		mapa[y * columnas + x] = true;
	}

	public void cambiarASuelo(int x, int y) {
		mapa[y * columnas + x] = false;
	}

	public boolean tipoCasilla(int x, int y) {
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
