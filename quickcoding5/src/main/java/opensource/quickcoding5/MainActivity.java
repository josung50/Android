package opensource.quickcoding5;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {
    ArrayList list1;
    ArrayList list2;
    TextView view1 , view2;
    EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view1 = (TextView)findViewById(R.id.view1);
        view2 = (TextView)findViewById(R.id.view2);
        input = (EditText)findViewById(R.id.input);

        list1 = new ArrayList<Integer>();
        list2 = new ArrayList<String>();
    }

    // 입력 버튼 -> array에 추가
    public void button1(View v){
        String buffer;
        buffer = input.getText().toString();

       try{
           list1.add(Integer.parseInt(buffer));
       }
       catch (Exception e){
           list2.add(buffer);
       }

        input.setText(null);
        Toast.makeText(getApplicationContext(), "입력 성공" , Toast.LENGTH_SHORT).show();
    }

    // 입력 완료 버튼 -> 보여주기
    public void button2(View v){
        view1.setText(list1.toString());
        view2.setText(list2.toString());
    }
}
