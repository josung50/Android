package opensource.mobileprogram;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by JSR on 2016-10-31.
 */
public class DBHelper extends SQLiteOpenHelper{
    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블 생성
        db.execSQL("CREATE TABLE list (_id INTEGER PRIMARY KEY AUTOINCREMENT, lat REAL , lng REAL , address TEXT);");
    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void DropTalbe(){
        SQLiteDatabase db = getWritableDatabase();
        String sql = "DROP TABLE list";
        db.execSQL(sql);
        onCreate(db);
    }

    public void insert(double lat, double lng, String address) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO list VALUES(null, '" + lat + "', " + lng + ", '" + address + "');");
        db.close();
    }

    public String getResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM list", null);
        while (cursor.moveToNext()) {
            result += cursor.getString(0)
                    + " : "
                    + cursor.getInt(1)
                    + " , "
                    + cursor.getInt(2)
                    + ", "
                    + cursor.getString(3)
                    + "\n";
        }
        return result;
    }
}
