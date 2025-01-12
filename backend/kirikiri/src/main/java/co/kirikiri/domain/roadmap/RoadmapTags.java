package co.kirikiri.domain.roadmap;

import co.kirikiri.domain.roadmap.vo.RoadmapTagName;
import co.kirikiri.exception.BadRequestException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapTags {

    private static final int MAX_COUNT = 5;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true)
    @JoinColumn(name = "roadmap_id", updatable = false, nullable = false)
    private final Set<RoadmapTag> values = new HashSet<>();

    public RoadmapTags(final List<RoadmapTag> roadmapTags) {
        validate(roadmapTags);
        values.addAll(new HashSet<>(roadmapTags));
    }

    private void validate(final List<RoadmapTag> roadmapTags) {
        validateCount(roadmapTags);
        validateDuplicatedName(roadmapTags);
    }

    private void validateCount(final List<RoadmapTag> roadmapTags) {
        if (roadmapTags.size() > MAX_COUNT) {
            throw new BadRequestException(
                    String.format("태그의 개수는 최대 %d개까지 가능합니다.", MAX_COUNT));
        }
    }

    private void validateDuplicatedName(final List<RoadmapTag> roadmapTags) {
        final Set<RoadmapTagName> nonDuplicatedNames = roadmapTags.stream()
                .map(RoadmapTag::getName)
                .collect(Collectors.toSet());
        if (roadmapTags.size() != nonDuplicatedNames.size()) {
            throw new BadRequestException("태그 이름은 중복될 수 없습니다.");
        }
    }

    public void addAll(final RoadmapTags tags) {
        this.values.addAll(new HashSet<>(tags.values));
    }

    public Set<RoadmapTag> getValues() {
        return new HashSet<>(values);
    }
}
