package visual;

import logico.UbicacionService;

import javax.swing.JComboBox;
import java.util.List;
import java.util.Objects;

/** Comportamiento compartido de los combos dependientes de ubicación. */
final class UbicacionComboSupport {

    private final UbicacionService ubicaciones = UbicacionService.getInstancia();
    private final JComboBox<String> provincia = new JComboBox<String>();
    private final JComboBox<String> municipio = new JComboBox<String>();
    private boolean actualizando;
    private String provinciaOriginal;
    private String municipioOriginal;

    UbicacionComboSupport() {
        provincia.addItem(UbicacionService.SELECCIONE_PROVINCIA);
        for (String valor : ubicaciones.getProvincias()) {
            provincia.addItem(valor);
        }
        municipio.addItem(UbicacionService.SELECCIONE_MUNICIPIO);
        municipio.setEnabled(false);
        provincia.addActionListener(event -> {
            if (!actualizando) {
                cargarMunicipios(null);
            }
        });
    }

    JComboBox<String> getProvinciaCombo() {
        return provincia;
    }

    JComboBox<String> getMunicipioCombo() {
        return municipio;
    }

    void limpiar() {
        provinciaOriginal = null;
        municipioOriginal = null;
        actualizando = true;
        try {
            provincia.setSelectedIndex(0);
            municipio.removeAllItems();
            municipio.addItem(UbicacionService.SELECCIONE_MUNICIPIO);
            municipio.setEnabled(false);
            limpiarTooltips();
        } finally {
            actualizando = false;
        }
    }

    void seleccionar(String provinciaActual, String municipioActual) {
        provinciaOriginal = provinciaActual;
        municipioOriginal = municipioActual;
        String provinciaCanonica = ubicaciones.buscarProvinciaCanonica(provinciaActual);
        actualizando = true;
        try {
            if (provinciaCanonica == null && !vacio(provinciaActual)) {
                provincia.addItem(provinciaActual);
                provincia.setSelectedItem(provinciaActual);
            } else if (provinciaCanonica != null) {
                provincia.setSelectedItem(provinciaCanonica);
            } else {
                provincia.setSelectedIndex(0);
            }
            cargarMunicipiosInterno(municipioActual);
            if (esLegada()) {
                String advertencia = "Valor legado fuera del catálogo geográfico; "
                        + "se conservará si no cambia.";
                provincia.setToolTipText(advertencia);
                municipio.setToolTipText(advertencia);
            } else {
                limpiarTooltips();
            }
        } finally {
            actualizando = false;
        }
    }

    String getProvincia() {
        Object valor = provincia.getSelectedItem();
        return provincia.getSelectedIndex() <= 0 || valor == null ? null : valor.toString();
    }

    String getMunicipio() {
        Object valor = municipio.getSelectedItem();
        return municipio.getSelectedIndex() <= 0 || valor == null ? null : valor.toString();
    }

    boolean esLegada() {
        String seleccionProvincia = getProvincia();
        String seleccionMunicipio = getMunicipio();
        return seleccionProvincia != null && seleccionMunicipio != null
                && !ubicaciones.esUbicacionValida(seleccionProvincia, seleccionMunicipio);
    }

    void validar() {
        if (getProvincia() == null) {
            throw new IllegalArgumentException("Debe seleccionar una provincia.");
        }
        if (getMunicipio() == null) {
            throw new IllegalArgumentException("Debe seleccionar un municipio.");
        }
        if (esLegada() && !(Objects.equals(provinciaOriginal, getProvincia())
                && Objects.equals(municipioOriginal, getMunicipio()))) {
            throw new IllegalArgumentException(
                    "La nueva provincia y el municipio deben pertenecer al catálogo geográfico.");
        }
    }

    private void cargarMunicipios(String municipioSeleccionado) {
        actualizando = true;
        try {
            cargarMunicipiosInterno(municipioSeleccionado);
            limpiarTooltips();
        } finally {
            actualizando = false;
        }
    }

    private void cargarMunicipiosInterno(String municipioSeleccionado) {
        municipio.removeAllItems();
        municipio.addItem(UbicacionService.SELECCIONE_MUNICIPIO);
        String seleccionProvincia = getProvincia();
        List<String> valores = ubicaciones.getMunicipios(seleccionProvincia);
        for (String valor : valores) {
            municipio.addItem(valor);
        }
        municipio.setEnabled(seleccionProvincia != null);
        if (!vacio(municipioSeleccionado)) {
            String canonico = ubicaciones.buscarMunicipioCanonico(
                    seleccionProvincia, municipioSeleccionado);
            if (canonico != null) {
                municipio.setSelectedItem(canonico);
            } else {
                municipio.addItem(municipioSeleccionado);
                municipio.setSelectedItem(municipioSeleccionado);
            }
        } else {
            municipio.setSelectedIndex(0);
        }
    }

    private void limpiarTooltips() {
        provincia.setToolTipText(null);
        municipio.setToolTipText(null);
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
