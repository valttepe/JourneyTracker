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

public class SqlContentProvider extends ContentProvider {

    // Access writing and reading functionality to database
    private SQLiteDatabase db;

    //Helper strings for the making Uri's with lesser amount of typos
    private static final String cont = "content://";
    private static final String PROVIDER_NAME = "com.example.valtteri.journeytracker.content.provider.contentprovider";
    private static final String getAll = "/getAll";
    private static final String insertRoute = "/insertRoute";
    private static final String getOwnCoords = "/getOwnCoords";
    private static final String getMarkerLoc = "/getMarkerLocations";

    // Uri for the getting list of routes from the route table
    public static final Uri get_ALL = Uri.parse(cont + PROVIDER_NAME + getAll);
    // Uri for the inserting Route to route table and also coordinates to the tables markers and coordinates
    public static final Uri insertROUTE = Uri.parse(cont + PROVIDER_NAME + insertRoute);
    // Uri for the getting your Route coordinates from specific route
    public static final Uri getOwn_Coords = Uri.parse(cont + PROVIDER_NAME + getOwnCoords);
    // Uri for the getting Markers coordinates from specific route
    public static final Uri getMarker_Coordinates = Uri.parse(cont + PROVIDER_NAME + getMarkerLoc);

    // Helpers for the urimatcher and switch case functionality
    private static final int getList = 1;
    private static final int insertingRoute = 2;
    private static final int getowncoords = 3;
    private static final int getmarkercoords = 4;

    // Making uri matcher that allows specific uris to use database
    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, getAll, getList);
        uriMatcher.addURI(PROVIDER_NAME, insertRoute, insertingRoute);
        uriMatcher.addURI(PROVIDER_NAME, getOwnCoords, getowncoords);
        uriMatcher.addURI(PROVIDER_NAME, getMarkerLoc, getmarkercoords);

    }
    // Coordinates variables for inserting
    protected ArrayList<LatLng> markers;
    protected ArrayList<LatLng> locations;

    // variable to save latest _id
    int lastId;
    // Instance from the sqliteopenhelper
    myDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new myDbHelper(getContext());

         //Create a write able database which will trigger its
         //creation if it doesn't already exist

        db = dbHelper.getWritableDatabase();
        return db != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // opens database connection
        db = dbHelper.getWritableDatabase();
        // switch that checks incoming uri and compares it to defined uris
        switch (uriMatcher.match(uri)){
            case getList:
                // gets list of the Routes and changes its order to start from the newest insertion
                return db.rawQuery("SELECT * FROM Route ORDER BY _id DESC", null);

            case getowncoords:
                // gets list of coordinates that are for the specific route
                return db.rawQuery("SELECT * FROM Coordinates WHERE routeId = ?", selectionArgs );

            case getmarkercoords:
                // gets list of marker coordinates that are for the specific route
                return db.rawQuery("SELECT * FROM Markers WHERE routeId = ?", selectionArgs);

            default:
                break;
        }
        return null;
    }
    // required function for the content provider
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        if(uriMatcher.match(uri) == insertingRoute){
            if(contentValues != null){
                // Creating date with dd.MM.yyyy format
                Date today = new Date();
                SimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy");
                String todate = sd.format(today);

                // Adding it to contentvalues that already contains timer and distance
                contentValues.put(myDbHelper.DATE, todate);

                // Taking coordinates from the orienteering fragment because
                // it was too hard to get them with contentValues
                markers = OrienteeringFragment.markerPositions;
                locations = OrienteeringFragment.locations;

                // opens connection to database
                db = dbHelper.getWritableDatabase();
                // Inserts date, timer and distance to new row
                db.insert(myDbHelper.Table_route, null, contentValues);

                // Getting all routes from database because we need _id to routeId column
                // that database generates when inserting data to table
                Cursor cu = db.rawQuery("SELECT * FROM Route", null);

                // moving to latest insertion and getting the _id from there
                cu.moveToLast();
                lastId = cu.getInt(cu.getColumnIndex("_id"));
                // starting transaction that allows making insertions before actually inserting
                // them to the database
                db.beginTransaction();
                try {
                    // going trough locations LatLng list insertions
                    for (int i= 0; i< locations.size(); i++){
                        // Creating new contentvalues variable for the insertion
                        ContentValues coordContent = new ContentValues();
                        coordContent.put(myDbHelper.RouteId, lastId);
                        coordContent.put(myDbHelper.Longitude, locations.get(i).longitude);
                        coordContent.put(myDbHelper.Latitude, locations.get(i).latitude);
                        db.insert(myDbHelper.Table_coordinates, null, coordContent);

                    }
                    // going trough markers LatLng list insertions
                    for(int i = 0; i < markers.size(); i++){
                        // Creating new contentvalues variable for the insertion
                        ContentValues markerContent = new ContentValues();
                        markerContent.put(myDbHelper.RouteId, lastId);
                        markerContent.put(myDbHelper.Longitude, markers.get(i).longitude);
                        markerContent.put(myDbHelper.Latitude, markers.get(i).latitude);
                        db.insert(myDbHelper.Table_markers, null, markerContent);
                    }
                    // When everything is ready to move to database
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                // closing database connection and cursor
                db.close();
                cu.close();
            }

        }
        return null;
    }
    // required function for the content provider
    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
    // required function for the content provider
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }



    private static class myDbHelper extends SQLiteOpenHelper {

        // Database creation values
        static final String DATABASE_NAME = "DataBase";    // Database Name
        static final int DATABASE_Version = 1;    // Database Version

        //  Basic primary key to every table
        static final String UID="_id";     // Column I (Primary Key)

        // Steps table creation values
        static final String MYTABLE_NAME = "Steps"; // Table name
        static final String DATE = "date"; // Current date (YYYY:MM:DD)
        static final String STEPCOUNT = "stepcount"; // Steps for one day
        static final String CREATE_TABLE = "CREATE TABLE " + MYTABLE_NAME + " ("
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE + " TEXT ," + STEPCOUNT + " INTEGER);";

        // Route table creation values
        static final String Table_route = "Route"; // Table name
        static final String DISTANCE = "distance"; // distance in meters
        static final String TIMER = "timer"; // used time
        static final String Create_Route_Table = "CREATE TABLE " + Table_route + " ("
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE + " TEXT ," + DISTANCE + " REAL ,"
                + TIMER + " TEXT);";

        static final String Table_coordinates = "Coordinates"; // table name
        static final String Longitude = "longitude"; // longitude in double
        static final String Latitude = "latitude"; // latitude in double
        static final String RouteId = "routeId"; // routeId (Foreign key)
        static final String Create_Coordinates_Table = "CREATE TABLE " + Table_coordinates + " ("
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Longitude + " REAL ," + Latitude + " REAL ,"
                + RouteId + " INTEGER);";


        static final String Table_markers = "Markers"; // table name
        // Other variables are from the previous table creations
        static final String Create_Markers_Table = "CREATE TABLE " + Table_markers + " ("
                + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Longitude + " REAL ," + Latitude + " REAL ,"
                + RouteId + " INTEGER);";



        myDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_Version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            try {
                // Creating the tables for the database
                sqLiteDatabase.execSQL(CREATE_TABLE);
                sqLiteDatabase.execSQL(Create_Coordinates_Table);
                sqLiteDatabase.execSQL(Create_Route_Table);
                sqLiteDatabase.execSQL(Create_Markers_Table);
            } catch (Exception e) {
                Log.e("HMMMMM", "Something is wrong " + e);

            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            try {
                // if there was updates for the version but we don't use this
                onCreate(sqLiteDatabase);
            }catch (Exception e) {
                Log.e("HYYYMMM", "Something is " + e);
            }
        }

    }
}
