package com.example.news;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.news.models.Articles;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {
    private List<Articles> articles;
    private Context context;


    NewsAdapter(List<Articles> articles, Context context) {
        this.articles = articles;
        this.context = context;  //We should pass the context
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new MyViewHolder(view, onItemClickListener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holders, int position) {

        //Because holders is accessed from inner class so should be final
        //get articles one after another from articles list
        Articles article = articles.get(position);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.placeholder(Utils.getRandomDrawableColor());  //when program start
        requestOptions = requestOptions.error(Utils.getRandomDrawableColor()); //when there is no image
        requestOptions = requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);  //store the image in the cache memory, that make the app speed
        requestOptions = requestOptions.centerCrop();

        //set the news image
        Glide.with(context)
                // .thumbnail(Glide.with(context).load(R.drawable.no_result))
                .load(article.getUrlToImage())
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holders.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holders.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .transition(DrawableTransitionOptions.withCrossFade())
                /*Without a transition, your image will pop into place, immediately replacing the previous image.
                 To avoid the sudden change, you can fade in your view,or cross fade between Drawables using TransitionOptions*/
                .into(holders.imageView);

        //set the news author
        holders.author.setText(article.getAuthor());
        //set the news published date
        holders.published_at.setText(Utils.DateFormat(article.getPublishedAt()));
        //set the news title
        holders.title.setText(article.getTitle());
        //set the news description
        holders.desc.setText(article.getDescription());
        //set the news source
        holders.source.setText(article.getSource().getName());
        //set the news time
        holders.time.setText(" \u2022 " + Utils.DateToTimeFormat(article.getPublishedAt()));
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }


    /*From here to the end is the first part*/

    public interface OnItemClickListener {
        void OnItemClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;

    void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView author, published_at, title, desc, source, time;
        ImageView imageView;
        ProgressBar progressBar;
        OnItemClickListener onItemClickListener;

        MyViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            itemView.setOnClickListener(this);
            author = itemView.findViewById(R.id.author);
            published_at = itemView.findViewById(R.id.publishedAt);
            title = itemView.findViewById(R.id.title);
            desc = itemView.findViewById(R.id.desc);
            source = itemView.findViewById(R.id.source);
            time = itemView.findViewById(R.id.time);
            imageView = itemView.findViewById(R.id.img);
            progressBar = itemView.findViewById(R.id.progress_load_photo);
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.OnItemClick(view, getAdapterPosition());
        }
    }
}
