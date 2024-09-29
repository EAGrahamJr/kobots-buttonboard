#!/bin/bash

#
# Copyright 2022-2024 by E. A. Graham, Jr.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
# or implied. See the License for the specific language governing
# permissions and limitations under the License.
#

# Check if the command-line parameter is provided
if [ $# -eq 0 ]; then
    echo "No arguments provided"
    exit 1
fi

# Parse the command-line parameter
case "$1" in
    install)
        sudo cp $2.service /etc/systemd/user/

        systemctl --user daemon-reload
        systemctl --user start $2.service
        systemctl --user enable $2.service
        loginctl enable-linger

        echo "Service $2 installed successfully"
        ;;
    stop)
        systemctl --user stop $2.service
        echo "Service $2 stopped successfully"
        ;;
    start)
        systemctl --user start $2.service
        echo "Service $2 started successfully"
        ;;
    status)
        systemctl --user status $2.service
        ;;
    enable)
        systemctl --user enable $2.service
        echo "Service $2 enabled successfully"
        ;;
    disable)
        systemctl --user disable $2.service
        echo "Service $2 disabled successfully"
        ;;
    less)
      journalctl --user-unit $2.service --no-pager | less -S
      ;;
    log)
      journalctl --user-unit $2.service -f
      ;;
    *)
        echo "Invalid argument"
        exit 1
        ;;
esac

exit 0
