package co.kirikiri.domain.goalroom;

import co.kirikiri.domain.member.Member;
import co.kirikiri.exception.NotFoundException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoalRoomMembers {

    private static final int MIN_SIZE_TO_FIND_NEXT_LEADER = 1;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true, mappedBy = "goalRoom")
    private final List<GoalRoomMember> values = new ArrayList<>();

    public GoalRoomMembers(final List<GoalRoomMember> values) {
        this.values.addAll(new ArrayList<>(values));
    }

    public void add(final GoalRoomMember goalRoomMember) {
        this.values.add(goalRoomMember);
    }

    public void addAll(final List<GoalRoomMember> goalRoomMembers) {
        this.values.addAll(new ArrayList<>(goalRoomMembers));
    }

    public Optional<GoalRoomMember> findByMember(final Member member) {
        return values.stream()
                .filter(value -> value.isSameMember(member))
                .findFirst();
    }

    public Optional<GoalRoomMember> findNextLeader() {
        if (size() <= MIN_SIZE_TO_FIND_NEXT_LEADER) {
            return Optional.empty();
        }
        values.sort(Comparator.comparing(GoalRoomMember::getJoinedAt));
        return Optional.of(values.get(1));
    }

    public boolean isMember(final Member member) {
        return values.stream()
                .anyMatch(value -> value.isSameMember(member));
    }

    public boolean isNotLeader(final Member member) {
        final Member goalRoomLeader = findGoalRoomLeader();
        return !goalRoomLeader.equals(member);
    }

    public Member findGoalRoomLeader() {
        return values.stream()
                .filter(GoalRoomMember::isLeader)
                .findFirst()
                .map(GoalRoomMember::getMember)
                .orElseThrow(() -> new NotFoundException("골룸의 리더가 없습니다."));
    }

    public int size() {
        return values.size();
    }

    public void remove(final GoalRoomMember goalRoomMember) {
        values.remove(goalRoomMember);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public List<GoalRoomMember> getValues() {
        return values;
    }
}
