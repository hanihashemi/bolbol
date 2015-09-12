package io.github.hanihashemi.podgir.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import io.github.hanihashemi.podgir.App;
import io.github.hanihashemi.podgir.R;
import io.github.hanihashemi.podgir.activity.PlayerActivity;
import io.github.hanihashemi.podgir.adapter.PodcastDetailRecyclerView;
import io.github.hanihashemi.podgir.adapter.viewholder.FeedInPodcastDetailViewHolder;
import io.github.hanihashemi.podgir.base.BaseFragment;
import io.github.hanihashemi.podgir.model.Feed;
import io.github.hanihashemi.podgir.model.FeedResultResponse;
import io.github.hanihashemi.podgir.model.Podcast;
import io.github.hanihashemi.podgir.network.request.GsonRequest;
import io.github.hanihashemi.podgir.util.Directory;
import io.github.hanihashemi.podgir.util.DownloadFile;

/**
 * Created by hani on 8/24/15.
 */
public class PodcastDetailFragment extends BaseFragment implements Response.Listener<FeedResultResponse> {
    public static final String ARG_PODCAST = "arg_podcast";
    @Bind(R.id.recycler_view)
    protected RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private Podcast podcast;
    private List<Feed> feeds;

    private FeedInPodcastDetailViewHolder.OnClick feedOnClick = new FeedInPodcastDetailViewHolder.OnClick() {
        @Override
        public void onDownload(int position) {
            Feed feed = feeds.get(position - 1);

            if (!feed.isDownloaded()) {
                if (Directory.getInstance().isFileThere(podcast.getObjectId(), feed.getObjectId())) {
                    feed.save();
                    feed.setDownloaded(true);
                    adapter.notifyItemChanged(position);
                } else
                    new DownloadFile(podcast, feed).execute(feed.getUrl());
            } else {
                startActivity(PlayerActivity.getIntent(PodcastDetailFragment.this.getActivity(), feed));
            }
        }
    };

    public static PodcastDetailFragment getInstance(Podcast podcast) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_PODCAST, podcast);
        PodcastDetailFragment fragment = new PodcastDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_recycler;
    }

    @Override
    protected void customizeUI() {
        super.customizeUI();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        feeds = new ArrayList<>();
        adapter = new PodcastDetailRecyclerView(podcast, feeds, feedOnClick);
        recyclerView.setAdapter(adapter);

        fetchData();
    }

    private void fetchData() {
        GsonRequest<FeedResultResponse> request = new Feed().remoteFindAll(podcast.getObjectId(), this, this);
        App.getInstance().addRequestToQueue(request, this);
    }

    @Override
    public void onResponse(FeedResultResponse response) {
        feeds.clear();
        feeds.addAll(response.getFeeds());

        checkIsFileDownloaded();
        adapter.notifyDataSetChanged();
    }

    private void checkIsFileDownloaded() {
        for (Feed feed : feeds)
            feed.isThereInDB();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        podcast = getArguments().getParcelable(ARG_PODCAST);
    }
}
