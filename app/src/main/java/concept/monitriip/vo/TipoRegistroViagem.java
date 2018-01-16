package concept.monitriip.vo;

public enum TipoRegistroViagem {

    FIM("0"), INICIO("1"), FIM_COM_TRANSBORDO("2"), INICIO_COM_TRANSBORDO("3");

    private String codigo;
    TipoRegistroViagem(String pCodigo) {
        this.codigo = pCodigo;
    }

    public String getCod(){
        return this.codigo;
    }

    public static TipoRegistroViagem getByCod(String pCodigo) {
        switch (pCodigo) {
            case "0": return FIM;
            case "1": return INICIO;
            case "2": return FIM_COM_TRANSBORDO;
            case "3": return INICIO_COM_TRANSBORDO;
            default: return  INICIO;
        }
    }


}
