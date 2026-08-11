package com.xinglin.video.config;

import com.xinglin.video.entity.Video;
import com.xinglin.video.entity.VideoCategory;
import com.xinglin.video.repository.VideoCategoryRepository;
import com.xinglin.video.repository.VideoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private final VideoCategoryRepository categoryRepository;
    private final VideoRepository videoRepository;

    public DataInitializer(VideoCategoryRepository categoryRepository, VideoRepository videoRepository) {
        this.categoryRepository = categoryRepository;
        this.videoRepository = videoRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            List<String> names = Arrays.asList("中医基础理论", "中药学", "方剂学", "针灸学", "黄帝内经", "伤寒论");
            for (int i = 0; i < names.size(); i++) {
                VideoCategory category = new VideoCategory();
                category.setName(names.get(i));
                category.setIcon("category-" + (i + 1));
                category.setSortValue(i + 1);
                categoryRepository.save(category);
            }
        }
        if (videoRepository.count() == 0) {
            List<VideoCategory> categories = categoryRepository.findByStatusOrderBySortValueAsc(1);
            createVideo("中医基础理论入门", "系统讲解阴阳五行、藏象、气血津液等中医基础概念。", categories.get(0).getId(), "李明远", "阴阳五行,藏象,入门", 1860, 1260, 98, 76);
            createVideo("针灸经络精讲", "围绕十二经脉、常用腧穴和针灸学习方法进行分层讲解。", categories.get(3).getId(), "周若溪", "经络,腧穴,针灸", 2420, 980, 73, 61);
            createVideo("中药学常用药性速记", "从四气五味、升降浮沉和常用中药功效出发建立记忆框架。", categories.get(1).getId(), "陈知夏", "中药,药性,速记", 1680, 760, 54, 49);
            createVideo("方剂学组方思路", "以君臣佐使为主线，理解经典方剂的组成逻辑与学习方法。", categories.get(2).getId(), "王青禾", "方剂,君臣佐使,经典", 2100, 680, 43, 38);
            createVideo("黄帝内经选读", "精选《素问》《灵枢》核心篇章，帮助初学者建立经典阅读路径。", categories.get(4).getId(), "赵怀瑾", "黄帝内经,经典,素问", 1950, 1120, 89, 70);
            createVideo("伤寒论六经辨证导学", "从太阳病到厥阴病，梳理六经辨证的学习脉络。", categories.get(5).getId(), "沈砚秋", "伤寒论,六经辨证,导学", 2250, 590, 39, 31);
        }
    }

    private void createVideo(String title,
                             String description,
                             Long categoryId,
                             String lecturer,
                             String tags,
                             int duration,
                             long playCount,
                             long likeCount,
                             long collectCount) {
        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setCategoryId(categoryId);
        video.setLecturer(lecturer);
        video.setTags(tags);
        video.setDuration(duration);
        video.setCoverUrl("https://images.unsplash.com/photo-1584467735871-8297329f9eb3?auto=format&fit=crop&w=900&q=80");
        video.setVideoUrl("https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4");
        video.setStatus("ONLINE");
        video.setPublishTime(LocalDateTime.now().minusDays((long) (Math.random() * 20)));
        video.setPlayCount(playCount);
        video.setLikeCount(likeCount);
        video.setCollectCount(collectCount);
        videoRepository.save(video);
    }
}
