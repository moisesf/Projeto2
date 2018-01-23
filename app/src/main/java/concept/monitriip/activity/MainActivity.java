package concept.monitriip.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import concept.monitriip.R;
import concept.monitriip.fachada.DatabaseHelper;
import concept.monitriip.fachada.HttpFachada;
import concept.monitriip.fachada.MixedPositionProvider;
import concept.monitriip.fachada.MonitriipFachada;
import concept.monitriip.fachada.PositionProvider;
import concept.monitriip.fachada.TrackingController;
import concept.monitriip.vo.TipoRegistroEvento;
import concept.monitriip.vo.TipoRegistroViagem;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    private static final SimpleDateFormat formatoYYYYMMDD = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat formatoHHmm = new SimpleDateFormat("HHmm");

    private int idVeiculo;
    private int idCliente;
    private String nomeMotorista = "Antonio das Neves";
    private String placaVeiculo = "PIS-1650";
    private String cpf;
    private String autorizacaoViagem;
    private String sentidoLinha;
    private Button btnIniciarJornada;
    private Button btnFinalizarJornada;
    private Button btnInformarAndamentoJornada;
    private Button btnRegistrarParada;
    private Button btnIniciarViagemFretado;
    private Button btnFinalizarViagemFretado;
    private Button btnIniciarViagemFretadoComTransbordo;
    private Button btnFinalizarViagemFretadoComTransbordo;

    private Button btnIniciarViagemRegular;
    private Button btnFinalizarViagemRegular;
    private Button btnIniciarViagemRegularComTransbordo;
    private Button btnFinalizarViagemRegularComTransbordo;

    private Button btnSair;
    private TextView mLabelNomeMotorista;
    private TextView mLabelPlacaVeiculo;
    private MonitriipFachada fachada;
    private HttpFachada fachadaHttp;
    private TelephonyManager telephonyManager;
    private static String IMEI;
    private TrackingController trackingController;
    private AtualizadorTask mEnviarAtualizacoesTask;
    private Timer myTimer;
    private TextView mtvDataViagem;
    private TextView mtvHoraViagem;
    private DatePickerFragment newFragmentDatePicker = new DatePickerFragment();
    private TimePickerFragment newFragmentTimePicker = new TimePickerFragment();


    public static String getIMEI(){
        return IMEI;
    }
    private final CharSequence[] motivosParadas = { "Parada programada",  "Solicitação de passageiro", "Solicitação de motorista", "Solicitação Externa", "Solicitação agente rodoviário",
            "Acidente na Via", "Acidente com o veículo", "Acidente com o passageiro", "Defeito no veículo", "Troca programada de veículo" , "Outro" };

    public void setIMEI(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_PHONE_STATE)) {
                telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                if (IMEI == null){
                    IMEI = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        } else {
            telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            IMEI = telephonyManager.getDeviceId();
            if (IMEI == null){
                IMEI = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fachada = new MonitriipFachada(this );
        fachadaHttp = new HttpFachada(this);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        idCliente = intent.getIntExtra(LoginActivity.ID_CLIENTE,0);
        idVeiculo = intent.getIntExtra(LoginActivity.ID_VEICULO,0);
        nomeMotorista = intent.getStringExtra(LoginActivity.NOME_MOTORISTA);
        placaVeiculo = intent.getStringExtra(LoginActivity.PLACA_VEICULO);
        cpf = intent.getStringExtra(LoginActivity.CPF);

        mLabelNomeMotorista = (TextView) findViewById(R.id.tvNomeMotorista);
        mLabelNomeMotorista.setText(nomeMotorista);

        mLabelPlacaVeiculo = (TextView) findViewById(R.id.tvPlacaVeiculo);
        mLabelPlacaVeiculo.setText(placaVeiculo);
        setIMEI();
        trackingController = new TrackingController(this);
        trackingController.start();

        btnIniciarJornada = (Button) findViewById(R.id.btn_iniciar_jornada);
        btnIniciarJornada.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (trackingController.getLastPosition() != null) {
                    btnIniciarJornada.setVisibility(View.GONE);
                    btnFinalizarJornada.setVisibility(View.VISIBLE);
                    btnInformarAndamentoJornada.setVisibility(View.VISIBLE);
                    btnRegistrarParada.setVisibility(View.VISIBLE);
                    btnIniciarViagemFretado.setVisibility(View.VISIBLE);
                    btnIniciarViagemFretadoComTransbordo.setVisibility(View.VISIBLE);
                    btnIniciarViagemRegular.setVisibility(View.VISIBLE);
                    btnIniciarViagemRegularComTransbordo.setVisibility(View.VISIBLE);
                    fachada.inserirLogJornadaTrabalhoMotoristaVO(idVeiculo, IMEI, trackingController.getLastPosition(), cpf, TipoRegistroEvento.INICIO);
                } else {
                    Toast.makeText(v.getContext(), "Aguarde o GPS inicializar", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnFinalizarJornada = (Button) findViewById(R.id.btn_finalizar_jornada);
        btnFinalizarJornada.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (trackingController.getLastPosition() != null) {
                    btnIniciarJornada.setVisibility(View.VISIBLE);
                    btnFinalizarJornada.setVisibility(View.GONE);
                    btnInformarAndamentoJornada.setVisibility(View.GONE);
                    btnRegistrarParada.setVisibility(View.GONE);
                    btnIniciarViagemFretado.setVisibility(View.GONE);
                    btnFinalizarViagemFretado.setVisibility(View.GONE);
                    btnIniciarViagemFretadoComTransbordo.setVisibility(View.GONE);
                    btnFinalizarViagemFretadoComTransbordo.setVisibility(View.GONE);
                    btnIniciarViagemRegular.setVisibility(View.GONE);
                    btnFinalizarViagemRegular.setVisibility(View.GONE);
                    btnIniciarViagemRegularComTransbordo.setVisibility(View.GONE);
                    btnFinalizarViagemRegularComTransbordo.setVisibility(View.GONE);
                    fachada.inserirLogJornadaTrabalhoMotoristaVO(idVeiculo, IMEI, trackingController.getLastPosition(), cpf, TipoRegistroEvento.FIM );
                } else {
                    Toast.makeText(v.getContext(), "Aguarde o GPS inicializar", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnInformarAndamentoJornada = (Button) findViewById(R.id.btn_informar_jornada_andamento);
        btnInformarAndamentoJornada.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (trackingController.getLastPosition() != null) {
                    fachada.inserirLogJornadaTrabalhoMotoristaVO(idVeiculo, IMEI, trackingController.getLastPosition(), cpf, TipoRegistroEvento.JORNADA );
                } else {
                    Toast.makeText(v.getContext(), "Aguarde o GPS inicializar", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnRegistrarParada = (Button) findViewById(R.id.btn_registrar_parada);
        btnRegistrarParada.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Informe o motivo da parada:");

                builder.setItems(motivosParadas, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (trackingController.getLastPosition() != null) {
                            fachada.inserirLogDetectorParadaVO(idVeiculo, IMEI, trackingController.getLastPosition(), which );
                        } else {
                            Toast.makeText(v.getContext(), "Aguarde o GPS inicializar", Toast.LENGTH_LONG).show();
                        }
                    }
                }).show();
            }
        });

        btnIniciarViagemFretado = (Button) findViewById(R.id.btn_iniciar_viagem_fretado);
        btnIniciarViagemFretado.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();

                final View view = inflater.inflate(R.layout.dialog_viagem_fretada, null);
                builder.setView(view);
                builder.setPositiveButton("Gravar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Validar os campos
                        if (trackingController.getLastPosition() != null) {

                            EditText etAutorizacao = (EditText) view.findViewById(R.id.autorizacaoViagem);
                            if (etAutorizacao.getText() == null) {
                                Toast.makeText(v.getContext(), "Código Autorização da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }
                            RadioGroup rg = (RadioGroup) view.findViewById(R.id.rgSentidoViagem);
                            int radioButtonID = rg.getCheckedRadioButtonId();
                            if (radioButtonID < 0) {
                                Toast.makeText(v.getContext(), "Sentido da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }
                            String sentido = (radioButtonID == 0 ? "1" : "0");

                            fachada.inserirLogInicioFimViagemFretadoVO(idVeiculo, IMEI, trackingController.getLastPosition(), TipoRegistroViagem.INICIO, etAutorizacao.getText().toString(), sentido);

                            btnIniciarViagemFretado.setVisibility(View.GONE);
                            btnFinalizarViagemFretado.setVisibility(View.VISIBLE);
                            btnIniciarViagemFretadoComTransbordo.setVisibility(View.GONE);
                            btnFinalizarViagemFretadoComTransbordo.setVisibility(View.GONE);
                            btnIniciarViagemRegular.setVisibility(View.GONE);
                            btnFinalizarViagemRegular.setVisibility(View.GONE);
                            btnIniciarViagemRegularComTransbordo.setVisibility(View.GONE);
                            btnFinalizarViagemRegularComTransbordo.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(v.getContext(), "Aguarde o GPS inicializar", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("Voltar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //MainActivity.this.getDialog().cancel(); ???? O que fazer ?
                            }
                });

                builder.show();
            }
        });

        btnFinalizarViagemFretado = (Button) findViewById(R.id.btn_finalizar_viagem_fretado);
        btnFinalizarViagemFretado.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();

                final View view = inflater.inflate(R.layout.dialog_viagem_fretada, null);
                builder.setView(view);
                builder.setPositiveButton("Gravar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Validar os campos
                        if (trackingController.getLastPosition() != null) {

                            EditText etAutorizacao = (EditText) view.findViewById(R.id.autorizacaoViagem);
                            if (etAutorizacao.getText() == null) {
                                Toast.makeText(v.getContext(), "Código Autorização da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }
                            RadioGroup rg = (RadioGroup) view.findViewById(R.id.rgSentidoViagem);
                            int radioButtonID = rg.getCheckedRadioButtonId();
                            if (radioButtonID < 0) {
                                Toast.makeText(v.getContext(), "Sentido da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }
                            String sentido = (radioButtonID == 0 ? "1" : "0");

                            fachada.inserirLogInicioFimViagemFretadoVO(idVeiculo, IMEI, trackingController.getLastPosition(), TipoRegistroViagem.FIM, etAutorizacao.getText().toString(), sentido);

                            btnIniciarViagemFretado.setVisibility(View.VISIBLE);
                            btnFinalizarViagemFretado.setVisibility(View.GONE);
                            btnIniciarViagemFretadoComTransbordo.setVisibility(View.VISIBLE);
                            btnFinalizarViagemFretadoComTransbordo.setVisibility(View.GONE);
                            btnIniciarViagemRegular.setVisibility(View.VISIBLE);
                            btnFinalizarViagemRegular.setVisibility(View.GONE);
                            btnIniciarViagemRegularComTransbordo.setVisibility(View.VISIBLE);
                            btnFinalizarViagemRegularComTransbordo.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(v.getContext(), "Aguarde o GPS inicializar", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("Voltar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //MainActivity.this.getDialog().cancel(); ???? O que fazer ?
                    }
                });

                builder.show();

            }
        });

        btnIniciarViagemFretadoComTransbordo = (Button) findViewById(R.id.btn_iniciar_viagem_fretado_com_tramsbordo);
        btnIniciarViagemFretadoComTransbordo.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();

                final View view = inflater.inflate(R.layout.dialog_viagem_fretada, null);
                builder.setView(view);
                builder.setPositiveButton("Gravar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Validar os campos
                        if (trackingController.getLastPosition() != null) {

                            EditText etAutorizacao = (EditText) view.findViewById(R.id.autorizacaoViagem);
                            if (etAutorizacao.getText() == null) {
                                Toast.makeText(v.getContext(), "Código Autorização da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }
                            RadioGroup rg = (RadioGroup) view.findViewById(R.id.rgSentidoViagem);
                            int radioButtonID = rg.getCheckedRadioButtonId();
                            if (radioButtonID < 0) {
                                Toast.makeText(v.getContext(), "Sentido da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }
                            String sentido = (radioButtonID == 0 ? "1" : "0");

                            fachada.inserirLogInicioFimViagemFretadoVO(idVeiculo, IMEI, trackingController.getLastPosition(), TipoRegistroViagem.INICIO_COM_TRANSBORDO, etAutorizacao.getText().toString(), sentido);

                            btnIniciarViagemFretado.setVisibility(View.GONE);
                            btnFinalizarViagemFretado.setVisibility(View.GONE);
                            btnIniciarViagemFretadoComTransbordo.setVisibility(View.GONE);
                            btnFinalizarViagemFretadoComTransbordo.setVisibility(View.VISIBLE);
                            btnIniciarViagemRegular.setVisibility(View.GONE);
                            btnFinalizarViagemRegular.setVisibility(View.GONE);
                            btnIniciarViagemRegularComTransbordo.setVisibility(View.GONE);
                            btnFinalizarViagemRegularComTransbordo.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(v.getContext(), "Aguarde o GPS inicializar", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("Voltar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //MainActivity.this.getDialog().cancel(); ???? O que fazer ?
                    }
                });

                builder.show();
            }
        });

        btnFinalizarViagemFretadoComTransbordo = (Button) findViewById(R.id.btn_finalizar_viagem_fretado_com_transbordo);
        btnFinalizarViagemFretadoComTransbordo.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();

                final View view = inflater.inflate(R.layout.dialog_viagem_fretada, null);
                builder.setView(view);
                builder.setPositiveButton("Gravar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Validar os campos
                        if (trackingController.getLastPosition() != null) {

                            EditText etAutorizacao = (EditText) view.findViewById(R.id.autorizacaoViagem);
                            if (etAutorizacao.getText() == null) {
                                Toast.makeText(v.getContext(), "Código Autorização da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }
                            RadioGroup rg = (RadioGroup) view.findViewById(R.id.rgSentidoViagem);
                            int radioButtonID = rg.getCheckedRadioButtonId();
                            if (radioButtonID < 0) {
                                Toast.makeText(v.getContext(), "Sentido da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }
                            String sentido = (radioButtonID == 0 ? "1" : "0");

                            fachada.inserirLogInicioFimViagemFretadoVO(idVeiculo, IMEI, trackingController.getLastPosition(), TipoRegistroViagem.FIM_COM_TRANSBORDO, etAutorizacao.getText().toString(), sentido);

                            btnIniciarViagemFretado.setVisibility(View.VISIBLE);
                            btnFinalizarViagemFretado.setVisibility(View.GONE);
                            btnIniciarViagemFretadoComTransbordo.setVisibility(View.VISIBLE);
                            btnFinalizarViagemFretadoComTransbordo.setVisibility(View.GONE);
                            btnIniciarViagemRegular.setVisibility(View.VISIBLE);
                            btnFinalizarViagemRegular.setVisibility(View.GONE);
                            btnIniciarViagemRegularComTransbordo.setVisibility(View.VISIBLE);
                            btnFinalizarViagemRegularComTransbordo.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(v.getContext(), "Aguarde o GPS inicializar", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("Voltar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //MainActivity.this.getDialog().cancel(); ???? O que fazer ?
                    }
                });

                builder.show();
            }
        });

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////// VIAGEM REGULAR //////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        btnIniciarViagemRegular = (Button) findViewById(R.id.btn_iniciar_viagem_regular);
        btnIniciarViagemRegular.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();

                final View view = inflater.inflate(R.layout.dialog_viagem_regular, null);
                builder.setView(view);
                mtvDataViagem = (TextView) view.findViewById(R.id.tvDataProgramadaViagem);
                mtvHoraViagem = (TextView) view.findViewById(R.id.tvHoraProgramdaViagem);

                builder.setPositiveButton("Gravar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Validar os campos
                        if (trackingController.getLastPosition() != null) {

                            EditText etIdentificacaoLinha = (EditText) view.findViewById(R.id.identificacaoLinha);
                            if (etIdentificacaoLinha.getText() == null) {
                                Toast.makeText(v.getContext(), "Identificação da Linha obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }

                            EditText etTipoViagem = (EditText) view.findViewById(R.id.etTipoViagem);
                            if (etTipoViagem.getText() == null) {
                                Toast.makeText(v.getContext(), "Tipo da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }

                            TextView tvDataViagem = (TextView) view.findViewById(R.id.tvDataProgramadaViagem);
                            if (tvDataViagem.getText() == null) {
                                Toast.makeText(v.getContext(), "Data da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }

                            TextView tvHoraViagem = (TextView) view.findViewById(R.id.tvHoraProgramdaViagem);
                            if (tvHoraViagem.getText() == null) {
                                Toast.makeText(v.getContext(), "Hora da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }

                            RadioGroup rg = (RadioGroup) view.findViewById(R.id.rgSentidoViagem);
                            int radioButtonID = rg.getCheckedRadioButtonId();
                            if (radioButtonID < 0) {
                                Toast.makeText(v.getContext(), "Sentido da Linha obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }
                            String sentido = (radioButtonID == 0 ? "1" : "0");

                            fachada.inserirLogInicioFimViagemRegularVO(idVeiculo, IMEI, trackingController.getLastPosition(), TipoRegistroViagem.INICIO, etIdentificacaoLinha.getText().toString(),
                                    etTipoViagem.getText().toString(), tvDataViagem.getText().toString(), tvHoraViagem.getText().toString(), sentido);

                            btnIniciarViagemRegular.setVisibility(View.GONE);
                            btnFinalizarViagemRegular.setVisibility(View.VISIBLE);
                            btnIniciarViagemRegularComTransbordo.setVisibility(View.GONE);
                            btnFinalizarViagemRegularComTransbordo.setVisibility(View.GONE);
                            btnIniciarViagemFretado.setVisibility(View.GONE);
                            btnFinalizarViagemFretado.setVisibility(View.GONE);
                            btnIniciarViagemFretadoComTransbordo.setVisibility(View.GONE);
                            btnFinalizarViagemFretadoComTransbordo.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(v.getContext(), "Aguarde o GPS inicializar", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("Voltar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //MainActivity.this.getDialog().cancel(); ???? O que fazer ?
                    }
                });

                builder.show();
            }
        });

        btnFinalizarViagemRegular = (Button) findViewById(R.id.btn_finalizar_viagem_regular);
        btnFinalizarViagemRegular.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();

                final View view = inflater.inflate(R.layout.dialog_viagem_regular, null);
                builder.setView(view);
                builder.setPositiveButton("Gravar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Validar os campos
                        if (trackingController.getLastPosition() != null) {

                            EditText etAutorizacao = (EditText) view.findViewById(R.id.autorizacaoViagem);
                            if (etAutorizacao.getText() == null) {
                                Toast.makeText(v.getContext(), "Código Autorização da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }
                            RadioGroup rg = (RadioGroup) view.findViewById(R.id.rgSentidoViagem);
                            int radioButtonID = rg.getCheckedRadioButtonId();
                            if (radioButtonID < 0) {
                                Toast.makeText(v.getContext(), "Sentido da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }
                            String sentido = (radioButtonID == 0 ? "1" : "0");

                            fachada.inserirLogInicioFimViagemFretadoVO(idVeiculo, IMEI, trackingController.getLastPosition(), TipoRegistroViagem.FIM, etAutorizacao.getText().toString(), sentido);

                            btnIniciarViagemRegular.setVisibility(View.VISIBLE);
                            btnFinalizarViagemRegular.setVisibility(View.GONE);
                            btnIniciarViagemRegularComTransbordo.setVisibility(View.VISIBLE);
                            btnFinalizarViagemRegularComTransbordo.setVisibility(View.GONE);
                            btnIniciarViagemFretado.setVisibility(View.VISIBLE);
                            btnFinalizarViagemFretado.setVisibility(View.GONE);
                            btnIniciarViagemFretadoComTransbordo.setVisibility(View.VISIBLE);
                            btnFinalizarViagemFretadoComTransbordo.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(v.getContext(), "Aguarde o GPS inicializar", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("Voltar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //MainActivity.this.getDialog().cancel(); ???? O que fazer ?
                    }
                });

                builder.show();

            }
        });

        btnIniciarViagemRegularComTransbordo = (Button) findViewById(R.id.btn_iniciar_viagem_regular_com_tramsbordo);
        btnIniciarViagemRegularComTransbordo.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();

                final View view = inflater.inflate(R.layout.dialog_viagem_fretada, null);
                builder.setView(view);
                builder.setPositiveButton("Gravar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Validar os campos
                        if (trackingController.getLastPosition() != null) {

                            EditText etAutorizacao = (EditText) view.findViewById(R.id.autorizacaoViagem);
                            if (etAutorizacao.getText() == null) {
                                Toast.makeText(v.getContext(), "Código Autorização da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }
                            RadioGroup rg = (RadioGroup) view.findViewById(R.id.rgSentidoViagem);
                            int radioButtonID = rg.getCheckedRadioButtonId();
                            if (radioButtonID < 0) {
                                Toast.makeText(v.getContext(), "Sentido da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }
                            String sentido = (radioButtonID == 0 ? "1" : "0");

                            fachada.inserirLogInicioFimViagemFretadoVO(idVeiculo, IMEI, trackingController.getLastPosition(), TipoRegistroViagem.INICIO_COM_TRANSBORDO, etAutorizacao.getText().toString(), sentido);

                            btnIniciarViagemRegular.setVisibility(View.GONE);
                            btnFinalizarViagemRegular.setVisibility(View.GONE);
                            btnIniciarViagemRegularComTransbordo.setVisibility(View.GONE);
                            btnFinalizarViagemRegularComTransbordo.setVisibility(View.VISIBLE);
                            btnIniciarViagemFretado.setVisibility(View.GONE);
                            btnFinalizarViagemFretado.setVisibility(View.GONE);
                            btnIniciarViagemFretadoComTransbordo.setVisibility(View.GONE);
                            btnFinalizarViagemFretadoComTransbordo.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(v.getContext(), "Aguarde o GPS inicializar", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("Voltar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //MainActivity.this.getDialog().cancel(); ???? O que fazer ?
                    }
                });

                builder.show();
            }
        });

        btnFinalizarViagemRegularComTransbordo = (Button) findViewById(R.id.btn_finalizar_viagem_regular_com_transbordo);
        btnFinalizarViagemRegularComTransbordo.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();

                final View view = inflater.inflate(R.layout.dialog_viagem_regular, null);
                builder.setView(view);
                builder.setPositiveButton("Gravar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Validar os campos
                        if (trackingController.getLastPosition() != null) {

                            EditText etAutorizacao = (EditText) view.findViewById(R.id.autorizacaoViagem);
                            if (etAutorizacao.getText() == null) {
                                Toast.makeText(v.getContext(), "Código Autorização da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }
                            RadioGroup rg = (RadioGroup) view.findViewById(R.id.rgSentidoViagem);
                            int radioButtonID = rg.getCheckedRadioButtonId();
                            if (radioButtonID < 0) {
                                Toast.makeText(v.getContext(), "Sentido da Viagem obrigatório", Toast.LENGTH_LONG).show();
                                return;
                            }
                            String sentido = (radioButtonID == 0 ? "1" : "0");

                            fachada.inserirLogInicioFimViagemFretadoVO(idVeiculo, IMEI, trackingController.getLastPosition(), TipoRegistroViagem.FIM_COM_TRANSBORDO, etAutorizacao.getText().toString(), sentido);

                            btnIniciarViagemRegular.setVisibility(View.VISIBLE);
                            btnFinalizarViagemRegular.setVisibility(View.GONE);
                            btnIniciarViagemRegularComTransbordo.setVisibility(View.VISIBLE);
                            btnFinalizarViagemRegularComTransbordo.setVisibility(View.GONE);
                            btnIniciarViagemFretado.setVisibility(View.VISIBLE);
                            btnFinalizarViagemFretado.setVisibility(View.GONE);
                            btnIniciarViagemFretadoComTransbordo.setVisibility(View.VISIBLE);
                            btnFinalizarViagemFretadoComTransbordo.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(v.getContext(), "Aguarde o GPS inicializar", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton("Voltar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //MainActivity.this.getDialog().cancel(); ???? O que fazer ?
                    }
                });

                builder.show();
            }
        });

        btnSair = (Button) findViewById(R.id.btn_sair);
        btnSair.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (myTimer != null) {
                    myTimer.cancel();
                }
                fachadaHttp.stopNetworkManager();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnFinalizarJornada.setVisibility(View.GONE);
        btnInformarAndamentoJornada.setVisibility(View.GONE);
        btnRegistrarParada.setVisibility(View.GONE);
        btnIniciarViagemFretado.setVisibility(View.GONE);
        btnFinalizarViagemFretado.setVisibility(View.GONE);
        btnIniciarViagemFretadoComTransbordo.setVisibility(View.GONE);
        btnFinalizarViagemFretadoComTransbordo.setVisibility(View.GONE);
        btnIniciarViagemRegular.setVisibility(View.GONE);
        btnFinalizarViagemRegular.setVisibility(View.GONE);
        btnIniciarViagemRegularComTransbordo.setVisibility(View.GONE);
        btnFinalizarViagemRegularComTransbordo.setVisibility(View.GONE);
        ativarTimerDeAtualizacao();
    }


    @Override
    protected void onResume() {
        super.onResume();
        ativarTimerDeAtualizacao();
    }

    private void ativarTimerDeAtualizacao(){
        // Ativar o Timer para atualização periodica;
        if (myTimer == null) {
            myTimer = new Timer();
        } else {
            myTimer.cancel();
            myTimer = null;
            myTimer = new Timer();
        }

        if (mEnviarAtualizacoesTask == null) {
            mEnviarAtualizacoesTask = new AtualizadorTask();
        } else {
            mEnviarAtualizacoesTask = null;
            mEnviarAtualizacoesTask = new AtualizadorTask();
        }

        myTimer.scheduleAtFixedRate(mEnviarAtualizacoesTask, 10000, 15000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (trackingController != null) {
            trackingController.stop();
        }
    }

    private class AtualizadorTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new  Runnable() {
                public void run() {
                    fachadaHttp.atualizacoesEmBackground();
                }
            });
        }
    }

    public void showDatePickerDialog(View v) {
        newFragmentDatePicker.show(getFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View v) {
        newFragmentTimePicker.show(getFragmentManager(), "timePicker");
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar cal = new GregorianCalendar(year, month, day);
        mtvDataViagem.setText(formatoYYYYMMDD.format(cal.getTime()));

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        mtvHoraViagem.setText(formatoHHmm.format(cal.getTime()));
    }

    public static class DatePickerFragment extends DialogFragment  {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener)getActivity(), year, month, day);
        }

    }

    public static class TimePickerFragment extends DialogFragment  {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), (TimePickerDialog.OnTimeSetListener)getActivity(), hour, minute, true);
        }

    }
}