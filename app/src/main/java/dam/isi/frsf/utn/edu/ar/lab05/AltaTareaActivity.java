package dam.isi.frsf.utn.edu.ar.lab05;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dam.isi.frsf.utn.edu.ar.lab05.dao.ProyectoDAO;
import dam.isi.frsf.utn.edu.ar.lab05.dao.ProyectoDBMetadata;
import dam.isi.frsf.utn.edu.ar.lab05.modelo.Prioridad;
import dam.isi.frsf.utn.edu.ar.lab05.modelo.Proyecto;
import dam.isi.frsf.utn.edu.ar.lab05.modelo.Tarea;
import dam.isi.frsf.utn.edu.ar.lab05.modelo.Usuario;

public class AltaTareaActivity extends AppCompatActivity {
    private ProyectoDAO myDao;
    int idTarea;
    EditText descripcion, horasEstimadas;
    SeekBar prioridad;
    Spinner responsable;
    Button btnGuardar, btnCancelar;
    List<Prioridad> listaPrioridad;
    List<Usuario> listaUsuario;
    Proyecto proyecto;
    Integer userID, minutosTrabajados;
    Boolean esEdicion, tareaFinalizada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alta_tarea);
        myDao = new ProyectoDAO(AltaTareaActivity.this);
        myDao.open();
        idTarea = getIntent().getExtras().getInt("ID_TAREA");

        descripcion = (EditText)findViewById(R.id.etDescripcion);
        horasEstimadas = (EditText)findViewById(R.id.etHorasEstimadas);
        prioridad = (SeekBar)findViewById(R.id.sbPrioridad);

        /**Seteamos el Spinner**/
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                myDao.getCursorUsuarios(),
                new String[] {ProyectoDBMetadata.TablaUsuariosMetadata.USUARIO},
                new int[] {android.R.id.text1},
                0
        );
        responsable = (Spinner)findViewById(R.id.spnrResponsable);
        responsable.setAdapter(adapter);
        responsable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                userID = ((Cursor) parent.getItemAtPosition(position)).getInt(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /**********************/

        btnGuardar = (Button)findViewById(R.id.btnGuardar);
        btnCancelar = (Button)findViewById(R.id.btnCanelar);
        listaPrioridad = myDao.listarPrioridades();
        listaUsuario = myDao.listarUsuarios();
        proyecto = new Proyecto(1,"TP Integrador");

        /**Verificamos si es una edición en lugar de una tarea nueva**/
        esEdicion = getIntent().getExtras().getBoolean("esEdicion");
        if (esEdicion){
            Cursor c = myDao.listaTareas(1);
            if (c.moveToFirst()) {
                do {
                    if (c.getInt(0)==idTarea){
                        descripcion.setText(c.getString(c.getColumnIndex(ProyectoDBMetadata.TablaTareasMetadata.TAREA)));
                        horasEstimadas.setText(String.valueOf(c.getInt(c.getColumnIndex(ProyectoDBMetadata.TablaTareasMetadata.HORAS_PLANIFICADAS))));
                        prioridad.setProgress(c.getInt(c.getColumnIndex(ProyectoDBMetadata.TablaTareasMetadata.PRIORIDAD)));
                        responsable.setSelection(c.getInt(c.getColumnIndex(ProyectoDBMetadata.TablaTareasMetadata.RESPONSABLE))-1);
                        if (c.getInt(c.getColumnIndex(ProyectoDBMetadata.TablaTareasMetadata.FINALIZADA))==0){
                            tareaFinalizada = Boolean.FALSE;
                        }
                        else {
                            tareaFinalizada = Boolean.TRUE;
                        }
                        minutosTrabajados = c.getInt(c.getColumnIndex(ProyectoDBMetadata.TablaTareasMetadata.MINUTOS_TRABAJADOS));
                        break;
                    }
                }while (c.moveToNext());
            }
        }

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prioridad.getProgress()>0) {
                    Usuario usuario = new Usuario();
                    for (Usuario user : listaUsuario){
                        if (user.getId()==userID){
                            usuario = user;
                            break;
                        }
                    }
                    if (esEdicion){
                        Tarea tarea = new Tarea(
                                idTarea,
                                Integer.parseInt(horasEstimadas.getText().toString()),
                                minutosTrabajados,
                                tareaFinalizada,
                                proyecto,
                                listaPrioridad.get(prioridad.getProgress()-1),
                                usuario
                        );
                        tarea.setDescripcion(descripcion.getText().toString());

                        myDao.actualizarTarea(tarea);
                    }
                    else {
                        Tarea tarea = new Tarea(
                                idTarea,
                                Integer.parseInt(horasEstimadas.getText().toString()),
                                0,
                                false,
                                proyecto,
                                listaPrioridad.get(prioridad.getProgress()-1),
                                usuario
                        );
                        tarea.setDescripcion(descripcion.getText().toString());

                        myDao.nuevaTarea(tarea);
                    }
                    finish();
                } else {
                    Toast.makeText(AltaTareaActivity.this, "La prioridad debe ser mayor a cero", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
