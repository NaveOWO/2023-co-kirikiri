package co.kirikiri.domain.goalroom;

import co.kirikiri.domain.BaseCreatedTimeEntity;
import co.kirikiri.domain.ImageContentType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckFeed extends BaseCreatedTimeEntity {

    @Column(nullable = false)
    private String serverFilePath;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private ImageContentType imageContentType;

    @Column(nullable = false)
    private String originalFileName;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_room_roadmap_node_id", nullable = false)
    private GoalRoomRoadmapNode goalRoomRoadmapNode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "goal_room_member_id", nullable = false)
    private GoalRoomMember goalRoomMember;

    public CheckFeed(final String serverFilePath, final ImageContentType imageContentType,
                     final String originalFileName, final String description,
                     final GoalRoomRoadmapNode goalRoomRoadmapNode, final GoalRoomMember goalRoomMember) {
        this(serverFilePath, imageContentType, originalFileName, description, goalRoomRoadmapNode, goalRoomMember,
                null);
    }

    public CheckFeed(final String serverFilePath, final ImageContentType imageContentType,
                     final String originalFileName, final String description,
                     final GoalRoomRoadmapNode goalRoomRoadmapNode, final GoalRoomMember goalRoomMember, final
                     LocalDateTime createdAt) {
        this.serverFilePath = serverFilePath;
        this.imageContentType = imageContentType;
        this.originalFileName = originalFileName;
        this.description = description;
        this.goalRoomRoadmapNode = goalRoomRoadmapNode;
        this.goalRoomMember = goalRoomMember;
        this.createdAt = createdAt;
    }

    public String getServerFilePath() {
        return serverFilePath;
    }

    public String getDescription() {
        return description;
    }

    public GoalRoomMember getGoalRoomMember() {
        return goalRoomMember;
    }
}
