package com.xinglin.forum.service;

import com.xinglin.forum.entity.ForumBoard;
import com.xinglin.forum.repository.ForumBoardRepository;
import com.xinglin.forum.vo.BoardVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumBoardService {
    private final ForumBoardRepository boardRepository;

    public ForumBoardService(ForumBoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public List<BoardVO> listEnabledBoards() {
        return boardRepository.findByStatusOrderBySortOrderAscIdAsc("ENABLED")
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    public String resolveBoardName(Long boardId) {
        if (boardId == null) {
            return "未分区";
        }
        return boardRepository.findById(boardId).map(ForumBoard::getName).orElse("未分区");
    }

    private BoardVO toVO(ForumBoard board) {
        BoardVO vo = new BoardVO();
        vo.setId(board.getId());
        vo.setName(board.getName());
        vo.setDescription(board.getDescription());
        vo.setIcon(board.getIcon());
        return vo;
    }
}
