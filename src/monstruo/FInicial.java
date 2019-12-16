package monstruo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class FInicial {

	public FInicial() {

		JLabel txt_tam = new JLabel("Número de casillas :"),
				txt_num_agentes = new JLabel("Número de agentes: ");
		JTextField field_tam = new JTextField("10", 10),
				field_num_agentes = new JTextField("4", 10);
		JButton btn_inicia = new JButton("Iniciar");
		GridBagConstraints gbc = new GridBagConstraints();
		JFrame inicial = new JFrame("Monstruo");
		inicial.setLayout(new GridBagLayout());
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.ipadx = 20; // Anchuro de los elementos
		gbc.ipady = 10; // Altura de los elementos
		gbc.insets = new Insets(5, 10, 5, 10);
		gbc.gridx = 0;
		gbc.gridy = 0;
		inicial.add(txt_tam, gbc);
		gbc.gridx = 1;
		inicial.add(field_tam, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		inicial.add(txt_num_agentes, gbc);
		gbc.gridx = 1;
		inicial.add(field_num_agentes, gbc);
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		inicial.add(btn_inicia, gbc);
		inicial.pack();
		inicial.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		inicial.setLocationRelativeTo(null);
		inicial.setVisible(true);
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

	}
}
