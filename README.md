# ZQL

## ZooKeeper Query Language

ZQL представляет собой специальный язык запросов, предназначенный для работы с распределенной координационной системой Apache ZooKeeper.
Этот язык позволяет пользователям извлекать и анализировать данные, хранящиеся в узлах ZooKeeper, используя синтаксис, схожий с SQL-запросами.

#№ Примеры запросов

<p>Показать содержимое корня</p>

```select path, data from /;```

<p>Показать содержимое содержимого корня</p>

```select path, data from ls/;```

<p>Отфильтровать записи, поле в JSON которых равен единице</p>

```select path, json_path(data, '$.field') as json_field where json_field == '1';```

<p>Суммировать значения записей, в ноде /my/znode</p>

```select substr(path, 0, 8) as substr, sum(data) as sum from ls/my/znode group by substr;```

<p>Сложение и вычитание</p>

```select data + data - data as value from ls/my/znode;```

## Конфигурация

```
zql-exporter:
  zookeeper:
    host: 127.0.0.1
    port: 2181
    timeout:
      session: 10000
      connection: 10000
    ssl:
      enabled: false
      # Не обязательны при enabled: false
      key-store-location: keystore.jks
      key-store-password: password
      trust-store-location: truststore.jks
      trust-store-password: password
      hostname-verification: true
  exporter:
    endpoints:
      query-as-table:
        enabled: true
      query-as-metrics:
        enabled: true
      queries:
        enabled: true
      metrics:
        enabled: true
    metrics:
      - query: "select path, data, 1 from /;"
        name: root
      - query: "select path, data, 1 from ls/;"
        name: ls_one
      - query: "select path, data, 1 from ls/ls/;"
        name: ls_two
      - query: "select path, data, 1 from ls/ls/ls/;"
        name: ls_three
      - query: "select path, data/data as data_div_data from ls/ls/;"
        name: test

logging:
  level:
    root: info
    org.apache.zookeeper: warn
```
