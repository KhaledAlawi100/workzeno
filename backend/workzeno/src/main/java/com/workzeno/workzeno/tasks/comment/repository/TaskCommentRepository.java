
package com.workzeno.workzeno.tasks.comment.repository;

import com.workzeno.workzeno.tasks.comment.domain.TaskComment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Database operations for task comments.
 */
public interface TaskCommentRepository
        extends JpaRepository<TaskComment, UUID> {


    @Query(
            value = """
                    SELECT
                        comment.id AS id,
                        comment.task.id AS taskId,
                        comment.author.id AS authorId,
                        comment.author.email AS authorEmail,
                        comment.content AS content,
                        comment.createdAt AS createdAt,
                        comment.updatedAt AS updatedAt
                    FROM TaskComment comment
                    WHERE comment.task.id = :taskId
                    ORDER BY comment.createdAt ASC, comment.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(comment)
                    FROM TaskComment comment
                    WHERE comment.task.id = :taskId
                    """
    )
    Page<TaskCommentView> findCommentViewsByTaskId(
            @Param("taskId") UUID taskId,
            Pageable pageable
    );
}
