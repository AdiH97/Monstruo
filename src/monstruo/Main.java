package monstruo;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {

	public static void main(String[] args) {
		try {
			// Establecemos como apariencia de la ventana la del sistema operativo
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("Button.font", UIManager.getFont("Button.font").deriveFont(16.0f));
			UIManager.put("Label.font", UIManager.getFont("Button.font").deriveFont(16.0f));
			UIManager.put("TextField.font", UIManager.getFont("Button.font").deriveFont(16.0f));
			UIManager.put("ComboBox.font", UIManager.getFont("Button.font").deriveFont(16.0f));
			UIManager.put("OptionPane.messageFont", UIManager.getFont("Button.font").deriveFont(16.0f));
			UIManager.put("OptionPane.buttonFont", UIManager.getFont("Button.font").deriveFont(16.0f));
			FInicial inicial = new FInicial();
			//snew Monstruo(10, 4);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
			System.err.println("[ERROR] Main => " + ex.getMessage());
		}
	}
}
