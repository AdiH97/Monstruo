package monstruo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class FInicial {

	public FInicial() {

		try {
			JLabel txt_tam = new JLabel("Número de casillas :"),
					txt_num_agentes = new JLabel("Número de agentes: ");
			JTextField field_tam = new JTextField("10", 10),
					field_num_agentes = new JTextField("4", 10);
			JButton btn_inicia = new JButton("Iniciar");
			Atlas atlas = new Atlas("./res/atlas.png", 32, 32);

			ImageIcon image = new ImageIcon(ImageIO.read(new File("./res/blob.png")).getScaledInstance(128, 128, Image.SCALE_FAST));
			JLabel icon_img = new JLabel(image, JLabel.CENTER);
			JLabel title = new JLabel("La cueva del monstruo", JLabel.CENTER);

			GridBagConstraints gbc = new GridBagConstraints();
			JFrame inicial = new JFrame("La cueva del monstruo");

			// Establecer el layout de la ventana
			inicial.setLayout(new GridBagLayout());

			// Propiedades generales del layout
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(5, 10, 5, 10); // Espaciado entre los elementos

			// Añadir imagen
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 2; // ocupa 2 filas
			inicial.add(icon_img, gbc);
			
			// Añadir título
			gbc.gridy++;
			title.setFont(title.getFont().deriveFont(32.0f));
			inicial.add(title, gbc);

			gbc.ipadx = 20; // Anchuro de los elementos
			gbc.ipady = 10; // Altura de los elementos
			gbc.gridwidth = 1; // Todos los elementos ocuparán 1 fila

			// Casilla (0, 1)
			gbc.gridy++;
			inicial.add(txt_tam, gbc);

			// Casilla (1, 1)
			gbc.gridx = 1;
			inicial.add(field_tam, gbc);

			// Casilla (0, 2)
			gbc.gridy++;
			gbc.gridx = 0;
			inicial.add(txt_num_agentes, gbc);

			// Casilla (1, 2)
			gbc.gridx = 1;
			inicial.add(field_num_agentes, gbc);

			// Casilla (0, 3)
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.gridwidth = 2; // Ocupa 2 filas
			inicial.add(btn_inicia, gbc);

			// Propiedades de la ventana
			inicial.pack();
			inicial.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			inicial.setLocationRelativeTo(null);
			inicial.setVisible(true);
			
			
			// Acción del botón
			btn_inicia.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					try {
						int tam = Integer.parseInt(field_tam.getText());
						int agentes = Integer.parseInt(field_num_agentes.getText());

						if (agentes > 4 || agentes < 1) {
							JOptionPane.showConfirmDialog(null, "Número de agentes inválido!", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
						} else {
							inicial.dispose();

							Thread hilo = new Thread(new Runnable() {

								@Override
								public void run() {
									Monstruo monstruo = new Monstruo(tam, agentes);

								}
							});
							hilo.start();
						}
					} catch (NumberFormatException ex) {
						JOptionPane.showConfirmDialog(null, "Número de casillas/agentes inválido!", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		} catch (IOException ex) {
			System.err.println("[ERROR] Incial => " + ex.getMessage());
		}
	}
}
