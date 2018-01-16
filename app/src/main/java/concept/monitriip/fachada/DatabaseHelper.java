package concept.monitriip.fachada;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;


import concept.monitriip.vo.EventoVO;
import concept.monitriip.vo.OperacaoMonitriip;
import concept.monitriip.vo.TipoRegistroEvento;
import concept.monitriip.vo.TipoRegistroViagem;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "conceptMonitriip.db";
    private static SimpleDateFormat parseFormat = new SimpleDateFormat("dd/MM/yy HH:mm");

    public interface DatabaseHandler<T> {
        void onComplete(boolean success, T result);
    }

    private static abstract class DatabaseAsyncTask<T> extends AsyncTask<Void, Void, T> {

        private DatabaseHandler<T> handler;
        private RuntimeException error;


        public DatabaseAsyncTask(DatabaseHandler<T> handler) {
            this.handler = handler;
        }

        @Override
        protected T doInBackground(Void... params) {
            try {
                return executeMethod();
            } catch (RuntimeException error) {
                this.error = error;
                return null;
            }
        }

        protected abstract T executeMethod();

        @Override
        protected void onPostExecute(T result) {
            handler.onComplete(error == null, result);
        }
    }

    private SQLiteDatabase db;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Evento (" +
                "id INTEGER PRIMARY KEY," +
                "idVeiculo INTEGER," +
                "operacaoMonitriip TEXT," +
                "dataHoraLong INTEGER," +
                "imei TEXT," +
                "latitude TEXT," +
                "longitude TEXT," +
                "pdop TEXT," +
                "cpfMotorista TEXT," +
                "tipoEventoMonitriip TEXT," +
                "autorizacaoViagem TEXT," +
                "tipoRegistroViagem TEXT," +
                "motivoParada TEXT," +
                "tipoRegistroEvento TEXT," +
                "sentidoLinha TEXT," +
                "identificaoLinha TEXT," +
                "codigoTipoViagem TEXT," +
                "dataProgramadaViagem TEXT," +
                "horaProgramadaViagem TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Evento;");
        onCreate(db);
    }

    public void deleteTodosEventos( String idVeiculo) {
        db.execSQL("delete from Evento WHERE idVeiculo = ? ", new String[] { idVeiculo });
    }

    public void deleteEvento( EventoVO evento) {
        db.execSQL("delete from Evento WHERE id = ? ", new String[] { String.valueOf(evento.getId()) });
    }

    public void inserirLogDetectorParadaVO(EventoVO evento) {
        ContentValues values = new ContentValues();
        values.put("operacaoMonitriip",evento.getOperacao().name());
        values.put("idVeiculo",evento.getIdVeiculo());
        values.put("dataHoraLong", evento.getDataHoraLong());
        values.put("imei", evento.getImei());
        values.put("latitude", evento.getLatitude());
        values.put("longitude",evento.getLongitude());
        values.put("pdop",evento.getPdop());
        values.put("motivoParada", evento.getMotivoParada());
        db.insertOrThrow("Evento", null, values);
    }

    public void inserirLogInicioFimViagemFretadoVO(EventoVO evento) {
        ContentValues values = new ContentValues();
        values.put("operacaoMonitriip",evento.getOperacao().name());
        values.put("idVeiculo",evento.getIdVeiculo());
        values.put("dataHoraLong", evento.getDataHoraLong());
        values.put("imei", evento.getImei());
        values.put("latitude", evento.getLatitude());
        values.put("longitude",evento.getLongitude());
        values.put("pdop",evento.getPdop());
        values.put("autorizacaoViagem", evento.getAutorizacaoViagem());
        values.put("tipoRegistroViagem", evento.getTipoRegistroViagem().getCod());
        values.put("sentidoLinha", evento.getSentidoLinha());
        db.insertOrThrow("Evento", null, values);
    }

    public void inserirLogJornadaTrabalhoMotoristaVO(EventoVO evento) {
        ContentValues values = new ContentValues();
        values.put("operacaoMonitriip",evento.getOperacao().name());
        values.put("idVeiculo",evento.getIdVeiculo());
        values.put("dataHoraLong", evento.getDataHoraLong());
        values.put("imei", evento.getImei());
        values.put("latitude", evento.getLatitude());
        values.put("longitude",evento.getLongitude());
        values.put("pdop",evento.getPdop());
        values.put("cpfMotorista", evento.getCpfMotorista());
        values.put("tipoRegistroEvento", evento.getTipoRegistroEvento().getCod());
        db.insertOrThrow("Evento", null, values);
    }

    public void inserirLogInicioFimViagemRegularVO(EventoVO evento) {
        ContentValues values = new ContentValues();
        values.put("operacaoMonitriip",evento.getOperacao().name());
        values.put("idVeiculo",evento.getIdVeiculo());
        values.put("dataHoraLong", evento.getDataHoraLong());
        values.put("imei", evento.getImei());
        values.put("latitude", evento.getLatitude());
        values.put("longitude",evento.getLongitude());
        values.put("pdop",evento.getPdop());
        values.put("identificaoLinha", evento.getIdentificaoLinha());
        values.put("codigoTipoViagem", evento.getCodigoTipoViagem());
        values.put("dataProgramadaViagem", evento.getDataProgramadaViagem());
        values.put("horaProgramadaViagem", evento.getHoraProgramadaViagem());
        db.insertOrThrow("Evento", null, values);
    }


    public ArrayList<EventoVO> selectEventos() {

        Cursor cursor = db.rawQuery("SELECT * FROM Evento ", null);
        try {
            if (cursor.getCount() > 0) {

                ArrayList<EventoVO> lista = new ArrayList<EventoVO>(cursor.getCount());
                EventoVO vo = null;

                while (cursor.moveToNext()) {
                    vo = new EventoVO();
                    vo.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    vo.setOperacao(OperacaoMonitriip.valueOf(cursor.getString(cursor.getColumnIndex("operacaoMonitriip"))));
                    vo.setIdVeiculo(cursor.getInt(cursor.getColumnIndex("idVeiculo")));
                    vo.setLatitude(cursor.getString(cursor.getColumnIndex("latitude")));
                    vo.setLongitude(cursor.getString(cursor.getColumnIndex("longitude")));
                    vo.setDataHoraLong(cursor.getLong(cursor.getColumnIndex("dataHoraLong")));
                    vo.setImei(cursor.getString(cursor.getColumnIndex("imei")));
                    vo.setPdop(cursor.getString(cursor.getColumnIndex("pdop")));
                    vo.setMotivoParada(cursor.getString(cursor.getColumnIndex("motivoParada")));
                    vo.setAutorizacaoViagem(cursor.getString(cursor.getColumnIndex("autorizacaoViagem")));
                    if (cursor.getString(cursor.getColumnIndex("tipoRegistroViagem")) != null) {
                        vo.setTipoRegistroViagem(TipoRegistroViagem.getByCod(cursor.getString(cursor.getColumnIndex("tipoRegistroViagem"))));
                    }
                    vo.setSentidoLinha(cursor.getString(cursor.getColumnIndex("sentidoLinha")));
                    vo.setCpfMotorista(cursor.getString(cursor.getColumnIndex("cpfMotorista")));
                    if (cursor.getString(cursor.getColumnIndex("tipoRegistroEvento")) != null) {
                        vo.setTipoRegistroEvento(TipoRegistroEvento.getByCod(cursor.getString(cursor.getColumnIndex("tipoRegistroEvento"))));
                    }
                    vo.setIdentificaoLinha(cursor.getString(cursor.getColumnIndex("identificaoLinha")));
                    vo.setCodigoTipoViagem(cursor.getString(cursor.getColumnIndex("codigoTipoViagem")));
                    vo.setDataProgramadaViagem(cursor.getString(cursor.getColumnIndex("dataProgramadaViagem")));
                    vo.setHoraProgramadaViagem(cursor.getString(cursor.getColumnIndex("horaProgramadaViagem")));
                    lista.add(vo);
                }

                return lista;
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }

    }

}