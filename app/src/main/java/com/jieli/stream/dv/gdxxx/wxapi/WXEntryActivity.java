package com.jieli.stream.dv.gdxxx.wxapi;

import android.content.Intent;
import android.os.Bundle;

import com.jieli.stream.dv.gdxxx.ui.base.BaseActivity;
import com.jieli.stream.dv.gdxxx.util.Dbug;
import com.jieli.stream.dv.gdxxx.util.ManifestUtil;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXEntryActivity extends BaseActivity implements IWXAPIEventHandler {
	private String tag = getClass().getSimpleName();
	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
	
    private IWXAPI api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		api = WXAPIFactory.createWXAPI(this, ManifestUtil.getWeixinKey(this), false);
		api.handleIntent(getIntent(), this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Dbug.e(tag, "onNewIntent---");
		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		Dbug.e(tag, "onReq-----");
	}

	@Override
	public void onResp(BaseResp resp) {
		Dbug.e(tag, "onResp------");
		finish();
	}
}