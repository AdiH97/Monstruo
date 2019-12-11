package monstruo;

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

public class Monstruo {

	private static final String TITLE = "Monstruo",
			STOP = "Parar robot",
			START = "Iniciar robot",
			VIEW_PERC = "Ver percepciones";

	// Separación entre los paneles de la ventana
	private static final int PADDING = 25;

	// Valores máximos y mínimos del slider/animación
	private static final int MIN_TICK = 60;
	private static final int MAX_TICK = 720;

	// Tamaño de los iconos
	private static final int ICON_SIZE = 30;

	// Valor del slider / espera entre ciclos
	private static int framerate = MIN_TICK;

	// Indica si el agente se puede mover
	private static boolean isMoving;

	// Indicar si se puede añadir/quitar paredes/agentes
	private static boolean canPaint = false;
	private static boolean canAddAgente = false;
	private static boolean canRemoveAgente = false;
	private static boolean canAddPared = true;
	private static boolean canRemovePared = false;

	// Indica si se pueden pintar las percepciones del agente
	private static boolean viewPercep = false;

	public static void main(String[] args) {

		try {
			/**
			 * *********************************************************************************************************
			 * INICIALIZACIÓN DE COMPONENTES
			 * *********************************************************************************************************
			 */

			/**
			 * PARTE UI. *
			 */
			// Establecemos como apariencia de la ventana la del sistema operativo
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			// Crear iconos de añadir y eliminar paredes
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
					// Botones con iconos
					jbAddWall = new JButton("Añadir paredes", icon_pencil),
					jbRemoveWall = new JButton("Eliminar paredes", icon_eraser),
					jbAddAgent = new JButton("Añadir agentes", icon_add),
					jbRemoveAgent = new JButton("Eliminar agentes", icon_remove);

			// Crear slider (control del límite de ticks)
			JSlider jslTicks = new JSlider(0, MIN_TICK, MAX_TICK, framerate);

			// Bordes del panel de Debug (añadir el padding a los cuatro lados)
			EmptyBorder ebPadding = new EmptyBorder(PADDING, PADDING, PADDING, PADDING);
			Border ebLowered = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

			// Títulos de los bordes
			TitledBorder tbTools = new TitledBorder(ebLowered, "Herramientas");
			TitledBorder tbControl = new TitledBorder(ebLowered, "Variables del agente");

			// Constantes del layout de la ventana (GridBagLayout)
			GridBagConstraints gbc = new GridBagConstraints();

			/**
			 * PARTE LÓGICA.
			 */
			final Atlas atlas = new Atlas("./res/atlas.png", 32, 32);
			Entorno jpEntorno = new Entorno(atlas, 10, 10);

			/**
			 * *********************************************************************************************************
			 * ESTABLECER LAS PROPIEDADES DE LOS COMPONENTES
			 * *********************************************************************************************************
			 */
			/**
			 * PARTE GRÁFICA. *
			 */
			// No se puede modificar el texto de los campos de texto
			jtfAccionp.setEditable(false);
			jtfAccion.setEditable(false);
			jtfWp.setEditable(false);
			jtfW.setEditable(false);

			// Personalizar slider
			jslTicks.setPaintTrack(true);		// Línea
			jslTicks.setPaintTicks(true);		// Marcas de ticks
			jslTicks.setPaintLabels(true);		// Texto de los ticks
			jslTicks.setMajorTickSpacing(120);	// Espacios entre ticks
			jslTicks.setMinorTickSpacing(120);

			// Añadir los bordes con títulos al panel correspondiente
			jpTools.setBorder(new CompoundBorder(ebPadding, tbTools));
			jpControl.setBorder(new CompoundBorder(ebPadding, tbControl));

			// Añadir botones al panel de herramientas
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

			// Establecer el layout del panel de herramientas (GroupLayout)
			GroupLayout tools_layout = new GroupLayout(jpTools);
			jpTools.setLayout(tools_layout);
			tools_layout.setAutoCreateGaps(true);
			tools_layout.setAutoCreateContainerGaps(true);

			// Establecer el layout del panel de Debug (GroupLayout)
			GroupLayout db_layout = new GroupLayout(jpControl);
			jpControl.setLayout(db_layout);
			db_layout.setAutoCreateGaps(true);
			db_layout.setAutoCreateContainerGaps(true);

			/**
			 * LAYOUTS. *
			 * 
			 * Para que los layouts tengan efecto, se tienen que definir antes que el layout de la ventana principal.
			 * 
			 * En los layouts debe haber una correspondencia entre los elementos horizontales y verticales (dos
			 * elementos que están en la misma posición en dos columnas diferentes estáran en la misma fila).
			 */
			// Horizontal (los elementos estarán centrados)
			tools_layout.setHorizontalGroup(tools_layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					// 1 grupo padre
					.addGroup(tools_layout.createSequentialGroup()
							// 1a columna
							.addGroup(tools_layout.createParallelGroup(GroupLayout.Alignment.TRAILING) // "Izquierda"
									.addComponent(jbAddWall)
									.addComponent(jbAddAgent)
							)
							// Separador
							.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
							// 2a columna
							.addGroup(tools_layout.createParallelGroup(GroupLayout.Alignment.LEADING) // "Derecha"
									.addComponent(jbRemoveWall)
									.addComponent(jbRemoveAgent)
							)
					)
			);
			// Vertical
			tools_layout.setVerticalGroup(tools_layout.createSequentialGroup()
					// 2 grupos padre (filas)
					.addGroup(tools_layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jbAddWall)
							.addComponent(jbRemoveWall)
					)
					.addGroup(tools_layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(jbAddAgent)
							.addComponent(jbRemoveAgent)
					)
			);

			/* Ordenar los elementos del panel */
			// Horizontal (los elementos estarán centrados)
			db_layout.setHorizontalGroup(db_layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					// 2 grupos padres y un elemento

					// 1r grupo
					.addGroup(db_layout.createSequentialGroup()
							// 1a columna
							.addGroup(db_layout.createParallelGroup(GroupLayout.Alignment.TRAILING) // "Izquierda"
									.addComponent(jlW)
									.addComponent(jlWp)
									.addComponent(jlAccion)
									.addComponent(jlAccionp)
							)
							// Separador
							.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
							// 2a columna
							.addGroup(db_layout.createParallelGroup(GroupLayout.Alignment.LEADING) // "Derecha"
									.addComponent(jlCount)
									.addComponent(jtfW)
									.addComponent(jtfWp)
									.addComponent(jtfAccion)
									.addComponent(jtfAccionp)
							)
					)
					// 2o grupo
					.addGroup(db_layout.createSequentialGroup()
							// No están dividos por columnas porque interesa que estén centrados (propiedad global)
							.addComponent(jbMoving)
							.addComponent(jbPercep)
					)
					// Componente (también centrado por la propiedad global)
					.addComponent(jslTicks)
			);
			// Vertical
			db_layout.setVerticalGroup(db_layout.createSequentialGroup()
					// 6 grupos padre (filas) y un elemento
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
			
			
			// Establecer la forma del layout de la ventana
			jfVentana.setLayout(new GridBagLayout());

			// Celda 0, 0 (Entorno)
			gbc.gridx = 0;
			gbc.gridy = 0;
			// Ocupará 2 celdas verticales
			gbc.gridheight = 2;
			jfVentana.add(jpEntorno, gbc); // Añadir el entorno con las constantes definidas

			// Celda 1, 0 (Panel de herramientas)
			gbc.gridx = 1;
			gbc.gridy = 0;
			// Parte superior de la ventana
			gbc.anchor = GridBagConstraints.PAGE_START;
			jfVentana.add(jpTools, gbc);

			// Celda 1, 1 (Panel de ebug)
			gbc.gridx = 1;
			gbc.gridy = 1;
			// Parte central de la ventana
			gbc.anchor = GridBagConstraints.PAGE_END;
			jfVentana.add(jpControl, gbc);

			// Establecer las propiedas de la ventana
			jfVentana.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			jfVentana.pack();
			jfVentana.setLocationRelativeTo(null);
			jfVentana.setAlwaysOnTop(true);
			jfVentana.setVisible(true);


			/**
			 * PARTE LÓGICA. *
			 */
			// El agente inicialmente está parado
			isMoving = false;

			/**
			 * *********************************************************************************************************
			 * EVENTOS
			 * *********************************************************************************************************
			 */
			// Evento del panel del entorno
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
					int casillaX = e.getX() / (atlas.getSubancho() * jpEntorno.getGfxFactorEscaladoIntegral());
					int casillaY = e.getY() / (atlas.getSubalto() * jpEntorno.getGfxFactorEscaladoIntegral());

					if (casillaX > 0 && casillaX < jpEntorno.getColumnas() - 1 && casillaY > 0 && casillaY < jpEntorno.getFilas() - 1) {
						if (e.getButton() == 1) // Izquierdo
						// Cambiar celda
						{
							/*if (canAddPared) {
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
							}*/
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

			// Evento del panel del entorno (arrastrar ratón)
			jpEntorno.addMouseMotionListener(new MouseMotionListener() {

				// Pintar/Borrar paredes mientras se mantega pulsado el botón izquierdo
				@Override
				public void mouseDragged(MouseEvent e) {
					int casillaX = e.getX() / (atlas.getSubancho() * jpEntorno.getGfxFactorEscaladoIntegral());
					int casillaY = e.getY() / (atlas.getSubalto() * jpEntorno.getGfxFactorEscaladoIntegral());

					if (casillaX > 0 && casillaX < jpEntorno.getColumnas() - 1 && casillaY > 0 && casillaY < jpEntorno.getFilas() - 1) {
						if (canPaint) // Izquierdo
						// Cambiar celda
						{
							/*if (canAddPared) {
								jpEntorno.cambiarAPared(casillaX, casillaY);
							} else if (canRemovePared) {
								jpEntorno.cambiarASuelo(casillaX, casillaY);
							}*/
						}
					}
				}

				@Override
				public void mouseMoved(MouseEvent e) {
				}
			});

			// Cambiar el valor del framerate con el cambio de valor del slider
			jslTicks.addChangeListener((ChangeEvent cl) -> {
				framerate = jslTicks.getValue();
			});

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

			/**
			 * *********************************************************************************************************
			 * BUCLE PRINCIPAL
			 * *********************************************************************************************************
			 */
			long time = System.nanoTime() / 1000000;
			while (true) {

				int uw = jfVentana.getContentPane().getWidth() - jpControl.getWidth();
				int uh = jfVentana.getContentPane().getHeight();
				jpEntorno.setIntegralFactor(uw, uh);

				// CALCULO //
				if (isMoving) {
					jpEntorno.ciclo();
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
			Logger.getLogger(Monstruo.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
