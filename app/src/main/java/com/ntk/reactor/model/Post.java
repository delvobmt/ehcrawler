package com.ntk.reactor.model;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.ntk.reactor.ReactorConstants;
import com.ntk.reactor.database.PostDatabaseHelper;
import com.volokh.danylo.video_player_manager.meta.CurrentItemMetaData;
import com.volokh.danylo.video_player_manager.ui.VideoPlayerView;
import com.volokh.danylo.visibility_utils.items.ListItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Post implements ListItem{
    private String id;
    private List<Content> contents = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private String url;
    private String commentCount;
    private boolean isLoaded;
    private Rect mCurrentViewRect = new Rect();

    public Post(String id) {
        this.id = id;
    }

    public void addTag(String tag){
        tags.add(tag);
    }

    public List<String> getTags() {
        return tags;
    }

    public void addContent(Content content) {
        if (content != null) {
            this.contents.add(content);
        }
    }

    public void addContent(Collection contents) {
        this.contents.addAll(contents);
    }

    public List<Content> getContents() {
        return contents;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        return id != null ? id.equals(post.id) : post.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int getVisibilityPercents(View view) {
//        if(SHOW_LOGS) Logger.v(TAG, ">> getVisibilityPercents view " + view);

        int percents = 100;

        view.getLocalVisibleRect(mCurrentViewRect);
//        if(SHOW_LOGS) Logger.v(TAG, "getVisibilityPercents mCurrentViewRect top " + mCurrentViewRect.top + ", left " + mCurrentViewRect.left + ", bottom " + mCurrentViewRect.bottom + ", right " + mCurrentViewRect.right);

        int height = view.getHeight();
//        if(SHOW_LOGS) Logger.v(TAG, "getVisibilityPercents height " + height);

        if(viewIsPartiallyHiddenTop()){
            // view is partially hidden behind the top edge
            percents = (height - mCurrentViewRect.top) * 100 / height;
        } else if(viewIsPartiallyHiddenBottom(height)){
            percents = mCurrentViewRect.bottom * 100 / height;
        }

//        if(SHOW_LOGS) Logger.v(TAG, "<< getVisibilityPercents, percents " + percents);

        return percents;
    }

    private boolean viewIsPartiallyHiddenBottom(int height) {
        return mCurrentViewRect.bottom > 0 && mCurrentViewRect.bottom < height;
    }

    private boolean viewIsPartiallyHiddenTop() {
        return mCurrentViewRect.top > 0;
    }

    @Override
    public void setActive(final View newActiveView, final int newActiveViewPosition) {
        Context context = newActiveView.getContext();
        final VideoPlayerView videoView = (VideoPlayerView) newActiveView.getTag();
        Content firstContent = getContents().get(0);
        if (VideoGifContent.class.equals(firstContent.getClass())) {
            final List<String> sources = ((VideoGifContent) firstContent).getSrc();
            String src = "";
            for(String s : sources){
                if(s.endsWith(".webm") ){
                    src = s;
                    break;
                }else if(s.endsWith(".gif")){
                    src = s;
                }else if("".equals(src)){
                    src = s;
                }
            }
            String filename = src.substring(src.lastIndexOf("/"));
            final String path = context.getFilesDir().getAbsolutePath() + filename;
            File file = new File(path);
            if (!file.exists()) {
                FileDownloader.getImpl().create(src).addHeader("Referer", ReactorConstants.HOST).setPath(path).setListener(new FileDownloadLargeFileListener() {
                    @Override
                    protected void pending(BaseDownloadTask baseDownloadTask, long l, long l1) {

                    }

                    @Override
                    protected void progress(BaseDownloadTask baseDownloadTask, long l, long l1) {

                    }

                    @Override
                    protected void paused(BaseDownloadTask baseDownloadTask, long l, long l1) {

                    }

                    @Override
                    protected void completed(final BaseDownloadTask baseDownloadTask) {
                        PostDatabaseHelper.playNewVideo(new CurrentItemMetaData(newActiveViewPosition, newActiveView), videoView, path);
                    }

                    @Override
                    protected void error(BaseDownloadTask baseDownloadTask, Throwable throwable) {

                    }

                    @Override
                    protected void warn(BaseDownloadTask baseDownloadTask) {

                    }
                }).start();
            }else{
                PostDatabaseHelper.playNewVideo(new CurrentItemMetaData(newActiveViewPosition, newActiveView), videoView, path);
            }
        }
    }

    @Override
    public void deactivate(View currentView, int position) {

    }
}
