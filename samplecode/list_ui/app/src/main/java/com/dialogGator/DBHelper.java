package com.dialogGator;

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
    private static String DB_NAME ="Dialog";// Database name
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
    }

    public ArrayList<Product> Query(Map<String, String> searchBox) {
        ArrayList<Product> products = new ArrayList<Product>();
        String queryString = GetQueryString(searchBox);
        Cursor data = readData(queryString);
        while (data.moveToNext())
        {
            Product product = new Product();
            product.id = (data.getInt(0));
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
            for(String attribute : attributeSet)
            {
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
                if(EditDistance.findEditDistance(lhs.toLowerCase(), rhs.toLowerCase()) >= 70)
                {
                    products.add(product);
                }
            }
        }
        return products;
    }

    private Cursor readData(String sql)
    {
        try
        {
            return this.getReadableDatabase().rawQuery(sql, null);
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

    //Open the database, so we can query it
    public boolean openDataBase() throws SQLException
    {
        String mPath = DB_PATH + DB_NAME;
        //Log.v("mPath", mPath);
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
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

