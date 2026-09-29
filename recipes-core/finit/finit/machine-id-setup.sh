#!/bin/bash

if [ ! -s /etc/machine-id ]; then
    uuidgen | tr -d '-' | tr 'A-F' 'a-f' | tee /etc/machine-id
fi
