package com.project.zxt.ustauctionhouse.ItemListView;

/**
 * Created by Paul on 2015/4/22.
 * This is for pull refresh
 * Hehe
 */
import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.project.zxt.ustauctionhouse.R;

public class RefreshListView extends ListView implements OnScrollListener{
    //顶部View
    private View topView;
    //第一个可见View
    int firstVisibleItem;
    //顶部View的高度
    int headerHeight;
    // listview 当前滚动状态；
    int scrollState;
    // 标记，当前是在listview最顶端摁下的；
    boolean isRefresh;
    // 摁下时的Y值；
    int startY;
    //控制下拉的速度
    double speed = 0.5;
    //控制下拉的距离
    int distance = 50;

    int state;// 当前的状态；
    final int NONE = 0;// 正常状态；
    final int PULL = 1;// 提示下拉状态；
    final int RELESE = 2;// 提示释放状态；
    final int REFLASHING = 3;// 刷新状态；

    public RefreshListView(Context context) {
        super(context);
        initView(context);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);

    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    /**
     * 初始化ListView
     */
    private void initView(Context context){
        LayoutInflater mInflater = LayoutInflater.from(context);
        topView = mInflater.inflate(R.layout.top, null);

        measureView(topView);
        headerHeight = topView.getMeasuredHeight();
        //初始状态设置隐藏
        topPadding(-headerHeight);

        this.setOnScrollListener(this);
        this.addHeaderView(topView);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //当滑动到底端，并滑动状态为 not scrolling
        this.scrollState = scrollState;
    }

    /**
     * 通知父布局，占用的宽，高；
     *
     * @param view
     */
    private void measureView(View view) {
        ViewGroup.LayoutParams p = view.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int width = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int height;
        int tempHeight = p.height;
        if (tempHeight > 0) {
            height = MeasureSpec.makeMeasureSpec(tempHeight,
                    MeasureSpec.EXACTLY);
        } else {
            height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        view.measure(width, height);
    }

    /**
     * 设置header 布局 上边距；
     *
     * @param topPadding
     */
    private void topPadding(int topPadding) {
        topView.setPadding(topView.getPaddingLeft(), topPadding,
                topView.getPaddingRight(), topView.getPaddingBottom());
        topView.invalidate();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        this.firstVisibleItem = firstVisibleItem ;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        switch (ev.getAction()) {
            //往下
            case MotionEvent.ACTION_DOWN:
                if (firstVisibleItem == 0) {
                    isRefresh = true;
                    startY = (int) ev.getY();
                }
                break;
            //移动
            case MotionEvent.ACTION_MOVE:
                onMove(ev);
                break;
            //向上
            case MotionEvent.ACTION_UP:
                if (state == RELESE) {
                    state = REFLASHING;
                    // 加载最新数据；
                    reflashViewByState();
                    onRefreshListener.onRefresh();
                } else if (state == PULL) {
                    state = NONE;
                    isRefresh = false;
                    reflashViewByState();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 判断移动过程操作；
     *
     * @param ev
     */
    private void onMove(MotionEvent ev) {
        if (!isRefresh) {
            return;
        }
        int tempY = (int)(speed * ev.getY());
        int space = tempY - (int)(speed * startY);
        int topPadding = (space - headerHeight);
        switch (state) {
            case NONE:
                if (space > 0) {
                    state = PULL;
                    reflashViewByState();
                }
                break;
            case PULL:
                topPadding(topPadding);
                if (space > headerHeight + distance
                        && scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    state = RELESE;
                    reflashViewByState();
                }
                break;
            case RELESE:
                topPadding(topPadding);
                if (space < headerHeight + distance) {
                    state = PULL;
                    reflashViewByState();
                } else if (space <= 0) {
                    state = NONE;
                    isRefresh = false;
                    reflashViewByState();
                }
                break;
        }
    }

    /**
     * 根据当前状态，改变界面显示；
     */
    private void reflashViewByState() {
        TextView tip = (TextView) topView.findViewById(R.id.tip);
        ImageView arrow = (ImageView) topView.findViewById(R.id.arrow);
        ProgressBar progress = (ProgressBar) topView.findViewById(R.id.progress_refresh);
        RotateAnimation anim = new RotateAnimation(0, 180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(500);
        anim.setFillAfter(true);
        RotateAnimation anim1 = new RotateAnimation(180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        anim1.setDuration(500);
        anim1.setFillAfter(true);
        switch (state) {
            case NONE:
                arrow.clearAnimation();
                topPadding(-headerHeight);
                break;

            case PULL:
                arrow.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                tip.setText("Pull Down to Refresh!");
                arrow.clearAnimation();
                arrow.setAnimation(anim1);
                break;
            case RELESE:
                arrow.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                tip.setText("Release to refresh!");
                arrow.clearAnimation();
                arrow.setAnimation(anim);
                break;
            case REFLASHING:
                topPadding(50);
                arrow.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                tip.setText("Refreshing...");
                arrow.clearAnimation();
                break;
        }
    }

    private OnRefreshListener onRefreshListener;
    public void setOnRefreshListener(OnRefreshListener onRefreshListener){
        this.onRefreshListener = onRefreshListener;
    }

    /**
     * 加载数据接口
     * @author Administrator
     *
     */
    public interface OnRefreshListener{
        void onRefresh();
    }

    /**
     * 数据加载完成
     */
    @SuppressLint("SimpleDateFormat")
    public void refreshComplete(){
        state = NONE;
        isRefresh = false;
        reflashViewByState();
        TextView lastupdatetime = (TextView) topView
                .findViewById(R.id.lastupdate_time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String time = format.format(date);
        lastupdatetime.setText("last update: "+ time);
    }

}