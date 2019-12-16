package monstruo;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Atlas {

	private final BufferedImage imagen;
	private final int subancho, subalto;
	private final int columnas;

	public Atlas(String ruta, int subancho, int subalto) throws IOException {
		imagen = ImageIO.read(new File(ruta));
		this.subancho = subancho;
		this.subalto = subalto;
		columnas = imagen.getWidth() / subancho;
	}

	public void pintarTexturaEscala(Graphics g, int x, int y, int indice, int escala) {
		int dx1 = x * escala;
		int dy1 = y * escala;
		int dx2 = (x + subancho) * escala;
		int dy2 = (y + subalto) * escala;
		int sx1 = (indice % columnas) * subancho;
		int sy1 = (indice / columnas) * subalto;
		int sx2 = sx1 + subancho;
		int sy2 = sy1 + subalto;
		g.drawImage(imagen, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
	}

	public int getSubancho() {
		return subancho;
	}

	public int getSubalto() {
		return subalto;
	}
	
	public Image getSubImagen (int indice) {
		int sx1 = (indice % columnas) * subancho;
		int sy1 = (indice / columnas) * subalto;
		int sx2 = subancho;
		int sy2 = subalto;
		
		return imagen.getSubimage(sx1, sy1, sx2, sy2);
	}
}
