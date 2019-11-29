package robot;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;

public class Agente implements Ciclico {

	private int ciclos;
	private final Atlas atlas;
	private int x, y;
	private int direccion;
	private boolean piernaAire, alternaPierna;
	private boolean showPerceptions;

	private final boolean[] w, wp;

	private enum Movimiento {
		NORTE, ESTE, SUD, OESTE
	}
	private Movimiento accion, accionp;

	public Agente(Atlas atlas, int X, int Y) {
		this.atlas = atlas;
		x = X * atlas.getSubancho();
		y = Y * atlas.getSubalto();
		ciclos = 0;
		direccion = 3;
		piernaAire = alternaPierna = false;
		w = new boolean[8];
		wp = new boolean[8];
		showPerceptions = false;
	}

	public void calcularAccion() {
		// Determinar las percepciones 0, 2, 4, 6
		if (wp[1] && accionp == Movimiento.ESTE) {
			w[0] = true;
			w[2] = w[4] = w[6] = false;
		} else if (wp[5] && accionp == Movimiento.OESTE) {
			w[4] = true;
			w[0] = w[2] = w[6] = false;
		} else if (wp[3] && accionp == Movimiento.SUD) {
			w[2] = true;
			w[0] = w[4] = w[6] = false;
		} else if (wp[7] && accionp == Movimiento.NORTE) {
			w[6] = true;
			w[0] = w[2] = w[4] = false;
		} else
			w[0] = w[2] = w[4] = w[6] = false;

		// Salida del pasillo
		if (w[4] && !w[5] && accionp == Movimiento.OESTE)
			accion = Movimiento.SUD;
		else if (w[2] && !w[3] && accionp == Movimiento.SUD)
			accion = Movimiento.ESTE;
		else if (w[0] && !w[1] && accionp == Movimiento.ESTE)
			accion = Movimiento.NORTE;
		else if (w[6] && !w[7] && accionp == Movimiento.NORTE)
			accion = Movimiento.OESTE;
		
//		if (w[6] && !w[7] && accionp == Movimiento.NORTE)
//			accion = Movimiento.OESTE;
//		else if (w[0] && !w[1] && accionp == Movimiento.ESTE)
//			accion = Movimiento.NORTE;
//		else if (w[2] && !w[3] && accionp == Movimiento.SUD)
//			accion = Movimiento.ESTE;
//		else if (w[4] && !w[5] && accionp == Movimiento.OESTE)
//			accion = Movimiento.SUD;

		// Recorrido del pasillo
		else if (w[3] && w[7] && !w[1] && accionp == Movimiento.NORTE)
			accion = Movimiento.NORTE;
		else if (w[1] && w[5] && !w[3] && accionp == Movimiento.ESTE)
			accion = Movimiento.ESTE;
		else if (w[3] && w[7] && !w[5] && accionp == Movimiento.SUD)
			accion = Movimiento.SUD;
		else if (w[1] && w[5] && !w[7] && accionp == Movimiento.OESTE)
			accion = Movimiento.OESTE;

		// Casos normales
		else if (w[1] && !w[3])
			accion = Movimiento.ESTE;
		else if (w[3] && !w[5])
			accion = Movimiento.SUD;
		else if (w[5] && !w[7])
			accion = Movimiento.OESTE;
		else if (w[7] && !w[1])
			accion = Movimiento.NORTE;
		else if (w[0])
			accion = Movimiento.NORTE;
		else if (w[2])
			accion = Movimiento.ESTE;
		else if (w[4])
			accion = Movimiento.SUD;
		else if (w[6])
			accion = Movimiento.OESTE;
		else
			accion = Movimiento.NORTE;

		// Actualizar la última acción realizada
		for (int i = 0; i < wp.length; i++)
			wp[i] = w[i];
		accionp = accion;
	}

	@Override
	public void ciclo() {
		switch (ciclos % 32) {
			case 0: // un ciclo propiamente dicho en el entorno, el resto de casos son intraciclo
				calcularAccion();
				// animación
				switch (accion) {
					case NORTE:
						direccion = 6;
						break;
					case ESTE:
						direccion = 9;
						break;
					case SUD:
						direccion = 3;
						break;
					case OESTE:
						direccion = 12;
						break;
				}
				alternaPierna = !alternaPierna;
				break;
			case 8:
			case 24:
				// animación
				piernaAire = !piernaAire;
				break;
		}
		// esto se hace siempre
		// animación
		switch (accion) {
			case NORTE:
				y -= 1;
				break;
			case ESTE:
				x += 1;
				break;
			case SUD:
				y += 1;
				break;
			case OESTE:
				x -= 1;
				break;
		}
		//System.out.println(x + " " + y);
		ciclos++;
	}

	public void pintar(Graphics g, int escala) {
		int indice = direccion + (piernaAire ? 1 : 0) + (piernaAire && alternaPierna ? 1 : 0);
		Color verde = new Color(0, 255, 0, 80);
		Color rojo = new Color(255, 0, 0, 100);
		int[][] offset = {
			{-1, -1}, {0, -1}, {1, -1}, {1, 0},
			{1, 1}, {0, 1}, {-1, 1}, {-1, 0}
		};
		if (showPerceptions)
			for (int i = 0; i < offset.length; i++) {
				g.setColor(w[i] ? rojo : verde);
				int x1 = (this.getX() + offset[i][0]) * atlas.getSubancho();
				int y1 = (this.getY() + offset[i][1]) * atlas.getSubalto();
				g.fillRect(x1 * escala, y1 * escala, atlas.getSubancho() * escala, atlas.getSubalto() * escala);
			}
		atlas.pintarTexturaEscala(g, x, y, indice, escala);
	}

	public int getX() {
		return (x + (accion == Movimiento.OESTE ? 31 : 0)) / atlas.getSubancho();
	}

	public int getY() {
		return (y + (accion == Movimiento.NORTE ? 31 : 0)) / atlas.getSubalto();
	}

	public void setW(boolean[] w) {
		this.w[1] = w[0];
		this.w[3] = w[1];
		this.w[5] = w[2];
		this.w[7] = w[3];
	}

	public String getTxtW() {
		String res = "";
		for (int i = 0; i < w.length; i++)
			res += w[i] + " ";
		return res;
	}

	public String getTxtWp() {
		String res = "";
		for (int i = 0; i < wp.length; i++)
			res += wp[i] + " ";
		return res;
	}

	public String getTxtAccion() {
		String res = "";

		if (accion != null)
			res = accion.toString();

		return res;
	}

	public String getTxtAccionp() {
		String res = "";

		if (accionp != null)
			res = accionp.toString();

		return res;
	}

	public void setShowPerceptions(boolean b) {
		showPerceptions = b;
	}
}
