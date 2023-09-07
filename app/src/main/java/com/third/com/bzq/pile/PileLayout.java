package com.third.com.bzq.pile;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.common.tools.VLog;
import com.common.tools.StringUtil;
import com.demo.my.androiddemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 循环-轮播图
 * <p>
 * 作者：贲志强
 * 时间：2023-09
 */

public class PileLayout extends ViewGroup {

    /*
    * 整体架构：
    *
    * 总共使用4个FrameLayout：第一个在最左面（屏幕左侧，处于隐藏状态），第二个和第一个处于同一层（顶层），且在第一个右侧，同时也是主图。第三个和第四个依此在第二个的右侧，并且层级（Z轴）依此降低
    * 手势左滑的时候，第一个放到FrameLayout队列最后面。手势右滑的时候，第四个放到FrameLayout队列的最上面。
    *
    * PS：针对右滑另一种实现思路是新增第二个层的辅助展示层，通过控制其展示状态，也可起到展示效果。可以避免目前通过层的来回移动，来实现中间态的展示效果。
    *
    * */

    //全局常量
    private static final int FOCUS_DISPLAY_COUNT = 3;//轮播图实际展示的个数
    private static final int FIRST_FRAME_SHOW_INDEX = 1;//因为索引是0的是最左边，属于不展示状态，所以第一个展示的是索引是1的

    //全局变量
    private boolean isRightScrolling = false;//是否向右滑动中
    private boolean isForceRightScroll = false;//是否强制右滑

    //属性参数
    private int interval = 30; //View的间隔
    private float sizeRatio = 0.61f;//图的长宽比

    //布局与图片相关
    private List<Integer> originX = new ArrayList<>(); //存放的是最初的n个View的位置
    private int everyWidth;
    private int everyHeight;

    //定义适配器，监听事件
    private OnClickListener onClickListener;//点击监听
    private Adapter adapter;//适配器
    private boolean hasSetAdapter = false;//是否设置适配器

    //拖拽相关
    private int scrollMode;
    private static final int MODE_IDLE = 0;//模式：空闲
    private static final int MODE_HORIZONTAL = 1;//模式：水平
    private static final int MODE_VERTICAL = 2;//模式：垂直
    private float downX, downY;//手指按下时的XY的坐标
    private float lastX;//最后一次x的坐标
    private int diff_X = 0;//水平滑动的距离

    //**************************************** 构造函数 ****************************************//

    public PileLayout(Context context) {
        this(context, null);
    }

    public PileLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PileLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);//初始化数据

        setListener();//定义监听事件
        initAdapter();//初始化适配器
    }

    //**************************************** 常规处理 ****************************************//

    //初始化
    private void init(Context context, AttributeSet attrs) {

        //获取属性值
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.pile);
        interval = (int) a.getDimension(R.styleable.pile_interval, interval);
        sizeRatio = a.getFloat(R.styleable.pile_sizeRatio, sizeRatio);
        a.recycle();

    }

    //定义监听
    private void setListener() {

        onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter != null) {

                    int position = StringUtil.str2Int(v.getTag().toString());
                    int dataIndex = getDataIndex(position);//取得数据库对应的索引
                    VLog.d("点击的索引数据", "轮播图索引：" + StringUtil.intToStr(position) + "，数据库索引：" + StringUtil.intToStr(dataIndex));

                    if (dataIndex >= 0) {
                        adapter.onItemClick(((FrameLayout) v).getChildAt(0), dataIndex);
                    }
                }
            }
        };

    }

    //初始化适配器
    private void initAdapter() {

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (getHeight() > 0 && adapter != null && !hasSetAdapter) {
                    setAdapter(adapter);
                }
            }
        });

    }

    //测量视图，进行前期计算
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);//屏幕宽度
        everyWidth = (width - getPaddingLeft() - getPaddingRight() - interval * FOCUS_DISPLAY_COUNT);//图的宽
        everyHeight = (int) (everyWidth * sizeRatio);//图的高
        setMeasuredDimension(width, everyHeight);

        //把每个View的初始位置坐标都计算好（不参与实际绘制）
        if (originX.size() == 0) {

            //1最左边的（隐藏）
            int position0 = 0 - everyWidth - interval;
            originX.add(position0);

            //2主图
            int position1 = 0;
            originX.add(position1);

            //3第二个显示图
            int position2 = position1 + interval + 100;
            originX.add(position2);

            //4第三个显示图
            int position3 = position2 + interval + 100;
            originX.add(position3);

        }
    }

    //绘制布局
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if (isRightScrolling) {//当前正在滑动中，无需处理
            return;
        }

        int num = getChildCount();

        for (int i = 0; i < num; i++) {
            View itemView = getChildAt(i);
            int left = originX.get(i);
            int top = (getMeasuredHeight() - everyHeight) / 2;
            int right = left + everyWidth;
            int bottom = top + everyHeight;

            itemView.layout(left, top, right, bottom);

            if (i == 0 || i == 1) {//前2个在同一层
                itemView.setZ(num - 1);//恒定是3
            } else {
                itemView.setZ(num - i);//2,1,0...
            }

            adjustScale(itemView, i);
            adjustAlpha(itemView, i);
        }
    }

    //透明度
    private void adjustAlpha(View itemView, int position) {
        if (position == 2) {
            itemView.setAlpha(0.6f);
        } else if (position == 3) {
            itemView.setAlpha(0.3f);
        } else {
            itemView.setAlpha(1f);
        }
    }

    //缩放
    private void adjustScale(View itemView, int position) {
        if (position == 2) {
            itemView.setScaleX(0.85f);
            itemView.setScaleY(0.85f);
        } else if (position == 3) {
            itemView.setScaleX(0.7f);
            itemView.setScaleY(0.7f);
        } else {
            itemView.setScaleX(1f);
            itemView.setScaleY(1f);
        }
    }

    //**************************************** 适配器与数据 ****************************************//

    //设置Adapter
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;

        if (everyWidth > 0 && everyHeight > 0) {//视图渲染后，才能做适配
            doBindAdapter();
        }
    }

    //绑定Adapter：对视图进行装载
    private void doBindAdapter() {
        if (adapter == null) {
            return;
        }

        hasSetAdapter = true;

        //初始化
        if (getChildCount() == 0) {
            LayoutInflater inflater = LayoutInflater.from(getContext());

            int focus_display_all_count = FOCUS_DISPLAY_COUNT + 1;//轮播图全部展示的个数（含最左侧的隐藏位）

            for (int i = 0; i < focus_display_all_count; i++) {
                FrameLayout frameLayout = new FrameLayout(getContext());
                View view = inflater.inflate(adapter.getLayoutId(), null);

                FrameLayout.LayoutParams lp1 = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                lp1.width = everyWidth;
                lp1.height = everyHeight;
                frameLayout.addView(view, lp1);

                LayoutParams lp2 = new LayoutParams(everyWidth, everyHeight);
                lp2.width = everyWidth;
                lp2.height = everyHeight;
                frameLayout.setLayoutParams(lp2);
                frameLayout.setOnClickListener(onClickListener);

                addView(frameLayout);

                frameLayout.setTag(i - FIRST_FRAME_SHOW_INDEX); //对应于在dataList中的数据index。这样可以和数据集索引匹配
                frameLayout.measure(everyWidth, everyHeight);
            }
        }

        //绑定视图:绑定数据，或者其他操作（显示，隐藏等）
        int num = getChildCount();
        for (int i = 0; i < num; i++) {
            if (i == 0) {//第一个是隐藏状态，无需处理
                continue;
            }

            FrameLayout frameLayout = (FrameLayout) getChildAt(i);

            int curPoistion = StringUtil.str2Int(frameLayout.getTag().toString());
            int dataIndex = getDataIndex(curPoistion);

            adapter.bindView(frameLayout.getChildAt(0), dataIndex);//绑定视图
        }

        if (adapter.getItemCount() > 0) {
            adapter.displaying(0);
        }
    }

    //数据更新通知
    public void notifyDataSetChanged() {
        int num = getChildCount();

        for (int i = 0; i < num; i++) {
            FrameLayout frameLayout = (FrameLayout) getChildAt(i);

            int position = StringUtil.str2Int(frameLayout.getTag().toString());
            int dataIndex = getDataIndex(position);//取得数据库对应的索引
            VLog.d("绑定的索引数据", "轮播图索引：" + StringUtil.intToStr(position) + "，数据库索引：" + StringUtil.intToStr(dataIndex));

            adapter.bindView(frameLayout.getChildAt(0), dataIndex);//绑定视图
        }
    }

    //通过positon转换为数据表对应的索引
    private int getDataIndex(int position) {

        if (position >= 0) {//正数
            if (position < adapter.getItemCount()) {//个数小于数据集
                return position;
            } else {
                return position % adapter.getItemCount();//超过索引，寻找下一轮的索引(求余数)
            }
        } else {//负数
            int positionNew = Math.abs(position);//提取绝对值

            if (positionNew < adapter.getItemCount()) {
                return adapter.getItemCount() - positionNew;//因为本身是负数，所以需要倒着查找
            } else {//负数且超过索引，寻找下一轮的索引(求余数)
                int nextIndx = positionNew % adapter.getItemCount();
                return adapter.getItemCount() - 1 - nextIndx;//负数需要相减
            }
        }

    }

    //适配器的抽象类
    public static abstract class Adapter {

        //Layout的ID
        public abstract int getLayoutId();

        //Item数量
        public abstract int getItemCount();

        //绑定视图
        public void bindView(View view, int index) {
        }

        //点击事件
        public void onItemClick(View view, int position) {
        }

        //显示中
        public void displaying(int position) {
        }

    }

    //**************************************** 点击事件 ****************************************//

    //用于进行点击事件的拦截
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:

                setParentScrollAble(false);

                scrollMode = MODE_IDLE;//空闲模式
                isRightScrolling = false;

                downX = (int) event.getX();
                downY = (int) event.getY();
                lastX = event.getX();

                break;
            case MotionEvent.ACTION_MOVE:
                if (scrollMode == MODE_IDLE) {
                    float xDistance = Math.abs(downX - event.getX());
                    float yDistance = Math.abs(downY - event.getY());

                    if (xDistance > yDistance) {//x的绝对值大于y，认定为水平滑动
                        scrollMode = MODE_HORIZONTAL;//水平滑动，需要拦截，不会再触发其他事件。拦截后走下面的onTouchEvent对应的方法
                        return true;
                    } else if (yDistance > xDistance) {
                        scrollMode = MODE_VERTICAL;//垂直滑动
                        setParentScrollAble(true);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                setParentScrollAble(true);// 当手指松开时，让父控件重新获取onTouch权限

                //ACTION_UP还能拦截（如果是水平滑动，抬起后不会走到这里），说明：
                //1.手指【没有水平】滑动
                //2.是一个click事件

                break;
        }

        return false;//默认不拦截的。不会走下面的onTouchEvent对应的方法
    }

    //设置父控件是否可以获取到触摸处理权限
    private void setParentScrollAble(boolean flag) {
        //请求不要拦截
        getParent().requestDisallowInterceptTouchEvent(!flag);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    //用于处理点击事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                int currentX = (int) event.getX();
                diff_X = (int) (currentX - lastX);

                handleScrollChangeIng(diff_X);//手指拖动过程中的处理

                lastX = currentX;

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                if (isRightScrolling) {//处理最终状态前，先复位
                    isRightScrolling = false;
                    recoverRightScrollStatus();
                }

                handleScrollChangeEnd(diff_X);

                break;
        }

        return true;
    }

    //**************************************** 滑动事件 ****************************************//

    //手指拖动过程中的处理
    private void handleScrollChangeIng(int diffX) {
        if (diffX == 0) {
            return;
        }

        int num = getChildCount();

        if (diffX > 0 && getChildAt(1).getLeft() >= 0) {//向右的滑动，先执行此逻辑，然后走下面的整体移动

            if (!isRightScrolling) {

                isRightScrolling = true;

                //取得当前第一个的索引值
                int curFirstIndex = StringUtil.str2Int(getChildAt(0).getTag().toString());

                //向右滑动，从左边把View补上
                FrameLayout lastView = (FrameLayout) getChildAt(FOCUS_DISPLAY_COUNT);
                LayoutParams lp = lastView.getLayoutParams();
                removeViewInLayout(lastView);
                lastView.setTag(curFirstIndex - 1);//设置position值
                addViewInLayout(lastView, 0, lp);

                notifyDataSetChanged();//更新适配器

                //位置调整
                for (int i = 0; i < num; i++) {
                    FrameLayout rowView = (FrameLayout) getChildAt(i);
                    if (i == 0 || i == 1) {
                        int newLeftX = originX.get(i) - everyWidth - interval;
                        rowView.setLeft(newLeftX);
                        rowView.setRight(newLeftX + everyWidth);
                        rowView.setZ(num - 1);
                    } else {
                        int newLeftX1 = originX.get(i);
                        rowView.setLeft(newLeftX1);
                        rowView.setRight(newLeftX1 + everyWidth);
                        rowView.setZ(num - i);
                    }
                }
            }
        }

        //整体移动：支持左右滑动
        for (int i = 0; i < num; i++) {
            View itemView = getChildAt(i);

            if (i == 0 || i == 1) {//主图
                itemView.offsetLeftAndRight(diffX);
            } else {
                itemView.offsetLeftAndRight(diffX / 10);
            }

            adjustAlpha(itemView, i); //调整透明度
            adjustScale(itemView, i); //调整缩放
        }
    }

    //恢复右滑产生的数据变化
    private void recoverRightScrollStatus() {

        //恢复之前，先判断滑动距离
        FrameLayout firstView = (FrameLayout) getChildAt(0);
        FrameLayout SecondView = (FrameLayout) getChildAt(1);

        if (Math.abs(SecondView.getRight()) > firstView.getWidth() / 2) {
            isForceRightScroll = true;
        } else {
            isForceRightScroll = false;
        }

        //取得当前最后一个的索引值
        int curLastIndex = StringUtil.str2Int(getChildAt(FOCUS_DISPLAY_COUNT).getTag().toString());

        //向左滑动，从右边把View补上
        LayoutParams lp = firstView.getLayoutParams();
        removeViewInLayout(firstView);
        firstView.setTag(curLastIndex + 1);//添加后，索引增1
        addViewInLayout(firstView, -1, lp);
    }

    //手指松开后的处理
    private void handleScrollChangeEnd(int diffX) {

        FrameLayout firstView = (FrameLayout) getChildAt(0);
        FrameLayout curView = (FrameLayout) getChildAt(1);//主图
        FrameLayout lastView = (FrameLayout) getChildAt(FOCUS_DISPLAY_COUNT);

        boolean isOffset = Math.abs(curView.getLeft()) > (firstView.getWidth() / 2);//偏移量超过图片的一半

        if (diffX > 0 && (isOffset || isForceRightScroll)) {//向右的滑动

            //取得当前第一个的索引值
            int curFirstIndex = StringUtil.str2Int(getChildAt(0).getTag().toString());

            //向右滑动，从左边把View补上
            LayoutParams lp = lastView.getLayoutParams();
            removeViewInLayout(lastView);
            lastView.setTag(curFirstIndex - 1);//添加后，索引减1
            addViewInLayout(lastView, 0, lp);

            notifyDataSetChanged();//更新适配器

        } else if (diffX < 0 && isOffset) {//向左的滑动：偏移量超过图片的一半

            //取得当前最后一个的索引值
            int curLastIndex = StringUtil.str2Int(getChildAt(FOCUS_DISPLAY_COUNT).getTag().toString());

            //向左滑动，从右边把View补上
            LayoutParams lp = firstView.getLayoutParams();
            removeViewInLayout(firstView);
            firstView.setTag(curLastIndex + 1);//添加后，索引增1
            addViewInLayout(firstView, -1, lp);

            notifyDataSetChanged();//更新适配器
        }

        int num = getChildCount();

        //位置初始化
        for (int i = 0; i < num; i++) {
            FrameLayout rowView = (FrameLayout) getChildAt(i);
            rowView.setLeft(originX.get(i));
            rowView.setRight(originX.get(i) + everyWidth);

            if (i == 0 || i == 1) {
                rowView.setZ(num - 1);
            } else {
                rowView.setZ(num - i);
            }

            adjustAlpha(rowView, i); //调整透明度
            adjustScale(rowView, i); //调整缩放
        }

    }

}