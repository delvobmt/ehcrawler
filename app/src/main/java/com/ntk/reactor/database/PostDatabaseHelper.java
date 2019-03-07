package com.ntk.reactor.database;

import com.ntk.reactor.model.Content;
import com.ntk.reactor.model.Post;
import com.volokh.danylo.video_player_manager.manager.PlayerItemChangeListener;
import com.volokh.danylo.video_player_manager.manager.SingleVideoPlayerManager;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;

import java.util.ArrayList;
import java.util.List;

public class PostDatabaseHelper {
    private static List<Post> posts = new ArrayList<>();

    public static List<Post> getAllPost(){
        return posts;
    }

    public static Post getPostAt(int index){
        return posts.get(index);
    }

    public static void add(Post post) {
        posts.add(post);
    }

    public static int size(){
        return posts.size();
    }

    public static void clear() {
        posts.clear();
    }

    public static boolean isEmpty(){
        return posts.isEmpty();
    }

    public static List<Content> getPostContentsAt(int index) {
        Post post = posts.get(index);
        return post.getContents();
    }

    public static List<String> getTagsAt(int index) {
        Post post = posts.get(index);
        return post.getTags();
    }

    public static void updatePost(Post post, int position) {
        posts.set(position, post);
    }

    /**
     * Here we use {@link SingleVideoPlayerManager}, which means that only one video playback is possible.
     */
    private final static VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(MetaData metaData) {

        }
    });

    public static VideoPlayerManager<MetaData> getVideoManagerInstance(){
        return mVideoPlayerManager;
    }
}
