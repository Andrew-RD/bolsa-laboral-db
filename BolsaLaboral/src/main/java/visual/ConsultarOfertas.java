package visual;

import logico.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import exception.NotRemovableException;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.SystemColor;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ConsultarOfertas extends JDialog {

	private final JPanel contentPanel = new JPanel();
	public static JTable table;
	public static DefaultTableModel modelo = new DefaultTableModel() {
		@Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
		
		public Class getColumnClass(int column) {
			Object value = getRowCount() == 0 ? null : getValueAt(0, column);
			return value == null ? Object.class : value.getClass();
		}
	};
	public static Object[] row;
	private OfertaLaboral seleccionado = null;
	private JButton btnUpdate;
	private JButton btnDelete;
	private JTextField txtFiltro;
	private JButton btnVincular;
	private JButton btnVerInforme;
	private JLabel lblRazonProcesamiento;
	
	/**
	 * Create the dialog.
	 */
	public ConsultarOfertas() {
		AutorizacionService.exigirPermiso(BolsaLaboral.getInstancia().getUsuarioActual(),
				Permiso.CONSULTAR_OFERTAS);
		setTitle("Listado de Ofertas Laborales");
		setIconImage(UIUtils.image("icono.png"));
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setBackground(new Color(228,228,228));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			panel.setBackground(SystemColor.desktop);
			panel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JScrollPane scrollPane = new JScrollPane();
				panel.add(scrollPane, BorderLayout.CENTER);
				{
					table = new JTable();
					table.setForeground(Color.BLACK);
					UIUtils.configureTable(table);
					table.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							int index = table.getSelectedRow();
							if(index >= 0) {
								seleccionado = BolsaLaboral.getInstancia().buscarOfertaByCodigo(table.getValueAt(index, 0).toString());
								actualizarBotones();
							}
						}
					});
					String [] headers = {"Código", "Puesto", "Ofertador", "Área", "Estado"};
					modelo.setColumnIdentifiers(headers);
					table.setModel(modelo);
					scrollPane.setViewportView(table);
				}
			}
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
				btnUpdate = new JButton("Modificar");
				btnUpdate.setBackground(Color.WHITE);
				btnUpdate.setIcon(UIUtils.icon("modificar.png"));
				btnUpdate.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						RegistroOfertaLaboral registro = new RegistroOfertaLaboral(seleccionado);
						registro.setLocationRelativeTo(ConsultarOfertas.this);
						registro.setVisible(true);
					}
				});
				{
					btnVincular = new JButton("Procesar");
					btnVincular.setIcon(UIUtils.icon("vincular.png"));
					btnVincular.setBackground(Color.WHITE);
					btnVincular.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							DecisionProcesamiento decision = BolsaLaboral.getInstancia()
									.evaluarProcesamiento(seleccionado);
							if (!decision.isPermitido()) {
								JOptionPane.showMessageDialog(ConsultarOfertas.this,
										decision.getRazon(), "No se puede procesar",
										JOptionPane.WARNING_MESSAGE);
								actualizarBotones();
								return;
							}
							try {
								ResultadosVinculacion res = new ResultadosVinculacion(seleccionado);
								res.setModal(true);
								res.setLocationRelativeTo(ConsultarOfertas.this);
								res.setVisible(true);
								cargarOfertas();
								actualizarBotones();
							} catch (IllegalStateException | SecurityException exception) {
								JOptionPane.showMessageDialog(ConsultarOfertas.this,
										exception.getMessage(), "No se puede procesar",
										JOptionPane.WARNING_MESSAGE);
							}
						}
					});
					{
						btnVerInforme = new JButton("Ver Informe");
						btnVerInforme.setIcon(UIUtils.icon("vacantescomp.png"));
						btnVerInforme.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								InformeOferta inf = new InformeOferta(seleccionado);
								inf.setModal(true);
								inf.setLocationRelativeTo(ConsultarOfertas.this);
								inf.setVisible(true);
							}
						});
						btnVerInforme.setFont(UIUtils.largeFont(Font.BOLD));
						btnVerInforme.setEnabled(false);
						btnVerInforme.setBackground(Color.WHITE);
						btnVerInforme.setActionCommand("OK");
						buttonPane.add(btnVerInforme);
					}
					btnVincular.setFont(UIUtils.largeFont(Font.BOLD));
					btnVincular.setEnabled(false);
					btnVincular.setActionCommand("OK");
					buttonPane.add(btnVincular);
				}
				btnUpdate.setFont(UIUtils.largeFont(Font.BOLD));
				btnUpdate.setEnabled(false);
				btnUpdate.setActionCommand("OK");
				buttonPane.add(btnUpdate);
				getRootPane().setDefaultButton(btnUpdate);
			}
			{
				btnDelete = new JButton("Eliminar");
				btnDelete.setBackground(Color.WHITE);
				btnDelete.setIcon(UIUtils.icon("eliminar.png"));
				btnDelete.setFont(UIUtils.largeFont(Font.BOLD));
				btnDelete.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(seleccionado != null) {
							int option = JOptionPane.showConfirmDialog(null, "¿Esta seguro que desea eliminar la oferta al puesto de " + seleccionado.getPuesto() + " que posee el ID: "+seleccionado.getCodigo()+"?", "Eliminar", JOptionPane.WARNING_MESSAGE);
							if(option == JOptionPane.OK_OPTION){
								try {
									BolsaLaboral.getInstancia().eliminarOfertaTrabajo(seleccionado);
									cargarOfertas();
								}
								catch (NotRemovableException | SecurityException ex) {
									JOptionPane.showMessageDialog(null,ex.getMessage(),"Advertencia",JOptionPane.ERROR_MESSAGE);
								}	
							}
						}
					}
				});
				btnDelete.setEnabled(false);
				buttonPane.add(btnDelete);
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

		cargarOfertas();
		actualizarBotones();
		UIUtils.finishDialog(this, getOwner(), 920, 540);
	}
	
	public void filtrar() {
	    String filtro = txtFiltro.getText().toLowerCase();
	    modelo.setRowCount(0);
	    row = new Object[table.getColumnCount()];
	    
	    seleccionado = null;
	    btnDelete.setEnabled(false);
	    btnUpdate.setEnabled(false);
	    btnVincular.setEnabled(false);
	    btnVerInforme.setEnabled(false);
	    btnVincular.setToolTipText("Debe seleccionar una oferta.");
	    actualizarRazonProcesamiento(
				BolsaLaboral.getInstancia().evaluarProcesamiento(null));

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
	            row[0] = aux.getCodigo();
	            row[1] = aux.getPuesto();
	            row[2] = aux.getOfertador().getNombre();
	            row[3] = UIUtils.valueIcon(aux.getArea());
	            row[4] = aux.getEstado();
	            modelo.addRow(row);
	        }
	    }
	}

	private void actualizarBotones() {
		BolsaLaboral bolsa = BolsaLaboral.getInstancia();
		Usuario usuario = bolsa.getUsuarioActual();
		boolean haySeleccion = seleccionado != null;
		boolean gestiona = AutorizacionService.tienePermiso(usuario, Permiso.GESTIONAR_OFERTAS);
		boolean procesamientoAvanzado = AutorizacionService.tienePermiso(
				usuario, Permiso.USAR_PROCESAMIENTO_AVANZADO);
		btnDelete.setEnabled(haySeleccion && gestiona);
		btnUpdate.setEnabled(haySeleccion && gestiona);
		btnVerInforme.setEnabled(haySeleccion
				&& AutorizacionService.tienePermiso(usuario, Permiso.VER_INFORMES));
		DecisionProcesamiento decision = bolsa.evaluarProcesamiento(seleccionado);
		btnVincular.setVisible(procesamientoAvanzado);
		lblRazonProcesamiento.setVisible(procesamientoAvanzado);
		btnVincular.setEnabled(procesamientoAvanzado
				&& haySeleccion && decision.isPermitido());
		btnVincular.setToolTipText(decision.getRazon());
		actualizarRazonProcesamiento(decision);
	}

	private void actualizarRazonProcesamiento(DecisionProcesamiento decision) {
		if (lblRazonProcesamiento != null && decision != null) {
			lblRazonProcesamiento.setText(
					"Procesamiento: " + decision.getRazon());
			lblRazonProcesamiento.setToolTipText(decision.getRazon());
		}
	}
	
	public static void cargarOfertas() {
		modelo.setRowCount(0);
		row = new Object[table.getColumnCount()];
		for (OfertaLaboral aux : BolsaLaboral.getInstancia().getOfertas()) {
			if (aux == null || aux.getOfertador() == null) {
				continue;
			}
            row[0] = aux.getCodigo();
            row[1] = aux.getPuesto();
            row[2] = aux.getOfertador().getNombre();
	            row[3] = UIUtils.valueIcon(aux.getArea());
            row[4] = aux.getEstado();
			modelo.addRow(row);
		}
	}

}
