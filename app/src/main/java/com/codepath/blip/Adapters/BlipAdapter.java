package com.codepath.blip.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.blip.R;
import com.codepath.blip.models.Blip;

import java.util.List;

import rx.Subscriber;

public class BlipAdapter extends RecyclerView.Adapter<BlipAdapter.ViewHolder> {

    private List<Blip> mBlips;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView body;
        public TextView upvotes;
        public ImageButton voteUp;
        public ImageButton voteDown;
        public ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            body = (TextView) itemView.findViewById(R.id.tvBlipBody);
            upvotes = (TextView) itemView.findViewById(R.id.tvUpvotes);
            voteUp = (ImageButton) itemView.findViewById(R.id.upvoteButton);
            voteDown = (ImageButton) itemView.findViewById(R.id.downvoteButton);
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
        ImageButton upVoteButton = holder.voteUp;
        ImageButton downVoteButton = holder.voteDown;
        TextView textUpVotes = holder.upvotes;

        blipBody.setText(blip.getCaption());
        textUpVotes.setText(blip.getScore());

        blip.getImage().subscribe(new Subscriber<Bitmap>() {
            @Override
            public void onCompleted() {
                // Nothing
            }

            @Override
            public void onError(Throwable e) {
                // Render a giant X or something maybe?
                blipImage.setVisibility(View.GONE);
            }

            @Override
            public void onNext(Bitmap bitmap) {
                if (bitmap == null) {
                    // Render a giant X or something maybe?
                    blipImage.setVisibility(View.GONE);
                    Log.d("DEBUG", "Unable to decode image data");
                } else {
                    blipImage.setImageBitmap(bitmap);
                }
            }
        });

        upVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blip.upVoteBlip();
            }
        });

        downVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blip.downVoteBlip();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBlips.size();
    }

}
