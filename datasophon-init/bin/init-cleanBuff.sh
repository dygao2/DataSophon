#!/bin/bash

echo "sync"
sync
sync
sync

echo "sleep"
sleep 10

echo 1 >/proc/sys/vm/drop_caches
echo 2 >/proc/sys/vm/drop_caches
echo 3 >/proc/sys/vm/drop_caches

echo "done"
