package concept.monitriip.fachada;

import android.content.Context;
import android.util.Log;

import concept.monitriip.vo.EventoVO;
import concept.monitriip.vo.OperacaoMonitriip;
import concept.monitriip.vo.Position;
import concept.monitriip.vo.TipoRegistroEvento;


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
}
