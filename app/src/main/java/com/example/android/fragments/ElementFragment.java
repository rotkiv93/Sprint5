/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.opengl.GLException;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.OrientationListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.R.attr.category;
import static android.R.attr.contextClickable;
import static android.R.attr.direction;
import static android.R.attr.preferenceCategoryStyle;
import static android.R.attr.tag;
import static android.R.attr.x;
import static android.R.id.list;
import static android.content.Context.SENSOR_SERVICE;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static android.os.Build.VERSION_CODES.M;
import static android.view.View.ROTATION;
import static android.view.View.Z;

public class ElementFragment extends Fragment {
    final static String ARG_POSITION = "position";
    final static String ARG_CATEGORY = "category";

    int mCurrentPosition = -1;
    int mCurrentCat;

    public ElementFragment() {
    }

    //Lista
    private DatabaseHelper db;
    private ArrayAdapter adapter;
    private List<Element> listElem;
    private ArrayList<Category> lista = new ArrayList<>();

    //SENSORS
    private SensorManager mSensorManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    //ORIENTATION CHANGES
    OrientationEventListener mOrientationListener;
    private long  tHistorico = System.currentTimeMillis();
    private long tActual = 0;

    private enum Pos{
        UP, DOWN, RIGHT, LEFT
    }
    private Pos posAnterior = Pos.UP;
    private Pos posActual = Pos.UP;
    private int countVueltas = 0;
    private long tVuelta = 0;

    //FOR RANDOMIZE FUNCTION
    private Random randomGenerator = new Random();


    /*****************************************SHAKE SENSOR********************************************/
    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            //SHAKE
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            //SHAKE MOVEMENT
            if (mAccel > 12) {
                //DO ACTION
                randomize(adapter);
                mAccel = 0;
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //NOTHING TO D
        }
    };


    /*************************************************************************************/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);
            mCurrentCat = savedInstanceState.getInt(ARG_CATEGORY);

        }
        return inflater.inflate(R.layout.elements_fragment, container, false);
    }

    /*************************************************************************************/
    @Override
    public void onStart() {
        super.onStart();

        //SHAKE SENSOR
        mSensorManager = (SensorManager) this.getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        //UP&DONW / LEFTTORIGHT ORIENTATION CHANGES
        mOrientationListener = new OrientationEventListener(getContext(), SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int i) {

                //GETTING THE POSITION
                if ((i > 0) && (i < 10)) {
                    tActual = System.currentTimeMillis();
                    posActual = Pos.UP;
                } else if ((i > 80) && (i < 100)) {
                    tActual = System.currentTimeMillis();
                    posActual = Pos.RIGHT;
                } else if ((i > 170) && (i < 190)) {
                    tActual = System.currentTimeMillis();
                    posActual = Pos.DOWN;
                } else if ((i > 260) && (i < 280)) {
                    tActual = System.currentTimeMillis();
                    posActual = Pos.LEFT;
                }

                //LEFT AND RIGHT
                if (((posAnterior == Pos.LEFT) && (posActual == Pos.RIGHT)) || ((posAnterior == Pos.RIGHT) && (posActual == Pos.LEFT))) {
                    tVuelta = tVuelta + (tHistorico-tActual);
                    if( tVuelta > 1000) { //MORE THAN A SECOND
                        countVueltas = 0;
                        tVuelta = 0;
                    } else if (countVueltas <= 1){ //LESS THAN 2 ROTATIONS
                        countVueltas++;;
                    } else { //LESS THAN SECOND AND MORE THAN 2 ROTATIONS
                        randomize(adapter);//DO THE ACTION
                        Toast t = Toast.makeText(getContext(),"GIRO DE IZQ A DR",Toast.LENGTH_SHORT);
                        t.show();
                        tVuelta = 0;
                        countVueltas =0;
                    }
                    tHistorico = tActual;
                    posAnterior = posActual;

                    //UP AND DOWN
                } else if (((posAnterior == Pos.UP) && (posActual == Pos.DOWN)) || ((posAnterior == Pos.DOWN) && (posActual == Pos.UP))) {
                    if (((tHistorico - tActual) >= 1000) && ((tHistorico-tActual) <=2000)) {
                        randomize(adapter);//DO ACTION
                        Toast t = Toast.makeText(getContext(),"GIRO DE ARRIBA A ABAJO",Toast.LENGTH_SHORT);
                        t.show();
                    }
                    posAnterior = posActual;
                    tHistorico = tActual;
                }
            }
        };

        //ENABLING OR NOT THE ORIENTATION LISTENER
        if (mOrientationListener.canDetectOrientation() ){
            mOrientationListener.enable();
        }else
            mOrientationListener.disable();

        //VIEW
        Bundle args = getArguments();
        if (args != null) {
            updateElementView(args.getInt(ARG_POSITION), args.getInt(ARG_CATEGORY));
        } else if (mCurrentPosition != -1) {
            updateElementView(mCurrentPosition , args.getInt(ARG_CATEGORY));
        }
    }


    /*************************************************************************************/
    public void updateElementView(int position, final int category) {

        db = new DatabaseHelper(getContext());
        adapter = new ArrayAdapter(getActivity().getBaseContext(), android.R.layout.simple_list_item_activated_1, lista);
        final ListView listView = (ListView) getActivity().findViewById(R.id.lista_elementos);
        listView.setAdapter(adapter);

        //ADDING THE ADAPTER WITH THE DB
        adapter.clear();
        listElem = db.getAllElementsByCat(category);

        for (Element elem : listElem) {
            adapter.add(elem.getElement_name());
        }

        adapter.notifyDataSetChanged();

        //CONTEXT MENU FOR THE LISTVIEW
        registerForContextMenu(listView);

        //FLOATING ACTION BUTTON TO ADD ELEMENTS
        FloatingActionButton boton = (FloatingActionButton) getView().findViewById(R.id.fab_elementos);
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ALERT DIALOG FOR ADDING NEW ELEMENT
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                final EditText edittext = new EditText(getContext());
                alert.setTitle(R.string.elem_name);
                alert.setView(edittext);
                alert.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int wichButton) {
                        String YouEditTextValue = edittext.getText().toString();
                        adapter.add(YouEditTextValue);
                        adapter.notifyDataSetChanged();
                        //ACTUALIZAR LA BD CON LA LIST
                        Element elem = new Element(YouEditTextValue);
                        db.createElement(elem, category);

                    }
                });
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                alert.show();
            }
        });
        mCurrentPosition = position;
    }


    /* ********************* CONTEXTUAL MENU ********************* */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        new MenuInflater(getContext()).inflate(R.menu.context_menu_elem, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final ListView listView = (ListView) getView().findViewById(R.id.lista_elementos);
        Object obj = listView.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case R.id.edit_element:
                //ALERT DIALOG TO EDIT THE FILE
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                final EditText edittext = new EditText(getContext());
                alert.setTitle(R.string.edit_elem);
                edittext.setText(obj.toString());
                alert.setView(edittext);
                alert.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int wichButton){
                        String YouEditTextValue = edittext.getText().toString();
                        ArrayAdapter adapter = (ArrayAdapter) listView.getAdapter();
                        int pos = info.position;
                        String c = (String) adapter.getItem(info.position);

                        //ELIMINAR DEL ADAPTER Y VOLVER A INSERTAR
                        adapter.remove(lista.get(info.position));
                        adapter.insert(YouEditTextValue,pos);
                        adapter.notifyDataSetChanged();

                        //ACTUALIZAR LA BASE DE DATOS
                        for (Element elem : listElem) {
                            if (elem.getElement_name().equals(c)){
                                elem.setElement_name(YouEditTextValue);
                                db.updateElement(elem);
                            }
                        }
                    }
                });
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alert.show();
                return true;
            case R.id.delete_element:
                //DELETE THE SELECTED FILE
                ArrayAdapter adapter = (ArrayAdapter) listView.getAdapter();
                String c = (String) adapter.getItem(info.position);
                adapter.remove(lista.get(info.position));
                adapter.notifyDataSetChanged();

                //ACTUALIZAR LA BD
                for (Element elem : listElem) {
                    if (elem.getElement_name().equals(c)){
                        db.deleteElement(elem.getId());
                    }
                }
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }


    /*************************************************************************************/
    //RANDOM ELEMENT ON LISTVIEW
    private void randomize (ArrayAdapter adapter){
        int index = randomGenerator.nextInt(adapter.getCount());
        String t = (String) adapter.getItem(index);
        adapter.clear();
        adapter.add(t);
        adapter.notifyDataSetChanged();
    }

    /*************************************************************************************/
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(ARG_POSITION, mCurrentPosition);
    }

    @Override
    public void onResume() {
        super.onResume();
        //SENSOR
        mOrientationListener.enable();
        mSensorManager.registerListener(mSensorListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        //SENSOR
        mOrientationListener.disable();
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

}



