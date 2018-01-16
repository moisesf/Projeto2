package concept.monitriip.fachada;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

import concept.monitriip.vo.EventoVO;

public class HttpFachada implements NetworkManager.NetworkHandler {

    private static final int RETRY_DELAY = 30 * 1000;
    private static final int WAKE_LOCK_TIMEOUT = 60 * 1000;

    private DatabaseHelper dbHelper;

    private boolean isOnline;
    private Context context;
    private Handler handler;
    private SharedPreferences preferences;

    private NetworkManager networkManager;
    private PowerManager.WakeLock wakeLock;

    private static int loopCounter = 0;

    public HttpFachada(Context context) {
        this.context = context;
        handler = new Handler();
        dbHelper = new DatabaseHelper(context);

        networkManager = new NetworkManager(context, this);
        isOnline = networkManager.isOnline();

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        networkManager.start();
    }

    private void lock() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            wakeLock.acquire();
        } else {
            wakeLock.acquire(WAKE_LOCK_TIMEOUT);
        }
    }

    private void unlock() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    public void onNetworkUpdate(boolean isOnline) {

        if (!this.isOnline && isOnline) {
            try {
                atualizacoesEmBackground();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.isOnline = isOnline;
    }
    @SuppressWarnings("deprecation")
    public void atualizacoesEmBackground() {
        ArrayList<EventoVO> listaOffline = dbHelper.selectEventos();
        if (listaOffline != null) {
            String resultadoAlteracao = null;
            for (EventoVO vo : listaOffline) {
                try {
                    resultadoAlteracao = enviarEventoParaPlataforma(vo);
                    if (resultadoAlteracao != null && resultadoAlteracao.toLowerCase().indexOf("sucesso") != -1) {
                        dbHelper.deleteEvento(vo);
                    } else {
                        //Mostrar no log
                    }
                } catch (HttpHostConnectException e) {
                    e.printStackTrace();
                }

            }
        }
    }



    @SuppressWarnings("deprecation")
    private String enviarEventoParaPlataforma(EventoVO evento) throws HttpHostConnectException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(Constantes.ENDERECO_SERVIDOR + "/IncluirEventoMonitriip");

        //Post Data
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(16);
        nameValuePair.add(new BasicNameValuePair("operacaoMonitriip",evento.getOperacao().name()));
        nameValuePair.add(new BasicNameValuePair("idVeiculo",String.valueOf(evento.getIdVeiculo())));
        nameValuePair.add(new BasicNameValuePair("dataHoraLong", String.valueOf(evento.getDataHoraLong())));
        nameValuePair.add(new BasicNameValuePair("imei", evento.getImei()));
        nameValuePair.add(new BasicNameValuePair("latitude", evento.getLatitude()));
        nameValuePair.add(new BasicNameValuePair("longitude",evento.getLongitude()));
        nameValuePair.add(new BasicNameValuePair("pdop",evento.getPdop()));
        nameValuePair.add(new BasicNameValuePair("motivoParada", evento.getMotivoParada()));
        nameValuePair.add(new BasicNameValuePair("autorizacaoViagem", evento.getAutorizacaoViagem()));
        if (evento.getTipoRegistroViagem() != null) {
            nameValuePair.add(new BasicNameValuePair("tipoRegistroViagem", evento.getTipoRegistroViagem().getCod()));
        }
        nameValuePair.add(new BasicNameValuePair("sentidoLinha", evento.getSentidoLinha()));
        nameValuePair.add(new BasicNameValuePair("cpfMotorista", evento.getCpfMotorista()));
        if (evento.getTipoRegistroEvento() != null) {
            nameValuePair.add(new BasicNameValuePair("tipoRegistroEvento", evento.getTipoRegistroEvento().getCod()));
        }
        nameValuePair.add(new BasicNameValuePair("identificaoLinha", evento.getIdentificaoLinha()));
        nameValuePair.add(new BasicNameValuePair("codigoTipoViagem", evento.getCodigoTipoViagem()));
        nameValuePair.add(new BasicNameValuePair("dataProgramadaViagem", evento.getDataProgramadaViagem()));
        nameValuePair.add(new BasicNameValuePair("horaProgramadaViagem", evento.getHoraProgramadaViagem()));

        //Encoding POST data
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            HttpResponse response = httpClient.execute(httpPost);
            String mensagemResultado = EntityUtils.toString(response.getEntity());
            return mensagemResultado;
        } catch (HttpHostConnectException e1) {
            throw e1;
        } catch (Exception e) {
            // Log exception
            e.printStackTrace();
        }

        return null;
    }

    private void retry() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isOnline) {
                    //read();
                }
            }
        }, RETRY_DELAY);
    }

    public void stopNetworkManager() {
        networkManager.stop();
    }
}
