#!/usr/bin/python3

import adafruit_seesaw.seesaw
import board

# executes from here
i2c = board.I2C()

for i in range(1024):
    try:
        seesaw = adafruit_seesaw.seesaw.Seesaw(i2c, 0x60)
        print(f"Success after {i} iterations")
        break
    except Exception as e:
        pass
else:
    print("Failed after 1024")
