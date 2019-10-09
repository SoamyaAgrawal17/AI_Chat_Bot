package algorithm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

@Getter
public final class KnowledgeBase {

    @NotEmpty
    private final List<Map<String, List<String>>> datasetList;

    KnowledgeBase(@JsonProperty("datasetList")List<Map<String, List<String>>> datasetList) {
        this.datasetList = datasetList;
    }
}
