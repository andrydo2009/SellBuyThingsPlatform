package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.comment.Comment;
import ru.skypro.homework.dto.comment.Comments;
import ru.skypro.homework.dto.comment.CreateOrUpdateComment;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.service.CommentMapper;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapperImpl implements CommentMapper {
    @Override
    public CommentEntity toCommentEntity(CreateOrUpdateComment createOrUpdateComment, CommentEntity commentEntity) {
        commentEntity.setText(createOrUpdateComment.getText());
        commentEntity.setCreatedAt(Instant.now().toEpochMilli());
        return commentEntity;
    }

    @Override
    public Comment toComment(CommentEntity commentEntity) {
        Comment comment = new Comment();
        comment.setAuthor(commentEntity.getUserEntity().getId());
        comment.setAuthorImage("/users/image/" + commentEntity.getUserEntity().getId());
        comment.setAuthorFirstName(commentEntity.getUserEntity().getFirstName());
        comment.setCreatedAt(commentEntity.getCreatedAt());
        comment.setPk(commentEntity.getId());
        comment.setText(commentEntity.getText());
        return comment;
    }

    @Override
    public Comments toComments(List<CommentEntity> commentEntityList) {
        Comments comments = new Comments();
        comments.setCount(commentEntityList.size());
        comments.setResults(commentEntityList.stream()
                .map(this::toComment)
                .collect(Collectors.toList()));
        return comments;
    }
}
