package jeremias.com.loginjson;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Para añadir la librería Volley poné esta línea en el build.gradle(Module:app):
    // compile 'com.mcxiaoke.volley:library:1.0.19'

    public String user = null;
    public String pass = null;

    // json array response url (servidor local creado con Netbeans)
    //
    private String urlJsonArray;

    public static String TAG = MainActivity.class.getSimpleName();

    private Button btnMakeArrayRequest;

    // Progress dialog
    private ProgressDialog pDialog;

    private EditText user_edit;
    private EditText pass_edit;
    private EditText ipAddress;

    // temporary string to show the parsed response
    private String jsonResponse;

    private GlobalData globalData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnMakeArrayRequest = (Button) findViewById(R.id.btnArrayRequest);
        user_edit = (EditText) findViewById(R.id.usuario_text);
        pass_edit = (EditText) findViewById(R.id.contraseña_text);
        ipAddress = (EditText) findViewById(R.id.editIpAddress);

        globalData = (GlobalData) getApplicationContext();

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Espere...");
        pDialog.setCancelable(false);

        btnMakeArrayRequest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                user = user_edit.getText().toString();
                pass = pass_edit.getText().toString();

                urlJsonArray = ipAddress.getText().toString() + "/IngresarService";

                if (!user.equals("") && !pass.equals("")){
                    ingresarUsuario();

                }
                else {
                    dialogError("Error", "No puede dejar campos vacíos", "Aceptar");
                }


            }
        });

    }

    private void ingresarUsuario() {

        showpDialog();

        StringRequest req = new StringRequest(Request.Method.POST, urlJsonArray,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, response.toString());

                        try {


                            JSONArray jsonArray = new JSONArray(response);

                            Integer userId = null;
                            String userName = null;

                            ArrayList<String> arrayNameGrupos = new ArrayList<>();
                            ArrayList<Integer> arrayIdGrupos = new ArrayList<>();

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                if (jsonObject.getBoolean("error")) {
                                    dialogError("Error", jsonObject.getString("msj"), "Aceptar");
                                }

                                userId = jsonObject.getInt("id_cuenta");
                                userName = jsonObject.getString("usuario");

                                JSONArray userGroupNames = jsonObject.getJSONArray("name_grupos");
                                JSONArray userGroupIds = jsonObject.getJSONArray("id_grupos");

                                for (int j = 0; j < userGroupIds.length(); j++) {

                                    arrayIdGrupos.add(userGroupIds.getInt(j));
                                    arrayNameGrupos.add(userGroupNames.getString(j));

                                }

                            }

                            Intent intent = new Intent(MainActivity.this, Home.class);

                            globalData.setUserID(userId);
                            globalData.setUserName(userName);
                            globalData.setUrl(ipAddress.getText().toString());
                            globalData.setIdGrupos(arrayIdGrupos);
                            globalData.setNameGrupos(arrayNameGrupos);

                            startActivity(intent);

                        } catch (JSONException e) {

                            e.printStackTrace();

                        }

                        hidepDialog();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                hidepDialog();
            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();

                // making json array request
                user = user_edit.getText().toString();
                pass = pass_edit.getText().toString();

                params.put("user", user);
                params.put("password", pass);

                return params;
            }
        };

        // Adding request to request queue

        AppController.getInstance().addToRequestQueue(req);

    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void dialogError(String title, String message, String posBtn) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(posBtn,null);
        builder.create();
        builder.show();
    }
}
