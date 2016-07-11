package it.sasabz.android.sasabus.util.recycler;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.model.News;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Alex Lardschneider
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private final List<News> mItems;
    private final Context mContext;

    public NewsAdapter(Context context, List<News> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_news, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        News item = mItems.get(position);

        holder.title.setText(item.getTitle());
        holder.message.setText(Html.fromHtml(item.getMessage()), TextView.BufferType.SPANNABLE);

        if (item.isHighlighted()) {
            holder.layout.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.orange_50));
        } else {
            holder.layout.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.card_background));
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.list_news_title) TextView title;
        @BindView(R.id.list_news_message) TextView message;
        @BindView(R.id.list_news_layout) CardView layout;

        ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }
}