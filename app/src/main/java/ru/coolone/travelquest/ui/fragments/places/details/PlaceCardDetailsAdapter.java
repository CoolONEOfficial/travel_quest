package ru.coolone.travelquest.ui.fragments.places.details;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.like.LikeButton;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.RequiredArgsConstructor;
import lombok.val;
import ru.coolone.travelquest.BuildConfig;
import ru.coolone.travelquest.R;
import ru.coolone.travelquest.ui.activities.MainActivity;

/**
 * @author coolone
 * @since 20.04.18
 */
@RequiredArgsConstructor
public class PlaceCardDetailsAdapter extends RecyclerView.Adapter<PlaceCardDetailsAdapter.ViewHolder> {
    final Activity activity;

    public ArrayList<Item> dataset = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView likes;
        RecyclerView recycler;
        LikeButton likeButton;

        public ViewHolder(View v) {
            super(v);

            title = v.findViewById(R.id.card_details_mail);
            title.setSelected(true);
            likes = v.findViewById(R.id.card_details_likes);
            likeButton = v.findViewById(R.id.card_details_star);
            recycler = v.findViewById(R.id.card_details_recycler);
            recycler.setLayoutManager(new LinearLayoutManager(recycler.getContext()));
            recycler.setHasFixedSize(true);
        }
    }

    public static class Item {
        String title;
        RecyclerView recyclerView;
        DocumentSnapshot rootDoc;
        String uId;

        public Item(
                String title,
                RecyclerView recyclerView,
                DocumentSnapshot rootDoc,
                String uId
        ) {
            this.title = title;
            this.recyclerView = recyclerView;
            this.rootDoc = rootDoc;
            this.uId = uId;
        }
    }

    @Override
    public PlaceCardDetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_details, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        val mItem = dataset.get(position);
        holder.title.setText(mItem.title);

        val userUid = MainActivity.firebaseUser.getUid();
        val likedUsers = (ArrayList<String>) mItem.rootDoc.get("score");
        holder.likeButton.setLiked(likedUsers.contains(userUid));
        holder.likeButton.setVisibility(View.VISIBLE);
        holder.likeButton.setEnabled(true);
        holder.likeButton.setOnClickListener(
                v -> {
                    if (MainActivity.firebaseUser.isAnonymous()) // user registered
                        Toast.makeText(
                                activity,
                                activity.getString(R.string.details_star_error_anonymous),
                                Toast.LENGTH_SHORT
                        ).show();
                    else if (mItem.uId.equals(userUid)) { // it is not self card
                        Toast.makeText(
                                activity,
                                activity.getString(R.string.details_star_error_self),
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {

                        val button = (LikeButton) v;

                        val fListener = (OnFailureListener) e ->
                                Toast.makeText(
                                        activity,
                                        activity.getText(R.string.error_network),
                                        Toast.LENGTH_SHORT
                                ).show();
                        val sListener = (OnSuccessListener) o -> button.setEnabled(true);

                        // Toggle button
                        button.onClick(v);
                        button.setEnabled(false);

                        // Add / Remove user
                        if (likedUsers.contains(userUid)) {
                            likedUsers.remove(userUid);
                        } else {
                            likedUsers.add(userUid);
                        }

                        // Update likes counter
                        holder.likes.setText(String.valueOf(likedUsers.size()));

                        // Apply changes
                        mItem.rootDoc.getReference()
                                .update(
                                        new HashMap<String, Object>() {{
                                            put("score", likedUsers);
                                        }}
                                )
                                .addOnSuccessListener(sListener)
                                .addOnFailureListener(fListener);
                    }
                }
        );
        holder.likes.setText(String.valueOf(likedUsers.size()));
        holder.recycler.setAdapter(mItem.recyclerView.getAdapter());
        holder.recycler.invalidate();
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}


/**
 * Custom layout manager from stackoverflow.com
 */
class LinearLayoutManager extends android.support.v7.widget.LinearLayoutManager {

    private static final int CHILD_WIDTH = 0;
    private static final int CHILD_HEIGHT = 1;
    private static final int DEFAULT_CHILD_SIZE = 100;

    private final int[] childDimensions = new int[2];

    private int childSize = DEFAULT_CHILD_SIZE;
    private boolean hasChildSize;

    @SuppressWarnings("UnusedDeclaration")
    public LinearLayoutManager(Context context) {
        super(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public LinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public static int makeUnspecifiedSpec() {
        return View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        final int widthMode = View.MeasureSpec.getMode(widthSpec);
        final int heightMode = View.MeasureSpec.getMode(heightSpec);

        final int widthSize = View.MeasureSpec.getSize(widthSpec);
        final int heightSize = View.MeasureSpec.getSize(heightSpec);

        final boolean exactWidth = widthMode == View.MeasureSpec.EXACTLY;
        final boolean exactHeight = heightMode == View.MeasureSpec.EXACTLY;

        final int unspecified = makeUnspecifiedSpec();

        if (exactWidth && exactHeight) {
            // in case of exact calculations for both dimensions let's use default "onMeasure" implementation
            super.onMeasure(recycler, state, widthSpec, heightSpec);
            return;
        }

        final boolean vertical = getOrientation() == VERTICAL;

        initChildDimensions(widthSize, heightSize, vertical);

        int width = 0;
        int height = 0;

        // it's possible to get scrap views in recycler which are bound to old (invalid) adapter entities. This
        // happens because their invalidation happens after "onMeasure" method. As a workaround let's clear the
        // recycler now (it should not cause any performance issues while scrolling as "onMeasure" is never
        // called whiles scrolling)
        recycler.clear();

        final int stateItemCount = state.getItemCount();
        final int adapterItemCount = getItemCount();
        // adapter always contains actual data while state might contain old data (f.e. data before the animation is
        // done). As we want to measure the view with actual data we must use data from the adapter and not from  the
        // state
        for (int i = 0; i < adapterItemCount; i++) {
            if (vertical) {
                if (!hasChildSize) {
                    if (i < stateItemCount) {
                        // we should not exceed state count, otherwise we'll get IndexOutOfBoundsException. For such items
                        // we will use previously calculated dimensions
                        measureChild(recycler, i, widthSpec, unspecified, childDimensions);
                    } else {
                        logMeasureWarning(i);
                    }
                }
                height += childDimensions[CHILD_HEIGHT];
                if (i == 0) {
                    width = childDimensions[CHILD_WIDTH];
                }
                if (height >= heightSize) {
                    break;
                }
            } else {
                if (!hasChildSize) {
                    if (i < stateItemCount) {
                        // we should not exceed state count, otherwise we'll get IndexOutOfBoundsException. For such items
                        // we will use previously calculated dimensions
                        measureChild(recycler, i, unspecified, heightSpec, childDimensions);
                    } else {
                        logMeasureWarning(i);
                    }
                }
                width += childDimensions[CHILD_WIDTH];
                if (i == 0) {
                    height = childDimensions[CHILD_HEIGHT];
                }
                if (width >= widthSize) {
                    break;
                }
            }
        }

        if ((vertical && height < heightSize) || (!vertical && width < widthSize)) {
            // we really should wrap the contents of the view, let's do it

            if (exactWidth) {
                width = widthSize;
            } else {
                width += getPaddingLeft() + getPaddingRight();
            }

            if (exactHeight) {
                height = heightSize;
            } else {
                height += getPaddingTop() + getPaddingBottom();
            }

            setMeasuredDimension(width, height);
        } else {
            // if calculated height/width exceeds requested height/width let's use default "onMeasure" implementation
            super.onMeasure(recycler, state, widthSpec, heightSpec);
        }
    }

    private void logMeasureWarning(int child) {
        if (BuildConfig.DEBUG) {
            Log.w("LinearLayoutManager", "Can't measure child #" + child + ", previously used dimensions will be reused." +
                    "To remove this message either use #setChildSize() method or don't run RecyclerView animations");
        }
    }

    private void initChildDimensions(int width, int height, boolean vertical) {
        if (childDimensions[CHILD_WIDTH] != 0 || childDimensions[CHILD_HEIGHT] != 0) {
            // already initialized, skipping
            return;
        }
        if (vertical) {
            childDimensions[CHILD_WIDTH] = width;
            childDimensions[CHILD_HEIGHT] = childSize;
        } else {
            childDimensions[CHILD_WIDTH] = childSize;
            childDimensions[CHILD_HEIGHT] = height;
        }
    }

    @Override
    public void setOrientation(int orientation) {
        // might be called before the constructor of this class is called
        //noinspection ConstantConditions
        if (childDimensions != null) {
            if (getOrientation() != orientation) {
                childDimensions[CHILD_WIDTH] = 0;
                childDimensions[CHILD_HEIGHT] = 0;
            }
        }
        super.setOrientation(orientation);
    }

    public void clearChildSize() {
        hasChildSize = false;
        setChildSize(DEFAULT_CHILD_SIZE);
    }

    public void setChildSize(int childSize) {
        hasChildSize = true;
        if (this.childSize != childSize) {
            this.childSize = childSize;
            requestLayout();
        }
    }

    private void measureChild(RecyclerView.Recycler recycler, int position, int widthSpec, int heightSpec, int[] dimensions) {
        final View child = recycler.getViewForPosition(position);

        final RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) child.getLayoutParams();

        final int hPadding = getPaddingLeft() + getPaddingRight();
        final int vPadding = getPaddingTop() + getPaddingBottom();

        final int hMargin = p.leftMargin + p.rightMargin;
        final int vMargin = p.topMargin + p.bottomMargin;

        final int hDecoration = getRightDecorationWidth(child) + getLeftDecorationWidth(child);
        final int vDecoration = getTopDecorationHeight(child) + getBottomDecorationHeight(child);

        final int childWidthSpec = getChildMeasureSpec(widthSpec, hPadding + hMargin + hDecoration, p.width, canScrollHorizontally());
        final int childHeightSpec = getChildMeasureSpec(heightSpec, vPadding + vMargin + vDecoration, p.height, canScrollVertically());

        child.measure(childWidthSpec, childHeightSpec);

        dimensions[CHILD_WIDTH] = getDecoratedMeasuredWidth(child) + p.leftMargin + p.rightMargin;
        dimensions[CHILD_HEIGHT] = getDecoratedMeasuredHeight(child) + p.bottomMargin + p.topMargin;

        recycler.recycleView(child);
    }
}