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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.awt.SystemColor;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JLabel;


public class ResultadosVinculacion extends JDialog {

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
	private ResultadoMatcheo seleccionado = null;
	private JButton btnContratar;
	private JLabel lblRazonProcesamiento;
	private static ArrayList<ResultadoMatcheo> resultados = new ArrayList<>();

	/**
	 * Create the dialog.
	 */
	 public ResultadosVinculacion(OfertaLaboral ofertaVinculada) {
		 DecisionProcesamiento decisionInicial = BolsaLaboral.getInstancia()
				 .evaluarProcesamiento(ofertaVinculada);
		 if (!decisionInicial.isPermitido()) {
			 throw new IllegalStateException(decisionInicial.getRazon());
		 }
		 setTitle("Resultados de la Vinculación");
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
								 seleccionado = BolsaLaboral.getInstancia().buscarResultado(resultados,ofertaVinculada.getCodigo(),table.getValueAt(index,0).toString());
								 actualizarBoton();
							 }
						 }
					 });
					 String [] headers = {"Código", "Solicitante", "Porcentaje", "Condición"};
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
				 JLabel lblNewLabel = new JLabel("Oferta:");
				 lblNewLabel.setForeground(Color.BLACK);
				 lblNewLabel.setFont(UIUtils.largeFont(Font.BOLD));
				 pnlFiltro.add(lblNewLabel);
			 }
			 {
				 JLabel lblNombreOferta = new JLabel("");
				 lblNombreOferta.setText(ofertaVinculada.getPuesto() + ", " + ofertaVinculada.getOfertador().getNombre());
				 lblNombreOferta.setForeground(Color.BLACK);
				 lblNombreOferta.setFont(UIUtils.largeFont(Font.PLAIN));
				 pnlFiltro.add(lblNombreOferta);
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
				 btnContratar = new JButton("Vincular");
				 btnContratar.setBackground(Color.WHITE);
				 btnContratar.setIcon(UIUtils.icon("vincular.png"));
				 btnContratar.setFont(UIUtils.largeFont(Font.BOLD));
				 btnContratar.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent e) {
						 DecisionProcesamiento decision = BolsaLaboral.getInstancia()
								 .evaluarVinculacion(seleccionado);
						 if (!decision.isPermitido()) {
							 JOptionPane.showMessageDialog(ResultadosVinculacion.this,
									 decision.getRazon(), "No se puede vincular",
									 JOptionPane.WARNING_MESSAGE);
							 actualizarBoton();
							 return;
						 }
						 try {
							 if (BolsaLaboral.getInstancia().vincularOferta(seleccionado)) {
								 JOptionPane.showMessageDialog(ResultadosVinculacion.this,
										 "Se ha creado la solicitud correctamente a la oferta "
												 + seleccionado.getOferta().getPuesto() + ".",
										 "Información", JOptionPane.INFORMATION_MESSAGE);
							 }
						 } catch (SecurityException exception) {
							 JOptionPane.showMessageDialog(ResultadosVinculacion.this,
									 exception.getMessage(), "Acción no autorizada",
									 JOptionPane.WARNING_MESSAGE);
						 }
						 resultados = BolsaLaboral.getInstancia()
								 .obtenerCandidatosOrdenadosParaOferta(ofertaVinculada);
						 cargarResultados(ofertaVinculada);
						 seleccionado = null;
						 actualizarBoton();
					 }
				 });
				 btnContratar.setEnabled(false);
				 buttonPane.add(btnContratar);
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

		 resultados = BolsaLaboral.getInstancia().obtenerCandidatosOrdenadosParaOferta(ofertaVinculada);
		 cargarResultados(ofertaVinculada);
		 actualizarBoton();
		 UIUtils.finishDialog(this, getOwner(), 720, 520);
	 }

	 public static void cargarResultados(OfertaLaboral oferta) {
		 modelo.setRowCount(0);
		 row = new Object[table.getColumnCount()];
		 for (ResultadoMatcheo aux : resultados) {
			 row[0] = aux.getSolicitante().getCodigo();
			 row[1] = aux.getSolicitante().getNombres() + " " + aux.getSolicitante().getApellidos();
			 row[2] = aux.getPorcentaje() + "%";
			 row[3] = UIUtils.valueIcon(aux.getCondicion());
			 modelo.addRow(row);
		 }
	 }

	 private void actualizarBoton() {
		 DecisionProcesamiento decision = BolsaLaboral.getInstancia()
				 .evaluarVinculacion(seleccionado);
		 btnContratar.setEnabled(decision.isPermitido());
		 btnContratar.setToolTipText(decision.getRazon());
		 lblRazonProcesamiento.setText("Vinculación: " + decision.getRazon());
		 lblRazonProcesamiento.setToolTipText(decision.getRazon());
	 }

}
