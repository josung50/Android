package opensource.mobileprogram;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by JSR on 2016-11-10.
 */
public class List extends Activity{
    int count;
    String Address = null;
    double lat;
    double lng;
    String hour = null;
    String minute = null;
    String doing = null;
    String detail = null;
    final DBHelper2 dbHelper2 = new DBHelper2(this, "list2.db", null , 1); // 일지를 위한 DB

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
        Cursor c = dbHelper2.getReadableDatabase().query("list2",null,null,null,null,null,null);

        while(c.moveToNext())
        {
            count = c.getInt(c.getColumnIndex("_id"));
            Address = c.getString(c.getColumnIndex("address"));
            lat = c.getDouble(c.getColumnIndex("lat"));
            lng = c.getDouble(c.getColumnIndex("lng"));
            Log.i("디비얻어오기", "성공" + lat + " " + lng);
           hour = c.getString(c.getColumnIndex("hour"));
            minute = c.getString(c.getColumnIndex("minute"));
            doing = c.getString(c.getColumnIndex("doing"));
            detail = c.getString(c.getColumnIndex("detail"));
            Log.i("정보" , "내용 :" + count + " " + Address);
        }

    }
}
