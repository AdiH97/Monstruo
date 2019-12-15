package monstruo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class PanelDebug extends JPanel {

	private static final int SIZE = 10;
	private final Agente agente;

	public PanelDebug(Agente agente) {
		this.agente = agente;
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		int x = 0;
		int y = 0;

		g2.setFont(g2.getFont().deriveFont(10.0f));
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
		g.setColor(Color.MAGENTA);
		g.fillRect(x, y, this.getPreferredSize().width - SIZE, this.getPreferredSize().height - SIZE * 4);
		g.setColor(Color.BLACK);
		g2.setFont(g2.getFont().deriveFont(9.0f));
		for (int i = 0; i < agente.getMapa().length; i++) {
			for (int j = 0; j < agente.getMapa()[0].length; j++) {
				if (agente.getMapa()[i][j] == null) {

					g.setColor(Color.WHITE);
					g.fillRect(x, y, SIZE, SIZE);
					g.setColor(Color.BLACK);
					g.drawRect(x, y, SIZE - 1, SIZE - 1);
					g.drawString("?", x + 3, y + g.getFontMetrics().getAscent() - 1);
				} else {
					

					if (agente.getMapa()[i][j].get(Estado.MURO)) {
						g.setColor(Color.BLACK);
						g.fillRect(x, y, SIZE, SIZE);
					} else if (agente.getMapa()[i][j].get(Estado.POSIBLE_MONSTRUO) && agente.getMapa()[i][j].get(Estado.POSIBLE_PRECIPICIO)) {
						g.setColor(Color.ORANGE);
						int[] lotrix = {x, x + SIZE, x};
						int[] lotriy = {y, y + SIZE, y + SIZE};
						int[] hitrix = {x, x + SIZE, x + SIZE};
						int[] hitriy = {y, y, y + SIZE};
						g.setColor(Color.ORANGE);
						g.fillPolygon(lotrix, lotriy, 3);
						g.setColor(Color.BLUE);
						g.fillPolygon(hitrix, hitriy, 3);
					} else if (agente.getMapa()[i][j].get(Estado.POSIBLE_MONSTRUO)) {
						g.setColor(Color.ORANGE);
						g.fillRect(x, y, SIZE, SIZE);
					} else if (agente.getMapa()[i][j].get(Estado.POSIBLE_PRECIPICIO)) {
						g.setColor(Color.BLUE);
						g.fillRect(x, y, SIZE, SIZE);
					}
					if (agente.getMapa()[i][j].get(Estado.SIN_CONSUMIR)) {
						g.setColor(Color.WHITE);
						g.drawRect(x, y, SIZE - 1, SIZE - 1);
					}
				}
				y += SIZE + 2;

			}
			y = 24;
			x += SIZE + 2;
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(128, 160);
	}
}
