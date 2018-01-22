package com.cdkj.bcoin.user;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.cdkj.baselibrary.activitys.PayPwdModifyActivity;
import com.cdkj.baselibrary.appmanager.SPUtilHelper;
import com.cdkj.baselibrary.base.AbsBaseActivity;
import com.cdkj.baselibrary.interfaces.BaseRefreshCallBack;
import com.cdkj.baselibrary.model.EventBusModel;
import com.cdkj.baselibrary.model.IsSuccessModes;
import com.cdkj.baselibrary.nets.BaseResponseModelCallBack;
import com.cdkj.baselibrary.nets.RetrofitUtils;
import com.cdkj.baselibrary.utils.RefreshHelper;
import com.cdkj.baselibrary.utils.StringUtils;
import com.cdkj.baselibrary.views.MyPickerPopupWindow;
import com.cdkj.bcoin.R;
import com.cdkj.bcoin.adapter.AddressAdapter;
import com.cdkj.bcoin.api.MyApi;
import com.cdkj.bcoin.databinding.FootUserAddressBinding;
import com.cdkj.bcoin.model.AddressModel;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

import static com.cdkj.baselibrary.appmanager.EventTags.ADDRESS_SELECT;

/**
 * Created by lei on 2017/11/1.
 */

public class UserAddressActivity extends AbsBaseActivity {

    public static String TYPE_WITHDRAW = "withdraw";
    private String openType = "";

    private FootUserAddressBinding footBinding;

    private RefreshHelper refreshHelper;

    private BaseRefreshCallBack back;

    private AddressAdapter adapter;

    private String type = "ETH";
    private String[] types = {"ETH"};
//    private String[] types = {"ETH","BTC"};

    public static void open(Context context, String openType){
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, UserAddressActivity.class).putExtra("openType", openType));
    }

    @Override
    public View addMainView() {
        footBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.foot_user_address, null, false);

        back = new BaseRefreshCallBack() {
            @Override
            public SmartRefreshLayout getRefreshLayout() {
                footBinding.refreshLayout.setEnableLoadmore(false);
                return footBinding.refreshLayout;
            }

            @Override
            public RecyclerView getRecyclerView() {
                return footBinding.rv;
            }

            @Override
            public BaseQuickAdapter getAdapter(List listData) {
                adapter = new AddressAdapter(listData);
                return adapter;
            }

            @Override
            public void getListDataRequest(int pageIndex, int limit, boolean isShowDialog) {

                List<String> statusList = new ArrayList<>();
                statusList.add("0");
                statusList.add("1");

                Map<String, Object> map = new HashMap<>();
                map.put("statusList", statusList);
                map.put("type", "Y");
                map.put("userId", SPUtilHelper.getUserId());
                map.put("start", pageIndex+"");
                map.put("limit", limit+"");

                Call call = RetrofitUtils.createApi(MyApi.class).getAddress("625205", StringUtils.getJsonToString(map));

                addCall(call);

                showLoadingDialog();

                call.enqueue(new BaseResponseModelCallBack<AddressModel>(UserAddressActivity.this) {

                    @Override
                    protected void onSuccess(AddressModel data, String SucMessage) {

                        if (data == null)
                            return;

                        refreshHelper.setData(data.getList(),getStrRes(R.string.user_address_none), R.mipmap.order_none);

                    }

                    @Override
                    protected void onFinish() {
                        disMissLoading();
                    }
                });

            }
        };

        refreshHelper = new RefreshHelper(this, back);

        return footBinding.getRoot();
    }

    @Override
    public void afterCreate(Bundle savedInstanceState) {
        setTopTitle(getStrRes(R.string.user_title_address));
//        setTopTitle(getStrRes(R.string.user_title_address)+"("+type+")");
//        setTopImgEnable(true);
        setTopLineState(true);
        setSubLeftImgState(true);

//        setTopTitleClickListener(this::initPopup);

        refreshHelper.init(10);
        // 刷新
        refreshHelper.onDefaluteMRefresh(true);

        adapter.setOnItemClickListener((adapter1, view, position) -> {

            AddressModel.ListBean model = adapter.getItem(position);

            if (openType != null && openType.equals(TYPE_WITHDRAW)){
                EventBusModel eventBusModel = new EventBusModel();
                eventBusModel.setTag(ADDRESS_SELECT);
                eventBusModel.setEvInt(Integer.parseInt(model.getStatus()));
                eventBusModel.setEvInfo(model.getAddress());
                EventBus.getDefault().post(eventBusModel);
                finish();
            }else {
                tip(model.getCode());
            }

        });


        init();
        initListener();

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHelper.onMRefresh(1,10,true);
    }

    private void init() {
        if (getIntent() == null)
            return;

        openType = getIntent().getStringExtra("openType");
    }

    private void initPopup(View view) {
        MyPickerPopupWindow popupWindow = new MyPickerPopupWindow(this, R.layout.popup_picker);
        popupWindow.setNumberPicker(R.id.np_type, types);

        popupWindow.setOnClickListener(R.id.tv_cancel,v -> {
            popupWindow.dismiss();
        });

        popupWindow.setOnClickListener(R.id.tv_confirm,v -> {
            type = popupWindow.getNumberPicker(R.id.np_type, types);

            setTopTitle(getStrRes(R.string.user_title_published)+"("+type+")");
            refreshHelper.onMRefresh(1,10,true);
            popupWindow.dismiss();
        });

        popupWindow.show(view);
    }

    private void initListener() {

//        mBinding.rlBtc.setOnClickListener(view -> {
//            initView();
//
//            mBinding.tvBtc.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
//            mBinding.vBtc.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
//
//            getListData(0,0,true);
//        });
//
//        mBinding.rlEth.setOnClickListener(view -> {
//            initView();
//
//            mBinding.tvEth.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
//            mBinding.vEth.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
//
//            getListData(0,0,false);
//        });

        footBinding.btnWithdraw.setOnClickListener(view -> {

            if (SPUtilHelper.getTradePwdFlag()){
                UserAddAddressActivity.open(this, type);
            }else {
                PayPwdModifyActivity.open(this,SPUtilHelper.getTradePwdFlag(),SPUtilHelper.getUserPhoneNum());
            }
        });

    }

//    private void initView() {
//        mBinding.tvBtc.setTextColor(ContextCompat.getColor(this, R.color.black));
//        mBinding.vBtc.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
//
//        mBinding.tvEth.setTextColor(ContextCompat.getColor(this, R.color.black));
//        mBinding.vEth.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
//    }

    private void tip(String code) {
        new AlertDialog.Builder(this).setTitle(getStrRes(R.string.attention))
                .setMessage(getStrRes(R.string.user_address_delete_confirm))
                .setPositiveButton(getStrRes(R.string.confirm), (dialogInterface, i) -> {
                    delete(code);

                }).setNegativeButton(getStrRes(R.string.cancel), null).show();
    }

    private void delete(String code){
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);

        Call call = RetrofitUtils.getBaseAPiService().successRequest("625202", StringUtils.getJsonToString(map));

        addCall(call);

        showLoadingDialog();

        call.enqueue(new BaseResponseModelCallBack<IsSuccessModes>(UserAddressActivity.this) {

            @Override
            protected void onSuccess(IsSuccessModes data, String SucMessage) {

                if (data == null)
                    return;

                if (data.isSuccess()){
                    showToast(getStrRes(R.string.user_address_delete_success));
                    refreshHelper.onMRefresh(1,10,true);
                }


            }

            @Override
            protected void onFinish() {
                disMissLoading();
            }
        });
    }
}
