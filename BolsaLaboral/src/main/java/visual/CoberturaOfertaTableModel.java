package visual;

import logico.CoberturaOfertaDTO;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoberturaOfertaTableModel extends AbstractTableModel {

    private static final String[] COLUMNAS = {
            "Oferta", "Centro empleador", "Área laboral", "Vacantes totales",
            "Vacantes ocupadas", "Vacantes pendientes", "Oportunidades enviadas",
            "Cobertura (%)", "Diagnóstico"
    };

    private final List<CoberturaOfertaDTO> resultados;

    public CoberturaOfertaTableModel(List<CoberturaOfertaDTO> resultados) {
        this.resultados = resultados == null
                ? Collections.<CoberturaOfertaDTO>emptyList()
                : new ArrayList<CoberturaOfertaDTO>(resultados);
    }

    @Override
    public int getRowCount() {
        return resultados.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNAS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex >= 3 && columnIndex <= 6) {
            return Integer.class;
        }
        return columnIndex == 7 ? Double.class : String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CoberturaOfertaDTO resultado = resultados.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return resultado.getOferta();
            case 1:
                return resultado.getCentroEmpleador();
            case 2:
                return resultado.getAreaLaboral();
            case 3:
                return Integer.valueOf(resultado.getVacantesTotales());
            case 4:
                return Integer.valueOf(resultado.getVacantesOcupadas());
            case 5:
                return Integer.valueOf(resultado.getVacantesPendientes());
            case 6:
                return Integer.valueOf(resultado.getOportunidadesEnviadas());
            case 7:
                return Double.valueOf(resultado.getPorcentajeCobertura());
            case 8:
                return resultado.getDiagnostico();
            default:
                throw new IndexOutOfBoundsException("Columna no válida: " + columnIndex);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
