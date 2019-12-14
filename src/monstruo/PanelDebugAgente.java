package monstruo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class PanelDebugAgente extends JPanel {

	private static final int SIZE = 8;
	private final Agente agente;

	public PanelDebugAgente(Agente agente) {
		this.agente = agente;
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		int x = 0;
		int y = 0;

		g2.setFont(g2.getFont().deriveFont(9.0f));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		for (int i = 0; i < 4; i++) {
			y = 0;
			g2.setColor(Color.BLACK);
			g2.drawString("" + "HBRG".charAt(i), x, y + g2.getFontMetrics().getHeight());
			y = 12;
			g.setColor(agente.getPercepciones().get(i) ? Color.GREEN : Color.RED);
			g.fillRect(x, y, SIZE, SIZE);
			x += SIZE + 1;
		}

		x = 0;
		y = 24;
		g.setColor(Color.BLACK);
		for (int i = 0; i < agente.getMapa().length; i++) {
			for (int j = 0; j < agente.getMapa()[0].length; j++) {
				if (agente.getMapa()[i][j] == null) {
					g.drawRect(x, y, SIZE, SIZE);
				}
				y += SIZE + 2;

			}
			y = 24;
			x += SIZE + 2;
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(128, 128);
	}
}
