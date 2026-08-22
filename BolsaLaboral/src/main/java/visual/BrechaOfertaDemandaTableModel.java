package visual;

import logico.BrechaOfertaDemandaDTO;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrechaOfertaDemandaTableModel extends AbstractTableModel {

    private static final String[] COLUMNAS = {
            "Área laboral", "Ofertas activas", "Candidatos desempleados", "Balance", "Diagnóstico"
    };

    private final List<BrechaOfertaDemandaDTO> resultados;

    public BrechaOfertaDemandaTableModel(List<BrechaOfertaDemandaDTO> resultados) {
        this.resultados = resultados == null
                ? Collections.<BrechaOfertaDemandaDTO>emptyList()
                : new ArrayList<BrechaOfertaDemandaDTO>(resultados);
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
        return columnIndex >= 1 && columnIndex <= 3 ? Integer.class : String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        BrechaOfertaDemandaDTO resultado = resultados.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return resultado.getAreaLaboral();
            case 1:
                return Integer.valueOf(resultado.getOfertasActivas());
            case 2:
                return Integer.valueOf(resultado.getCandidatosDesempleados());
            case 3:
                return Integer.valueOf(resultado.getBalance());
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
