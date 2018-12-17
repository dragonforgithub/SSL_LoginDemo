package com.lxl.login.demo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginDialogFragment extends DialogFragment implements View.OnFocusChangeListener,
		 View.OnClickListener,CompoundButton.OnCheckedChangeListener{
	@BindView(R.id.et_phone)
	EditText mEtPhone;
	@BindView(R.id.et_pwd)
	EditText et_pwd;
	@BindView(R.id.btn_login)
	Button mBtnLogin;
	@BindView(R.id.tv_login)
	TextView tv_login;
	@BindView(R.id.btn_close)
	ImageButton btn_close;
	@BindView(R.id.iv_display)
	CheckBox iv_display;
	@BindView(R.id.loading)
	ProgressBar loading;

	private String phone;

	public static LoginDialogFragment newInstance(String phone) {
		LoginDialogFragment mFragment = new LoginDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString("phone", phone);
		mFragment.setArguments(bundle);
		return mFragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.login_layout, null);
		ButterKnife.bind(this, view);
		initView();
		builder.setView(view);
		return builder.create();
	}

	private void initView(){
		phone = getArguments().getString("phone");
		mEtPhone.setText(phone);
		loading.setVisibility(View.GONE);
		//mEtPhone.setOnFocusChangeListener(this);
		mEtPhone.addTextChangedListener(phoneNum_adapter);
		et_pwd.addTextChangedListener(pwd_adapter);
		mBtnLogin.setOnClickListener(this);
		tv_login.setOnClickListener(this);
		btn_close.setOnClickListener(this);
		iv_display.setOnCheckedChangeListener(this);
		loading.setVisibility(View.GONE);
	}

	@Override
	public void onFocusChange(View view, boolean b) {
		/** unused!!! */
		//PhoneDialogFragment dialog = new PhoneDialogFragment();
		//dialog.show(getFragmentManager(), "PhoneLogin");
		//dismiss();
	}

	// 手机号输入框
	TextWatcherAdapter phoneNum_adapter = new TextWatcherAdapter() {
		@Override
		public void afterTextChanged(Editable s) {
			if (s.length() == 11 && s.toString().matches("^1[3456789]\\d{9}$")) {
				mBtnLogin.setEnabled(true);
			} else{
				mBtnLogin.setEnabled(false);
			}
		}
	};

	//密码输入框
	TextWatcherAdapter pwd_adapter = new TextWatcherAdapter() {
		@Override
		public void afterTextChanged(Editable s) {
			int len = s.length();
			if (len >= 6 && len <= 16 ){
				mBtnLogin.setEnabled(true);
			} else{
				mBtnLogin.setEnabled(false);
			}
		}
	};

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.btn_login:
				//假装在登录...
				//btn_close.setEnabled(false);
				mEtPhone.setEnabled(false);
				et_pwd.setEnabled(false);
				mBtnLogin.setEnabled(false);
				iv_display.setEnabled(false);
				tv_login.setEnabled(false);
				loading.setVisibility(View.VISIBLE);

				break;
			case R.id.tv_login:
				VcodeDialogFragment dialogFragment = VcodeDialogFragment.newInstance(phone);
				dialogFragment.show(getFragmentManager(), "VcodeDialogFragment");
				dismiss();
				break;
			case R.id.btn_close:
				dismiss();
				break;

		}
	}

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
		if (b){
			et_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
		}else {
			et_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
		}
	}
}
