public class Movie {

    public String videoId = "";
    public int likeCount = 0;
    public int viewCount = 0;
    public String title = "";

    public Movie(String videoId, int likeCount, int viewCount, String title) {
        this.videoId = videoId;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
        this.title = title;

    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
