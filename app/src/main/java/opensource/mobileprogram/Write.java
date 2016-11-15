package opensource.mobileprogram;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by JSR on 2016-11-10.
 */
public class Write extends Activity{
    String Address = null;
    String hour = null;
    String minute = null;
    String doing = null;
    String detail = null;
    String date = getDateString();
    final DBHelper2 dbHelper2 = new DBHelper2(this, "list2.db", null , 1); // 일지를 위한 DB
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_layout);
        Address = getAddress(this, Mymap.LATLNG.latitude, Mymap.LATLNG.longitude); // 주소 변환

        //스피너//
        Spinner s1 = (Spinner)findViewById(R.id.spinner1); // 한 일 분류
        s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.i("id", "id: " + parent.getItemAtPosition(position));
                doing = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Spinner s2 = (Spinner)findViewById(R.id.spinner2); // 시간
        s2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.i("id", "id: " + parent.getItemAtPosition(position));
                hour = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Spinner s3 = (Spinner)findViewById(R.id.spinner3); // 분
        s3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.i("id", "id: " + parent.getItemAtPosition(position));
                minute = parent.getItemAtPosition(position).toString();
                Log.i("minute", "minute:" + minute);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    //저장 버튼 입력 시
    public void writeButton(View v) {
        EditText text = (EditText) findViewById(R.id.edittext);
        detail = text.getText().toString();
        Log.i("detail","내용: "+ doing);
        dbHelper2.insert(Mymap.LATLNG.latitude, Mymap.LATLNG.longitude, Address, doing, hour, minute, date, detail); // DB에 넣기
        finish();
    }

    // 좌표로 주소 얻기
    public static String getAddress(Context mContext, double lat, double lng) {
        String LocationName = "";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREAN);
        List<Address> address;
        try {
            // 한 좌표에 대해 두개 이상의 이름이 존재 할 수 있기에
            // 주소배열을 리턴 받기 위한 최대 갯수
            if(geocoder != null) {
                address = geocoder.getFromLocation(lat, lng, 1);
                if(address != null && address.size()>0) {
                    // 주소 받아오기
                    LocationName = address.get(0).getAddressLine(0).toString();
                }
            }
        }
        catch (IOException e) {
            Toast.makeText(mContext, "Can't find address." , Toast.LENGTH_LONG).show();
        }
        return LocationName;
    }

    // 현재시간 string으로 얻기
    public String getDateString()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        String str_date = df.format(new Date());
        return str_date;
    }
}
