package com.codepath.blip.Adapters;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.blip.R;
import com.codepath.blip.models.Blip;
import com.squareup.picasso.Picasso;

import java.util.List;

import rx.Subscriber;

public class BlipAdapter extends RecyclerView.Adapter<BlipAdapter.ViewHolder> {

    private List<Blip> mBlips;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView body;
        public TextView upvotes;
        public FloatingActionButton voteUp;
        public FloatingActionButton voteDown;
        public ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            body = (TextView) itemView.findViewById(R.id.tvBlipBody);
            upvotes = (TextView) itemView.findViewById(R.id.tvUpvotes);
            voteUp = (FloatingActionButton) itemView.findViewById(R.id.upvoteButton);
            voteDown = (FloatingActionButton) itemView.findViewById(R.id.downvoteButton);
            image = (ImageView) itemView.findViewById(R.id.blipImageView);
        }
    }

    public BlipAdapter(Context context, List<Blip> blips) {
        mContext = context;
        mBlips = blips;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View blipView = inflater.inflate(R.layout.blip_item, parent, false);
        ViewHolder v = new ViewHolder(blipView);
        return v;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Blip blip = mBlips.get(position);

        TextView blipBody = holder.body;
        final ImageView blipImage = holder.image;
        FloatingActionButton upVoteButton = holder.voteUp;
        FloatingActionButton downVoteButton = holder.voteDown;
        final TextView textUpVotes = holder.upvotes;
        blipBody.setText(blip.getCaption());
        textUpVotes.setText(String.format("%d", blip.getScore()));

        String imageUri = blip.getImageUri();
        if (imageUri != null) {
            Picasso.with(mContext).load(blip.getImageUri()).fit().centerCrop().placeholder(R.drawable.placeholder).into(blipImage);
        } else {
            blipImage.setVisibility(View.GONE);
        }

        upVoteButton.se

        upVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                blip.upvoteBlip().subscribe(new Subscriber<Blip>() {
                    @Override
                    public void onCompleted() {
                        // Nothing
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(Blip blip) {
                        textUpVotes.setText(String.format("%d", blip.getScore()));
                    }
                });
            }
        });

        downVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                blip.downvoteBlip().subscribe(new Subscriber<Blip>() {
                    @Override
                    public void onCompleted() {
                        // Nothing
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(Blip blip) {
                        textUpVotes.setText(String.format("%d", blip.getScore()));
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBlips.size();
    }

}
