package com.example.android.fragments;


import com.example.android.fragments.Category;
import com.example.android.fragments.Element;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.R.attr.category;
import static android.R.attr.tag;
import static android.provider.Contacts.SettingsColumns.KEY;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "Categories and Elements Manager";

    // Table Names
    private static final String TABLE_ELEMENT = "elements";
    private static final String TABLE_CATEGORY = "categories";

    // NOTES Table - column nmaes
    private static final String KEY_ELEMENT = "element";
    private static final String KEY_ELEMENT_NAME = "element_name";
    private static final String KEY_FOREIGN_ELEMENT= "foreign_key_element";

    // TAGS Table - column names
    private static final String KEY_CATEGORY = "caegory";
    private static final String KEY_CATEGORY_NAME = "category_name";

    // NOTE_TAGS Table - column names
    private static final String KEY_ELEMENT_ID = "element_id";
    private static final String KEY_CATEGORY_ID = "category_id";


    // Table Create Statements
    // Element table create statement
    private static final String CREATE_TABLE_ELEMENT = "CREATE TABLE "
            + TABLE_ELEMENT + "(" + KEY_ELEMENT_ID + " INTEGER PRIMARY KEY," + KEY_ELEMENT
            + " TEXT," + KEY_ELEMENT_NAME + " TEXT," + KEY_FOREIGN_ELEMENT
            + " INTEGER FOREING KEY" + ")";

    // Cat table create statement
    private static final String CREATE_TABLE_CATEGORY = "CREATE TABLE " + TABLE_CATEGORY
            + "(" + KEY_CATEGORY_ID + " INTEGER PRIMARY KEY," + KEY_CATEGORY + " TEXT,"+ KEY_CATEGORY_NAME +")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_CATEGORY);
        db.execSQL(CREATE_TABLE_ELEMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ELEMENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);

        // create new tables
        onCreate(db);
    }

    // ------------------------ "todos" table methods ----------------//

    /*
     * Creating an element
     */
    public long createElement(Element element, long id_cat) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ELEMENT_NAME, element.getElement_name());
        values.put(KEY_FOREIGN_ELEMENT, id_cat);

        // insert row
        long element_id = db.insert(TABLE_ELEMENT, null, values);

        return element_id;
    }

    /*
     * get single element
     */
    public Element getElement(long element_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_ELEMENT + " WHERE "
                + KEY_ELEMENT_ID + " = " + element_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Element td = new Element();
        td.setId(c.getInt(c.getColumnIndex(KEY_ELEMENT_ID)));
        td.setElement_name((c.getString(c.getColumnIndex(KEY_ELEMENT_NAME))));
        td.setForeign_id(c.getInt(c.getColumnIndex(KEY_FOREIGN_ELEMENT)));

        return td;
    }

    /**
     * getting all elements
     * */
    public List<Element> getAllElements() {
        List<Element> elements = new ArrayList<Element>();
        String selectQuery = "SELECT  * FROM " + TABLE_ELEMENT;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Element td = new Element();
                td.setId(c.getInt((c.getColumnIndex(KEY_ELEMENT_ID))));
                td.setElement_name(c.getString(c.getColumnIndex(KEY_ELEMENT_NAME)));
                td.setForeign_id(c.getInt(c.getColumnIndex(KEY_FOREIGN_ELEMENT)));

                // adding to element list
                elements.add(td);
            } while (c.moveToNext());
        }

        return elements;
    }

    /**
     * getting all elements under single category
     * */
    public List<Element> getAllElementsByCat(long cat_id) {
        List<Element> elements = new ArrayList<Element>();

        String selectQuery = "SELECT  * FROM " + TABLE_ELEMENT + " WHERE "
                + KEY_FOREIGN_ELEMENT + " = " + cat_id;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Element td = new Element();
                td.setId(c.getInt((c.getColumnIndex(KEY_ELEMENT_ID))));
                td.setElement_name(c.getString(c.getColumnIndex(KEY_ELEMENT_NAME)));
                td.setForeign_id(c.getInt(c.getColumnIndex(KEY_FOREIGN_ELEMENT)));

                // adding to element list
                elements.add(td);
            } while (c.moveToNext());
        }

        return elements;
    }

    /*
     * getting element count
     */
    public int getElementCount() {
        String countQuery = "SELECT  * FROM " + TABLE_ELEMENT;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    /*
     * Updating an element
     */
    public int updateElement(Element element) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ELEMENT_ID, element.getId());
        values.put(KEY_ELEMENT_NAME, element.getElement_name());
        values.put(KEY_FOREIGN_ELEMENT, element.getForeign_id());

        // updating row
        return db.update(TABLE_ELEMENT, values, KEY_ELEMENT_ID + " = ?",
                new String[] { String.valueOf(element.getId()) });
    }

    /*
     * Deleting a element
     */
    public void deleteElement(long element_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ELEMENT, KEY_ELEMENT_ID + " = ?",
                new String[] { String.valueOf(element_id) });
    }

    // ------------------------ "tags" table methods ----------------//

    /*
     * Creating cat
     */
    public long createCat(Category cat) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY_NAME, cat.getCatName());

        // insert row
        long tag_id = db.insert(TABLE_CATEGORY, null, values);

        return tag_id;
    }

    /**
     * getting all cats
     * */
    public List<Category> getAllCats() {
        List<Category> cats = new ArrayList<Category>();
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORY;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Category t = new Category();
                t.setId(c.getInt(c.getColumnIndex(KEY_CATEGORY_ID)));
                t.setCatName(c.getString(c.getColumnIndex(KEY_CATEGORY_NAME)));

                // adding to tags list
                cats.add(t);
            } while (c.moveToNext());
        }
        return cats;
    }

    /*
     * Updating a cag
     */
    public int updateCat(Category cat) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY_NAME, cat.getCatName());

        // updating row
        return db.update(TABLE_CATEGORY, values, KEY_CATEGORY_ID + " = ?",
                new String[] { String.valueOf(cat.getId()) });
    }

    /*
     * Deleting a cat
     */
    public void deleteCat(long id_cat , boolean should_delete_all_cat_elements) {
        SQLiteDatabase db = this.getWritableDatabase();

        // before deleting tag
        // check if todos under this tag should also be deleted
        if (should_delete_all_cat_elements) {
            // get all todos under this tag
            List<Element> allCatElements = getAllElementsByCat(id_cat);

            // delete all elements
            for (Element element : allCatElements) {
                // delete element
                deleteElement(element.getId());
            }
        }

        // now delete the cat
        db.delete(TABLE_CATEGORY, KEY_CATEGORY_ID + " = ?",
                new String[] { String.valueOf(id_cat) });
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    /**
     * get datetime
     * */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
