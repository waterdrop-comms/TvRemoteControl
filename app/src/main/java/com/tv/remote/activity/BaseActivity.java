package com.tv.remote.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.tv.remote.R;
import com.tv.remote.app.AppContext;
import com.tv.remote.net.NetUtils;
import com.tv.remote.utils.ConfigConst;
import com.tv.remote.utils.DrawerLayoutInstaller;
import com.tv.remote.utils.Utils;
import com.tv.remote.view.DeviceInfo;
import com.tv.remote.view.DevicesAdapter;
import com.tv.remote.view.GlobalMenu;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by 凯阳 on 2015/8/12.
 */
public abstract class BaseActivity extends AppCompatActivity
        implements GlobalMenu.OnHeaderClickListener, AdapterView.OnItemClickListener{

    public static final String AGR_DRAWING_START_LOCATION = "arg_drawing_start_location";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private DrawerLayout drawerLayout;

    private static ProgressDialog mDialog;

    private static NetHandler netHandler;

    private static List<Activity> mActivities = new ArrayList<>();

    private static DevicesAdapter mDeviceAdapter = new DevicesAdapter();

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.inject(this);
        initToolbar();
        initDrawer();
        initNetHandler();
        initProgressDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
                || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            NetUtils.getInstance().sendKey(keyCode);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void add(Activity activity) {
        mActivities.add(activity);
        if (!NetUtils.getInstance().isConnectToClient()) {
            if (mDialog != null) {
                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                mDialog.setMessage("正在连接TV");
                mDialog.show();

                Message msg = netHandler.obtainMessage();
                msg.what = ConfigConst.MSG_CONNECTION_TIME_OUT;
                netHandler.sendMessageDelayed(msg, 15000);
                NetUtils.getInstance().init(netHandler);
            }
        }
    }

    protected void remove(Activity activity) {
        mActivities.remove(activity);
        if (mActivities.size() == 0) {
            NetUtils.getInstance().release();
        }
    }

    public abstract Activity getActivityBySuper();

    private void initToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setSubtitle(null);
            toolbar.setNavigationIcon(R.drawable.btn_option);
        }
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    @OnClick(R.id.ibFindDevice)
    public void onClickFindDevice() {
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.setMessage("正在连接TV");
            mDialog.show();
            NetUtils.getInstance().startFindDevices();
            netHandler.sendEmptyMessageDelayed(ConfigConst.MSG_CONNECTION_TIME_OUT,15000);
        }
    }

    private void initDrawer() {
        GlobalMenu menuView = new GlobalMenu(this);
        menuView.setOnHeaderClickListener(this);
        menuView.setOnItemClickListener(this);

        drawerLayout = DrawerLayoutInstaller.from(this)
                .drawerRoot(R.layout.drawer_root)
                .drawerLeftView(menuView)
                .drawerLeftWidth(Utils.dpToPx(300))
                .withNavigationIconToggler(getToolbar())
                .build();
    }

    public DevicesAdapter getDevices() {
        return mDeviceAdapter;
    }

    private void initNetHandler() {
        if (netHandler == null) {
            netHandler = new NetHandler();
        }
    }

    @Override
    public void onGlobalMenuHeaderClick(View v) {
        drawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i("gky",getClass().getSimpleName()+"::onItemClick position:"+position+" curActivity:"+getActivityBySuper().getTitle());
        drawerLayout.closeDrawer(Gravity.LEFT);
        if (position == 1
                && !getActivityBySuper().getTitle().equals(TvRemoteActivity.class.getSimpleName())) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getActivityBySuper(), TvRemoteActivity.class);
                    getActivityBySuper().startActivity(intent);
                    getActivityBySuper().overridePendingTransition(0, 0);
                    getActivityBySuper().finish();
                }
            },200);
        }else if (position == 2
                && !getActivityBySuper().getTitle().equals(TvDevicesActivity.class.getSimpleName())) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getActivityBySuper(), TvDevicesActivity.class);
                    getActivityBySuper().startActivity(intent);
                    getActivityBySuper().overridePendingTransition(0, 0);
                    getActivityBySuper().finish();
                }
            },200);
        }/*else if (position == 3
                && !getActivityBySuper().getTitle().equals(TvDevicesActivity.class.getSimpleName())) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getActivityBySuper(), TvDevicesActivity.class);
                    getActivityBySuper().startActivity(intent);
                    getActivityBySuper().overridePendingTransition(0, 0);
                    getActivityBySuper().finish();
                }
            },200);
        }*/else if(position == 4
                && !getActivityBySuper().getTitle().equals(AboutActivity.class.getSimpleName())) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getActivityBySuper(), AboutActivity.class);
                    getActivityBySuper().startActivity(intent);
                    getActivityBySuper().overridePendingTransition(0, 0);
                    getActivityBySuper().finish();
                }
            },200);
        }
    }

    private void initProgressDialog() {
        if (mDialog == null) {
            mDialog = new ProgressDialog(AppContext.getContext());
            mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setMessage("正在连接TV");
            mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NetUtils.getInstance().stopInitClient();
                            if (netHandler.hasMessages(ConfigConst.MSG_CONNECTION_TIME_OUT)) {
                                netHandler.removeMessages(ConfigConst.MSG_CONNECTION_TIME_OUT);
                            }
                        }
                    });
        }
    }

    public class NetHandler extends Handler {

        public NetHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            int msgWhat = msg.what & 0xFF;
            switch (msgWhat) {
                case ConfigConst.MSG_INIT_CONNECTION:
                    Toast.makeText(BaseActivity.this, "初始化完成，查找设备...",
                            Toast.LENGTH_SHORT).show();
                    break;
                case ConfigConst.MSG_FIND_OUT_DEVICE:
                    getDevices().addItem((DeviceInfo) msg.obj);
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.setMessage("已经发现："+getDevices().getItemCount()+"设备");
                    }
                    if (hasMessages(ConfigConst.MSG_CONNECTION_TIME_OUT)) {
                        removeMessages(ConfigConst.MSG_CONNECTION_TIME_OUT);
                    }
                    break;
                case ConfigConst.MSG_DISCONNECTION:
                    Toast.makeText(BaseActivity.this,"连接已经断开，App不可用。",
                            Toast.LENGTH_SHORT).show();
                    break;
                case ConfigConst.MSG_THERE_NO_CONNECTION_TO_DEVICE:
                    Toast.makeText(BaseActivity.this,"未连接到TV，App不可用。",
                            Toast.LENGTH_SHORT).show();
                    break;
                case ConfigConst.MSG_INVALIED_INPUT_TEXT:
                    Toast.makeText(BaseActivity.this,"输入无效，请重新输入！",
                            Toast.LENGTH_SHORT).show();
                    break;
                case ConfigConst.MSG_CONNECTION_TIME_OUT:
                    Toast.makeText(BaseActivity.this,"连接超时......",
                            Toast.LENGTH_SHORT).show();
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    NetUtils.getInstance().stopInitClient();
                    break;
                case ConfigConst.MSG_DEVICE_DISCONNECTION:
                    String ip = (String) msg.obj;
                    DeviceInfo deviceInfo = getDevices().removeItem(ip);
                    if (deviceInfo != null) {
                        Toast.makeText(BaseActivity.this,"设备："+deviceInfo.name+"["+deviceInfo.ip+"] 离线。",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ConfigConst.MSG_EXCEPTION:
                    Toast.makeText(BaseActivity.this,"出现异常，无法操作",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
