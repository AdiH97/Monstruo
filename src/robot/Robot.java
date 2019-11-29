package robot;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;

public class Robot {

	private static final String TITLE = "Robot",
			STOP = "Parar robot",
			START = "Iniciar robot",
			VIEW_PERC = "Ver percepciones",
			NVIEW_PERC = "Ocultar percepciones";

	private static final int PADDING = 25;
	private static final int MIN_TICK = 60;
	private static final int MAX_TICK = 360;
	private static final int ICON_SIZE = 30;

	private static int framerate = 60;
	private static boolean isMoving;
	private static boolean canPaint = false;
	private static boolean canAddAgente = false;
	private static boolean canRemoveAgente = false;
	private static boolean canAddPared = true;
	private static boolean canRemovePared = false;
	private static boolean viewPercep = false;

	public static void main(String[] args) {

		try {
			// ADQUISICIÓN DE RECURSOS //
			// Establecemos como apariencia de la ventana la del sistema operativo
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			// Cargamos la imagen del atlas de texturas
			final Atlas atlas = new Atlas("./res/atlas.png", 32, 32);

			// CONSTRUCCIÓN E INICIALIZACIÓN DE LA INTERFAZ GRÁFICA //
			// El agente inicialmente está parado
			isMoving = false;
			Entorno jpEntorno = new Entorno(atlas, 20, 20);

			// Evento del entorno (click)
			jpEntorno.addMouseListener(new MouseListener() {
				// Pintar/Borrar pared
				@Override
				public void mouseClicked(MouseEvent e) {

				}

				// Indicar al evento de arrastrar que puede pintar
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.getButton() == 1) // Izquierdo
					{
						canPaint = true;
					}
				}

				// Indicar al evento de arrastrar que no puede pintar
				@Override
				public void mouseReleased(MouseEvent e) {
					canPaint = false;
					int casillaX = e.getX() / (atlas.getSubancho() * jpEntorno.getIntegralFactor());
					int casillaY = e.getY() / (atlas.getSubalto() * jpEntorno.getIntegralFactor());

					if (casillaX > 0 && casillaX < jpEntorno.getColumnas() - 1 && casillaY > 0 && casillaY < jpEntorno.getFilas() - 1) {
						if (e.getButton() == 1) // Izquierdo
						// Cambiar celda
						{
							if (canAddPared) {
								jpEntorno.cambiarAPared(casillaX, casillaY);
							} else if (canRemovePared) {
								jpEntorno.cambiarASuelo(casillaX, casillaY);
							} else if (canAddAgente && !jpEntorno.tipoCasilla(casillaX, casillaY)) {
								Agente agente = new Agente(atlas, casillaX, casillaY);
								agente.setShowPerceptions(viewPercep);
								jpEntorno.addAgente(agente);
							} else if (canRemoveAgente) {
								int agente_idx = jpEntorno.getAgenteIdx(casillaX, casillaY);
								if (agente_idx != -1) {
									jpEntorno.removeAgente(agente_idx);
								}
							}
						}
					}
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}
			});

			// Evento del entorno (arrastrar ratón)
			jpEntorno.addMouseMotionListener(new MouseMotionListener() {

				// Pintar/Borrar paredes mientras se mantega pulsado el botón izquierdo
				@Override
				public void mouseDragged(MouseEvent e) {
					int casillaX = e.getX() / (atlas.getSubancho() * jpEntorno.getIntegralFactor());
					int casillaY = e.getY() / (atlas.getSubalto() * jpEntorno.getIntegralFactor());

					if (casillaX > 0 && casillaX < jpEntorno.getColumnas() - 1 && casillaY > 0 && casillaY < jpEntorno.getFilas() - 1) {
						if (canPaint) // Izquierdo
						// Cambiar celda
						{
							if (canAddPared) {
								jpEntorno.cambiarAPared(casillaX, casillaY);
							} else if (canRemovePared) {
								jpEntorno.cambiarASuelo(casillaX, casillaY);
							}
						}
					}
				}

				@Override
				public void mouseMoved(MouseEvent e) {
				}
			});			// Crear iconos de añadir y eliminar paredes
			ImageIcon icon_pencil = new ImageIcon(new ImageIcon("./res/pencil.png").getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT));
			ImageIcon icon_eraser = new ImageIcon(new ImageIcon("./res/eraser.png").getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT));
			ImageIcon icon_add = new ImageIcon(new ImageIcon("./res/add.png").getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT));
			ImageIcon icon_remove = new ImageIcon(new ImageIcon("./res/remove.png").getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT));

			// Crear ventana
			JFrame jfVentana = new JFrame(TITLE);

			// Crear panel de debug
			JPanel jpControl = new JPanel();

			// Crear panel de herramientas
			JPanel jpTools = new JPanel();

			// Crear labels
			JLabel jlAccionp = new JLabel("Acción anterior: "),
					jlAccion = new JLabel("Acción actual: "),
					jlWp = new JLabel("Vector anterior: "),
					jlW = new JLabel("Vector actual: "),
					jlCount = new JLabel("w0     w1    w2    w3    w4    w5    w6    w7");

			// Crear campos de texto
			JTextField jtfAccionp = new JTextField(25),
					jtfAccion = new JTextField(25),
					jtfW = new JTextField(25),
					jtfWp = new JTextField(25);

			// Crear botón de parada
			JButton jbMoving = new JButton(START),
					jbPercep = new JButton(VIEW_PERC),
					jbAddWall = new JButton("Añadir paredes", icon_pencil),
					jbRemoveWall = new JButton("Eliminar paredes", icon_eraser),
					jbAddAgent = new JButton("Añadir agentes", icon_add),
					jbRemoveAgent = new JButton("Eliminar agentes", icon_remove);

			// Botón de añadir paredes seleccionado por defecto
			jbAddWall.setEnabled(false);

			// Crear slider (control del límite de ticks)
			JSlider jslTicks = new JSlider(0, MIN_TICK, MAX_TICK, framerate);

			// No se puede modificar el texto de los campos de texto
			jtfAccionp.setEditable(false);
			jtfAccion.setEditable(false);
			jtfWp.setEditable(false);
			jtfW.setEditable(false);

			// Personalizar slider
			jslTicks.setPaintTrack(true);     // Línea
			jslTicks.setPaintTicks(true);     // Marcas de ticks
			jslTicks.setPaintLabels(true);    // Texto de los ticks
			jslTicks.setMajorTickSpacing(50); // Espacios entre ticks
			jslTicks.setMinorTickSpacing(50);
			jslTicks.addChangeListener((ChangeEvent cl) -> {
				// Cuando se cambia el valor del slider, cambia el valor del 
				// límite de ticks
				framerate = jslTicks.getValue();
			});

			// Establecer foco
			jbMoving.setFocusable(true);
			jbMoving.requestFocus();
			jbMoving.grabFocus();

			// Parar o iniciar robot
			jbMoving.addActionListener((ActionEvent ae) -> {
				isMoving = !isMoving;
				if (isMoving) {
					jbMoving.setText(STOP);
				} else {
					jbMoving.setText(START);
				}
			});

			// Indicar que se pintarán paredes
			jbAddWall.addActionListener((ActionEvent ae) -> {
				// Habilitar todos los botones menos el actual
				jbAddWall.setEnabled(false);
				jbRemoveWall.setEnabled(true);
				jbAddAgent.setEnabled(true);
				jbRemoveAgent.setEnabled(true);

				canAddPared = true;
				canRemovePared = false;
				canAddAgente = false;
				canRemoveAgente = false;
			});

			// Indicar que se borrarán paredes
			jbRemoveWall.addActionListener((ActionEvent ae) -> {
				// Habilitar todos los botones menos el actual
				jbAddWall.setEnabled(true);
				jbRemoveWall.setEnabled(false);
				jbAddAgent.setEnabled(true);
				jbRemoveAgent.setEnabled(true);

				canAddPared = false;
				canRemovePared = true;
				canAddAgente = false;
				canRemoveAgente = false;
			});

			// Indicar que se añadirá un agente
			jbAddAgent.addActionListener((ActionEvent ae) -> {
				// Habilitar todos los botones menos el actual
				jbAddWall.setEnabled(true);
				jbRemoveWall.setEnabled(true);
				jbAddAgent.setEnabled(false);
				jbRemoveAgent.setEnabled(true);

				canAddPared = false;
				canRemovePared = false;
				canAddAgente = true;
				canRemoveAgente = false;
			});

			// Eliminar un agente al entorno
			jbRemoveAgent.addActionListener((ActionEvent ae) -> {
				// Habilitar todos los botones menos el actual
				jbAddWall.setEnabled(true);
				jbRemoveWall.setEnabled(true);
				jbAddAgent.setEnabled(true);
				jbRemoveAgent.setEnabled(false);

				canAddPared = false;
				canRemovePared = false;
				canAddAgente = false;
				canRemoveAgente = true;
			});
			
			// Ver/Ocultar percepciones
			jbPercep.addActionListener((ActionEvent ae) -> {
				viewPercep = !viewPercep;
				
				for(Agente agente : jpEntorno.agentes()) {
					agente.setShowPerceptions(viewPercep);
				}
			});

			// Bordes del panel de Debug
			EmptyBorder ebPadding = new EmptyBorder(PADDING, PADDING, PADDING, PADDING);
			Border ebLowered = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

			TitledBorder tbTools = new TitledBorder(ebLowered, "Herramientas");
			TitledBorder tbControl = new TitledBorder(ebLowered, "Variables del agente");

			jpTools.setBorder(new CompoundBorder(ebPadding, tbTools));
			jpControl.setBorder(new CompoundBorder(ebPadding, tbControl));

			// Añadir iconos al panel de herramientas
			jpTools.add(jbAddWall);
			jpTools.add(jbRemoveWall);
			jpTools.add(jbAddAgent);
			jpTools.add(jbRemoveAgent);

			// Añadir labels y campos de texto al panel de debug
			jpControl.add(jlAccionp);
			jpControl.add(jtfAccionp);
			jpControl.add(jlWp);
			jpControl.add(jtfWp);
			jpControl.add(jlAccion);
			jpControl.add(jtfAccion);
			jpControl.add(jlW);
			jpControl.add(jtfW);
			jpControl.add(jbMoving);
			jpControl.add(jbPercep);
			jpControl.add(jslTicks);

			// Establecer el layout del panel de herramientas
			GroupLayout tools_layout = new GroupLayout(jpTools);
			jpTools.setLayout(tools_layout);
			tools_layout.setAutoCreateGaps(true);
			tools_layout.setAutoCreateContainerGaps(true);

			// Ordenar los elementos del panel
			tools_layout.setHorizontalGroup(tools_layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addGroup(tools_layout.createSequentialGroup()
							.addGroup(tools_layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
									.addComponent(jbAddWall)
									.addComponent(jbAddAgent)
							)
							.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
							.addGroup(tools_layout.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(jbRemoveWall)
									.addComponent(jbRemoveAgent)
							)
					)
			);
			tools_layout.setVerticalGroup(tools_layout.createSequentialGroup()
					.addGroup(tools_layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jbAddWall)
							.addComponent(jbRemoveWall)
					)
					.addGroup(tools_layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jbAddAgent)
							.addComponent(jbRemoveAgent)
					)
			);

			// Establecer el layout del panel de Debug
			GroupLayout db_layout = new GroupLayout(jpControl);
			jpControl.setLayout(db_layout);
			db_layout.setAutoCreateGaps(true);
			db_layout.setAutoCreateContainerGaps(true);

			// Ordenar los elementos del panel
			db_layout.setHorizontalGroup(db_layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addGroup(db_layout.createSequentialGroup()
							.addGroup(db_layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
									.addComponent(jlW)
									.addComponent(jlWp)
									.addComponent(jlAccion)
									.addComponent(jlAccionp)
							)
							.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
							.addGroup(db_layout.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(jlCount)
									.addComponent(jtfW)
									.addComponent(jtfWp)
									.addComponent(jtfAccion)
									.addComponent(jtfAccionp)
							)
					)
					.addGroup(db_layout.createSequentialGroup()
							.addComponent(jbMoving)
							.addComponent(jbPercep)
					)
					.addComponent(jslTicks)
			);
			db_layout.setVerticalGroup(db_layout.createSequentialGroup()
					.addGroup(db_layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jlCount)
					)
					.addGroup(db_layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jlWp)
							.addComponent(jtfWp)
					)
					.addGroup(db_layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jlW)
							.addComponent(jtfW)
					)
					.addGroup(db_layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jlAccionp)
							.addComponent(jtfAccionp)
					)
					.addGroup(db_layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jlAccion)
							.addComponent(jtfAccion)
					)
					.addGroup(db_layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jbMoving)
							.addComponent(jbPercep)
					)
					.addComponent(jslTicks)
			);

			// Establecer las propiedas de la ventana
			GridBagConstraints gbc = new GridBagConstraints();

			jfVentana.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			jfVentana.setLayout(new GridBagLayout());

			// Celda 0, 0
			gbc.gridx = 0;
			gbc.gridy = 0;
			// Ocupará 2 celdas verticales
			gbc.gridheight = 2;
			jfVentana.add(jpEntorno, gbc); // Constantes por defecto

			// Celda 1, 0
			gbc.gridx = 1;
			gbc.gridy = 0;
			// Parte superior de la ventana
			gbc.anchor = GridBagConstraints.PAGE_START;
			jfVentana.add(jpTools, gbc);

			// Celda 1, 1
			gbc.gridx = 1;
			gbc.gridy = 1;
			// Parte central de la ventana
			gbc.anchor = GridBagConstraints.PAGE_END;
			jfVentana.add(jpControl, gbc);

			jfVentana.pack();
			jfVentana.setLocationRelativeTo(null);
			jfVentana.setVisible(true);

			// BUCLE PRINCIPAL //
			long time = System.nanoTime() / 1000000;
			while (true) {

				int uw = jfVentana.getContentPane().getWidth() - jpControl.getWidth();
				int uh = jfVentana.getContentPane().getHeight();
				jpEntorno.setIntegralFactor(uw, uh);

				// CALCULO //
				if (isMoving) {
					jpEntorno.ciclo();
				}
				if (jpEntorno.agentes().size() >= 1 && jpEntorno.agentes().get(0) != null) {
					// Actualizar campos de debug
					jtfAccion.setText(jpEntorno.agentes().get(0).getTxtAccion());
					jtfAccionp.setText(jpEntorno.agentes().get(0).getTxtAccionp());
					jtfW.setText(jpEntorno.agentes().get(0).getTxtW());
					jtfWp.setText(jpEntorno.agentes().get(0).getTxtWp());
				}

				// PINTADO //
				jfVentana.getContentPane().revalidate();
				jfVentana.getContentPane().repaint();

				while ((System.nanoTime() / 1000000 - time) < (1000 / framerate)) {
					// espera activa hasta que llegue el momento de pintar
				}

				time = System.nanoTime() / 1000000;
			}
		} catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
			Logger.getLogger(Robot.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
