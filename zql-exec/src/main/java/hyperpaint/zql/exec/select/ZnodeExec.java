package hyperpaint.zql.exec.select;

import hyperpaint.zql.lang.znode.Znode;
import hyperpaint.zql.lang.znode.ZnodeCollection;
import hyperpaint.zql.lang.znode.ZnodePath;
import hyperpaint.zql.lang.znode.ZnodeList;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZnodeExec {
    @FunctionalInterface
    public interface ZnodeToEntry {
        Map<String, String> run(ZooKeeper zooKeeper) throws InterruptedException, KeeperException;
    }

    private final ZnodeToEntry[] znodeToEntry;

    public ZnodeExec(Znode znode) {
        switch (znode) {
            case ZnodeCollection znodeCollection -> {
                final List<Znode> list = znodeCollection.getList();
                znodeToEntry = new ZnodeToEntry[list.size()];

                for (int i = 0; i < list.size(); i++) {
                    final ZnodeExec znode1 = new ZnodeExec(list.get(i));

                    znodeToEntry[i] = znode1.znodeToEntry[0];
                }
            }
            case ZnodePath znodePath -> znodeToEntry = new ZnodeToEntry[]{zooKeeper -> {
                final Map<String, String> entries = new HashMap<>(1);

                final String path = znodePath.getPath();
                final boolean exists = zooKeeper.exists(path, null) != null;

                if (exists) {
                    final byte[] bytes = zooKeeper.getData(path, null, null);
                    final String data = bytes != null ? new String(bytes, StandardCharsets.UTF_8) : null;
                    entries.put(path, data);
                }

                return entries;
            }};
            case ZnodeList znodeList -> znodeToEntry = new ZnodeToEntry[]{zooKeeper -> {
                final Map<String, String> entries = new HashMap<>();

                /* Получить изначальный путь и количество ls */

                final String path;

                int ls = 1;
                Znode buffZnode = znodeList.getWrappedZnode();

                main:
                while (true) {
                    switch (buffZnode.getType()) {
                        case LIST -> {
                            ls++;
                            buffZnode = ((ZnodeList) buffZnode).getWrappedZnode();
                        }
                        case PATH -> {
                            path = ((ZnodePath) buffZnode).getPath();
                            break main;
                        }
                        default -> throw new IllegalArgumentException("Unexpected value: " + buffZnode.getType());
                    }
                }

                /* Получить znode вместе с данными */

                List<String> prev, next = null;

                for (int i = 0; i < ls; i++) {
                    /* Корневой znode */
                    if (i == 0) {
                        next = zooKeeper.getChildren(path, null).stream().map(s -> path.equals("/") ? path + s : path + "/" + s).toList();
                    }

                    /* Промежуточные znode */
                    if (i != ls - 1) {
                        prev = next;
                        next = new ArrayList<>();

                        for (var item : prev) {
                            next.addAll(zooKeeper.getChildren(item, null).stream().map(s -> item.equals("/") ? item + s : item + "/" + s).toList());
                        }
                    }

                    /* Необходимая глубина znode */
                    if (i == ls - 1) {
                        prev = next;
                        for (var item : prev) {
                            final boolean exists = zooKeeper.exists(item, null) != null;

                            if (exists) {
                                final byte[] bytes = zooKeeper.getData(item, null, null);
                                final String data = bytes != null ? new String(bytes, StandardCharsets.UTF_8) : null;
                                entries.put(item, data);
                            }
                        }
                    }
                }

                return entries;
            }};
            case null -> znodeToEntry = new ZnodeToEntry[0];
            default -> throw new IllegalArgumentException("Unexpected value: " + znode);
        }
    }

    public Map<String, String> znodesToEntries(ZooKeeper zooKeeper) throws InterruptedException, KeeperException {
        if (isNull()) {
            return new HashMap<>();
        }

        final var entries = new HashMap<String, String>();

        for (var item : znodeToEntry) {
            entries.putAll(item.run(zooKeeper));
        }

        return entries;
    }

    public boolean isNull() {
        return znodeToEntry == null;
    }

    public boolean isNotNull() {
        return znodeToEntry != null;
    }
}
