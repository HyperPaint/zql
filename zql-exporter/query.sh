#!/bin/bash

echo "$(curl -s "http://localhost:8080/query?query=$(echo "$1" | sed -e 's/ /%20/g')")"
