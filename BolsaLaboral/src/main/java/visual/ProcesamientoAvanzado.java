package visual;

import logico.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ProcesamientoAvanzado extends JDialog {

	private final JPanel contentPanel = new JPanel();
	public static JTable tablaOfertas;
	public static DefaultTableModel modeloOfertas = new DefaultTableModel() {
		@Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
		
		public Class getColumnClass(int column) {
			Object value = getRowCount() == 0 ? null : getValueAt(0, column);
			return value == null ? Object.class : value.getClass();
		}
	};
	public static Object[] rowOferta;
	
	private JButton btnProcesar;
	private JTextField txtFiltro;
	private static JTable tablaMatcheo;
	public static DefaultTableModel modeloMatcheo = new DefaultTableModel() {
		@Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
		
		public Class getColumnClass(int column) {
			Object value = getRowCount() == 0 ? null : getValueAt(0, column);
			return value == null ? Object.class : value.getClass();
		}
	};
	public static Object[] rowMatcheo;
	private ResultadoMatcheo resMatchSelec = null;
	private OfertaLaboral ofertaSelec = null;
	private JLabel lblRazonProcesamiento;
	
	private static ArrayList<OfertaLaboral> ofertas = new ArrayList<>();
	private static ArrayList<ResultadoMatcheo> resultados = new ArrayList<>();
	
	
	/**
	 * Create the dialog.
	 */
	public ProcesamientoAvanzado() {
		AutorizacionService.exigirPermiso(
				BolsaLaboral.getInstancia().getUsuarioActual(),
				Permiso.USAR_PROCESAMIENTO_AVANZADO);
		setTitle("Procesamiento Avanzado de Ofertas");
		setIconImage(UIUtils.image("icono.png"));
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setBackground(new Color(228,228,228));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			panel.setBackground(new Color(228,228,228));
			panel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new GridLayout(2, 1, UIUtils.scale(8), UIUtils.scale(8)));
			{
				JScrollPane scrollPane = new JScrollPane();
				scrollPane.setPreferredSize(UIUtils.dimension(700, 210));
				panel.add(scrollPane);
				{
					tablaOfertas = new JTable();
					tablaOfertas.setForeground(Color.BLACK);
					UIUtils.configureTable(tablaOfertas);
					tablaOfertas.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							int index = tablaOfertas.getSelectedRow();
							if (index >= 0) {
								ofertaSelec = BolsaLaboral.getInstancia()
										.buscarOfertaByCodigo(
												tablaOfertas.getValueAt(index, 0).toString());
								resMatchSelec = null;
								tablaMatcheo.clearSelection();
								actualizarBoton();
							}
						}
					});
					String [] headers = {"Código", "Puesto", "Ofertador", "Área", "Estado"};
					modeloOfertas.setColumnIdentifiers(headers);
					tablaOfertas.setModel(modeloOfertas);
					scrollPane.setViewportView(tablaOfertas);
				}
			}
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setPreferredSize(UIUtils.dimension(700, 240));
			panel.add(scrollPane);
			
			tablaMatcheo = new JTable();
			tablaMatcheo.setForeground(Color.BLACK);
			UIUtils.configureTable(tablaMatcheo);
			tablaMatcheo.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int index = tablaMatcheo.getSelectedRow();
					if(index >= 0) {
						resMatchSelec = BolsaLaboral.getInstancia().buscarResultado(resultados,tablaMatcheo.getValueAt(index,0).toString(),tablaMatcheo.getValueAt(index,1).toString());
						ofertaSelec = resMatchSelec == null ? null : resMatchSelec.getOferta();
						actualizarBoton();
					}
				}
			});
			String [] headers = {"Oferta", "Código", "Nombre", "Porcentaje", "Condición"};
			modeloMatcheo.setColumnIdentifiers(headers);
			tablaMatcheo.setModel(modeloMatcheo);
			scrollPane.setViewportView(tablaMatcheo);
		}
		{
			JPanel pnlFiltro = new JPanel();
			pnlFiltro.setBackground(new Color(228, 228, 228));
			contentPanel.add(pnlFiltro, BorderLayout.NORTH);
			{
				JLabel lblIconFiltrar = new JLabel("");
				lblIconFiltrar.setIcon(UIUtils.icon("filtrar.png"));
				pnlFiltro.add(lblIconFiltrar);
				
			}
			{
				JLabel lblNewLabel = new JLabel("Criterio del Filtro: ");
				lblNewLabel.setForeground(Color.BLACK);
				lblNewLabel.setFont(UIUtils.largeFont(Font.BOLD));
				pnlFiltro.add(lblNewLabel);
			}
			{
				txtFiltro = new JTextField();
				txtFiltro.setFont(UIUtils.defaultFont(Font.PLAIN));
				txtFiltro.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						filtrar();
					}
				});
				pnlFiltro.add(txtFiltro);
				txtFiltro.setColumns(16);
			}
		}
		{
			JPanel pie = new JPanel(new BorderLayout());
			pie.setBackground(new Color(4, 87, 87));
			getContentPane().add(pie, BorderLayout.SOUTH);
			lblRazonProcesamiento = new JLabel(" ");
			lblRazonProcesamiento.setForeground(Color.WHITE);
			lblRazonProcesamiento.setFont(UIUtils.defaultFont(Font.PLAIN));
			lblRazonProcesamiento.setBorder(UIUtils.emptyBorder(4, 10, 2, 10));
			pie.add(lblRazonProcesamiento, BorderLayout.NORTH);
			JPanel buttonPane = new JPanel();
			buttonPane.setBackground(new Color(4, 87, 87));
			buttonPane.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			pie.add(buttonPane, BorderLayout.SOUTH);
			{
				btnProcesar = new JButton("Procesar");
				btnProcesar.setBackground(Color.WHITE);
				btnProcesar.setIcon(UIUtils.icon("vincular.png"));
				btnProcesar.setFont(UIUtils.largeFont(Font.BOLD));
				btnProcesar.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						DecisionProcesamiento decision = BolsaLaboral.getInstancia()
								.evaluarVinculacion(resMatchSelec);
						if (!decision.isPermitido()) {
							JOptionPane.showMessageDialog(ProcesamientoAvanzado.this,
									decision.getRazon(), "No se puede procesar",
									JOptionPane.WARNING_MESSAGE);
							actualizarBoton();
							return;
						}
						try {
							if(BolsaLaboral.getInstancia().vincularOferta(resMatchSelec)) {
								JOptionPane.showMessageDialog(ProcesamientoAvanzado.this,
										"Se ha creado la solicitud correctamente a la oferta "
												+ resMatchSelec.getOferta().getPuesto() + ".",
										"Información", JOptionPane.INFORMATION_MESSAGE);
							}
						} catch (SecurityException exception) {
							JOptionPane.showMessageDialog(ProcesamientoAvanzado.this,
									exception.getMessage(), "Acción no autorizada",
									JOptionPane.WARNING_MESSAGE);
						}
						recargarDatos();
					}
				});
				btnProcesar.setEnabled(false);
				buttonPane.add(btnProcesar);
			}
			{
				JButton btnCancelar = new JButton("Cancelar");
				btnCancelar.setBackground(Color.WHITE);
				btnCancelar.setIcon(UIUtils.icon("cerrar.png"));
				btnCancelar.setFont(UIUtils.largeFont(Font.BOLD));
				btnCancelar.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				btnCancelar.setActionCommand("Cancel");
				buttonPane.add(btnCancelar);
			}
		}

		resultados = BolsaLaboral.getInstancia().procesamientoAvanzando();
		ofertas = BolsaLaboral.getInstancia().ofertasDisponibles();
		cargarResultados();
		cargarOfertas();
		actualizarBoton();
		UIUtils.finishDialog(this, getOwner(), 820, 620);
	}
	
	public void filtrar() {
	    String filtro = txtFiltro.getText().toLowerCase();
	    modeloOfertas.setRowCount(0);
	    rowOferta = new Object[tablaOfertas.getColumnCount()];
	    ofertaSelec = null;
	    resMatchSelec = null;

	    ArrayList<OfertaLaboral> ofertasVisibles = new ArrayList<>();

	    for (OfertaLaboral aux : BolsaLaboral.getInstancia().getOfertas()) {
		if (aux == null || aux.getOfertador() == null) {
			continue;
		}
	        boolean coincide =
	            aux.getCodigo().toLowerCase().contains(filtro) ||
	            aux.getOfertador().getNombre().toLowerCase().contains(filtro) ||
	            aux.getPuesto().toLowerCase().contains(filtro) ||
	            aux.getArea().toLowerCase().contains(filtro) ||
	            aux.getEstado().toLowerCase().contains(filtro);
	        
	        if (coincide) {
	            rowOferta[0] = aux.getCodigo();
	            rowOferta[1] = aux.getPuesto();
	            rowOferta[2] = aux.getOfertador().getNombre();
	            rowOferta[3] = UIUtils.valueIcon(aux.getArea());
	            rowOferta[4] = aux.getEstado();
	            modeloOfertas.addRow(rowOferta);
	            ofertasVisibles.add(aux);
	        }
	    }

	    actualizarResultadosFiltrados(ofertasVisibles);
	    actualizarBoton();
	}

	private void actualizarResultadosFiltrados(ArrayList<OfertaLaboral> ofertasVisibles) {
	    modeloMatcheo.setRowCount(0);
	    rowMatcheo = new Object[tablaMatcheo.getColumnCount()];
	    
	    for (ResultadoMatcheo aux : resultados) {
	        if (ofertasVisibles.contains(aux.getOferta())) {
	            rowMatcheo[0] = aux.getOferta().getCodigo();
	            rowMatcheo[1] = aux.getSolicitante().getCodigo();
	            rowMatcheo[2] = aux.getSolicitante().getNombres() + " " + aux.getSolicitante().getApellidos();
	            rowMatcheo[3] = aux.getPorcentaje() + "%";
	            rowMatcheo[4] = UIUtils.valueIcon(aux.getCondicion());
	            modeloMatcheo.addRow(rowMatcheo);
	        }
	    }
	}
	
	public static void cargarOfertas() {
		modeloOfertas.setRowCount(0);
		rowOferta = new Object[tablaOfertas.getColumnCount()];
		for (OfertaLaboral aux : ofertas) {
			rowOferta[0] = aux.getCodigo();
            rowOferta[1] = aux.getPuesto();
            rowOferta[2] = aux.getOfertador().getNombre();
	            rowOferta[3] = UIUtils.valueIcon(aux.getArea());
            rowOferta[4] = aux.getEstado();
			modeloOfertas.addRow(rowOferta);
		}
	}
	
	public static void cargarResultados() {
		modeloMatcheo.setRowCount(0);
		rowMatcheo = new Object[tablaMatcheo.getColumnCount()];
		for (ResultadoMatcheo aux : resultados) {
			rowMatcheo[0] = aux.getOferta().getCodigo();
            rowMatcheo[1] = aux.getSolicitante().getCodigo();
            rowMatcheo[2] = aux.getSolicitante().getNombres() + " " + aux.getSolicitante().getApellidos();
            rowMatcheo[3] = aux.getPorcentaje() + "%";
	            rowMatcheo[4] = UIUtils.valueIcon(aux.getCondicion());
			modeloMatcheo.addRow(rowMatcheo);
		}
	}

	private void actualizarBoton() {
		DecisionProcesamiento decision;
		if (resMatchSelec != null) {
			decision = BolsaLaboral.getInstancia()
					.evaluarVinculacion(resMatchSelec);
		} else if (ofertaSelec != null) {
			decision = BolsaLaboral.getInstancia()
					.evaluarProcesamiento(ofertaSelec);
		} else {
			decision = DecisionProcesamiento.rechazar(
					"Debe seleccionar una oferta y un candidato elegible.");
		}
		btnProcesar.setEnabled(resMatchSelec != null && decision.isPermitido());
		btnProcesar.setToolTipText(decision.getRazon());
		lblRazonProcesamiento.setText("Procesamiento: " + decision.getRazon());
		lblRazonProcesamiento.setToolTipText(decision.getRazon());
	}

	private void recargarDatos() {
		resultados = BolsaLaboral.getInstancia().procesamientoAvanzando();
		ofertas = BolsaLaboral.getInstancia().ofertasDisponibles();
		resMatchSelec = null;
		ofertaSelec = null;
		cargarResultados();
		cargarOfertas();
		actualizarBoton();
	}
}
