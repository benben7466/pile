package com.demo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.common.tools.StringUtil;
import com.demo.bean.HomeFocusDetailBean;
import com.demo.my.androiddemo.R;
import com.third.com.bzq.pile.PileLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PileLayout pileLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        pileLayout = findViewById(R.id.pileLayout);

        init();
    }


    private void init() {
        final List<HomeFocusDetailBean> list_focus = getData();

        pileLayout.setAdapter(new PileLayout.Adapter() {
            @Override
            public int getLayoutId() {
                return R.layout.home_focus_2023_08_item;
            }

            @Override
            public void bindView(View view, int position) {
                ImageView imageView = view.findViewById(R.id.imageView);
                Glide.with(MainActivity.this).load(list_focus.get(position).getPic_url()).into(imageView);
            }

            @Override
            public int getItemCount() {
                return list_focus.size();
            }

            @Override
            public void displaying(int position) {
            }

            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(getApplicationContext(), StringUtil.intToStr(position), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private List<HomeFocusDetailBean> getData() {

        List<HomeFocusDetailBean> list_focus = new ArrayList<>();//因为控件不支持视图的循环调用，这里需要复制多份数据

        HomeFocusDetailBean homeFocusDetailBean = new HomeFocusDetailBean();
        homeFocusDetailBean.setPic_url("https://s2.chunboimg.com/group1/M01/37/98/Cv4JrmT4kTCAR02NAAIwulbN2tw378_640_390.jpg");
        list_focus.add(homeFocusDetailBean);

        homeFocusDetailBean = new HomeFocusDetailBean();
        homeFocusDetailBean.setPic_url("https://s2.chunboimg.com/group1/M01/37/86/Cv4JrWT1-U2AebXVAAHc2T_CgQI810_640_390.jpg");
        list_focus.add(homeFocusDetailBean);

        homeFocusDetailBean = new HomeFocusDetailBean();
        homeFocusDetailBean.setPic_url("https://s2.chunboimg.com/group1/M01/36/78/Cv4JrmTbMXeAEeasAAIYqz4koKI917_640_390.jpg");
        list_focus.add(homeFocusDetailBean);

        return list_focus;
    }
}
