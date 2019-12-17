package monstruo;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;

public class Monstruo {

	private static final String TITLE = "Monstruo",
	 STOP = "Parar",
	 START = "Iniciar",
	 VIEW_PERC = "Ver percepciones",
	 HIDE_PERC = "Ocultar percepciones",
	 VIEWHIDE_PERC = "Ver/Ocultar percepciones",
	 STEP = "Siguiente paso",
	 RESET = "Resetear";

	// Separación entre los paneles de la ventana
	private static final int PADDING = 25;

	// Valores máximos y mínimos del slider/animación
	private static final int MIN_TICK = 60;
	private static final int MAX_TICK = 1080;

	// Tamaño de los iconos
	private static final int ICON_SIZE = 30;

	// Valor del slider / espera entre ciclos
	private int framerate = MIN_TICK;

	// Indica si el agente se puede mover
	private boolean isMoving;

	// Mapeo de los botones a booleanes
	// [addChest, removeChest, addBlob, removeBlob, addEmpty, removeEmpty]
	private boolean[] estado_btn = new boolean[8];

	// Indica si se pueden pintar las percepciones del agente
	private boolean viewPercep = false;

	// Array con los botones de la interfaz
	// (Cambiar el color de el resto de botones al color original)
	private ArrayList<JButton> botones = new ArrayList();

	// Booleano que indica si se realizará un paso (32 ciclos)
	private boolean doStep = false;

	private Entorno jpEntorno;
	private Atlas atlas;
	private PanelDebug pd;

	public Monstruo(int tam, int num_agentes) {
		try {
			/**
			 * *********************************************************************************************************
			 * INICIALIZACIÓN DE COMPONENTES
			 * *********************************************************************************************************
			 */
			/**
			 * PARTE UI. *
			 */
			// Crear iconos de añadir y eliminar paredes
			ImageIcon icon_addChest = new ImageIcon(new ImageIcon("./res/add_chest.png").getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT));
			ImageIcon icon_removeChest = new ImageIcon(new ImageIcon("./res/remove_chest.png").getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT));
			ImageIcon icon_addBlob = new ImageIcon(new ImageIcon("./res/add_blob.png").getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT));
			ImageIcon icon_removeBlob = new ImageIcon(new ImageIcon("./res/remove_blob.png").getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT));
			ImageIcon icon_addEmpty = new ImageIcon(new ImageIcon("./res/add_empty.png").getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT));
			ImageIcon icon_removeEmpty = new ImageIcon(new ImageIcon("./res/remove_empty.png").getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT));
			// Crear ventana
			JFrame jfVentana = new JFrame(TITLE);
			// Crear panel de debug
			JPanel jpControl = new JPanel();
			// Crear panel de información
			JPanel jpInfo = new JPanel();
			// Cambiar panel de debug del agente
			JComboBox jcAgente = new JComboBox();

			// Crear panel de herramientas
			JPanel jpTools = new JPanel();
			JButton jbMoving = new JButton(START),
			 jbPercep = new JButton(VIEW_PERC),
			 jbStep = new JButton(STEP),
			 jbReset = new JButton(RESET),
			 // Botones con iconos
			 jbAddChest = new JButton("Añadir tesoro", icon_addChest),
			 jbRemoveChest = new JButton("Eliminar tesoro", icon_removeChest),
			 jbAddBlob = new JButton("Añadir monstruo", icon_addBlob),
			 jbRemoveBlob = new JButton("Eliminar monstruo", icon_removeBlob),
			 jbAddEmpty = new JButton("Añadir precipicio", icon_addEmpty),
			 jbRemoveEmpty = new JButton("Eliminar precipicio", icon_removeEmpty);
			// Añadir botones al array
			botones.add(jbAddChest);
			botones.add(jbRemoveChest);
			botones.add(jbAddBlob);
			botones.add(jbRemoveBlob);
			botones.add(jbAddEmpty);
			botones.add(jbRemoveEmpty);
			// Crear slider (control del límite de ticks)
			JSlider jslTicks = new JSlider(0, MIN_TICK, MAX_TICK, framerate);
			// Bordes del panel de Debug (añadir el padding a los cuatro lados)
			EmptyBorder ebPadding = new EmptyBorder(PADDING, PADDING, PADDING, PADDING);
			Border ebLowered = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			// Títulos de los bordes
			TitledBorder tbTools = new TitledBorder(ebLowered, "Herramientas");
			TitledBorder tbControl = new TitledBorder(ebLowered, "Variables del agente");
			// Constantes de los layouts de cada panel
			GridBagConstraints gbc_tools = new GridBagConstraints();
			GridBagConstraints gbc_db = new GridBagConstraints();
			GridBagConstraints gbc = new GridBagConstraints();
			/**
			 * PARTE LÓGICA.
			 */
			atlas = new Atlas("./res/atlas.png", 32, 32);
			jpEntorno = new Entorno(atlas, tam, tam);
			jpEntorno.setNumAgentes(num_agentes);
			pd = new PanelDebug(jpEntorno.getAgentes()[0], atlas, 0);

			/**
			 * *********************************************************************************************************
			 * ESTABLECER LAS PROPIEDADES DE LOS COMPONENTES
			 * *********************************************************************************************************
			 */
			/**
			 * PARTE GRÁFICA. *
			 */
			// Personalizar slider
			jslTicks.setPaintTrack(true);		// Línea
			jslTicks.setPaintTicks(true);		// Marcas de ticks
			jslTicks.setPaintLabels(true);		// Texto de los ticks
			jslTicks.setMajorTickSpacing(120);	// Espacios entre ticks
			jslTicks.setMinorTickSpacing(120);
			// Añadir los bordes con títulos al panel correspondiente
			jpTools.setBorder(new CompoundBorder(ebPadding, tbTools));
			jpControl.setBorder(new CompoundBorder(ebPadding, tbControl));

			// Establecer los layouts
			jpTools.setLayout(new GridBagLayout());
			jpControl.setLayout(new GridBagLayout());
			jfVentana.setLayout(new GridBagLayout());

			// Añadir opciones de los agente al combobox
			for (int i = 0; i < num_agentes; i++) {
				jcAgente.addItem("Agente " + i);
			}
			/**
			 * LAYOUTS. *
			 */
			/**
			 * Layout del panel de herramientas *
			 */
			// Casilla (0, 0)
			gbc_tools.gridx = 0;
			gbc_tools.gridy = 0;
			gbc_tools.fill = GridBagConstraints.HORIZONTAL;
			gbc_tools.ipadx = 70; // Anchuro de los elementos
			gbc_tools.ipady = 10; // Altura de los elementos
			jpTools.add(jbAddChest, gbc_tools);
			gbc_tools.gridy++;
			jpTools.add(jbAddBlob, gbc_tools);
			gbc_tools.gridy++;
			jpTools.add(jbAddEmpty, gbc_tools);
			// Siguiente columna
			gbc_tools.gridx++;
			// Casilla (0, 0)
			gbc_tools.gridy = 0;
			jpTools.add(jbRemoveChest, gbc_tools);
			gbc_tools.gridy++;
			jpTools.add(jbRemoveBlob, gbc_tools);
			gbc_tools.gridy++;
			jpTools.add(jbRemoveEmpty, gbc_tools);
			/**
			 * Layout del panel de debug *
			 */
			gbc_db.fill = GridBagConstraints.HORIZONTAL;
			// El botón de 'Ocultar percepciones' desplaza el panel de entorno y hace que no sea visible cuando no está
			// en pantalla completa. Esto se debe a que el ipad se suma al tamaño del texto de los botones.
			gbc_db.ipadx = 30;
			gbc_db.ipady = 10;
			gbc_db.gridx = 0;
			gbc_db.gridy = 0;
			gbc_db.gridheight = 1;
			gbc_db.gridwidth = 1;
			jpControl.add(jbReset, gbc_db);
			gbc_db.gridx = 1;
			gbc_db.gridy = 0;
			gbc_db.gridheight = 1;
			gbc_db.gridwidth = 1;
			jpControl.add(jbMoving, gbc_db);
			gbc_db.gridx = 2;
			gbc_db.gridy = 0;
			gbc_db.gridheight = 1;
			gbc_db.gridwidth = 1;
			jpControl.add(jbStep, gbc_db);
			gbc_db.gridx = 0;
			gbc_db.gridy = 1;
			gbc_db.gridheight = 1;
			gbc_db.gridwidth = 3;
			jpControl.add(jbPercep, gbc_db);
			gbc_db.gridy++;
			jpControl.add(jslTicks, gbc_db);
			gbc_db.gridy++;
			jpControl.add(jcAgente, gbc_db);
			gbc_db.gridx = 0;
			gbc_db.gridy++;
			gbc_db.gridheight = 2;
			gbc_db.gridwidth = 3;
			gbc_db.ipady = 20;
			jpControl.add(pd, gbc_db);

			/**
			 * Layout de la ventana principal *
			 */
			// Celda 0, 0 (Entorno)
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridheight = 2;
			gbc.gridwidth = 2;
			gbc.weightx = 1;
			gbc.weighty = 1;
			jfVentana.add(jpEntorno, gbc);
			// Celda 1, 0 (Panel de herramientas)
			gbc.gridx = 2;
			gbc.gridy = 0;
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			jfVentana.add(jpTools, gbc);
			// Celda 1, 1 (Panel de debug)
			gbc.gridx = 2;
			gbc.gridy = 1;
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			jfVentana.add(jpControl, gbc);
			// Establecer las propiedas de la ventana
			jfVentana.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			jfVentana.pack();
			jfVentana.setLocationRelativeTo(null);
			jfVentana.setVisible(true);

			/**
			 * PARTE LÓGICA. *
			 */
			// El agente inicialmente está parado
			isMoving = false;
			viewPercep = false;
			/**
			 * *********************************************************************************************************
			 * EVENTOS
			 * *********************************************************************************************************
			 */
			// Evento del panel del entorno
			jpEntorno.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {

				}

				@Override
				public void mousePressed(MouseEvent e) {

				}

				@Override
				public void mouseReleased(MouseEvent e) {
					int casillaX = e.getX() / (atlas.getSubancho() * jpEntorno.getgFactorEscalado());
					int casillaY = e.getY() / (atlas.getSubalto() * jpEntorno.getgFactorEscalado());

					if (casillaX > 0 && casillaX < jpEntorno.getAlto() - 1 && casillaY > 0 && casillaY < jpEntorno.getAncho() - 1) {
						if (e.getButton() == 1) // Izquierdo
						{
							if (estado_btn[0]) {
								jpEntorno.addElemento(casillaX, casillaY, Entorno.TESORO);
							} else if (estado_btn[1]) {
								jpEntorno.removeElemento(casillaX, casillaY, Entorno.TESORO);
							} else if (estado_btn[2]) {
								jpEntorno.addElemento(casillaX, casillaY, Entorno.MONSTRUO);
							} else if (estado_btn[3]) {
								jpEntorno.removeElemento(casillaX, casillaY, Entorno.MONSTRUO);
							} else if (estado_btn[4]) {
								jpEntorno.addElemento(casillaX, casillaY, Entorno.PRECIPICIO);
							} else if (estado_btn[5]) {
								jpEntorno.removeElemento(casillaX, casillaY, Entorno.PRECIPICIO);
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
			});	// Cambiar el valor del framerate con el cambio de valor del slider
			jslTicks.addChangeListener((ChangeEvent cl) -> {
				framerate = jslTicks.getValue();
			});	// Parar/iniciar robot
			jbMoving.addActionListener((ActionEvent ae) -> {
				isMoving = !isMoving;
				if (isMoving) {
					jbMoving.setText(STOP);
				} else {
					jbMoving.setText(START);
				}

				// Deshabilitar los botones de añadir/quitar elementos cuando el juego se inicia el juego
				for (JButton btn : botones) {
					btnCambiarColor(null);
					btn.setEnabled(false);
				}
			});

			// Ver/ocultar percepciones
			jbPercep.addActionListener((ActionEvent ae) -> {
				viewPercep = !viewPercep;
				if (viewPercep) {
					jbPercep.setText(HIDE_PERC);
				} else {
					jbPercep.setText(VIEW_PERC);
				}
				jpEntorno.togglePercepciones(jcAgente.getSelectedIndex());
			});

			// Avanzar un ciclo
			jbStep.addActionListener((ActionEvent) -> {
				doStep = true;

				// Deshabilitar los botones de añadir/quitar elementos cuando el juego se inicia el juego
				for (JButton btn : botones) {
					btnCambiarColor(null);
					btn.setEnabled(false);
				}
			});

			// Resetear entorno
			jbReset.addActionListener((ActionEvent ae) -> {
				jpEntorno.reset();
				jpEntorno.setNumAgentes(num_agentes);
				// Para entorno
				jbMoving.setText(START);
				isMoving = false;
				// Reactivar botones de herramientas
				for (JButton btn : botones) {
					btnCambiarColor(null); // Desmarcar botones
					btn.setEnabled(true);
				}
			});

			// Cambiar panel de debug según agente seleccionado
			jcAgente.addActionListener((ActionEvent ae) -> {
				for (int i = 0; i < 4; i++) {
					jpEntorno.clearPercepciones(i);
				}
				jpEntorno.clearPercepciones(jcAgente.getSelectedIndex());
				if (viewPercep) {
					jpEntorno.togglePercepciones(jcAgente.getSelectedIndex());
				}
				pd.setAgente(jpEntorno.getAgentes()[jcAgente.getSelectedIndex()], jcAgente.getSelectedIndex());
			});

			// Eventos de los botones del panel del panel de herramientas.
			// (Se utiliza un array de botones para no tener el mismo texto tantas veces como botones haya en él)
			for (int i = 0; i < botones.size(); i++) {
				JButton actual = botones.get(i);
				ActionListener al;
				// Crea y añadir el evento del botón
				al = (ActionEvent e) -> {
					// Cambiar color e indicar la acción que se realizará en el entorno
					btnCambiarColor(actual);
				};

				actual.addActionListener(al);
			}
			/**
			 * *********************************************************************************************************
			 * BUCLE PRINCIPAL
			 * *********************************************************************************************************
			 */
			long time = System.nanoTime() / 1000000;
			while (true) {

				int uw = jfVentana.getContentPane().getWidth() - jpControl.getWidth();
				int uh = jfVentana.getContentPane().getHeight();
				jpEntorno.setgFactorEscalado(uw, uh);

				// CALCULO //
				if (isMoving || doStep) {
					jpEntorno.ciclo();
				}

				// Para el 'Siguiente paso' cuando hayan pasod 32 ciclos
				if (jpEntorno.getCiclos() % 32 == 0) {
					doStep = false;
				}

				// PINTADO //
				jfVentana.getContentPane().revalidate();
				jfVentana.getContentPane().repaint();

				while ((System.nanoTime() / 1000000 - time) < (1000 / framerate)) {
					// espera activa hasta que llegue el momento de pintar
				}

				time = System.nanoTime() / 1000000;
			}
		} catch (IOException ex) {
			System.err.println("[ERROR] Monstruo => " + ex.getMessage());
		}
	}

	/**
	 * Metodo para cambiar la acción en el entorno y los colores de los botones.
	 *
	 * @param btn Botón seleccionado.
	 */
	private void btnCambiarColor(JButton btn) {
		for (int i = 0; i < botones.size(); i++) {
			JButton actual = botones.get(i);

			// Cambiar el color e indicar la acción que se realizará.
			// Las acciones están mapeadas en el mismo orden que los botones (se puede utilizar el mismo índice)
			if (actual == btn) {
				actual.setBackground(Color.GREEN);
				// Necesario para poder cambiar el color
				actual.setOpaque(true);
				actual.setBorderPainted(false);

				estado_btn[i] = true;
			} else {
				actual.setBackground(new Color(240, 240, 240));
				actual.setOpaque(true);
				// Si no se vuelve a cambiar el valor a true, el fondo de los botones se elimina.
				actual.setBorderPainted(true);

				estado_btn[i] = false;
			}
		}
	}
}
