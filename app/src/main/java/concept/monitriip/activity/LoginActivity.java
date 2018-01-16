package concept.monitriip.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import concept.monitriip.R;
import concept.monitriip.fachada.Constantes;
import concept.monitriip.vo.VeiculoVO;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {

    public static final String ID_CLIENTE = "concept.ID_CLIENTE";
    public static final String ID_VEICULO = "concept.ID_VEICULO";
    public static final String NOME_MOTORISTA = "concept.NOME_MOTORISTA";
    public static final String PLACA_VEICULO = "concept.PLACA_VEICULO";
    public static final String CPF = "concept.CPF";
    private static final int PERMISSIONS_REQUEST_LOCATION = 2;

    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mCPFView;
    private EditText mPlacaView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return true;
        }
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void verificarPermissoes() {

        Set<String> missingPermissions = new HashSet<>();
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            missingPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            missingPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            missingPermissions.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!hasPermission(Manifest.permission.INTERNET)) {
            missingPermissions.add(Manifest.permission.INTERNET);
        }

        if (!hasPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
            missingPermissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }

        if (!hasPermission(Manifest.permission.ACCESS_WIFI_STATE)) {
            missingPermissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        }

        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            missingPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            missingPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }


        if (!missingPermissions.isEmpty()) {
            if (Build.VERSION.SDK_INT >= 23) {
                requestPermissions(missingPermissions.toArray(new String[missingPermissions.size()]), PERMISSIONS_REQUEST_LOCATION);
            }
            return;
        }

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mCPFView = (EditText) findViewById(R.id.cpf);
        mPlacaView = (EditText) findViewById(R.id.placa);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEntrarButton = (Button) findViewById(R.id.entrar_button);
        mEntrarButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        verificarPermissoes();
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mCPFView.setError(null);
        mPlacaView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String cpf = mCPFView.getText().toString();
        String placa = mPlacaView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }


        if (TextUtils.isEmpty(cpf)) {
            mCPFView.setError(getString(R.string.error_field_required));
            focusView = mCPFView;
            cancel = true;
        } else if (!isCPFValid(cpf)) {
            mCPFView.setError(getString(R.string.error_invalid_email));
            focusView = mCPFView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(cpf, placa, password, this);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isCPFValid(String cpf) {
        return cpf.length() >= 11;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private final String mCPF;
        private final String mPlaca;
        private final String mPassword;
        private VeiculoVO veiculo;
        private String erro;

        UserLoginTask(String cpf, String placa, String password, Context pContext) {
            mCPF = cpf;
            mPlaca = placa;
            mPassword = password;
            context = pContext;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(Constantes.ENDERECO_SERVIDOR + "/ValidarAcessoMonitriip");

            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(3);
            nameValuePair.add(new BasicNameValuePair("cpf", mCPF));
            nameValuePair.add(new BasicNameValuePair("placa", mPlaca));
            nameValuePair.add(new BasicNameValuePair("senha", mPassword));

            //Encoding POST data
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            }
            catch (Exception e) {
                // log exception
                e.printStackTrace();
            }

            //making POST request.
            try {
                HttpResponse response = httpClient.execute(httpPost);
                JSONObject resultado = new JSONObject(EntityUtils.toString(response.getEntity()));

                if (resultado.getInt("id") != 0) {
                    veiculo = new VeiculoVO();
                    veiculo.setId(resultado.getInt("id"));
                    veiculo.setPlaca(resultado.getString("placa"));
                    veiculo.setCondutor(resultado.getString("nomeCondutor"));
                    veiculo.setIdCliente(resultado.getInt("idCliente"));
                    veiculo.setCpfCondutor(mCPF);
                } else {
                    return false;
                }

            } catch (ConnectException e) {
                SharedPreferences sharedPref = context.getSharedPreferences("DadosLogin", Context.MODE_PRIVATE);
                String cpfArmazenado = sharedPref.getString(getString(R.string.cpf_stored), null);
                String placaArmazenado = sharedPref.getString(getString(R.string.placa_stored), null);
                String senhaArmazenada = sharedPref.getString(getString(R.string.senha_stored), null);
                if (mCPF.equals(cpfArmazenado) && mPlaca.equals(placaArmazenado) && mPassword.equals(senhaArmazenada)) {

                    if (sharedPref.getString(ID_VEICULO, null) != null) {
                        veiculo = new VeiculoVO();
                        veiculo.setId(Integer.parseInt(sharedPref.getString(ID_VEICULO, null)));
                        veiculo.setIdCliente(Integer.parseInt(sharedPref.getString(ID_CLIENTE, null)));
                        erro = null;
                        return true;
                    }
                }

                erro = "Sem conexão com o servidor.";
                return false;
            } catch (UnknownHostException e) {
                SharedPreferences sharedPref = context.getSharedPreferences("DadosLogin",Context.MODE_PRIVATE);
                String cpfArmazenado = sharedPref.getString(getString(R.string.cpf_stored), null);
                String placaArmazenado = sharedPref.getString(getString(R.string.placa_stored), null);
                String senhaArmazenada = sharedPref.getString(getString(R.string.senha_stored), null);
                if (mCPF.equals(cpfArmazenado) && mPlaca.equals(placaArmazenado) && mPassword.equals(senhaArmazenada)) {

                    if (sharedPref.getString(ID_VEICULO, null) != null) {
                        veiculo = new VeiculoVO();
                        veiculo.setId(Integer.parseInt(sharedPref.getString(ID_VEICULO, null)));
                        veiculo.setIdCliente(Integer.parseInt(sharedPref.getString(ID_CLIENTE, null)));
                        erro = null;
                        return true;
                    }
                }

                erro = "Sem conexão com o servidor.";
                return false;
            } catch (Exception e) {
                // Log exception
                erro = null;
                e.printStackTrace();
                return false;
            }

            erro = null;

            SharedPreferences sharedPref = context.getSharedPreferences("DadosLogin",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.cpf_stored), mCPF);
            editor.putString(getString(R.string.placa_stored), mPlaca);
            editor.putString(getString(R.string.senha_stored), mPassword);
            editor.putString(ID_VEICULO, String.valueOf(veiculo.getId()));
            editor.putString(ID_CLIENTE, String.valueOf(veiculo.getIdCliente()));
            editor.commit();

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra(NOME_MOTORISTA, veiculo.getCondutor());
                intent.putExtra(PLACA_VEICULO, veiculo.getPlaca());
                intent.putExtra(ID_CLIENTE, veiculo.getIdCliente());
                intent.putExtra(ID_VEICULO, veiculo.getId());
                intent.putExtra(CPF, veiculo.getCpfCondutor());
                startActivity(intent);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

