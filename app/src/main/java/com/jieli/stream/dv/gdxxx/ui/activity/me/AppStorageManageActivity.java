package com.jieli.stream.dv.gdxxx.ui.activity.me;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.bean.SettingItem;
import com.jieli.stream.dv.gdxxx.ui.adapter.SettingAdapter;
import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.ui.dialog.BrowseFileDialog;
import com.jieli.stream.dv.gdxxx.ui.dialog.NotifyDialog;
import com.jieli.stream.dv.gdxxx.util.AppUtils;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.PreferencesHelper;
import com.jieli.stream.dv.gdxxx.util.ThumbLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 陈森华 on 2017/7/18.
 * 功能：用一句话描述
 */

public class AppStorageManageActivity extends BaseActivity implements BrowseFileDialog.OnSelectResultListener,
        AdapterView.OnItemClickListener {
    private String tag = getClass().getSimpleName();
    private PieChart mChart;
    private NotifyDialog cleanCacheDialog;
    private SettingItem cacheSizeItem;
    private SettingItem storagePathItem;
    private ListView settingListView;
    private SettingAdapter mAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, AppStorageManageActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams windowParams = getWindow().getAttributes();
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        windowParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(windowParams);
        setContentView(R.layout.activity_app_storage_manage);
        mChart = (PieChart) findViewById(R.id.app_pie_chart);
        TextView appStorageTextView = (TextView) findViewById(R.id.app_storage_tv);
        appStorageTextView.setText(AppUtils.getExternalMemorySize(getApplicationContext()));
        settingListView = (ListView) findViewById(R.id.app_storage_view);
        settingListView.setOnItemClickListener(this);

        initListView();
        initChart();
        setData(AppUtils.getAvailableExternalMemorySize(), AppUtils.getExternalMemorySize() - AppUtils.getAvailableExternalMemorySize());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissClearCacheDialog();
    }

    private void initChart() {
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(0, 10, 0, 0);
        mChart.setDragDecelerationFrictionCoef(0.95f);
        mChart.setDrawHoleEnabled(false);
        mChart.setRotationAngle(-90);
        mChart.setRotationEnabled(false);
        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        mChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
    }

    private void setData(float rest, float used) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        PieEntry restPieEntry = new PieEntry(rest, getString(R.string.remaining_storage));
        PieEntry usedPieEntry = new PieEntry(used, getString(R.string.used_storage));
        entries.add(restPieEntry);
        entries.add(usedPieEntry);
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSelectionShift(0f);
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.bg_pie_chart_rest));
        colors.add(getResources().getColor(R.color.bg_pie_chart_used));
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new IValueFormatter() {

            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return Formatter.formatFileSize(getApplicationContext(), (long) v);
            }
        });
        data.setValueTextSize(16);
        data.setValueTextColor(getResources().getColor(R.color.text_white));
        mChart.setEntryLabelTextSize(0);
        mChart.setData(data);
        mChart.highlightValues(null);
        mChart.invalidate();
    }

    public void returnBtnClick(View v) {
        onBackPressed();
    }

    @Override
    public void onResult(String path) {
        if (!TextUtils.isEmpty(path)) {
            String newPathName = path.substring(ROOT_PATH.length());
            if (newPathName.startsWith(File.separator)) {
                newPathName = newPathName.substring(newPathName.indexOf(File.separator) + 1);
            }
            Dbug.i(tag, " ============= newPathName : " + newPathName + "================");
            if (!newPathName.equals(mApplication.getAppName())) {
                PreferencesHelper.putStringValue(getApplicationContext(), KEY_ROOT_PATH_NAME, newPathName);
                mApplication.setAppName(newPathName);
                if (storagePathItem != null) {
                    storagePathItem.setValue(mApplication.getAppFilePath());
                    mAdapter.notifyDataSetChanged();
                }
                showToastLong(R.string.modify_storage_url_success);
            }
        }
    }

    /**
     * 初始化列表项
     */
    private void initListView() {
        String[] items = getResources().getStringArray(R.array.storage_operation);
        List<SettingItem> dataList = new ArrayList<>();
        for (String name : items) {
            SettingItem item = new SettingItem<>();
            item.setName(name);
            String content = "";
            if (name.equals(getString(R.string.storage_size))) {
                content = AppUtils.getFormatSize(DEFAULT_CACHE_SIZE);
                item.setType(2);
            } else if (name.equals(getString(R.string.storage_path))) {
                content = mApplication.getAppFilePath();
                storagePathItem = item;
            } else if (name.equals(getString(R.string.clean_cache))) {
                content = getCache();
                cacheSizeItem = item;
            }
            item.setValue(content);
            dataList.add(item);
        }
        mAdapter = new SettingAdapter(this, dataList);
        settingListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 获得应用的缓存大小
     */
    private String getCache() {
        long totalSize = 0;
        String rootPath = AppUtils.splicingFilePath(mApplication.getAppFilePath(), null, null, null);
        List<String> pathList = AppUtils.queryThumbDirPath(rootPath);
        if (pathList != null && pathList.size() > 0) {
            for (String thumbPath : pathList) {
                File file = new File(thumbPath);
                if (file.exists()) {
                    try {
                        totalSize += AppUtils.getFolderSize(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return AppUtils.getFormatSize(totalSize);
    }

    /**
     * 清除应用的缓存
     */
    private void clearCache() {
        String rootPath = AppUtils.splicingFilePath(mApplication.getAppFilePath(), null, null, null);
        List<String> pathList = AppUtils.queryThumbDirPath(rootPath);
        if (pathList != null && pathList.size() > 0) {
            for (String thumbPath : pathList) {
                File file = new File(thumbPath);
                if (file.exists()) {
                    AppUtils.deleteFile(file);
                }
            }
            ThumbLoader.getInstance().clearCache();
        }
    }

    private BrowseFileDialog browseFileDialog;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mAdapter != null) {
            SettingItem<String> item = (SettingItem<String>) mAdapter.getItem(i);
            if (item != null) {
                String name = item.getName();
                if (name.equals(getString(R.string.storage_size))) {

                } else if (name.equals(getString(R.string.storage_path))) {
                    browseFileDialog = new BrowseFileDialog();
                    browseFileDialog.setOnSelectResultListener(AppStorageManageActivity.this);
                    browseFileDialog.show(getSupportFragmentManager(), "browse_file_dialog");
                } else if (name.equals(getString(R.string.clean_cache))) {
                    showClearCacheDialog();
                }
            }
        }
    }


    /**
     * 显示清除缓存提示框
     */
    private void showClearCacheDialog() {
        if (cleanCacheDialog == null) {
            cleanCacheDialog = NotifyDialog.newInstance(R.string.dialog_tips, R.string.clean_cache_content,
                    R.string.dialog_cancel, R.string.dialog_confirm,
                    new NotifyDialog.OnNegativeClickListener() {
                        @Override
                        public void onClick() {
                            cleanCacheDialog.dismiss();
                        }
                    }, new NotifyDialog.OnPositiveClickListener() {
                        @Override
                        public void onClick() {
                            cleanCacheDialog.dismiss();
                            clearCache();
                            cacheSizeItem.setValue(getCache());
                            mAdapter.notifyDataSetChanged();
                        }
                    });
        }
        if (!cleanCacheDialog.isShowing()) {
            cleanCacheDialog.show(getSupportFragmentManager(), "clean_cache");
        }
    }

    /**
     * 销毁清除缓存提示框
     */
    private void dismissClearCacheDialog() {
        if (cleanCacheDialog != null) {
            if (cleanCacheDialog.isShowing()) {
                cleanCacheDialog.dismiss();
            }
            cleanCacheDialog = null;
        }
    }
}
