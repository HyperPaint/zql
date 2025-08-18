package hyperpaint.zql.groups;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class GroupList extends AbstractGroup {
    private final @NonNull List<AbstractGroup> list;

    public GroupList(GroupType type, @NonNull List<AbstractGroup> list) {
        super(type);

        this.list = list;
    }
}
