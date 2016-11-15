package opensource.mobileprogram;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Mymap extends FragmentActivity implements OnMapReadyCallback{
    // DB 관련 변수들
    final DBHelper dbHelper = new DBHelper(this, "list.db", null , 1); // 행적 추적을 위한 DB
    final DBHelper2 dbHelper2 = new DBHelper2(this, "list2.db", null , 1); // 일지를 위한 DB

    public static int count = 0;
    private final int ID_REQUEST_PERMISSION = 0x00;
    public static int flag_FINE_LOCATION = 0;
    public static int flag_COARSE_LOCATION = 0;

    static double latitude = 0.0;
    static double longitude = 0.0;

    static GoogleMap mMap;
    static MapFragment mapFragment;
    static public LatLng LATLNG;
    static RelativeLayout temp; // 일지 정보를 보여주기 위한 레이아웃 , 혹은 통계

    LocationManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mymap);

        // 마커의 윈도우 창 투명하게
        temp = (RelativeLayout)findViewById(R.id.temp);
        temp.setVisibility(View.INVISIBLE);

        String[] REQUEST_PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION ,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i("빌드버전23이상" , "성공");
            requestPermissions(REQUEST_PERMISSIONS, ID_REQUEST_PERMISSION);
        }

        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toast.makeText(getApplicationContext(), "일지를 작성하고자 하면 지도를 꾹 눌러주세요." , Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // 마커의 윈도우 클릭하면 확대
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                // 저장했던 시간 , 장소 , 얼마나 , 했던 일 , 세부사항
                temp.setVisibility(View.VISIBLE);
                TextView view1 = (TextView)findViewById(R.id.view1);
                TextView view2 = (TextView)findViewById(R.id.view2);
                view1.setText(marker.getTitle());
                view1.setTextSize(25);
                view2.setText(marker.getSnippet());
                view2.setTextSize(25);
            }
        });

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latlng) {
                LATLNG = latlng; // static latlng에 파라미터를 대입... 액티비티 전환용
                Log.i("LATLNG", "LATLNG" + LATLNG);
                mMap.clear();
                mMap.addMarker(new MarkerOptions()
                        .position(latlng)
                        .title("Your click location"));

                // 예 아니오 팝업 창 띄우기
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(Mymap.this);
                alert_confirm.setMessage("이 위치에서 일지를 작성하시겠습니까?").setCancelable(false).setPositiveButton("작성",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'YES'
                                Intent intent = new Intent(Mymap.this, Write.class);
                                startActivity(intent);


                            }
                        }).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'No'
                                return;
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();

            }
        });

        map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }

    // 행적 초기화
    public void DropButton(View v) {
        dbHelper.DropTalbe();
        Log.i("드랍", "성공");
    }

    // 일지 초기화
    public void DropButton2(View v) {
        Log.i("드랍2", "성공");
        dbHelper2.DropTalbe();
    }

    // 현재위치 버튼 클릭
    public void CurrentLocationButton(View v){
        startLocationService(); // 위도경도 얻어옴
        Log.i("위도경도", latitude + " " + longitude);
        LatLng CP = new LatLng(latitude, longitude);
        mMap.clear();
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(CP)
                .title(getAddress(this, latitude, longitude)));
        marker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CP, 17.0f));
    }

    // 행적저장 버튼 클릭
    public void StartButton(View v) {
        count++;
        startLocationService();
        String SaveLoctaion = getAddress(getApplicationContext(), latitude, longitude);
        dbHelper.insert(latitude, longitude, SaveLoctaion);
        Log.i("행적저장", "성공" + count);
    }

    // 저장한 행적 보여주기
    public void ListButton(View v) {
        int count;
        String address;
        double lat=0 , lng=0;
        Cursor c = dbHelper.getReadableDatabase().query("list",null,null,null,null,null,null);
        Log.i("진행도", "성공");
        mMap.clear();
        PolylineOptions pl = new PolylineOptions();
        pl.color(Color.RED);
        pl.width(5);
        ArrayList points = new ArrayList<LatLng>();

        while(c.moveToNext())
        {
            count = c.getInt(c.getColumnIndex("_id"));
            address = c.getString(c.getColumnIndex("address"));
            lat = c.getDouble(c.getColumnIndex("lat"));
            lng = c.getDouble(c.getColumnIndex("lng"));
            Log.i("디비얻어오기", "성공" + lat + " " + lng);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lng))
                    .title(count + ": " + address));
            marker.showInfoWindow();
            points.add(new LatLng(lat, lng));
            pl.addAll(points);
            mMap.addPolyline(pl);


        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat , lng), 17.0f)); // 마지막 저장 값으로 이동
    }


    public void startLocationService(){

        manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        long minTime = 1000;
        float minDistance = 1;

        if(checkPermission()) // 권한 비허용이면 실행
            Toast.makeText(getApplicationContext(), "Plz allows permission.1" , Toast.LENGTH_SHORT).show();

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, mLocationListener);
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, mLocationListener);

    }

    public  void stopLocationService(){
        if(checkPermission())
            Toast.makeText(this, "Plz allows permission.2", Toast.LENGTH_SHORT).show();
        manager.removeUpdates(mLocationListener);

    }

    public final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.i("로케이션변화진입" , "성공" + " " + location.getLatitude() + " " + longitude);
            stopLocationService();
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permission[], int[] grantResults) {
        if (requestCode == ID_REQUEST_PERMISSION) {
            Log.i("리퀘스트코드" , "성공" + requestCode + " " + permission[0] + " " +
                    Manifest.permission.ACCESS_FINE_LOCATION + " " +
                    grantResults[0] + " " + PackageManager.PERMISSION_GRANTED);

            if (permission[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("플래그fine" , "설정");
                flag_FINE_LOCATION = 1;
            }
            else if (permission[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Plz allows permissions3", Toast.LENGTH_SHORT).show();
                finish();
            }

            if (permission[1] == android.Manifest.permission.ACCESS_COARSE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("플래그corse" , "설정");
                flag_COARSE_LOCATION = 1;
            }
            else if (permission[1] == android.Manifest.permission.ACCESS_COARSE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Plz allows permissions4", Toast.LENGTH_SHORT).show();
                finish();
            }

            super.onRequestPermissionsResult(requestCode, permission, grantResults);
            flag_COARSE_LOCATION = 1;
            flag_FINE_LOCATION = 1;
        }
    }

    private boolean checkPermission() {
        if (flag_FINE_LOCATION == 0 && flag_COARSE_LOCATION == 0)
            return true;
        else
            return false;
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

    // 일지 보기
    public void button(View v) { // 일지 보는 버튼
        int Tcount;
        String TAddress = null;
        double Tlat = 0.0;
        double Tlng = 0.0;
        String Thour = null;
        String Tminute = null;
        String Tdoing = null;
        String Tdetail = null;
        String Tdate = null;
        Cursor Tc = dbHelper2.getReadableDatabase().query("list2",null,null,null, null, null, null);

        mMap.clear();
        while(Tc.moveToNext()) {
            Tcount = Tc.getInt(Tc.getColumnIndex("_id"));
            TAddress = Tc.getString(Tc.getColumnIndex("address"));
            Tlat = Tc.getDouble(Tc.getColumnIndex("lat"));
            Tlng = Tc.getDouble(Tc.getColumnIndex("lng"));
            Log.i("디비얻어오기", "성공" + Tlat + " " + Tlng);
            Thour = Tc.getString(Tc.getColumnIndex("hour"));
            Tminute = Tc.getString(Tc.getColumnIndex("minute"));
            Tdoing = Tc.getString(Tc.getColumnIndex("doing"));
            Tdetail = Tc.getString(Tc.getColumnIndex("detail"));
            Tdate = Tc.getString(Tc.getColumnIndex("date"));
            Log.i("정보" , "내용 :" + Tcount + " " + TAddress);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Tlat, Tlng))
                    .title(Tdate+": "+Tdoing)
                    .snippet("장소 :"+TAddress+"\n"
                                +"기간 :"+Thour+" "+Tminute+"\n"
                                +"세부사항 :"+Tdetail));
            //.snippet(TAddress + "\n" + Thour+" "+Tminute+"동안\n"+Tdetail));
            marker.showInfoWindow();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Tlat , Tlng), 10.0f)); // 마지막 저장 값으로 이동
    }

    // 윈도우 창 닫기 버튼
    public void close(View v) {
        temp.setVisibility(View.INVISIBLE);
    }

    // 통계
    public void rank(View v) {
        TextView view1 = (TextView)findViewById(R.id.view1);
        TextView view2 = (TextView)findViewById(R.id.view2);
        int []list= new int[8] ;
        String []rank = new String[8];
        StringBuffer Binglist = new StringBuffer();
        // list 초기화
        for(int i=0; i<list.length; i++)
            list[i] = 0;

        String doing = null;
        Cursor Tc = dbHelper2.getReadableDatabase().query("list2",null,null,null, null, null, null);
        if(Tc == null)
            Toast.makeText(getApplicationContext(), "Data가 없습니다." , Toast.LENGTH_LONG).show();

        while(Tc.moveToNext()) {
            doing = Tc.getString(Tc.getColumnIndex("doing"));
            list[searchDoing(doing)]++;
        }
        for(int i=0; i<list.length; i++)
        {
            if(list[i] == 0)
                continue;
            switch(i){
                case 0: rank[i] = "식사 :" + list[i] + "번\n"; break;
                case 1: rank[i] = "과제 :" + list[i] + "번\n"; break;
                case 2: rank[i] = "tv시청 :" + list[i] + "번\n"; break;
                case 3: rank[i] = "게임 :" + list[i] + "번\n"; break;
                case 4: rank[i] = "지인만나기 :" + list[i] + "번\n"; break;
                case 5: rank[i] = "산책 :" + list[i] + "번\n"; break;
                case 6: rank[i] = "독서 :" + list[i] + "번\n"; break;
                case 7: rank[i] = "기타 :" + list[i] + "번\n"; break;
            }
            Binglist.append(rank[i]);
        }
        temp.setVisibility(View.VISIBLE);
        view1.setText("한 일에 대한 통계");
        view2.setText(Binglist);
        view1.setTextSize(25);
        view2.setTextSize(25);
    }
    // doing에 대한 매칭 결과 반환
    public int searchDoing(String doing) {
        switch(doing){
            case "식사":  return 0;
            case "과제":  return 1;
            case "tv시청":    return 2;
            case "게임":  return 3;
            case "지인만나기":  return 4;
            case "산책":  return 5;
            case "독서":  return 6;
            case "기타":  return 7;
        }
        return -1;
    }
}