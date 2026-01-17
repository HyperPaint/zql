#!/bin/bash

echo "$(curl -s "http://localhost:8080/query-as-metrics?query=$(echo "$1" | sed -e 's/ /%20/g' | sed -e 's/+/%2B/g' | sed -e 's/*/%2A/g' | sed -e 's;/;%2F;g')")"
