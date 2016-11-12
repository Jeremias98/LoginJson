package jeremias.com.loginjson;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

public class Home extends AppCompatActivity {

    // Controles
    private TextView textView;
    private ListView listView;
    private Button btn;

    // URLs
    private String urlJsonArray;
    private String urlJsonArrayCursos;

    // TAG
    public static String TAG = Home.class.getSimpleName();

    // Progress dialog
    private ProgressDialog pDialog;

    // Parametro(s)
    private Integer par = null;

    // Bundle
    private Bundle bundle;

    private GlobalData globalData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        globalData = (GlobalData) getApplicationContext();

        textView = (TextView) findViewById(R.id.textViewIntegrantes);
        listView = (ListView) findViewById(R.id.listView);
        btn = (Button) findViewById(R.id.agregarBtn);

        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, globalData.getNameGrupos());

        //textView.setText(userName);
        textView.setText(globalData.getUserName());

        listView.setAdapter(adaptador);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Espere...");
        pDialog.setCancelable(false);

        if (globalData.isAgregado()) {
            dialogError("Quicklist", "Agregado correctamente", "Aceptar");
            globalData.setAgregado(false);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                par = globalData.getIdGrupos().get(position);
                urlJsonArray = globalData.getUrl() + "/AlumnoGrupoService";
                alumnosEnGrupo();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urlJsonArrayCursos = globalData.getUrl() + "/ListarCursosService";
                listarCursos();
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();

        Toast.makeText(this.getApplicationContext(), "Hola", Toast.LENGTH_SHORT);

    }

    private void listarCursos() {

        showpDialog();

        StringRequest req = new StringRequest(Request.Method.POST, urlJsonArrayCursos,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, response.toString());

                        try {

                            JSONArray jsonArray = new JSONArray(response);
                            ArrayList<String> arrayNames = new ArrayList<String>();
                            ArrayList<Integer> arrayIDs = new ArrayList<Integer>();

                            boolean flag = false;

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                JSONArray name = jsonObject.getJSONArray("nombre_curso");
                                JSONArray id = jsonObject.getJSONArray("id_curso");

                                for (int j = 0; j < id.length(); j++) {
                                    flag = false;
                                    // Verifico que no aparezcan los cursos que ya administra
                                    for (int k = 0; k < globalData.getIdGrupos().size(); k++){
                                        if (globalData.getIdGrupos().get(k) == id.getInt(j)){
                                            flag = true;
                                            break;
                                        }
                                    }
                                    if (!flag) {
                                        arrayNames.add(name.getString(j));
                                        arrayIDs.add(id.getInt(j));
                                    }

                                }

                            }

                            Intent intent = new Intent(Home.this, ListaCursos.class);

                            globalData.setIdGruposSinAdmin(arrayIDs);
                            globalData.setNameGruposSinAdmin(arrayNames);

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
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);

    }

    private void alumnosEnGrupo() {

        showpDialog();

        StringRequest req = new StringRequest(Request.Method.POST, urlJsonArray,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, response.toString());

                        try {

                            JSONArray jsonArray = new JSONArray(response);
                            ArrayList<String> arrayNames = new ArrayList<String>();

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                JSONArray gpo_name = jsonObject.getJSONArray("alumnos");

                                for (int j = 0; j < gpo_name.length(); j++) {
                                    arrayNames.add(gpo_name.getString(j));
                                }

                            }

                            Intent intent = new Intent(Home.this, Integrantes.class);
                            globalData.setNameAlumnosEnGrupo(arrayNames);
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

                params.put("id_grupo", par.toString());

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
        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(posBtn,null);
        builder.create();
        builder.show();
    }
}
