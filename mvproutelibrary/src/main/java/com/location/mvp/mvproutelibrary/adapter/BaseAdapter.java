package com.location.mvp.mvproutelibrary.adapter;

import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 项目名称: MvpRoute
 * 类描述:  Recycler的基础适配器
 * 创建人: 田晓龙
 * 创建时间: 2018/5/25 0025 23:26
 * 修改人:
 * 修改内容:
 * 修改时间:
 */


public abstract class BaseAdapter<T> extends RecyclerView.Adapter<ViewHolder> {
	//当数据没有时显示即使已经添加了头布局尾布局
	public static final int ERMTY_MODLE_HEADERS = 0x001;
	//当没有数据 并且头尾布局都没有的情况下显示
	public static final int EMPTY_MODLE_NOMAL = 0x002;
	//存储数据
	protected List<T> data;
	//存储空View
	private View emptyView;


	private int emptyModle = ERMTY_MODLE_HEADERS;
	//存储头布局
	private ArrayList<DataBean> headerList = new ArrayList<>();
	//存储尾布局
	private ArrayList<DataBean> footerList = new ArrayList<>();

	//标记type类型
	public static final int TYPE_EMPTY = -999999999;
	private final int TYPE_NOMAL = 0;

	/**
	 * 存储子View的点击事件
	 */
	private SparseArray<OnChildListener> listenerSparseArray;


	private boolean isDrawHeaderFooterLine = true;

	public void setDrawHeaderFooterLine(boolean isDrawHeaderFooterLine) {
		this.isDrawHeaderFooterLine = isDrawHeaderFooterLine;
	}

	public boolean isDrawHeaderFooterLine() {
		return isDrawHeaderFooterLine;
	}

	//布局数组
	protected @LayoutRes
	int[] layouts;

	public BaseAdapter(int[] layouts) {
		this.layouts = layouts;
		data = new ArrayList<>();
	}


	public BaseAdapter(Collection<T> data, int[] layouts) {
		this.data = new ArrayList<>(data);
		this.layouts = layouts;
	}

	public BaseAdapter(int layout) {
		this.layouts = new int[]{layout};
		data = new ArrayList<>();
	}

	public BaseAdapter(Collection<T> data, int layout) {
		this.data = new ArrayList<>(data);
		this.layouts = new int[]{layout};
	}


	public BaseAdapter(Collection<T> data, int[] layouts, AbsListView.OnItemClickListener listener) {
		this.data = new ArrayList<>(data);
		this.layouts = layouts;
		this.listener = listener;
	}

	public BaseAdapter(Collection<T> data, int layout, AbsListView.OnItemClickListener listener) {
		this.data = new ArrayList<>(data);
		this.layouts = new int[]{layout};
		this.listener = listener;
	}

	protected AbsListView.OnItemClickListener listener;

	/**
	 * view的点击事件
	 *
	 * @param listener
	 */
	public void setOnItemListener(AbsListView.OnItemClickListener listener) {
		this.listener = listener;
	}

	/**
	 * 添加字view的点击事件
	 *
	 * @param ids      子view的id
	 * @param listener 点击回调事件
	 */

	public void setOnChildClickListener(@IdRes int ids, @NonNull OnChildListener listener) {
		if (listenerSparseArray == null) {
			listenerSparseArray = new SparseArray<>();
		}
		listenerSparseArray.put(ids, listener);
	}

	public void setData(Collection<T> data) {
		this.data = new ArrayList<>(data);
	}


	/**
	 * @param parent
	 * @param viewType
	 * @return
	 * @see ViewHolder
	 */
	@Override
	public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = null;
		if (isHeaderType(viewType) != -1) {
			view = LayoutInflater.from(parent.getContext()).inflate(isHeaderType(viewType), parent, false);
			return new ViewHolder(view);
		}

		if (isFooterType(viewType) != -1) {
			view = LayoutInflater.from(parent.getContext()).inflate(isHeaderType(viewType),
					parent, false);
			return new ViewHolder(view);
		}
		if (viewType == TYPE_EMPTY) {
			return new ViewHolder(emptyView);
		}
		if (layouts.length > 1) {
			view = LayoutInflater.from(parent.getContext()).inflate(layouts[viewType], parent, false);
		} else {
			view = LayoutInflater.from(parent.getContext()).inflate(layouts[0], parent, false);
		}
		ViewHolder holder = new ViewHolder(view, listener, listenerSparseArray, getHeaderCount());
		return holder;
	}


	/**
	 * @param holder
	 * @param position
	 * @see #conver(ViewHolder, Object, int) {@link #onBindFooterViewHolder(ViewHolder, Object, int)}
	 * {@link #onBindHeaderViewHolder(ViewHolder, Object, int)}
	 */
	@Override
	public final void onBindViewHolder(ViewHolder holder, int position) {
		if (getItemViewType(position) == TYPE_EMPTY) return;
		if (isHeaderPos(position)) {
			onBindHeaderViewHolder(holder, headerList.get(position).getResponse(), headerList.get
					(position).getLayout());
			return;
		}
		if (isFooterPos(position)) {
			onBindFooterViewHolder(holder, footerList.get(position - headerList.size() - data.size()).getResponse(), footerList.get
					(position - headerList.size() - data.size()).getLayout());
		}
		if ((position - headerList.size()) > data.size() - 1) {
			conver(holder, null, getItemViewType((position)));
		} else {
			conver(holder, data.get((position - headerList.size())), getItemViewType((position)));
		}
	}


	public abstract void conver(ViewHolder holder, T data, int viewType);

	/**
	 * 解决GrildLayoutManager问题
	 *
	 * @param recyclerView
	 */
	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
		if (layoutManager instanceof GridLayoutManager) {
			final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
			final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();

			gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
				@Override
				public int getSpanSize(int position) {
					if (isHeaderPos(position)) {
						return gridLayoutManager.getSpanCount();
					} else if (isFooterPos(position)) {
						return gridLayoutManager.getSpanCount();
					}
					if (spanSizeLookup != null)
						return spanSizeLookup.getSpanSize(position);
					return 1;
				}
			});
			gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount());
		}
	}

	/**
	 * 解决StaggeredGridLayoutManager问题
	 *
	 * @param holder
	 */
	@Override
	public void onViewAttachedToWindow(ViewHolder holder) {
		super.onViewAttachedToWindow(holder);
		int position = holder.getLayoutPosition();
		if (isHeaderPos(position) || isFooterPos(position)) {
			ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();

			if (lp != null
					&& lp instanceof StaggeredGridLayoutManager.LayoutParams) {

				StaggeredGridLayoutManager.LayoutParams p =
						(StaggeredGridLayoutManager.LayoutParams) lp;

				p.setFullSpan(true);
			}
		}
	}

	/**
	 * 下面两个方法按需重写
	 *
	 * @param viewHolder
	 * @param response   用到时 强转为你所需要的实体类
	 * @param layout     添加多个头布局时  可以判断layout来控制视图
	 */

	public void onBindHeaderViewHolder(ViewHolder viewHolder, Object response, @LayoutRes int
			layout) {
	}

	//用法参见header
	public void onBindFooterViewHolder(ViewHolder viewHolder, Object response, @LayoutRes int
			layout) {
	}

	@Override
	public final int getItemCount() {

		if (emptyModle == EMPTY_MODLE_NOMAL) {
			if (data.isEmpty() && data.size() == 0 && emptyView != null && headerList.isEmpty() && footerList.isEmpty()) {
				return 1;
			}
		} else {
			if (data.isEmpty() && data.size() == 0 && emptyView != null) {
				return 1;
			}
		}

		return data.size() + headerList.size() + footerList.size();
	}

	/**
	 * 添加头布局 尾布局
	 *
	 * @param t      数据实体类
	 * @param layout 头尾布局的layoutID
	 */
	public void addHeaderView(Object t, @LayoutRes int layout) {
		DataBean<Object> dataBean = new DataBean<>(t, layout);
		headerList.add(dataBean);
	}

	public void addFooterView(Object t, @LayoutRes int layout) {
		DataBean dataBean = new DataBean(t, layout);
		footerList.add(dataBean);
	}


	/**
	 * 无数据时视图显示模式
	 * 默认模式为   {@link #ERMTY_MODLE_HEADERS}  当数据没有时显示
	 * 即使已经添加了头布局尾布局
	 * 可选模式     {@link #EMPTY_MODLE_NOMAL}  当没有数据 并且头尾布局都没有的情况下显示
	 *
	 * @param modle {@link #emptyModle}
	 */
	public void setEmptyModle(int modle) {
		this.emptyModle = modle;
	}


	/**
	 * 当没有数据时会显示此View
	 *
	 * @param view
	 */
	public void setEmptyView(@NonNull View view) {
		this.emptyView = view;
		RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
		this.emptyView.setLayoutParams(params);
	}


	/**
	 * 加入final 禁止子类重写
	 * 如果需要多布局  请重写  {@link #getItemType(int)}
	 *
	 * @param position
	 * @return
	 */
	@Override
	public final int getItemViewType(int position) {
		if (emptyModle == EMPTY_MODLE_NOMAL) {
			if (data.isEmpty() && data.size() == 0 && emptyView != null && headerList.isEmpty() && footerList.isEmpty())
				return TYPE_EMPTY;
		} else {
			if (data.isEmpty() && data.size() == 0 && emptyView != null) return TYPE_EMPTY;
		}

		if (isHeaderPos(position)) {
			return headerList.get(position).getLayout();
		}
		if (isFooterPos(position)) {
			return footerList.get(position - data.size() - headerList.size()).getLayout();
		}
		return getItemType(position - headerList.size());
	}

	/**
	 * 多布局时  请子类实现这个方法来控制多布局
	 *
	 * @param position
	 * @return
	 */
	protected int getItemType(int position) {
		return TYPE_NOMAL;
	}

	/**
	 * 增加数据
	 *
	 * @param items
	 */
	public final void loadItem(Collection<T> items) {
		int size = data.size() + headerList.size();
		data.addAll(items);
		notifyItemRangeInserted(size, items.size());
		compatibilityDataSizeChanged(items.size());
	}


	/**
	 * 增加一条数据
	 *
	 * @param t
	 */
	public final void loadItem(T t) {
		data.add(t);
		notifyItemInserted(data.size() + headerList.size());
		compatibilityDataSizeChanged(1);
	}


	/**
	 * 刷新本地数据
	 */
	public final void refresh() {
		notifyDataSetChanged();
	}

	/**
	 * 重新设置数据
	 *
	 * @param response
	 */
	public final void refresh(Collection<T> response) {
		data.clear();
		data.addAll(response);
		notifyDataSetChanged();
	}

	/**
	 * 删掉对应的索引
	 *
	 * @param position
	 */
	public final void remove(@IntRange(from = 0) int position) {
		if (position >= data.size()) return;
		int size = data.size();
		data.remove(position);
		notifyItemRemoved(position + headerList.size());
		compatibilityDataSizeChanged(0);
	}

	/**
	 * 清除数据
	 */
	public final void clear() {
		data.clear();
		notifyDataSetChanged();
	}

	private void compatibilityDataSizeChanged(int newDataSize) {
		int size = data.isEmpty() ? 0 : data.size();
		if (size == newDataSize) notifyDataSetChanged();
	}


	public final boolean isHeaderPos(int position) {
		return getHeaderCount() > position;
	}

	public final boolean isFooterPos(int position) {
		return position >= headerList.size() + data.size();
	}

	private int getHeaderCount() {
		return headerList.size();
	}

	private int getFooterCount() {
		return footerList.size();
	}

	private int isHeaderType(int type) {
		for (DataBean dataBean : headerList) {
			if (dataBean.getLayout() == type) {
				return dataBean.getLayout();
			}
		}
		return -1;
	}

	@Override
	public void onViewDetachedFromWindow(ViewHolder holder) {
		super.onViewDetachedFromWindow(holder);

	}

	private int isFooterType(int type) {
		for (DataBean dataBean : footerList) {
			if (dataBean.getLayout() == type) {
				return dataBean.getLayout();
			}
		}
		return -1;
	}


}
