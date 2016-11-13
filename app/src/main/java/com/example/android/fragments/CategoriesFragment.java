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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.tag;
import static android.R.attr.y;
import static android.R.id.list;
import static android.R.id.list_container;
import static android.R.id.message;
import static android.graphics.Canvas.EdgeType.AA;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.media.CamcorderProfile.get;
import static com.example.android.fragments.R.string.cat_name;

public class CategoriesFragment extends Fragment {
    OnCategorieSelectedListener mCallback;
    // The container Activity must implement this interface so the frag can deliver messages
    public interface OnCategorieSelectedListener {
        /** Called by CategoriesFragment when a list item is selected */
        public void onElementSelected(int position, Category cat_id);
    }

    //LISTA
    private DatabaseHelper db;
    private ArrayAdapter adapter;
    private List<Category> listCat;
    private ArrayList<Category> lista = new ArrayList<>();


    /* ************************************************* */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.categories_fragment, container, false);
        //Getting data of the DB
        db = new DatabaseHelper(getContext());
        listCat = db.getAllCats();

        //LIST VIEW OF CATEGORIES //simple_list_item_single_choice
        adapter = new ArrayAdapter(getActivity().getBaseContext(),android.R.layout.simple_list_item_activated_1,lista);
        final ListView listView = (ListView) view.findViewById(R.id.lista_categorias);
        listView.setAdapter(adapter);

        adapter.clear();
        for (Category tag : listCat) {
            adapter.add(tag.getCatName());
        }

        //Registering the contextual menu
        registerForContextMenu(listView);

        //setting up the simple click listener for the listview of categories
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listView.setItemChecked(i, true);

                String c = (String) adapter.getItem(i);
                listCat = db.getAllCats();

                for (Category tag : listCat) {
                    if (tag.getCatName().equals(c)){
                        mCallback.onElementSelected(i, tag);
                    }
                }
            }
        });


        //ADD BUTTON FOR CATEGORIES
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_categorias);
        fab.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //DIALOG OF ADD BUTTON
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                final EditText edittext = new EditText(getContext());
                alert.setTitle(cat_name);
                alert.setView(edittext);
                alert.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int wichButton){
                        String YouEditTextValue = edittext.getText().toString();
                        adapter.add(YouEditTextValue);
                        adapter.notifyDataSetChanged();
                        //ADD TO THE DB
                        Category cat = new Category(YouEditTextValue);
                        long tag1_id = db.createCat(cat);
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

        return view;
    }


    /* ********************* CONTEXTUAL MENU ********************* */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        new MenuInflater(getContext()).inflate(R.menu.context_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final ListView listView = (ListView) getView().findViewById(R.id.lista_categorias);
        Object obj = listView.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case R.id.edit_categorie:
                //ALERT DIALOG TO EDIT THE FILE
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                final EditText edittext = new EditText(getContext());
                alert.setTitle(R.string.edit_cat);
                edittext.setText(obj.toString());
                alert.setView(edittext);
                alert.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int wichButton){
                        String YouEditTextValue = edittext.getText().toString();
                        ArrayAdapter adapter = (ArrayAdapter) listView.getAdapter();
                        int pos = info.position;
                        String c = (String) adapter.getItem(info.position);

                        //DELETE FROM THE ADAPTER
                        adapter.remove(lista.get(pos));
                        adapter.insert(YouEditTextValue,pos);
                        adapter.notifyDataSetChanged();

                        //ACTUALIZAR LA BASE DE DATOS
                        listCat = db.getAllCats();

                        for (Category tag : listCat) {
                            if (tag.getCatName().equals(c)){
                                tag.setCatName(YouEditTextValue);
                                db.updateCat(tag);
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
            case R.id.delete_categorie:
                ArrayAdapter adapter = (ArrayAdapter) listView.getAdapter();
                String c = (String) adapter.getItem(info.position);

                //DELETE THE SELECTED FILE
                adapter.remove(lista.get(info.position));
                adapter.notifyDataSetChanged();

                //ACTUALIZAR LA BD
                listCat = db.getAllCats();
                for (Category tag : listCat) {
                    if (tag.getCatName().equals(c)){
                        db.deleteCat(tag.getId(),true);
                    }
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /* ************************************************* */
    @Override
    public void onStart() {
        super.onStart();
        // When in two-pane layout, set the listview to highlight the selected list item
        // (We do this during onStart because at the point the listview is available.)
        if (getFragmentManager().findFragmentById(R.id.element_fragment) != null) {
            ListView lista = (ListView) getView().findViewById(R.id.lista_categorias);
            lista.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }

    /* ************************************************* */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnCategorieSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnCategorieSelectedListener");
        }
    }
}