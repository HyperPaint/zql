#!/bin/bash

echo "$(curl -s "http://localhost:8080/query?format=table&query=$(echo "$1" | sed -e 's/ /%20/g')")"
