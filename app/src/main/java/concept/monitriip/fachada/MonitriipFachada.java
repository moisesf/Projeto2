package concept.monitriip.fachada;

import android.content.Context;
import android.util.Log;

import concept.monitriip.vo.EventoVO;
import concept.monitriip.vo.OperacaoMonitriip;
import concept.monitriip.vo.Position;
import concept.monitriip.vo.TipoRegistroEvento;
import concept.monitriip.vo.TipoRegistroViagem;


public class MonitriipFachada {

    private Context context;
    private DatabaseHelper fachadaDB;

    public MonitriipFachada(Context contexto) {
        context = contexto;
        fachadaDB = new DatabaseHelper(context);
    }

    private EventoVO criarEventoBase(int idVeiculo, String imei,Position posicao) {
        EventoVO evento = new EventoVO();
        evento.setIdVeiculo(idVeiculo);
        evento.setDataHoraLong(System.currentTimeMillis());
        evento.setImei(imei);
        evento.setLatitude(String.valueOf(posicao.getLatitude()));
        evento.setLongitude(String.valueOf(posicao.getLongitude()));
        evento.setPdop(posicao.getPdop());
        return evento;
    }

    public void inserirLogJornadaTrabalhoMotoristaVO(int idVeiculo, String imei, Position posicao, String cpf, TipoRegistroEvento tipoRegistroEvento) {
        EventoVO evento = criarEventoBase(idVeiculo, imei, posicao);
        evento.setOperacao(OperacaoMonitriip.InserirLogJornadaTrabalhoMotorista);
        evento.setCpfMotorista(cpf);
        evento.setTipoRegistroEvento(tipoRegistroEvento);
        fachadaDB.inserirLogJornadaTrabalhoMotoristaVO(evento);
        Log.d("Jornada", "Inseriu  Jornada " + cpf + " tipo " + tipoRegistroEvento.name());
    }

    public void inserirLogDetectorParadaVO(int idVeiculo, String imei, Position posicao, int indiceTipoParada) {
        EventoVO evento = criarEventoBase(idVeiculo, imei, posicao);
        evento.setOperacao(OperacaoMonitriip.InserirLogDetectorParada);
        evento.setMotivoParada(String.valueOf(indiceTipoParada));
        fachadaDB.inserirLogDetectorParadaVO(evento);
        Log.d("Parada", "Inseriu  Parada " + indiceTipoParada);
    }

    public void inserirLogInicioFimViagemFretadoVO(int idVeiculo, String imei, Position posicao, TipoRegistroViagem tipoRegistroViagem, String autorizacao, String sentidoLinha) {
        EventoVO evento = criarEventoBase(idVeiculo, imei, posicao);
        evento.setOperacao(OperacaoMonitriip.InserirLogInicioFimViagemFretado);
        evento.setAutorizacaoViagem(autorizacao);;
        evento.setTipoRegistroViagem(tipoRegistroViagem);
        evento.setSentidoLinha(sentidoLinha);
        fachadaDB.inserirLogInicioFimViagemFretadoVO(evento);
        Log.d("InicioFimViagemFretado", "Inseriu  " + tipoRegistroViagem.name());
    }

    public void inserirLogInicioFimViagemRegularVO(int idVeiculo, String imei, Position posicao, TipoRegistroViagem tipoRegistroViagem, String identificacaoLinha, String codigoTipoViagem, String dataProgramadaViagem, String horaProgramadaViagem, String sentidoLinha) {
        EventoVO evento = criarEventoBase(idVeiculo, imei, posicao);
        evento.setOperacao(OperacaoMonitriip.InserirLogInicioFimViagemRegular);
        evento.setIdentificaoLinha(identificacaoLinha);
        evento.setCodigoTipoViagem(codigoTipoViagem);
        evento.setDataProgramadaViagem(dataProgramadaViagem);
        evento.setHoraProgramadaViagem(horaProgramadaViagem);
        evento.setTipoRegistroViagem(tipoRegistroViagem);
        evento.setSentidoLinha(sentidoLinha);
        fachadaDB.inserirLogInicioFimViagemRegularVO(evento);
        Log.d("InicioFimViagemRegular", "Inseriu  " + tipoRegistroViagem.name());
    }
}
