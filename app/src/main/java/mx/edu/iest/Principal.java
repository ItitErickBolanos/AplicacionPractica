package mx.edu.iest.aplicacionpractica;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Principal extends AppCompatActivity {

    private String[] listaMenu;
    private DrawerLayout layoutDrawer;
    private NavigationView listaDrawer;
    private ActionBarDrawerToggle toggleDrawer;

    private CharSequence tituloDrawer;
    private CharSequence titulo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        titulo = tituloDrawer = getTitle();
        listaMenu = getResources().getStringArray(R.array.listaMenu);
        layoutDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        listaDrawer = (NavigationView) findViewById(R.id.left_drawer);

        layoutDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        setupDrawerContent(listaDrawer);

        //Establecer el adaptador para el listView
        /*listaDrawer.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, listaMenu));

        //Establece la escucha de eventos para el navigation drawer
        listaDrawer.setOnItemClickListener(new DrawerItemClickListener());*/

        toggleDrawer = new ActionBarDrawerToggle(
                this,
                layoutDrawer,
                R.string.drawer_open,
                R.string.drawer_close
                ){

            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(titulo);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(tituloDrawer);
            }
        };

        layoutDrawer.setDrawerListener(toggleDrawer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void setupDrawerContent(NavigationView navigationView){
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener(){
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem){
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggleDrawer.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggleDrawer.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (toggleDrawer.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }



    private void selectDrawerItem(MenuItem menuItem){
        //Crea un nuevo fragment y lo muestra
        /*

        Fragment fragment = new ClientesFragment();
        Bundle args = new Bundle();
        args.putInt(ClientesFragment.ARG_TITULO, position);
        fragment.setArguments(args);

        //Inserta el fragment reemplazando cualquier otro fragment existente
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                       .replace(R.id.content_frame, fragment)
                       .commit();

        //Actualiza el título y cierra el drawer
        listaDrawer.setItemChecked(position, true);
        setTitle(listaMenu[position]);
        layoutDrawer.closeDrawer(listaDrawer);*/

        Fragment fragment = null;
        Class fragmentClass;

        switch(menuItem.getItemId()){
            case R.id.nav_clientes_fragment:
                fragmentClass = ClientesFragment.class;
                break;
            case R.id.nav_autoridades_fragment:
                fragmentClass = AutoridadesFragment.class;
                break;

            case R.id.nav_agregar_caso_fragment:
                fragmentClass = NuevoCasoFragment.class;
                break;

            default:
                fragmentClass = ClientesFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch(Exception e){
            e.printStackTrace();
        }

        //Inserta el fragment reemplazando cualquier otro fragment existente
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        //Actualiza el título y cierra el drawer
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        layoutDrawer.closeDrawers();
    }

    public void setTitle(CharSequence title){
        titulo = title;
        getSupportActionBar().setTitle(titulo);
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public static class ClientesFragment extends Fragment {
        //public static final String ARG_TITULO = "Título fragment";

        public ClientesFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.clientes_fragment, container, false);

            new ObtieneDatosClientes().execute();

            return rootView;
        }

        private class ObtieneDatosClientes extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... params) {
                // These two need to be declared outside the try/catch
                // so that they can be closed in the finally block.
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Will contain the raw JSON response as a string.
                String jsonStr = null;

                try {
                    // Construct the URL for the OpenWeatherMap query
                    // Possible parameters are avaiable at OWM's forecast API page, at
                    // http://openweathermap.org/API#forecast
                    URL url = new URL("http://breadcrumb.hol.es/pruebas/api.php");

                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setReadTimeout(10000);
                    urlConnection.setConnectTimeout(15000);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("modulo", "clientes")
                            .appendQueryParameter("accion", "obtenerClientes")
                            .appendQueryParameter("tipoRespuesta", "json");
                    String query = builder.build().getEncodedQuery();

                    OutputStream os = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    os.close();

                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }
                    jsonStr = buffer.toString();

                    return jsonStr;
                } catch (IOException e) {
                    Log.e("PlaceholderFragment", "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attemping
                    // to parse it.
                    return null;
                } finally{
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e("PlaceholderFragment", "Error closing stream", e);
                        }
                    }
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //tvWeatherJson.setText(s);
                Log.i("json", "" + s);
            }
        }
    }



    public static class AutoridadesFragment extends Fragment {
        //public static final String ARG_TITULO = "Autoridades fragment";

        public AutoridadesFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.autoridades_fragment, container, false);

            return rootView;
        }
    }

    public static class NuevoCasoFragment extends Fragment {
        //public static final String ARG_TITULO = "Autoridades fragment";

        public NuevoCasoFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.nuevo_caso_fragment, container, false);

            return rootView;
        }
    }
}
