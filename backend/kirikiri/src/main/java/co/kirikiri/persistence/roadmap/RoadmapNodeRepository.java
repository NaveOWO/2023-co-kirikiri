package co.kirikiri.persistence.roadmap;

import co.kirikiri.domain.roadmap.RoadmapContent;
import co.kirikiri.domain.roadmap.RoadmapNode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapNodeRepository extends JpaRepository<RoadmapNode, Long> {

    List<RoadmapNode> findAllByRoadmapContent(final RoadmapContent roadmapContent);
}
