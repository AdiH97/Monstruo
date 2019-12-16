package monstruo;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {

	public static void main(String[] args) {
		try {
			// Establecemos como apariencia de la ventana la del sistema operativo
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			FInicial inicial = new FInicial();
			//snew Monstruo(10, 4);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
			System.err.println("[ERROR] Incial => " + ex.getMessage());
		}
	}
}
