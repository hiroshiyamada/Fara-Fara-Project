package jp.recognize.cameraSample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BluetoothDetecter extends Activity{

	private BluetoothAdapter mBtAdapter;
	private TextView mResultView;
	private String mResult = "";
	// ブロードキャストレシーバの定義
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)){
				// 見つけたデバイス情報の取得
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
				mResult += "Device : " + device.getName() + "/" + device.getAddress() + "/"+rssi+"\n";
				mResultView.setText(mResult);
			}
			mBtAdapter.startDiscovery();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mResultView = (TextView)findViewById(R.id.bt_text);
		// インテントフィルタの作成
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		// ブロードキャストレシーバの登録
		registerReceiver(mReceiver, filter);

		
		// BluetoothAdapterのインスタンス取得
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		// Bluetooth有効
		if (!mBtAdapter.isEnabled()) {
			mBtAdapter.enable();
		}
		
		//Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		
		// 周辺デバイスの検索開始
		mBtAdapter.startDiscovery();
		
		
		
		//一回のみ
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 検索中止
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}
		unregisterReceiver(mReceiver);
	}
}
