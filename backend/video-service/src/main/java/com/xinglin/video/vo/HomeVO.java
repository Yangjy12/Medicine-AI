package com.xinglin.video.vo;

import java.util.ArrayList;
import java.util.List;

public class HomeVO {
    private List<CategoryVO> categories = new ArrayList<>();
    private List<VideoCardVO> recommended = new ArrayList<>();
    private List<VideoCardVO> hot = new ArrayList<>();
    private List<VideoCardVO> latest = new ArrayList<>();
    private List<VideoCardVO> continueLearning = new ArrayList<>();

    public List<CategoryVO> getCategories() { return categories; }
    public void setCategories(List<CategoryVO> categories) { this.categories = categories; }
    public List<VideoCardVO> getRecommended() { return recommended; }
    public void setRecommended(List<VideoCardVO> recommended) { this.recommended = recommended; }
    public List<VideoCardVO> getHot() { return hot; }
    public void setHot(List<VideoCardVO> hot) { this.hot = hot; }
    public List<VideoCardVO> getLatest() { return latest; }
    public void setLatest(List<VideoCardVO> latest) { this.latest = latest; }
    public List<VideoCardVO> getContinueLearning() { return continueLearning; }
    public void setContinueLearning(List<VideoCardVO> continueLearning) { this.continueLearning = continueLearning; }
}
