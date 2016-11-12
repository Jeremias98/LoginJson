package jeremias.com.loginjson;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class ListaCursos extends AppCompatActivity {

    // Controles
    private ListView listView;

    private ArrayList<Integer> arrayIdGrupos;
    private ArrayList<String> arrayNameGrupos;

    private String urlJsonArray;

    private ProgressDialog pDialog;

    public static String TAG = ListaCursos.class.getSimpleName();

    // Parámetros
    private Integer idGrupo = null;

    private GlobalData globalData;

    private Integer posIndex;

    private Intent starterIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_cursos);

        globalData = (GlobalData) getApplicationContext();

        starterIntent = getIntent();

        arrayNameGrupos = globalData.getNameGruposSinAdmin();
        arrayIdGrupos = globalData.getIdGruposSinAdmin();

        listView = (ListView) findViewById(R.id.listView3);

        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayNameGrupos);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Espere...");
        pDialog.setCancelable(false);

        urlJsonArray = globalData.getUrl()+"/AgregarGrupoService";

        listView.setAdapter(adaptador);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                idGrupo = arrayIdGrupos.get(position);
                posIndex = position;
                enlazarCuentaGrupo();

            }
        });

    }

    @Override
    public void onBackPressed() {
        actualizarHome();
    }

    private void enlazarCuentaGrupo() {

        showpDialog();

        StringRequest req = new StringRequest(Request.Method.POST, urlJsonArray,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, response.toString());

                        try {

                            JSONArray jsonArray = new JSONArray(response);

                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                            globalData.setAgregado(true);

                            actualizarHome();

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

                params.put("id_grupo", idGrupo.toString());
                params.put("id_cuenta", globalData.getUserID().toString());

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);

    }

    private void actualizarHome() {

        showpDialog();

        StringRequest req = new StringRequest(Request.Method.POST, globalData.getUrl() + "/IngresarService",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, response.toString());

                        try {


                            JSONArray jsonArray = new JSONArray(response);

                            ArrayList<String> arrayNameGrupos = new ArrayList<String>();
                            ArrayList<Integer> arrayIdGrupos = new ArrayList<Integer>();

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                if (jsonObject.getBoolean("error")) {
                                    dialogError("Error", jsonObject.getString("msj"), "Aceptar");
                                }

                                JSONArray userGroupNames = jsonObject.getJSONArray("name_grupos");
                                JSONArray userGroupIds = jsonObject.getJSONArray("id_grupos");

                                for (int j = 0; j < userGroupIds.length(); j++) {

                                    arrayIdGrupos.add(userGroupIds.getInt(j));
                                    arrayNameGrupos.add(userGroupNames.getString(j));

                                }

                            }

                            Intent intent = new Intent(ListaCursos.this, Home.class);

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

                params.put("id", globalData.getUserID().toString());

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
        AlertDialog.Builder builder = new AlertDialog.Builder(ListaCursos.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(posBtn,null);
        builder.create();
        builder.show();
    }
}
