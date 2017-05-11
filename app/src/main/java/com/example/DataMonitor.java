package com.example;

import java.io.File;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.PrivateCredentialPermission;

import com.example.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.GestureOverlayView;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class DataMonitor extends FragmentActivity implements OnClickListener {

	boolean slideAction = false;
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothService mBluetoothService = null;
	private String mConnectedDeviceName = null;

	private TextView mTitle;
	private boolean recordStartorStop=false;

	private DataFragment dataFragment;
	private UsFragment usFragment;
	private ConfigFragment configFragment;

	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	protected static final String TAG = null;
	private short sOffsetAccX,sOffsetAccY,sOffsetAccZ;

	private final Handler mHandler = new Handler() {
		// 匿名内部类写法，实现接口Handler的一些方法
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					break;
				case BluetoothService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_READ:
				try {
					float [] fData=msg.getData().getFloatArray("Data");
					switch (RunMode){
						case 0:
							switch (iCurrentGroup){
								case 0:
									((TextView)findViewById(R.id.tvNum1)).setText(msg.getData().getString("Date"));
									((TextView)findViewById(R.id.tvNum2)).setText(msg.getData().getString("Time"));
									break;
								case 1:
									((TextView)findViewById(R.id.tvNum1)).setText(String.format("% 10.2fg", fData[0]));
									((TextView)findViewById(R.id.tvNum2)).setText(String.format("% 10.2fg", fData[1]));
									((TextView)findViewById(R.id.tvNum3)).setText(String.format("% 10.2fg", fData[2]));
									((TextView)findViewById(R.id.tvNum4)).setText(String.format("% 10.2f℃", fData[16]));
									break;
								case 2:
									((TextView)findViewById(R.id.tvNum1)).setText(String.format("% 10.2f°/s", fData[3]));
									((TextView)findViewById(R.id.tvNum2)).setText(String.format("% 10.2f°/s", fData[4]));
									((TextView)findViewById(R.id.tvNum3)).setText(String.format("% 10.2f°/s", fData[5]));
									((TextView)findViewById(R.id.tvNum4)).setText(String.format("% 10.2f℃", fData[16]));
									break;
								case 3:
									((TextView)findViewById(R.id.tvNum1)).setText(String.format("% 10.2f°", fData[6]));
									((TextView)findViewById(R.id.tvNum2)).setText(String.format("% 10.2f°", fData[7]));
									((TextView)findViewById(R.id.tvNum3)).setText(String.format("% 10.2f°", fData[8]));
									((TextView)findViewById(R.id.tvNum4)).setText(String.format("% 10.2f℃", fData[16]));
									break;
								case 4://磁场
									((TextView)findViewById(R.id.tvNum1)).setText(String.format("% 10.0f", fData[9]));
									((TextView)findViewById(R.id.tvNum2)).setText(String.format("% 10.0f", fData[10]));
									((TextView)findViewById(R.id.tvNum3)).setText(String.format("% 10.0f", fData[11]));
									((TextView)findViewById(R.id.tvNum4)).setText(String.format("% 10.2f℃", fData[16]));
									break;
								case 5://端口
									((TextView)findViewById(R.id.tvNum1)).setText(String.format("% 10.2f", fData[12]));
									((TextView)findViewById(R.id.tvNum2)).setText(String.format("% 10.2f", fData[13]));
									((TextView)findViewById(R.id.tvNum3)).setText(String.format("% 10.2f", fData[14]));
									((TextView)findViewById(R.id.tvNum4)).setText(String.format("% 10.2f", fData[15]));
									break;
								case 6://气压
									((TextView)findViewById(R.id.tvNum1)).setText(String.format("% 10.2fPa", fData[17]));
									((TextView)findViewById(R.id.tvNum2)).setText(String.format("% 10.2fm", fData[18]));
									break;
								case 7://经纬度
									((TextView)findViewById(R.id.tvNum1)).setText(String.format("% 14.6f°", fData[19]));
									((TextView)findViewById(R.id.tvNum2)).setText(String.format("% 14.6f°", fData[20]));
									break;
								case 8://地速
									((TextView)findViewById(R.id.tvNum1)).setText(String.format("% 10.2fm", fData[21]));
									((TextView)findViewById(R.id.tvNum2)).setText(String.format("% 10.2f°", fData[22]));
									((TextView)findViewById(R.id.tvNum3)).setText(String.format("% 10.2fm/s", fData[23]));
									break;
								case 9://四元数
									((TextView)findViewById(R.id.tvNum1)).setText(String.format("% 7.3f", fData[24]));
									((TextView)findViewById(R.id.tvNum2)).setText(String.format("% 7.3f", fData[25]));
									((TextView)findViewById(R.id.tvNum3)).setText(String.format("% 7.3f", fData[26]));
									((TextView)findViewById(R.id.tvNum4)).setText(String.format("% 7.3f", fData[27]));
									break;
								case 10:
									((TextView)findViewById(R.id.tvNum1)).setText(String.format("% 5.0f", fData[28]));
									((TextView)findViewById(R.id.tvNum2)).setText(String.format("% 7.1f", fData[29]));
									((TextView)findViewById(R.id.tvNum3)).setText(String.format("% 7.1f", fData[30]));
									((TextView)findViewById(R.id.tvNum4)).setText(String.format("% 7.1f", fData[31]));
									break;
							}
							break;
						case 1://Cali Acc
							((TextView)findViewById(R.id.tvAccX)).setText(String.format("% 10.2fg", fData[0]));sOffsetAccX = (short)(fData[0]/16*32768);
							((TextView)findViewById(R.id.tvAccY)).setText(String.format("% 10.2fg", fData[1]));sOffsetAccY = (short)(fData[1]/16*32768);
							((TextView)findViewById(R.id.tvAccZ)).setText(String.format("% 10.2fg", fData[2]));sOffsetAccZ = (short)((fData[2]-1)/16*32768);
							break;
						case 2://Cali Gyro
							((TextView)findViewById(R.id.tvGyroX)).setText(String.format("% 10.2f°/s", fData[3]));
							((TextView)findViewById(R.id.tvGyroY)).setText(String.format("% 10.2f°/s", fData[4]));
							((TextView)findViewById(R.id.tvGyroZ)).setText(String.format("% 10.2f°/s", fData[5]));
							break;
						case 3://Cali Mag
							((TextView)findViewById(R.id.tvMagX)).setText(String.format("% 10.0f", fData[9]));
							((TextView)findViewById(R.id.tvMagY)).setText(String.format("% 10.0f", fData[10]));
							((TextView)findViewById(R.id.tvMagZ)).setText(String.format("% 10.0f", fData[11]));
							break;
					}


					
				} catch (Exception e) {
					// TODO: handle exception
				}
				break;
			case MESSAGE_DEVICE_NAME:
				mConnectedDeviceName = msg.getData().getString("device_name");
				Toast.makeText(getApplicationContext(),"Connected to " + mConnectedDeviceName,Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	private static final int REQUEST_CONNECT_DEVICE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		SelectFragment(0);

		try {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				Toast.makeText(this, "蓝牙不可用", Toast.LENGTH_LONG).show();
				//finish();
				return;
			}

			if (!mBluetoothAdapter.isEnabled()) mBluetoothAdapter.enable();
			if (mBluetoothService == null)
				mBluetoothService = new BluetoothService(this, mHandler); // 用来管理蓝牙的连接
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
		}
		catch (Exception err){}
	}
	public void onClickedBTSet(View v){
		try {
			if (!mBluetoothAdapter.isEnabled()) mBluetoothAdapter.enable();
			if (mBluetoothService == null)
				mBluetoothService = new BluetoothService(this, mHandler); // 用来管理蓝牙的连接
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
		}
		catch (Exception err){}
	}
	@SuppressLint("NewApi")
	private void SelectFragment(int Index) {
		// TODO Auto-generated method stub
		android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
		android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();

		if (dataFragment==null) {dataFragment = new DataFragment();transaction.add(R.id.id_content, dataFragment);}
		if (usFragment==null) {usFragment = new UsFragment();transaction.add(R.id.id_content, usFragment);	}
		if (configFragment==null) {configFragment = new ConfigFragment();transaction.add(R.id.id_content, configFragment);	}
		switch (Index) {
		case 0:
			transaction.show(dataFragment);
			transaction.hide(usFragment);
			transaction.hide(configFragment);
			break;
		case 1:
			transaction.hide(dataFragment);
			transaction.show(usFragment);
			transaction.hide(configFragment);
		break;
		case 2:
			transaction.hide(dataFragment);
			transaction.hide(usFragment);
			transaction.show(configFragment);
			break;
		default:
			break;
		}
		transaction.commit();
	}

	@Override
	public void onStart() {
		super.onStart();
		try{
			GetSelected();
			SharedPreferences mySharedPreferences= getSharedPreferences("Output", Activity.MODE_PRIVATE);
			IcType = Integer.parseInt(mySharedPreferences.getString("IC","0"));
			Log.i("IC",String.format("%d",IcType));
			SetICType(IcType);
		}
		catch (Exception err){}

	}

	public synchronized void onResume() {
		super.onResume();

		if (mBluetoothService != null) {
			if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
				mBluetoothService.start();
			}
		}
	}

	@Override
	public synchronized void onPause() {
		super.onPause();

	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mBluetoothService != null) mBluetoothService.stop();
	}
	public BluetoothDevice device;
	// 利用startActivityForResult 和 onActivityResult在activity间传递数据
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:// When DeviceListActivity returns with a device to connect			
			if (resultCode == Activity.RESULT_OK) {				
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);// Get the device MAC address				
				device = mBluetoothAdapter.getRemoteDevice(address);// Get the BLuetoothDevice object				
				mBluetoothService.connect(device);// Attempt to connect to the device
			}
			break;
		}
	}
	public void URLClick(View v) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		switch (v.getId()){
			case R.id.urlJYZK:intent.setData(Uri.parse("http://RobotControl.taobao.com"));break;
			case R.id.title_left_text:intent.setData(Uri.parse("http://RobotControl.taobao.com"));break;
			case R.id.urlEM:intent.setData(Uri.parse("http://ElecMaster.net"));break;
		}
		startActivity(intent);
	}
	boolean[] selected = new boolean[]{false,true,true,true,false,false,false,false,false,false,false};
	String[] SelectItem = new String[]{"时间","加速度","角速度","角度","磁场","端口","气压","经纬度","地速","四元数","卫星数"};
	public void RefreshButtonStatus(){
		if (selected[0]) ((TextView)findViewById(R.id.button0)).setTextColor(Color.BLACK); else ((TextView)findViewById(R.id.button0)).setTextColor(Color.GRAY);
		if (selected[1]) ((TextView)findViewById(R.id.button1)).setTextColor(Color.BLACK); else ((TextView)findViewById(R.id.button1)).setTextColor(Color.GRAY);
		if (selected[2]) ((TextView)findViewById(R.id.button2)).setTextColor(Color.BLACK); else ((TextView)findViewById(R.id.button2)).setTextColor(Color.GRAY);
		if (selected[3]) ((TextView)findViewById(R.id.button3)).setTextColor(Color.BLACK); else ((TextView)findViewById(R.id.button3)).setTextColor(Color.GRAY);
		if (selected[4]) ((TextView)findViewById(R.id.button4)).setTextColor(Color.BLACK); else ((TextView)findViewById(R.id.button4)).setTextColor(Color.GRAY);
		if (selected[5]) ((TextView)findViewById(R.id.button5)).setTextColor(Color.BLACK); else ((TextView)findViewById(R.id.button5)).setTextColor(Color.GRAY);
		if (selected[6]) ((TextView)findViewById(R.id.button6)).setTextColor(Color.BLACK); else ((TextView)findViewById(R.id.button6)).setTextColor(Color.GRAY);
		if (selected[7]) ((TextView)findViewById(R.id.button7)).setTextColor(Color.BLACK); else ((TextView)findViewById(R.id.button7)).setTextColor(Color.GRAY);
		if (selected[8]) ((TextView)findViewById(R.id.button8)).setTextColor(Color.BLACK); else ((TextView)findViewById(R.id.button8)).setTextColor(Color.GRAY);
		if (selected[9]) ((TextView)findViewById(R.id.button9)).setTextColor(Color.BLACK); else ((TextView)findViewById(R.id.button9)).setTextColor(Color.GRAY);
		if (selected[10]) ((TextView)findViewById(R.id.buttonA)).setTextColor(Color.BLACK); else ((TextView)findViewById(R.id.buttonA)).setTextColor(Color.GRAY);
	}
	public void GetSelected(){
		SharedPreferences mySharedPreferences= getSharedPreferences("Output", Activity.MODE_PRIVATE);
		try{
			int iOut = Integer.parseInt(mySharedPreferences.getString("Out", "15"));
			for (int i=0;i<selected.length;i++){
				selected[i]=((iOut>>i)&0x01)==0x01;
			}
			RefreshButtonStatus();
		}
		catch (Exception err){}
	}


	public void OnClickConfig(View v) {

		GetSelected();
		new AlertDialog.Builder(this)
				.setTitle("请选择输出内容：")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMultiChoiceItems(SelectItem, selected, new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i, boolean b) {
						selected[i] = b;
					}
				})
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						byte[] buffer = new byte[5];
						buffer[0] = (byte) 0xff;
						buffer[1] = (byte) 0xaa;
						buffer[2] = (byte) 0x02;
						short sOut = 0;
						for (int i = 0; i < selected.length; i++) {
							if (selected[i]) sOut |= 0x01 << i;
						}
						buffer[3] = (byte) (sOut&0xff);
						buffer[4] = (byte) (sOut>>8);
						SharedPreferences mySharedPreferences= getSharedPreferences("Output",Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor = mySharedPreferences.edit();
						editor.putString("Out",String.format("%d",sOut));
						editor.commit();
						RefreshButtonStatus();
						mBluetoothService.Send(buffer);
					}
				})
				.setNegativeButton("取消", null)
				.show();

	}
	public int IcType=0,IcSelectedType=0;
	public void SetICType(int type){
		if (type == 0) {
			findViewById(R.id.lljy901).setVisibility(View.INVISIBLE);
			findViewById(R.id.lljy61).setVisibility(View.VISIBLE);
			findViewById(R.id.btnSet).setVisibility(View.INVISIBLE);
			short sOut = 0x0e;
			for (int i = 0; i < selected.length; i++) {
				selected[i]=((sOut>>i)&0x01)==0x01;
			}
			RefreshButtonStatus();
			SharedPreferences mySharedPreferences= getSharedPreferences("Output",Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = mySharedPreferences.edit();
			editor.putString("Out", String.format("%d", sOut));
			editor.commit();

		} else {
			findViewById(R.id.lljy901).setVisibility(View.VISIBLE);
			findViewById(R.id.lljy61).setVisibility(View.INVISIBLE);
			findViewById(R.id.btnSet).setVisibility(View.VISIBLE);
		}
		SharedPreferences mySharedPreferences= getSharedPreferences("Output",Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putString("IC",String.format("%d",type));
		editor.commit();
	}
	public void OnClickSelectIC(View v) {

		SharedPreferences mySharedPreferences= getSharedPreferences("Output", Activity.MODE_PRIVATE);

		new AlertDialog.Builder(this)
				.setTitle("请选择输出内容：")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setSingleChoiceItems(new String[]{"JY-61系列", "JY-901系列"}, Integer.parseInt(mySharedPreferences.getString("IC","0")), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						IcSelectedType = i;
					}
				})
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						IcType = IcSelectedType;
						SetICType(IcType);
					}
				})
				.setNegativeButton("取消", null)
				.show();

	}
	private int OutRate = 6,SelectedRate = 0;
	public void OnClickRate(View v) {

		new AlertDialog.Builder(this)
				.setTitle("请选择输出速率：")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setSingleChoiceItems(new String[]{"0.1Hz","0.2Hz","0.5Hz","1Hz","2Hz","5Hz","10Hz","20Hz","50Hz","100Hz","125Hz","200Hz"},OutRate, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						SelectedRate = i;
					}
				})
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						OutRate = SelectedRate;
						SharedPreferences mySharedPreferences = getSharedPreferences("Output", Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor = mySharedPreferences.edit();
						editor.putString("Rate", String.format("%d", OutRate));
						editor.commit();
						mBluetoothService.Send(new byte[]{(byte)0xff,(byte)0xaa,(byte)0x03,(byte)OutRate,(byte)0x00});
					}
				})
				.setNegativeButton("取消", null)
				.show();

	}
	public void OnClickJY61Rate(View v) {

		new AlertDialog.Builder(this)
				.setTitle("输出速率：")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage("请选择数据输出频率")
				.setPositiveButton("20Hz", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						mBluetoothService.Send(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x62});
					}
				})
				.setNegativeButton("100Hz",  new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						mBluetoothService.Send(new byte[]{(byte)0xff,(byte)0xaa,(byte)0x61});
					}
				})
				.show();

	}
	public void OnClickZeroZ(View v) {

		mBluetoothService.Send(new byte[]{(byte)0xff,(byte)0xaa,(byte)0x52});

	}
	public void OnClickHeight(View v) {
		mBluetoothService.Send(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x01, (byte) 0x03, (byte) 0x00});
	}
	public void OnClickReset(View v) {
		mBluetoothService.Send(new byte[]{(byte)0xff,(byte)0xaa,(byte)0x00,(byte)0x01,(byte)0x00});
	}
	int RunMode = 0;
	public void OnClickMode(View v) {
		switch (v.getId()){
			case R.id.CalAcc:
				if (((TextView)findViewById(R.id.tvCaliAcc)).getText()=="完成") {
					RunMode = 0;
					mBluetoothService.Send(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x05, (byte) (sOffsetAccX&0xff), (byte) (sOffsetAccX>>8)});
					mBluetoothService.Send(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x06, (byte) (sOffsetAccY&0xff), (byte) (sOffsetAccY>>8)});
					mBluetoothService.Send(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x07, (byte) (sOffsetAccZ&0xff), (byte) (sOffsetAccZ>>8)});
					mBluetoothService.Send(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x01, (byte) 0x00, (byte) 0x00});

					((TextView)findViewById(R.id.tvCaliAcc)).setText("加速度计校准");
				}
				else {
					new AlertDialog.Builder(this)
							.setTitle("请选择输出速率：")
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setMessage("加速计校准需要保持模块水平，待校准值稳定不变后，点击完成按钮即可。模块是否水平？")
							.setPositiveButton("是", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									mBluetoothService.Send(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x01, (byte) 0x01, (byte) 0x00});
									RunMode = 1;
									((TextView)findViewById(R.id.tvCaliAcc)).setText("完成");
								}
							})
							.setNegativeButton("否", null)
							.show();
				}
				break;
			case R.id.CalGyro:
				if (((TextView) findViewById(R.id.tvCaliGyro)).getText() == "完成") {
					mBluetoothService.Send(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x01, (byte) 0x00, (byte) 0x00});
					RunMode = 0;
					((TextView)findViewById(R.id.tvCaliGyro)).setText("陀螺仪校准");
				} else {
					new AlertDialog.Builder(this)
							.setTitle("陀螺仪校准：")
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setMessage("陀螺仪校准需要保持模块静止，待校准值稳定不变后，点击完成按钮即可。模块是否静止？")
							.setPositiveButton("是", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									mBluetoothService.Send(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x01, (byte) 0x01, (byte) 0x00});
									RunMode = 2;
									((TextView)findViewById(R.id.tvCaliGyro)).setText("完成");
								}
							})
							.setNegativeButton("否", null)
							.show();

				}
				break;
			case R.id.CalMag:
				if (((TextView)findViewById(R.id.tvCaliMag)).getText()=="完成") {
					mBluetoothService.Send(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x01, (byte) 0x00, (byte) 0x00});
					RunMode = 0;
					((TextView)findViewById(R.id.tvCaliMag)).setText("磁场校准校准");
				}
				else {
					new AlertDialog.Builder(this)
							.setTitle("磁力计校准：")
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setMessage("磁力计校准需要将模块放在远离磁场干扰的环境中，距离磁性物质、铁等物质20cm以上，然后分别绕模块的X轴、Y轴、Z轴旋转几圈，待校准值稳定不变后，点击完成按钮即可。准备好了吗？")
							.setPositiveButton("是", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									mBluetoothService.Send(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x01, (byte) 0x02, (byte) 0x00});
									RunMode = 3;
									((TextView)findViewById(R.id.tvCaliMag)).setText("完成");
								}
							})
							.setNegativeButton("否", null)
							.show();
				}
				break;
		}
	}
	int iCurrentGroup=3;
	public void ControlClick(View v) {
		switch (v.getId()) {
			case R.id.button0:
				if (selected[0]==false) return;
				iCurrentGroup=0;
				((TextView)findViewById(R.id.tvDataName1)).setText("日期：");((TextView)findViewById(R.id.tvNum1)).setText("2015-1-1");
				((TextView)findViewById(R.id.tvDataName2)).setText("时间：");((TextView)findViewById(R.id.tvNum2)).setText("00:00:00.0");
				((TextView)findViewById(R.id.tvDataName3)).setText("");((TextView)findViewById(R.id.tvNum3)).setText("");
				((TextView)findViewById(R.id.tvDataName4)).setText("");((TextView)findViewById(R.id.tvNum4)).setText("");
				break;
			case R.id.button1:
				if (selected[1]==false) return;
				iCurrentGroup=1;
				((TextView)findViewById(R.id.tvDataName1)).setText("X轴：");((TextView)findViewById(R.id.tvNum1)).setText("0");
				((TextView)findViewById(R.id.tvDataName2)).setText("Y轴：");((TextView)findViewById(R.id.tvNum2)).setText("0");
				((TextView)findViewById(R.id.tvDataName3)).setText("Z轴：");((TextView)findViewById(R.id.tvNum3)).setText("0");
				((TextView)findViewById(R.id.tvDataName4)).setText("温度：");((TextView)findViewById(R.id.tvNum4)).setText("25℃");
				break;
			case R.id.button2:
				if (selected[2]==false) return;
				iCurrentGroup=2;
				((TextView)findViewById(R.id.tvDataName1)).setText("X轴：");((TextView)findViewById(R.id.tvNum1)).setText("0");
				((TextView)findViewById(R.id.tvDataName2)).setText("Y轴：");((TextView)findViewById(R.id.tvNum2)).setText("0");
				((TextView)findViewById(R.id.tvDataName3)).setText("Z轴：");((TextView)findViewById(R.id.tvNum3)).setText("0");
				((TextView)findViewById(R.id.tvDataName4)).setText("温度：");((TextView)findViewById(R.id.tvNum4)).setText("25℃");
				break;
			case R.id.button3:
				if (selected[3]==false) return;
				iCurrentGroup=3;
				((TextView)findViewById(R.id.tvDataName1)).setText("X轴：");((TextView)findViewById(R.id.tvNum1)).setText("0");
				((TextView)findViewById(R.id.tvDataName2)).setText("Y轴：");((TextView)findViewById(R.id.tvNum2)).setText("0");
				((TextView)findViewById(R.id.tvDataName3)).setText("Z轴：");((TextView)findViewById(R.id.tvNum3)).setText("0");
				((TextView)findViewById(R.id.tvDataName4)).setText("温度：");((TextView)findViewById(R.id.tvNum4)).setText("25℃");
				break;
			case R.id.button4:
				if (selected[4]==false) return;
				iCurrentGroup=4;
				((TextView)findViewById(R.id.tvDataName1)).setText("X轴：");((TextView)findViewById(R.id.tvNum1)).setText("0");
				((TextView)findViewById(R.id.tvDataName2)).setText("Y轴：");((TextView)findViewById(R.id.tvNum2)).setText("0");
				((TextView)findViewById(R.id.tvDataName3)).setText("Z轴：");((TextView)findViewById(R.id.tvNum3)).setText("0");
				((TextView)findViewById(R.id.tvDataName4)).setText("温度：");((TextView)findViewById(R.id.tvNum4)).setText("25℃");
				break;
			case R.id.button5:
				if (selected[5]==false) return;
				iCurrentGroup=5;
				((TextView)findViewById(R.id.tvDataName1)).setText("D0：");((TextView)findViewById(R.id.tvNum1)).setText("0");
				((TextView)findViewById(R.id.tvDataName2)).setText("D1：");((TextView)findViewById(R.id.tvNum2)).setText("0");
				((TextView)findViewById(R.id.tvDataName3)).setText("D2：");((TextView)findViewById(R.id.tvNum3)).setText("0");
				((TextView)findViewById(R.id.tvDataName4)).setText("D3：");((TextView)findViewById(R.id.tvNum4)).setText("0");
				break;
			case R.id.button6:
				if (selected[6]==false) return;
				iCurrentGroup=6;
				((TextView)findViewById(R.id.tvDataName1)).setText("气压：");((TextView)findViewById(R.id.tvNum1)).setText("0");
				((TextView)findViewById(R.id.tvDataName2)).setText("海拔：");((TextView)findViewById(R.id.tvNum2)).setText("0");
				((TextView)findViewById(R.id.tvDataName3)).setText("");((TextView)findViewById(R.id.tvNum3)).setText("");
				((TextView)findViewById(R.id.tvDataName4)).setText("");((TextView)findViewById(R.id.tvNum4)).setText("");
				break;
			case R.id.button7:
				if (selected[7]==false) return;
				iCurrentGroup=7;
				((TextView)findViewById(R.id.tvDataName1)).setText("经度：");((TextView)findViewById(R.id.tvNum1)).setText("0");
				((TextView)findViewById(R.id.tvDataName2)).setText("纬度：");((TextView)findViewById(R.id.tvNum2)).setText("0");
				((TextView)findViewById(R.id.tvDataName3)).setText("");((TextView)findViewById(R.id.tvNum3)).setText("");
				((TextView)findViewById(R.id.tvDataName4)).setText("");((TextView)findViewById(R.id.tvNum4)).setText("");
				break;
			case R.id.button8:
				if (selected[8]==false) return;
				iCurrentGroup=8;
				((TextView)findViewById(R.id.tvDataName1)).setText("海拔：");((TextView)findViewById(R.id.tvNum1)).setText("0");
				((TextView)findViewById(R.id.tvDataName2)).setText("航向：");((TextView)findViewById(R.id.tvNum2)).setText("0");
				((TextView)findViewById(R.id.tvDataName3)).setText("地速");((TextView)findViewById(R.id.tvNum3)).setText("0");
				((TextView)findViewById(R.id.tvDataName4)).setText("");((TextView)findViewById(R.id.tvNum4)).setText("");
				break;
			case R.id.button9:
				if (selected[9]==false) return;
				iCurrentGroup=9;
				((TextView)findViewById(R.id.tvDataName1)).setText("q0：");((TextView)findViewById(R.id.tvNum1)).setText("0");
				((TextView)findViewById(R.id.tvDataName2)).setText("q1：");((TextView)findViewById(R.id.tvNum2)).setText("0");
				((TextView)findViewById(R.id.tvDataName3)).setText("q2：");((TextView)findViewById(R.id.tvNum3)).setText("0");
				((TextView)findViewById(R.id.tvDataName4)).setText("q3：");((TextView)findViewById(R.id.tvNum4)).setText("0");
				break;
			case R.id.buttonA:
				if (selected[10]==false) return;
				iCurrentGroup=10;
				((TextView)findViewById(R.id.tvDataName1)).setText("卫星数：");((TextView)findViewById(R.id.tvNum1)).setText("0");
				((TextView)findViewById(R.id.tvDataName2)).setText("PDOP：");((TextView)findViewById(R.id.tvNum2)).setText("0");
				((TextView)findViewById(R.id.tvDataName3)).setText("HDOP：");((TextView)findViewById(R.id.tvNum3)).setText("0");
				((TextView)findViewById(R.id.tvDataName4)).setText("VDOP：");((TextView)findViewById(R.id.tvNum4)).setText("0");
				break;
		}
		((Button) findViewById(R.id.button0)).setBackgroundResource(R.drawable.ic_preference_single_normal);
		((Button) findViewById(R.id.button1)).setBackgroundResource(R.drawable.ic_preference_single_normal);
		((Button) findViewById(R.id.button2)).setBackgroundResource(R.drawable.ic_preference_single_normal);
		((Button) findViewById(R.id.button3)).setBackgroundResource(R.drawable.ic_preference_single_normal);
		((Button) findViewById(R.id.button4)).setBackgroundResource(R.drawable.ic_preference_single_normal);
		((Button) findViewById(R.id.button5)).setBackgroundResource(R.drawable.ic_preference_single_normal);
		((Button) findViewById(R.id.button6)).setBackgroundResource(R.drawable.ic_preference_single_normal);
		((Button) findViewById(R.id.button7)).setBackgroundResource(R.drawable.ic_preference_single_normal);
		((Button) findViewById(R.id.button8)).setBackgroundResource(R.drawable.ic_preference_single_normal);
		((Button) findViewById(R.id.button9)).setBackgroundResource(R.drawable.ic_preference_single_normal);
		((Button) findViewById(R.id.buttonA)).setBackgroundResource(R.drawable.ic_preference_single_normal);
		((Button) findViewById(R.id.buttonB)).setBackgroundResource(R.drawable.ic_preference_single_normal);
		((Button) v).setBackgroundResource(R.drawable.ic_preference_single_pressed);
	}
	public void onBottomBtnClick(View v) {
		switch (v.getId()) {
			case R.id.BtnData:
				SelectFragment(0);
				((Button)findViewById(R.id.BtnData)).setTextColor(Color.GREEN);
				((Button)findViewById(R.id.BtnHelp)).setTextColor(Color.WHITE);
				((Button)findViewById(R.id.BtnConfig)).setTextColor(Color.WHITE);
				break;
			case R.id.BtnHelp:
				SelectFragment(1);
				((Button)findViewById(R.id.BtnData)).setTextColor(Color.WHITE);
				((Button)findViewById(R.id.BtnHelp)).setTextColor(Color.GREEN);
				((Button)findViewById(R.id.BtnConfig)).setTextColor(Color.WHITE);
				break;
			case R.id.BtnConfig:
				SelectFragment(2);
				((Button)findViewById(R.id.BtnData)).setTextColor(Color.WHITE);
				((Button)findViewById(R.id.BtnHelp)).setTextColor(Color.WHITE);
				((Button)findViewById(R.id.BtnConfig)).setTextColor(Color.GREEN);
				break;
			case R.id.BtnRecord:
				if (this.recordStartorStop == false)
				{
					this.recordStartorStop = true;
					mBluetoothService.setRecord(true);
					((Button)v).setText("停止");
					((Button)findViewById(R.id.BtnRecord)).setTextColor(Color.RED);
				}
				else{
					this.recordStartorStop = false;
					mBluetoothService.setRecord(false);
					((Button)findViewById(R.id.BtnRecord)).setText("记录");
					((Button)v).setTextColor(Color.WHITE);
					new AlertDialog.Builder(this)
							.setTitle("提示")
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setMessage("数据已经记录至手机根目录：/mnt/sdcard/Record.txt\n是否打开已保存的文件？")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									File myFile=new File("/mnt/sdcard/Record.txt");
										Intent intent = new Intent(Intent.ACTION_VIEW);
										intent.setData(Uri.fromFile(myFile));
										startActivity(intent);
								}
							})
							.setNegativeButton("取消", null)
							.show();
				}
				break;
		}
	}
	@Override
	public void onClick(View v) {

	}

}
