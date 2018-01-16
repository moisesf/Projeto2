package concept.monitriip.vo;

public class EventoVO {
    private OperacaoMonitriip operacao;
    private int id;
    private int idVeiculo;
    private long dataHoraLong;
    private String imei;
    private String latitude;
    private String longitude;
    private String pdop;
    private String cpfMotorista;
    private String tipoEventoMonitriip;
    private String autorizacaoViagem;
    private TipoRegistroViagem tipoRegistroViagem;
    private String motivoParada;
    private TipoRegistroEvento tipoRegistroEvento;
    private String sentidoLinha;
    private String identificaoLinha;
    private String codigoTipoViagem;
    private String dataProgramadaViagem;
    private String horaProgramadaViagem;

    public OperacaoMonitriip getOperacao() {
        return operacao;
    }

    public void setOperacao(OperacaoMonitriip operacao) {
        this.operacao = operacao;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdVeiculo() {
        return idVeiculo;
    }

    public void setIdVeiculo(int idVeiculo) {
        this.idVeiculo = idVeiculo;
    }

    public long getDataHoraLong() {
        return dataHoraLong;
    }

    public void setDataHoraLong(long dataHoraLong) {
        this.dataHoraLong = dataHoraLong;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPdop() {
        return pdop;
    }

    public void setPdop(String pdop) {
        this.pdop = pdop;
    }

    public String getCpfMotorista() {
        return cpfMotorista;
    }

    public void setCpfMotorista(String cpfMotorista) {
        this.cpfMotorista = cpfMotorista;
    }

    public String getTipoEventoMonitriip() {
        return tipoEventoMonitriip;
    }

    public void setTipoEventoMonitriip(String tipoEventoMonitriip) {
        this.tipoEventoMonitriip = tipoEventoMonitriip;
    }

    public String getAutorizacaoViagem() {
        return autorizacaoViagem;
    }

    public void setAutorizacaoViagem(String autorizacaoViagem) {
        this.autorizacaoViagem = autorizacaoViagem;
    }

    public TipoRegistroViagem getTipoRegistroViagem() {
        return tipoRegistroViagem;
    }

    public void setTipoRegistroViagem(TipoRegistroViagem tipoRegistroViagem) {
        this.tipoRegistroViagem = tipoRegistroViagem;
    }

    public String getMotivoParada() {
        return motivoParada;
    }

    public void setMotivoParada(String motivoParada) {
        this.motivoParada = motivoParada;
    }

    public TipoRegistroEvento getTipoRegistroEvento() {
        return tipoRegistroEvento;
    }

    public void setTipoRegistroEvento(TipoRegistroEvento tipoRegistroEvento) {
        this.tipoRegistroEvento = tipoRegistroEvento;
    }

    public String getSentidoLinha() {
        return sentidoLinha;
    }

    public void setSentidoLinha(String sentidoLinha) {
        this.sentidoLinha = sentidoLinha;
    }

    public String getIdentificaoLinha() {
        return identificaoLinha;
    }

    public void setIdentificaoLinha(String identificaoLinha) {
        this.identificaoLinha = identificaoLinha;
    }

    public String getCodigoTipoViagem() {
        return codigoTipoViagem;
    }

    public void setCodigoTipoViagem(String codigoTipoViagem) {
        this.codigoTipoViagem = codigoTipoViagem;
    }

    public String getDataProgramadaViagem() {
        return dataProgramadaViagem;
    }

    public void setDataProgramadaViagem(String dataProgramadaViagem) {
        this.dataProgramadaViagem = dataProgramadaViagem;
    }

    public String getHoraProgramadaViagem() {
        return horaProgramadaViagem;
    }

    public void setHoraProgramadaViagem(String horaProgramadaViagem) {
        this.horaProgramadaViagem = horaProgramadaViagem;
    }
}
