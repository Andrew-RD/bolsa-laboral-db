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

public class ConsultarCandidatos extends JDialog {

	private final JPanel contentPanel = new JPanel();
	public static JTable table;
	public static DefaultTableModel modelo = new DefaultTableModel() {
		@Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
	};
	public static Object[] row;
	private Candidato seleccionado = null;
	private JButton btnUpdate;
	private JButton btnDelete;
	private JTextField txtFiltro;
	private JButton btnVisualizar;
	
	/**
	 * Create the dialog.
	 */
	public ConsultarCandidatos() {
		setTitle("Listado de Candidatos");
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
								seleccionado = BolsaLaboral.getInstancia().buscarCandidatoByCodigo(table.getValueAt(index, 0).toString());
								btnDelete.setEnabled(true);
								btnUpdate.setEnabled(true);
								btnVisualizar.setEnabled(true);
							}
						}
					});
					String [] headers = {"Código", "Nombre", "Cédula", "Nivel Académico"};
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
			buttonPane.setBackground(new Color(92, 131, 116));
			buttonPane.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				btnUpdate = new JButton("Modificar");
				btnUpdate.setBackground(Color.WHITE);
				btnUpdate.setIcon(UIUtils.icon("modificar.png"));
				btnUpdate.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						RegistroCandidato registro = new RegistroCandidato(seleccionado);
						registro.setModal(true); 
						registro.setLocationRelativeTo(ConsultarCandidatos.this); 
						registro.setVisible(true);
						
						cargarCandidatos();
						
						seleccionado = null;
						btnDelete.setEnabled(false);
						btnUpdate.setEnabled(false);
						btnVisualizar.setEnabled(false);
					}
				});
				{
					btnVisualizar = new JButton("Visualizar");
					btnVisualizar.setBackground(Color.WHITE);
					btnVisualizar.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if(seleccionado != null) {
								visualizarCandidato(seleccionado);
							}
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
							int option = JOptionPane.showConfirmDialog(null, "¿Esta seguro que desea eliminar al candidato " + seleccionado.getNombres() + " " + seleccionado.getApellidos() + " que posee el ID: "+seleccionado.getCodigo()+"?", "Eliminar", JOptionPane.WARNING_MESSAGE);
							if(option == JOptionPane.OK_OPTION){
								try {
									BolsaLaboral.getInstancia().eliminarCandidato(seleccionado);
									cargarCandidatos();
									seleccionado = null;
									btnDelete.setEnabled(false);
									btnUpdate.setEnabled(false);
									btnVisualizar.setEnabled(false);
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
				btnCancelar.setFont(UIUtils.largeFont(Font.BOLD));
				btnCancelar.setIcon(UIUtils.icon("cerrar.png"));
				btnCancelar.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				btnCancelar.setActionCommand("Cancel");
				buttonPane.add(btnCancelar);
			}
		}

		cargarCandidatos();
		UIUtils.finishDialog(this, getOwner(), 720, 540);
	}
	
	private void visualizarCandidato(Candidato candidato) {
		CV visualizar = new CV(candidato);
		visualizar.setModal(true);
		visualizar.setLocationRelativeTo(this);
		visualizar.setVisible(true);
	}
	
	public void filtrar() {
	    String filtro = txtFiltro.getText().toLowerCase();
	    modelo.setRowCount(0);
	    row = new Object[table.getColumnCount()];
	    
	    seleccionado = null;
	    btnDelete.setEnabled(false);
	    btnUpdate.setEnabled(false);
	    btnVisualizar.setEnabled(false);
	    
	    for (Candidato aux : BolsaLaboral.getInstancia().getCandidatos()) {
	        boolean coincide = 
	            aux.getCodigo().toLowerCase().contains(filtro) ||
	            (aux.getNombres() + " " + aux.getApellidos()).toLowerCase().contains(filtro) ||
	            aux.getIdentificacion().toLowerCase().contains(filtro) ||
	            getNivelAcademico(aux).toLowerCase().contains(filtro);
	        
	        if (coincide) {
	            row[0] = aux.getCodigo();
	            row[1] = aux.getNombres() + " " + aux.getApellidos();
	            row[2] = aux.getIdentificacion();
	            row[3] = getNivelAcademico(aux);
	            modelo.addRow(row);
	        }
	    }
	}

	private static String getNivelAcademico(Candidato candidato) {
	    if (candidato instanceof Universitario) {
	        return "Estudiante Universitario";
	    } else if (candidato instanceof TecnicoSuperior) {
	        return "Estudiante Técnico";
	    } else if (candidato instanceof Obrero) {
	        return "Obrero";
	    }
	    return "";
	}
	
	public static void cargarCandidatos() {
		modelo.setRowCount(0);
		row = new Object[table.getColumnCount()];
		for (Candidato aux : BolsaLaboral.getInstancia().getCandidatos()) {
			row[0] = aux.getCodigo();
			row[1] = aux.getNombres() + " " + aux.getApellidos();
			row[2] = aux.getIdentificacion();
			row[3] = getNivelAcademico(aux);
			modelo.addRow(row);
		}
	}

}
