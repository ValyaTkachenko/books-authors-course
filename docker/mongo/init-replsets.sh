#!/bin/bash
# Ініціалізація replica set для config-сервера та трьох шардів (по одному вузлу в кожному).
init_rs() {
  local host="$1" rs="$2" extra="$3"
  until mongosh --host "$host" --quiet --eval "db.adminCommand({ping:1}).ok" >/dev/null 2>&1; do
    echo "waiting for $host ..."; sleep 2
  done
  mongosh --host "$host" --quiet --eval "
    try { rs.status(); print('$rs already initiated'); }
    catch (e) { printjson(rs.initiate({_id: '$rs', $extra members: [{_id: 0, host: '$host:27017'}]})); }"
  until mongosh --host "$host" --quiet --eval "quit(db.hello().isWritablePrimary ? 0 : 1)" >/dev/null 2>&1; do
    echo "waiting for PRIMARY on $host ..."; sleep 1
  done
  echo "$rs is ready"
}

init_rs configsvr cfgrs "configsvr: true,"
init_rs shard1 shard1rs ""
init_rs shard2 shard2rs ""
init_rs shard3 shard3rs ""
echo "All replica sets initiated."
