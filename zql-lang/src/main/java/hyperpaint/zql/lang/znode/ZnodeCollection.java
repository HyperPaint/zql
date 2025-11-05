package hyperpaint.zql.lang.znode;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

@Getter
@ToString(callSuper = true)
public class ZnodeCollection extends Znode {
    private final List<Znode> list;

    public ZnodeCollection(@NonNull List<Znode> list) {
        super(Type.COMMA);

        this.list = list;
    }

    @Override
    public String toZql(boolean formatted) {
        final StringBuilder result = new StringBuilder();

        if (formatted) {
            for (var iterator = list.iterator(); iterator.hasNext(); ) {
                result.append(iterator.next().toZql(true));

                if (iterator.hasNext()) {
                    result.append(",\n");
                }
            }
        } else {
            for (var iterator = list.iterator(); iterator.hasNext(); ) {
                result.append(iterator.next().toZql(false));

                if (iterator.hasNext()) {
                    result.append(", ");
                }
            }
        }

        return result.toString();
    }

    @Override
    public Znode[] toComponents() {
        return list.toArray(new Znode[0]);
    }
}
