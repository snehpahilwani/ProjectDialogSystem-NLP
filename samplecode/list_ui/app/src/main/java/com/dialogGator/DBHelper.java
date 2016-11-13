package com.dialogGator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper
{
    private static DBHelper sInstance;

    private static final Set<String> _tableNames = new HashSet<String>(Arrays.asList("brand", "category", "color", "size"));
    private static String TAG = "DataBaseHelper"; // Tag just for the LogCat window
    //destination path (location) of our database on device
    private static String DB_PATH = "";
    private static String DB_NAME ="Dialog.db";// Database name
    private SQLiteDatabase mDataBase;
    private final Context mContext;

    public static synchronized DBHelper getInstance(Context context)
    {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DBHelper(Context context)
    {
        super(context, DB_NAME, null, 1);// 1? Its database Version
        if(android.os.Build.VERSION.SDK_INT >= 17){
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        }
        else
        {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        this.mContext = context;
        try {
            createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Product> Query(Map<String, String> searchBox) {
        boolean db = openDataBase();
        ArrayList<Product> products = new ArrayList<Product>();
        String queryString = GetQueryString(searchBox);
        Cursor data = readData(queryString);
        try
        {
            while (data.moveToNext()) {
                Product product = new Product();
                product.id = Integer.toString(data.getInt(0));
                product.title = (data.getString(1));
                product.category = (data.getString(2));
                product.brand = (data.getString(3));
                product.price = (data.getDouble(4));
                product.size = (data.getString(5));
                product.color = (data.getString(6));
                product.imgUrl = (data.getString(7));

                Set<String> attributeSet = searchBox.keySet();

            /*
            Native sqlite in android does not support any function for fuzzy string matching
            TODO : Move to a remote MySQL db
            */
                for (String attribute : attributeSet) {
                    Field field = null;
                    try {
                        field = Product.class.getDeclaredField(attribute);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                    field.setAccessible(true);
                    String lhs = null;
                    try {
                        lhs = (String) field.get(product);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    String rhs = searchBox.get(attribute);
                    if (EditDistance.findEditDistance(lhs.toLowerCase(), rhs.toLowerCase()) >= 70) {
                        products.add(product);
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
        }
        close();
        return products;
    }


    private Cursor readData(String sql)
    {
        try
        {
            return mDataBase.rawQuery(sql, null);
            //Cursor mCur = mDataBase.rawQuery(sql, null);
            //return mCur;
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "getTestData >>"+ mSQLException.toString());
            throw mSQLException;
        }
    }

    /*
    TODO : Make a parametrized query string to prevent sql injection
    TODO : Another method to match any keyword with any attribute, for non-slot based search
    */
    public String GetQueryString(Map<String, String> searchBox)
    {
        Set<String> attributeSet = searchBox.keySet();
        String query = "select P.Id," +
                "P.Title," +
                "category.Name," +
                "brand.Name," +
                "Price," +
                "size.Name," +
                "color.Name," +
                "ImgUrl " +
                "from product P " +
                "inner join category on category.Id = categoryId " +
                "inner join brand on brand.Id = brandId " +
                "inner join color on color.Id = colorId " +
                "inner join size on size.Id = sizeId ";

        String whereClause = "";

        for(String attribute : attributeSet)
        {
            if(_tableNames.contains(attribute))
            {
                whereClause += NewWhereClause(whereClause);
                Log.i("ProductMap", searchBox.get(attribute).toString());
                String attributeValue = searchBox.get(attribute);
                int valueLength = attributeValue.length();
                whereClause += "LOWER(" + attribute + ".Name) LIKE '%" + attributeValue.substring(0, Math.min(3, valueLength)).toLowerCase() + "%'";
            }
            else if(attribute == "priceStart")
            {
                whereClause += NewWhereClause(whereClause);
                whereClause += "price >= " + searchBox.get(attribute);
            }
            else if(attribute == "priceEnd")
            {
                whereClause += NewWhereClause(whereClause);
                whereClause += "price <= " + searchBox.get(attribute);
            }
        }
        return query + whereClause;
    }

    private String NewWhereClause(String whereClause)
    {
        return whereClause.equals("") ? " where " : " and ";
    }

    public void createDataBase() throws IOException
    {
        //If the database does not exist, copy it from the assets.

        boolean mDataBaseExist = checkDataBase();
        if(!mDataBaseExist)
        {
            this.getReadableDatabase();
            this.close();
            try
            {
                //Copy the database from assests
                copyDataBase();
                Log.e(TAG, "createDatabase database created");
            }
            catch (IOException mIOException)
            {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    //Check that the database exists here: /data/data/your package/databases/Da Name
    private boolean checkDataBase()
    {
        File dbFile = new File(DB_PATH + DB_NAME);
        //Log.v("dbFile", dbFile + "   "+ dbFile.exists());
        return dbFile.exists();
    }

    //Copy the database from assets
    private void copyDataBase() throws IOException
    {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer))>0)
        {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    //Open the database, so we can query it
    public boolean openDataBase() throws SQLException
    {
        String mPath = DB_PATH + DB_NAME;
        //Log.v("mPath", mPath);
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READONLY);
        //mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        return mDataBase != null;
    }

    @Override
    public synchronized void close()
    {
        if(mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {}
}

