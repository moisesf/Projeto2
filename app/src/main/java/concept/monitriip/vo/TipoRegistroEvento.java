package concept.monitriip.vo;

public enum TipoRegistroEvento {

    FIM("00"), INICIO("01"), JORNADA("02");

    private String codigo;
    TipoRegistroEvento(String pCodigo) {
        this.codigo = pCodigo;
    }

    public String getCod(){
        return this.codigo;
    }

    public static TipoRegistroEvento getByCod(String pCodigo) {
        switch (pCodigo) {
            case "00": return FIM;
            case "01": return INICIO;
            case "02": return JORNADA;
            default: return  INICIO;
        }
    }


}
