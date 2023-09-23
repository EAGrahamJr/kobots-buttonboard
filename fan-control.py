#!/usr/bin/python3

#  Copyright 2022-2023 by E. A. Graham, Jr.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
#  or implied. See the License for the specific language governing
#  permissions and limitations under the License.

import RPi.GPIO as GPIO
import logging
import subprocess
import time

GPIO.setmode(GPIO.BCM)
GPIO.setup(14, GPIO.OUT)
pwm = GPIO.PWM(14, 100)

print("\nPress Ctrl+C to quit \n")
dc = 0
pwm.start(dc)

_MAX_TEMP = 83
_MID_TEMP = 75
_LOW_TEMP = 65

logging.basicConfig(format='%(asctime)s %(message)s', datefmt='%m/%d/%Y %I:%M:%S %p')

try:
    last_dc = 0

    while True:
        reading = subprocess.getoutput("vcgencmd measure_temp")[5:9]
        temp = round(float(reading))
        if temp >= _MAX_TEMP:
            dc = 100
            nap = 180
        elif temp >= _MID_TEMP:
            dc = 80
            nap = 120.0
        elif temp >= _LOW_TEMP:
            dc = 65
            nap = 90.0
        else:
            dc = 50
            nap = 60.00

        if dc != last_dc:
            pwm.ChangeDutyCycle(dc)
            logging.warning(f"CPU Temp: {float(temp)} Fan duty cycle: {dc}")
            time.sleep(nap)
            last_dc = dc

except KeyboardInterrupt:
    pwm.stop()
    GPIO.cleanup()
    print("Ctrl + C pressed -- Ending program")
