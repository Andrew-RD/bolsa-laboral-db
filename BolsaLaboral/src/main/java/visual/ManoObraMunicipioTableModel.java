package visual;

import logico.ManoObraMunicipioDTO;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManoObraMunicipioTableModel extends AbstractTableModel {

    private static final String[] COLUMNAS = {
            "Provincia", "Municipio", "Candidatos desempleados", "Vacantes disponibles",
            "Balance", "Diagnóstico"
    };

    private final List<ManoObraMunicipioDTO> resultados;

    public ManoObraMunicipioTableModel(List<ManoObraMunicipioDTO> resultados) {
        this.resultados = resultados == null
                ? Collections.<ManoObraMunicipioDTO>emptyList()
                : new ArrayList<ManoObraMunicipioDTO>(resultados);
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
        return columnIndex >= 2 && columnIndex <= 4 ? Integer.class : String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ManoObraMunicipioDTO resultado = resultados.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return resultado.getProvincia();
            case 1:
                return resultado.getMunicipio();
            case 2:
                return Integer.valueOf(resultado.getCandidatosDesempleados());
            case 3:
                return Integer.valueOf(resultado.getVacantesDisponibles());
            case 4:
                return Integer.valueOf(resultado.getBalance());
            case 5:
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