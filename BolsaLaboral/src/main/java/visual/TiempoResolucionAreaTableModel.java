package visual;

import logico.TiempoResolucionAreaDTO;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TiempoResolucionAreaTableModel extends AbstractTableModel {

    private static final String[] COLUMNAS = {
            "Área laboral", "Oportunidades enviadas", "Vinculaciones resueltas",
            "Vinculaciones pendientes", "Pendientes > 7 días", "Promedio (días)",
            "Resolución (%)", "Diagnóstico"
    };

    private final List<TiempoResolucionAreaDTO> resultados;

    public TiempoResolucionAreaTableModel(List<TiempoResolucionAreaDTO> resultados) {
        this.resultados = resultados == null
                ? Collections.<TiempoResolucionAreaDTO>emptyList()
                : new ArrayList<TiempoResolucionAreaDTO>(resultados);
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
        if (columnIndex >= 1 && columnIndex <= 4) {
            return Integer.class;
        }
        if (columnIndex == 5 || columnIndex == 6) {
            return Double.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TiempoResolucionAreaDTO resultado = resultados.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return resultado.getAreaLaboral();
            case 1:
                return Integer.valueOf(resultado.getOportunidadesEnviadas());
            case 2:
                return Integer.valueOf(resultado.getVinculacionesResueltas());
            case 3:
                return Integer.valueOf(resultado.getVinculacionesPendientes());
            case 4:
                return Integer.valueOf(resultado.getPendientesMasSieteDias());
            case 5:
                return Double.valueOf(resultado.getDiasPromedioResolucion());
            case 6:
                return Double.valueOf(resultado.getPorcentajeResolucion());
            case 7:
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
