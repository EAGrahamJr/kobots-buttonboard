#!/usr/bin/python3

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

_MAX_TEMP = 65
_MID_TEMP = 45

logging.basicConfig(format='%(asctime)s %(message)s', datefmt='%m/%d/%Y %I:%M:%S %p')

try:
    last_temp = 0
    while True:
        temp = subprocess.getoutput("vcgencmd measure_temp|sed 's/[^0-9.]//g'")
        if round(float(temp)) >= _MAX_TEMP:
            dc = 100
            nap = 180
        if round(float(temp)) >= _MID_TEMP:
            dc = 70
            nap = 120.0
        else:
            dc = 50
            nap = 60.00

        if temp != last_temp:
            pwm.ChangeDutyCycle(dc)
            logging.warning(f"CPU Temp: {float(temp)} Fan duty cycle: {dc}")
            time.sleep(nap)
            last_temp = temp

except KeyboardInterrupt:
    pwm.stop()
    GPIO.cleanup()
    print("Ctrl + C pressed -- Ending program")
