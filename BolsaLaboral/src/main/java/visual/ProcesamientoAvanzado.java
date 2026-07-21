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
	
	private static ArrayList<OfertaLaboral> ofertas = new ArrayList<>();
	private static ArrayList<ResultadoMatcheo> resultados = new ArrayList<>();
	
	
	/**
	 * Create the dialog.
	 */
	public ProcesamientoAvanzado() {
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
						btnProcesar.setEnabled(true);
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
			JPanel buttonPane = new JPanel();
			buttonPane.setBackground(new Color(4, 87, 87));
			buttonPane.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				btnProcesar = new JButton("Procesar");
				btnProcesar.setBackground(Color.WHITE);
				btnProcesar.setIcon(UIUtils.icon("vincular.png"));
				btnProcesar.setFont(UIUtils.largeFont(Font.BOLD));
				btnProcesar.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(BolsaLaboral.getInstancia().vincularOferta(resMatchSelec)) {
							JOptionPane.showMessageDialog(null,"Se ha creado la solicitud correctamente a la oferta " + resMatchSelec.getOferta().getPuesto() + ".","Información",JOptionPane.INFORMATION_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(null,"Esta solicitud ya existe.","Información",JOptionPane.INFORMATION_MESSAGE);
						}
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
		UIUtils.finishDialog(this, getOwner(), 820, 620);
	}
	
	public void filtrar() {
	    String filtro = txtFiltro.getText().toLowerCase();
	    modeloOfertas.setRowCount(0);
	    rowOferta = new Object[tablaOfertas.getColumnCount()];
	    btnProcesar.setEnabled(false);

	    ArrayList<OfertaLaboral> ofertasVisibles = new ArrayList<>();
	    
	    for (OfertaLaboral aux : BolsaLaboral.getInstancia().getOfertas()) {
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
}
