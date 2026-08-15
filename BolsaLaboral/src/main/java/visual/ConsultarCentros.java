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

public class ConsultarCentros extends JDialog {

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
	private final GestionCentroService servicio = new GestionCentroService(BolsaLaboral.getInstancia());
	private CentroEmpleador seleccionado = null;
	private JButton btnUpdate;
	private JButton btnDelete;
	private JTextField txtFiltro;
	private JButton btnVisualizar;

	/**
	 * Create the dialog.
	 */
	public ConsultarCentros() {
		AutorizacionService.exigirPermiso(BolsaLaboral.getInstancia().getUsuarioActual(),
				Permiso.CONSULTAR_CENTROS);
		setTitle("Listado de Centros");
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
								seleccionado = BolsaLaboral.getInstancia().buscarCentroByCodigo(table.getValueAt(index, 0).toString());
								btnDelete.setEnabled(true);
								btnUpdate.setEnabled(true);
								btnVisualizar.setEnabled(true);
							}
						}
					});
					String [] headers = {"Código", "Nombre", "RNC", "Sector", " "};
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
				btnUpdate = new JButton("Modificar");
				btnUpdate.setBackground(Color.WHITE);
				btnUpdate.setIcon(UIUtils.icon("modificar.png"));
				btnUpdate.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						btnDelete.setEnabled(true);
						btnUpdate.setEnabled(true);
						btnVisualizar.setEnabled(true);
						RegistroCentro registro = new RegistroCentro(seleccionado);
						registro.setModal(true);
						registro.setLocationRelativeTo(ConsultarCentros.this);
						registro.setVisible(true);
					}
				});
				{
					btnVisualizar = new JButton("Visualizar");
					btnVisualizar.setBackground(Color.WHITE);
					btnVisualizar.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							btnDelete.setEnabled(true);
							btnUpdate.setEnabled(true);
							btnVisualizar.setEnabled(true);
							VistaCentro vist = new VistaCentro(seleccionado);
							vist.setModal(true);
							vist.setLocationRelativeTo(ConsultarCentros.this);
							vist.setVisible(true);
						}
					});
					btnVisualizar.setIcon(UIUtils.icon("cv.png"));
					btnVisualizar.setFont(UIUtils.largeFont(Font.BOLD));
					btnVisualizar.setEnabled(false);
					btnVisualizar.setActionCommand("OK");
					buttonPane.add(btnVisualizar);
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
							int cantidadOfertas = servicio.contarOfertasVinculadas(seleccionado);
							String mensaje = "¿Esta seguro que desea eliminar el centro de trabajo llamado "
									+ seleccionado.getNombre() + " que posee el ID: " + seleccionado.getCodigo() + "?";
							if (cantidadOfertas > 0) {
								mensaje += "\n\nEste centro tiene " + cantidadOfertas
										+ (cantidadOfertas == 1 ? " oferta laboral registrada" : " ofertas laborales registradas")
										+ ". Si continúa, también se eliminarán esas ofertas y las solicitudes/contrataciones asociadas a ellas.";
							}
							int option = JOptionPane.showConfirmDialog(null, mensaje, "Eliminar", JOptionPane.WARNING_MESSAGE);
							if(option == JOptionPane.OK_OPTION){
								btnDelete.setEnabled(true);
								btnUpdate.setEnabled(true);
								try {
									btnDelete.setEnabled(true);
									btnUpdate.setEnabled(true);
									servicio.eliminar(seleccionado);
									cargarCentros();
								}
								catch (NotRemovableException ex) {
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

		cargarCentros();
		UIUtils.finishDialog(this, getOwner(), 760, 540);
	}

	public void filtrar() {
		String filtro = txtFiltro.getText().toLowerCase();
		modelo.setRowCount(0);
		row = new Object[table.getColumnCount()];

		seleccionado = null;
		btnDelete.setEnabled(false);
		btnUpdate.setEnabled(false);
		btnVisualizar.setEnabled(false);

		for (CentroEmpleador aux : BolsaLaboral.getInstancia().getCentros()) {
			if (aux == null) {
				continue;
			}
			boolean coincide =
					aux.getCodigo().toLowerCase().contains(filtro) ||
							aux.getNombre().toLowerCase().contains(filtro) ||
							aux.getRnc().toLowerCase().contains(filtro) ||
							aux.getSector().toLowerCase().contains(filtro);

			if (coincide) {
				row[0] = aux.getCodigo();
				row[1] = aux.getNombre();
				row[2] = aux.getRnc();
				row[3] = aux.getSector();
				row[4] = UIUtils.valueIcon(aux.getSector());
				modelo.addRow(row);
			}
		}
	}

	public static void cargarCentros() {
		modelo.setRowCount(0);
		row = new Object[table.getColumnCount()];
		for (CentroEmpleador aux : BolsaLaboral.getInstancia().getCentros()) {
			if (aux == null) {
				continue;
			}
			row[0] = aux.getCodigo();
			row[1] = aux.getNombre();
			row[2] = aux.getRnc();
			row[3] = aux.getSector();
			row[4] = UIUtils.valueIcon(aux.getSector());
			modelo.addRow(row);
		}
	}

}