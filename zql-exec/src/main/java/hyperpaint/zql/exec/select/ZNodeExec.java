package hyperpaint.zql.exec.select;

import hyperpaint.zql.lang.znode.ZNode;
import hyperpaint.zql.lang.znode.ZNodeCollection;
import hyperpaint.zql.lang.znode.ZNodeList;
import hyperpaint.zql.lang.znode.ZNodePath;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
class ZNodeExec {
    @FunctionalInterface
    interface ExecEntry {
        Map<String, String> run(ZooKeeper zooKeeper) throws InterruptedException, KeeperException;
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

    public static Map<String, String> convertZNodesToEntries(ZNodeExec[] execs, ZooKeeper zooKeeper) throws InterruptedException, KeeperException {
        final long startMilliseconds = System.currentTimeMillis();

        final var entries = new HashMap<String, String>();

        if (execs != null) {
            // todo parallel
            for (var exec : execs) {
                entries.putAll(exec.execEntry.run(zooKeeper));
            }
        }

        final long diffMilliseconds = System.currentTimeMillis() - startMilliseconds;
        if (log.isDebugEnabled()) {
            log.debug("Converting ZNodes to entries took {} millis", diffMilliseconds);
        } else if (diffMilliseconds > 1000) {
            log.warn("Converting ZNodes to entries took too long: {} millis", diffMilliseconds);
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

            /* Получить глубину и базовый путь */

            int depth = 1;
            final String path;

            {
                ZNode buff = zNode.getWrappedZnode();

                loop:
                while (true) {
                    switch (buff.getType()) {
                        case LIST -> {
                            buff = ((ZNodeList) buff).getWrappedZnode();
                            depth++;
                        }
                        case PATH -> {
                            path = ((ZNodePath) buff).getPath();
                            break loop;
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + buff.getType());
                    }
                }
            }

            /* Получить ZNode вместе с данными */

            /* Начальные узлы */
            List<String> prev, next = zooKeeper.getChildren(path, null).parallelStream().map(s -> path.equals("/") ? path + s : path + "/" + s).toList();

            /* Промежуточные узлы */
            for (int i = 1; i < depth; i++) {
                prev = next;

                for (var item : prev) {
                    next = zooKeeper.getChildren(item, null).parallelStream().map(s -> item.equals("/") ? item + s : item + "/" + s).toList();
                }
            }

            /* Конечные узлы */
            // todo parallel
            for (var item : next) {
                final byte[] bytes = zooKeeper.getData(item, null, null);
                final String data = bytes != null ? new String(bytes, StandardCharsets.UTF_8) : null;
                entries.put(item, data);
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
