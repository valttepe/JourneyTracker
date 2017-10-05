package com.example.valtteri.journeytracker.content.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.valtteri.journeytracker.route.tracking.OrienteeringFragment;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Valtteri on 2.10.2017.
 */

public class SqlContentProvider extends ContentProvider {
    private SQLiteDatabase db;
    public static final String cont = "content://";
    public static final String PROVIDER_NAME = "com.example.valtteri.journeytracker.content.provider.contentprovider";
    public static final Uri base_URI = Uri.parse( cont + PROVIDER_NAME);


    public static final String getAll = "/getAll";
    public static final String insertRoute = "/insertRoute";
    public static final String getOwnCoords = "/getOwnCoords";
    public static final String getMarkerLoc = "/getMarkerLocations";

    public static final Uri get_ALL = Uri.parse(cont + PROVIDER_NAME + getAll);
    public static final Uri inserROUTE = Uri.parse(cont + PROVIDER_NAME + insertRoute);
    public static final Uri getOwn_Coords = Uri.parse(cont + PROVIDER_NAME + getOwnCoords);
    public static final Uri getMarker_Coordinates = Uri.parse(cont + PROVIDER_NAME + getMarkerLoc);

    public static final int getList = 1;
    public static final int insertingRoute = 2;
    public static final int getowncoords = 3;
    public static final int getmarkercoords = 4;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, getAll, getList);
        uriMatcher.addURI(PROVIDER_NAME, insertRoute, insertingRoute);
        uriMatcher.addURI(PROVIDER_NAME, getOwnCoords, getowncoords);
        uriMatcher.addURI(PROVIDER_NAME, getMarkerLoc, getmarkercoords);

    }
    // Coordinates variables for inserting
    private ArrayList<LatLng> markers;
    private ArrayList<LatLng> locations;
    int lastId;

    myDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new myDbHelper(getContext());
        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */

        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        switch (uriMatcher.match(uri)){
            case getList:
                db = dbHelper.getWritableDatabase();
                //Cursor c = db.rawQuery("SELECT * FROM Coordinates", null);
                Cursor c = db.rawQuery("SELECT * FROM Route ORDER BY _id DESC", null);
                //Cursor c = db.query(myDbHelper.Table_route, projection, selection, null, null, null, null);
                return c;
            case getowncoords:
               Cursor cd = db.rawQuery("SELECT * FROM Coordinates WHERE routeId = ?", selectionArgs );
                return cd;
            case getmarkercoords:
                Cursor cur = db.rawQuery("SELECT * FROM Markers WHERE routeId = ?", selectionArgs);
                return cur;
            default:
                break;
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        if(uriMatcher.match(uri) == insertingRoute){
            // TODO: insert to database
            if(contentValues != null){
                Log.d("TÄMÄ näin",contentValues.get("timer").toString() );
                //contentValues.
                //contentValues.get("distance");
                Date today = new Date();
                SimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy");
                String todate = sd.format(today);
                contentValues.put(myDbHelper.DATE, todate);
                markers = OrienteeringFragment.markerPositions;
                locations = OrienteeringFragment.locations;
                dbHelper.getWritableDatabase();

                db.insert(myDbHelper.Table_route, null, contentValues);

                Cursor cu = db.rawQuery("SELECT * FROM Route", null);
                cu.moveToLast();
                lastId = cu.getInt(cu.getColumnIndex("_id"));
                Log.i("mmmmmm", "" + lastId);

                db.beginTransaction();
                try {
                    for (int i= 0; i< locations.size(); i++){
                        ContentValues coordContent = new ContentValues();
                        coordContent.put(myDbHelper.RouteId, lastId);
                        coordContent.put(myDbHelper.Longitude, locations.get(i).longitude);
                        coordContent.put(myDbHelper.Latitude, locations.get(i).latitude);
                        db.insert(myDbHelper.Table_coordinates, null, coordContent);

                    }
                    for(int i = 0; i < markers.size(); i++){
                        ContentValues markerContent = new ContentValues();
                        markerContent.put(myDbHelper.RouteId, lastId);
                        markerContent.put(myDbHelper.Longitude, markers.get(i).longitude);
                        markerContent.put(myDbHelper.Latitude, markers.get(i).latitude);
                        db.insert(myDbHelper.Table_markers, null, markerContent);
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                db.close();
            }

        }
        else{

        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }



    static class myDbHelper extends SQLiteOpenHelper {

        // Database creation values
        private static final String DATABASE_NAME = "DataBase";    // Database Name
        private static final int DATABASE_Version = 1;    // Database Version

        //  Basic primary key to every table
        static final String UID="_id";     // Column I (Primary Key)

        // Steps table creation values
        private static final String MYTABLE_NAME = "Steps"; // Table name
        public static final String DATE = "date"; // Current date (YYYY:MM:DD)
        private static final String STEPCOUNT = "stepcount"; // Steps for one day
        //private static final String DROP_TABLE_STEPS ="DROP TABLE IF EXISTS "+MYTABLE_NAME; // Delete Table
        private static final String CREATE_TABLE = "CREATE TABLE " + MYTABLE_NAME + " ("
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE + " TEXT ," + STEPCOUNT + " INTEGER);";
        private Context context;

        // Route table creation values
        public static final String Table_route = "Route";
        public static final String DISTANCE = "distance";
        public static final String TIMER = "timer";
        private static final String Create_Route_Table = "CREATE TABLE " + Table_route + " ("
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE + " TEXT ," + DISTANCE + " REAL ,"
                + TIMER + " TEXT);";

        public static final String Table_coordinates = "Coordinates";
        public static final String Longitude = "longitude";
        public static final String Latitude = "latitude";
        public static final String RouteId = "routeId";
        private static final String Create_Coordinates_Table = "CREATE TABLE " + Table_coordinates + " ("
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Longitude + " REAL ," + Latitude + " REAL ,"
                + RouteId + " INTEGER);";

        public static final String Table_markers = "Markers";
        private static final String Create_Markers_Table = "CREATE TABLE " + Table_markers + " ("
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Longitude + " REAL ," + Latitude + " REAL ,"
                + RouteId + " INTEGER);";



        public myDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_Version);
            this.context=context;
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            Log.i("Testing", "Does it call this once");
            try {
                sqLiteDatabase.execSQL(CREATE_TABLE);
                sqLiteDatabase.execSQL(Create_Coordinates_Table);
                sqLiteDatabase.execSQL(Create_Route_Table);
                sqLiteDatabase.execSQL(Create_Markers_Table);
            } catch (Exception e) {
                Log.e("HMMMMM", "Something is wrong" + e);

            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            try {
                onCreate(sqLiteDatabase);
            }catch (Exception e) {
                Log.e("HYYYMMM", "Something is " + e);
            }
        }

    }
}
