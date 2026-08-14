package com.xinglin.forum.config;

import com.xinglin.forum.entity.ForumBoard;
import com.xinglin.forum.repository.ForumBoardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final ForumBoardRepository boardRepository;

    public DataInitializer(ForumBoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public void run(String... args) {
        List<String[]> boards = Arrays.asList(
                new String[]{"中医基础", "阴阳五行、藏象、气血津液等基础理论", "book-open"},
                new String[]{"经方学习", "伤寒论、金匮要略与经典方证研习", "scroll-text"},
                new String[]{"针灸推拿", "经络腧穴、针法灸法和推拿手法", "activity"},
                new String[]{"本草方剂", "中药功效、方剂配伍与学习笔记", "leaf"},
                new String[]{"学习笔记", "课程复盘、读书记录和个人总结", "notebook"},
                new String[]{"课程讨论", "围绕视频课程展开的交流讨论", "play-circle"},
                new String[]{"问答互助", "学习问题答疑和经验互助", "messages-square"}
        );
        int index = 1;
        for (String[] boardValue : boards) {
            if (!boardRepository.existsByName(boardValue[0])) {
                ForumBoard board = new ForumBoard();
                board.setName(boardValue[0]);
                board.setDescription(boardValue[1]);
                board.setIcon(boardValue[2]);
                board.setSortOrder(index * 10);
                boardRepository.save(board);
                log.info("forum board initialized name={}", board.getName());
            }
            index++;
        }
    }
}
