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

import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.SystemColor;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ConsultarSolicitudes extends JDialog {

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
	private Solicitud seleccionado = null;
	private JTextField txtFiltro;
	private JButton btnContratar;
	private JButton btnRechazar;
	
	/**
	 * Create the dialog.
	 */
	public ConsultarSolicitudes() {
		AutorizacionService.exigirPermiso(BolsaLaboral.getInstancia().getUsuarioActual(),
				Permiso.CONSULTAR_SOLICITUDES);
		setTitle("Listado de Solicitudes a Procesar");
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
					table.getSelectionModel().addListSelectionListener(event -> {
						if (!event.getValueIsAdjusting()) {
							int index = table.getSelectedRow();
							if (index >= 0) {
								int modelIndex = table.convertRowIndexToModel(index);
								Object codigo = table.getModel().getValueAt(modelIndex, 0);
								seleccionado = codigo == null ? null
										: BolsaLaboral.getInstancia().buscarSolicitudByCodigo(codigo.toString());
							} else {
								seleccionado = null;
							}
							actualizarBotones();
						}
					});
					String [] headers = {"Código", "Solicitante", "Puesto", "Ofertador", "Estado"};
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
			JPanel buttonPane = new JPanel();
			buttonPane.setBackground(new Color(4, 87, 87));
			buttonPane.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				{
					btnContratar = new JButton("Contratar");
					btnContratar.setIcon(UIUtils.icon("trabajar.png"));
					btnContratar.setBackground(Color.WHITE);
					btnContratar.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							try {
								if(BolsaLaboral.getInstancia().contratarCandidato(seleccionado)) {
									JOptionPane.showMessageDialog(ConsultarSolicitudes.this,"Contratación procesada satisfactoriamente.","Información",JOptionPane.INFORMATION_MESSAGE);
									cargarSolicitudes();
									filtrar();
								} else {
									JOptionPane.showMessageDialog(ConsultarSolicitudes.this,
											"Solo se puede aprobar una solicitud Enviada cuya oferta tenga vacantes disponibles.",
											"Advertencia", JOptionPane.WARNING_MESSAGE);
								}
							} catch (SecurityException exception) {
								JOptionPane.showMessageDialog(ConsultarSolicitudes.this,
										exception.getMessage(), "Acción no autorizada",
										JOptionPane.WARNING_MESSAGE);
							}
						}
					});
					{
						btnRechazar = new JButton("Rechazar");
						btnRechazar.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								if(seleccionado != null && BolsaLaboral.getInstancia().esProcesable(seleccionado)) {
									try {
									BolsaLaboral.getInstancia().rechazarCandidato(seleccionado);
									JOptionPane.showMessageDialog(ConsultarSolicitudes.this,"Rechazo procesado satisfactoriamente.","Información",JOptionPane.INFORMATION_MESSAGE);
									cargarSolicitudes();
									filtrar();
									} catch (SecurityException exception) {
										JOptionPane.showMessageDialog(ConsultarSolicitudes.this,
												exception.getMessage(), "Acción no autorizada",
												JOptionPane.WARNING_MESSAGE);
									}
								}
								else {
									JOptionPane.showMessageDialog(ConsultarSolicitudes.this,
											"Solo se puede rechazar una solicitud con estado Enviada.",
											"Advertencia", JOptionPane.WARNING_MESSAGE);
								}
							}
						});
						btnRechazar.setIcon(UIUtils.icon("rechazar.png"));
						btnRechazar.setFont(UIUtils.largeFont(Font.BOLD));
						btnRechazar.setEnabled(false);
						btnRechazar.setBackground(Color.WHITE);
						btnRechazar.setActionCommand("OK");
						buttonPane.add(btnRechazar);
					}
					btnContratar.setFont(UIUtils.largeFont(Font.BOLD));
					btnContratar.setEnabled(false);
					btnContratar.setActionCommand("OK");
					buttonPane.add(btnContratar);
				}
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

		cargarSolicitudes();
		UIUtils.finishDialog(this, getOwner(), 820, 540);
	}
	
	public void filtrar() {
	    String filtro = txtFiltro.getText().toLowerCase();
	    modelo.setRowCount(0);
	    row = new Object[table.getColumnCount()];
	    
	    seleccionado = null;
	    btnContratar.setEnabled(false);
	    btnRechazar.setEnabled(false);
	    
	    for (Solicitud aux : BolsaLaboral.getInstancia().getSolicitudes()) {
		if (aux == null || aux.getSolicitante() == null
				|| aux.getOfertaSolicitada() == null
				|| aux.getOfertaSolicitada().getOfertador() == null) {
			continue;
		}
		String estado = aux.getEstado() == null ? "" : aux.getEstado();
	        boolean coincide =
	            aux.getCodigo().toLowerCase().contains(filtro) ||
	            aux.getOfertaSolicitada().getPuesto().toLowerCase().contains(filtro) ||
	            aux.getOfertaSolicitada().getOfertador().getNombre().toLowerCase().contains(filtro) ||
	            (aux.getSolicitante().getNombres() + aux.getSolicitante().getApellidos()).toLowerCase().contains(filtro) ||
	            estado.toLowerCase().contains(filtro);
	        
	        if (coincide) {
	            row[0] = aux.getCodigo();
	            row[1] = aux.getSolicitante().getNombres() + " " + aux.getSolicitante().getApellidos();
	            row[2] = aux.getOfertaSolicitada().getPuesto();
	            row[3] = aux.getOfertaSolicitada().getOfertador().getNombre();
	            row[4] = aux.getEstado();
	            
	            modelo.addRow(row);
	        }
	    }
	}

	public static void cargarSolicitudes() {
		modelo.setRowCount(0);
		row = new Object[table.getColumnCount()];
		for (Solicitud aux : BolsaLaboral.getInstancia().getSolicitudes()) {
			if (aux == null || aux.getSolicitante() == null
					|| aux.getOfertaSolicitada() == null
					|| aux.getOfertaSolicitada().getOfertador() == null) {
				continue;
			}
            row[0] = aux.getCodigo();
            row[1] = aux.getSolicitante().getNombres() + " " + aux.getSolicitante().getApellidos();
            row[2] = aux.getOfertaSolicitada().getPuesto();
            row[3] = aux.getOfertaSolicitada().getOfertador().getNombre();
            row[4] = aux.getEstado();
			modelo.addRow(row);
		}
	}

	private void actualizarBotones() {
		BolsaLaboral bolsa = BolsaLaboral.getInstancia();
		boolean autorizado = AutorizacionService.tienePermiso(
				bolsa.getUsuarioActual(), Permiso.PROCESAR_SOLICITUDES);
		boolean procesable = bolsa.esProcesable(seleccionado);
		boolean contratar = bolsa.puedeContratarCandidato(seleccionado);
		btnRechazar.setEnabled(autorizado && procesable);
		btnContratar.setEnabled(contratar);
		btnRechazar.setToolTipText(!autorizado ? "No tiene permiso para procesar solicitudes."
				: procesable ? "Rechazar la solicitud seleccionada."
				: "Solo se puede rechazar una solicitud Enviada.");
		btnContratar.setToolTipText(!autorizado ? "No tiene permiso para procesar solicitudes."
				: contratar ? "Aprobar y ocupar una vacante."
				: "La solicitud debe estar Enviada y la oferta debe tener una vacante disponible.");
	}

}
