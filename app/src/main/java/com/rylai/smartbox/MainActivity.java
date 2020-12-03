package com.rylai.smartbox;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.esim.rylai.smartbox.ekey.EkeyFailStatusEnum;
import com.esim.rylai.smartbox.ekey.EkeyManager;
import com.esim.rylai.smartbox.ekey.EkeyStatusChange;
import com.esim.rylai.smartbox.uhf.ReaderResult;
import com.esim.rylai.smartbox.uhf.UhfManager;
import com.esim.rylai.smartbox.uhf.UhfTag;

import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @BindView(R.id.keyStatus1)
    TextView keyStatus1;
    @BindView(R.id.openKey1)
    Button openKey1;
    @BindView(R.id.keyStatus2)
    TextView keyStatus2;
    @BindView(R.id.openKey2)
    Button openKey2;
    @BindView(R.id.keyStatus3)
    TextView keyStatus3;
    @BindView(R.id.openKey3)
    Button openKey3;
    @BindView(R.id.antNums)
    EditText antNums;
    @BindView(R.id.tagTotal)
    TextView tagTotal;
    @BindView(R.id.invStart)
    Button invStart;
    @BindView(R.id.invStatus)
    TextView invStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        UhfManager.getInstance().config("172.16.63.100",uhfListener).setShowLog(true);
//        EkeyManager.getInstance().config(this,"/dev/ttyS4",9600,listener);
        EkeyManager.getInstance().config(this, "/dev/ttyS4", 9600, ekeyListener, 2000, 1, 2);
        EkeyManager.getInstance().setShowLog(true);
    }

    @OnClick({R.id.openKey1, R.id.openKey2, R.id.openKey3})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.openKey1:
                EkeyManager.getInstance().openEkey(1);
                break;
            case R.id.openKey2:
                EkeyManager.getInstance().openEkey(2);
                break;
            case R.id.openKey3:
                EkeyManager.getInstance().openEkey(3);
                break;
        }
    }

    @OnClick(R.id.invStart)
    public void onClick() {
        try {
            int[] intAntNum = null;
            String ants = antNums.getText().toString();
            String[] split = ants.split(",");
            intAntNum = new int[split.length];
            for (int i = 0; i < split.length; i++) {
                intAntNum[i] = Integer.valueOf(split[i]);
            }
            tagTotal.setText("" + 0);
            invStatus.setText("start...");
            UhfManager.getInstance().startReadTags(intAntNum);
        } catch (Exception ex) {

        }
    }
    private final EkeyManager.EkeyStatusListener ekeyListener=new EkeyManager.EkeyStatusListener() {
        @Override
        public void onEkeyStatusChange(int ekeyAddr, EkeyStatusChange ekeyStatusChange) {
            Log.d(TAG, "onEkeyStatusChange: " + "Ekey Addr：" + ekeyAddr + "     StatusChange:" + ekeyStatusChange.getDisp());
            runOnUiThread(() -> {
                switch (ekeyAddr) {
                    case 1:
                        keyStatus1.setText(ekeyStatusChange.getDisp());
                        if(ekeyStatusChange==EkeyStatusChange.OPENED_TO_CLOSED){
                            UhfManager.getInstance().startReadTags();
                        }
                        break;
                    case 2:
                        keyStatus2.setText(ekeyStatusChange.getDisp());
                        break;
                    case 3:
                        keyStatus3.setText(ekeyStatusChange.getDisp());
                        break;
                }
            });
        }
        @Override
        public void onFail(int ekeyAddr, EkeyFailStatusEnum ekeyFailStatusEnum) {
            Log.d(TAG, "onFail: " + ekeyFailStatusEnum.toString());
            runOnUiThread(() ->
                    Toast.makeText(MainActivity.this, ekeyFailStatusEnum.getDisp(), Toast.LENGTH_SHORT).show()
            );
        }
    };
    private final UhfManager.EsimUhfReadListener uhfListener=new UhfManager.EsimUhfReadListener() {
        private long startTime=0;
        @Override
        public void onStartSuc() {
            startTime=SystemClock.elapsedRealtime();
            runOnUiThread(() -> {
                invStatus.setText("统计中...");
            });
            Log.d(TAG, "onStartSuc:" + "=============");
        }

        @Override
        public void onStartFail(ReaderResult reader_err) {
            Log.d(TAG, "onStartFail:" + "=============" + reader_err.toString());
            runOnUiThread(() -> {
                invStatus.setText("读取器异常");
            });
        }

        @Override
        public void onReadIncrementalTotal(Collection<UhfTag> tags) {
            runOnUiThread(() -> {
                tagTotal.setText("" + tags.size());
            });
        }

        @Override
        public void onReadFinish(Collection<UhfTag> tags) {
            runOnUiThread(() -> {
                long endTime= SystemClock.elapsedRealtime();
                invStatus.setText("统计完成,耗时:"+(endTime-startTime)+"ms");
                tagTotal.setText("" + tags.size());
            });
        }
    };
}