package hyperpaint.zql.exec.select;

import hyperpaint.zql.lang.znode.ZNode;
import hyperpaint.zql.lang.znode.ZNodeCollection;
import hyperpaint.zql.lang.znode.ZNodePath;
import hyperpaint.zql.lang.znode.ZNodeList;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.nio.charset.StandardCharsets;
import java.util.*;

class ZNodeExec {
    @FunctionalInterface
    interface ExecEntry {
        Map<String, String> run(ZooKeeper zooKeeper) throws Exception;
    }

    final ExecEntry execEntry;

    private ZNodeExec(ZNode zNode) {
        execEntry = buildExecEntry(zNode);
    }

    public static ZNodeExec[] get(ZNode zNode) {
        if (zNode == null) {
            return null;
        }

        if (zNode instanceof ZNodeCollection zNodeCollection) {
            final var list = zNodeCollection.getList();
            final var result = new ZNodeExec[list.size()];

            for (int i = 0; i < list.size(); i++) {
                result[i] = new ZNodeExec(list.get(i));
            }

            return result;
        } else {
            return new ZNodeExec[] { new ZNodeExec(zNode) };
        }
    }

    public static Map<String, String> convertZNodesToEntries(ZNodeExec[] execs, ZooKeeper zooKeeper) throws Exception {
        final var entries = new HashMap<String, String>();

        if (execs != null) {
            for (var exec : execs) {
                entries.putAll(exec.execEntry.run(zooKeeper));
            }
        }

        return entries;
    }

    // region ExecEntry

    static ExecEntry buildExecEntry(ZNode zNode) {
        return switch (zNode) {
            case ZNodeList zNodeList -> buildExecEntry(zNodeList);
            case ZNodePath zNodePath -> buildExecEntry(zNodePath);
            default -> throw new IllegalStateException("Unexpected value: " + zNode);
        };
    }

    private static ExecEntry buildExecEntry(ZNodeList zNode) {
        return zooKeeper -> {
            final Map<String, String> entries = new HashMap<>();

            /* Получить изначальный путь и количество ls */

            final String path;

            int ls = 1;
            ZNode buffZNode = zNode.getWrappedZnode();

            main:
            while (true) {
                switch (buffZNode.getType()) {
                    case LIST -> {
                        ls++;
                        buffZNode = ((ZNodeList) buffZNode).getWrappedZnode();
                    }
                    case PATH -> {
                        path = ((ZNodePath) buffZNode).getPath();
                        break main;
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + buffZNode.getType());
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
        };
    }

    private static ExecEntry buildExecEntry(ZNodePath zNode) {
        return zooKeeper -> {
            final Map<String, String> entries = new HashMap<>(1);

            final String path = zNode.getPath();
            final boolean exists = zooKeeper.exists(path, null) != null;

            if (exists) {
                final byte[] bytes = zooKeeper.getData(path, null, null);
                final String data = bytes != null ? new String(bytes, StandardCharsets.UTF_8) : null;
                entries.put(path, data);
            }

            return entries;
        };
    }

    // endregion
}
