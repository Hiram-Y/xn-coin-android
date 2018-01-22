package com.cdkj.bcoin.market;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;

import com.cdkj.baselibrary.appmanager.EventTags;
import com.cdkj.baselibrary.appmanager.MyConfig;
import com.cdkj.baselibrary.appmanager.SPUtilHelper;
import com.cdkj.baselibrary.base.BaseRefreshFragment;
import com.cdkj.baselibrary.model.EventBusModel;
import com.cdkj.baselibrary.nets.BaseResponseListCallBack;
import com.cdkj.baselibrary.nets.BaseResponseModelCallBack;
import com.cdkj.baselibrary.nets.RetrofitUtils;
import com.cdkj.baselibrary.utils.StringUtils;
import com.cdkj.bcoin.R;
import com.cdkj.bcoin.adapter.MarketAdapter;
import com.cdkj.bcoin.api.MyApi;
import com.cdkj.bcoin.databinding.FootMarketBinding;
import com.cdkj.bcoin.databinding.HeadMarketBinding;
import com.cdkj.bcoin.model.MarketCoinModel;
import com.cdkj.bcoin.model.MarketModel;
import com.cdkj.bcoin.util.StringUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import retrofit2.Call;


/**
 * Created by lei on 2017/8/21.
 */

public class MarketFragment extends BaseRefreshFragment<MarketModel> {

    private HeadMarketBinding mBinding;
    private FootMarketBinding mFootBinding;

    /**
     * 获得fragment实例
     *
     * @return
     */
    public static MarketFragment getInstance() {
        MarketFragment fragment = new MarketFragment();
        return fragment;
    }

    @Override
    protected boolean canLoadTopTitleView() {
        return true;
    }

    @Override
    protected void afterCreate(int pageIndex, int limit) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(mActivity), R.layout.head_market, null, false);
        mFootBinding = DataBindingUtil.inflate(LayoutInflater.from(mActivity), R.layout.foot_market, null, false);

        setTopTitle(StringUtil.getString(R.string.market_title));
        setTopTitleLine(true);

        mAdapter.setHeaderAndEmpty(true);
        mAdapter.addHeaderView(mBinding.getRoot());
        mAdapter.addFooterView(mFootBinding.getRoot());

        // 取消上啦加载
        setEnableLoadmore(false);

    }

    @Override
    protected void lazyLoad() {
        super.lazyLoad();

//        getCoin();
        getListData(1, 10, true);
    }

    @Override
    protected void getListData(int pageIndex, int limit, boolean canShowDialog) {
        Map<String, String> map = new HashMap<>();
        map.put("systemCode", MyConfig.SYSTEMCODE);
        map.put("companyCode", MyConfig.COMPANYCODE);

        Call call = RetrofitUtils.createApi(MyApi.class).getMarket("625293", StringUtils.getJsonToString(map));

        addCall(call);

        showLoadingDialog();

        call.enqueue(new BaseResponseListCallBack<MarketModel>(mActivity) {

            @Override
            protected void onSuccess(List<MarketModel> data, String SucMessage) {
                if (data == null)
                    return;
                setData(data);
            }

            @Override
            protected void onFinish() {
                disMissLoading();
            }
        });
    }

    @Override
    protected BaseQuickAdapter onCreateAdapter(List<MarketModel> mDataList) {
        return new MarketAdapter(mDataList);
    }

    @Override
    public String getEmptyInfo() {
        return StringUtil.getString(R.string.market_none);
    }

    @Override
    public int getEmptyImg() {
        return R.mipmap.order_none;
    }


    private void timer(){

        mSubscription.clear();
        mSubscription.add(Observable.timer(25, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {//延迟10秒进行跳转

                    getCoin();

                }, Throwable::printStackTrace));

    }

    private void getCoin() {
        Map<String, Object> map = new HashMap<>();
        map.put("coin", "ETH");
        map.put("systemCode", MyConfig.SYSTEMCODE);
        map.put("companyCode", MyConfig.COMPANYCODE);

        Call call = RetrofitUtils.createApi(MyApi.class).getTruePrice("625292", StringUtils.getJsonToString(map));

        addCall(call);

        call.enqueue(new BaseResponseModelCallBack<MarketCoinModel>(mActivity) {

            @Override
            protected void onSuccess(MarketCoinModel data, String SucMessage) {
                if (data == null)
                    return;

                SPUtilHelper.saveMarketCoin("ETH",data.getMid());

                // 轮询，10s一次
                timer();
                // 提醒发布页更新最新价格
                EventBusModel model = new EventBusModel();
                model.setTag(EventTags.COIN_PRICE_CHANGE);
                EventBus.getDefault().post(model);
            }

            @Override
            protected void onFinish() {
                disMissLoading();
            }
        });

    }

}
