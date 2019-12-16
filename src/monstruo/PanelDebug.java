package monstruo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class PanelDebug extends JPanel {

	private static final int PADDING = 10;
	private static final float FONT_SIZE = 16.0f;
	private static final int IMG_SIZE = 32;
	private static final int IMG_STARTX = 128;
	private static final int IMG_STARTY = 25;
	private static final int SIZE = 10;
	
	private static final int[] OFFSET_BOMBA = {17, 18, 55, 56};
	
	private Agente agente;
	private final Atlas atlas;
	private int indice;

	public PanelDebug(Agente agente, Atlas atlas, int indice) {
		this.agente = agente;
		this.atlas = atlas;
		this.indice = indice;
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		int x = PADDING;
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

		x = PADDING;
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
		
		// Pintar texturas
		g2.drawImage(atlas.getSubImagen(OFFSET_BOMBA[indice]), IMG_STARTX, IMG_STARTY + PADDING, IMG_SIZE, IMG_SIZE, null);
		g2.drawImage(atlas.getSubImagen(21), IMG_STARTX, IMG_STARTY + IMG_SIZE + PADDING, IMG_SIZE, IMG_SIZE, null);
		g2.drawImage(atlas.getSubImagen(57), IMG_STARTX, IMG_STARTY + (IMG_SIZE * 2) + PADDING, IMG_SIZE, IMG_SIZE, null);
		
		g.setColor(Color.BLACK);
		g2.setFont(g2.getFont().deriveFont(FONT_SIZE));
		g2.drawString(agente.getNumBombasRestantes()+ "", IMG_STARTX + IMG_SIZE + PADDING, IMG_STARTY + PADDING + g2.getFontMetrics().getHeight());
		g2.drawString("(" + agente.getCiclosRestantes()+ " ciclos restantes)", IMG_STARTX + IMG_SIZE + PADDING + 20, IMG_STARTY + PADDING + g2.getFontMetrics().getHeight());
		g2.drawString(agente.getNumProyectiles()+ "", IMG_STARTX + IMG_SIZE + PADDING, IMG_STARTY + IMG_SIZE + PADDING + g2.getFontMetrics().getHeight());
		g2.drawString(agente.getNumTesorosEncontrados()+ "", IMG_STARTX + IMG_SIZE + PADDING, IMG_STARTY + (IMG_SIZE * 2) + PADDING + g2.getFontMetrics().getHeight());
		
	}
	
	public void setAgente (Agente agente, int indice) {
		this.agente = agente;
		this.indice = indice;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(128, 160);
	}
}
