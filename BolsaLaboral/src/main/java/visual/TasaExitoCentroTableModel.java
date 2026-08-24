package visual;

import logico.TasaExitoCentroDTO;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TasaExitoCentroTableModel extends AbstractTableModel {

    private static final String[] COLUMNAS = {
            "Centro empleador", "Solicitudes recibidas", "Contrataciones",
            "Tasa de éxito (%)", "Diagnóstico"
    };

    private final List<TasaExitoCentroDTO> resultados;

    public TasaExitoCentroTableModel(List<TasaExitoCentroDTO> resultados) {
        this.resultados = resultados == null
                ? Collections.<TasaExitoCentroDTO>emptyList()
                : new ArrayList<TasaExitoCentroDTO>(resultados);
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
        if (columnIndex == 1 || columnIndex == 2) {
            return Integer.class;
        }
        if (columnIndex == 3) {
            return Double.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TasaExitoCentroDTO resultado = resultados.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return resultado.getCentroEmpleador();
            case 1:
                return Integer.valueOf(resultado.getSolicitudesRecibidas());
            case 2:
                return Integer.valueOf(resultado.getContrataciones());
            case 3:
                return Double.valueOf(resultado.getTasaExito());
            case 4:
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