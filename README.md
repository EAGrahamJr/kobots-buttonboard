# I2C Button Board

The world's most expensive remote control for HomeAssistant. Uses

- [HAssK client](https://github.com/EAGrahamJr/HAssK)
- [kobots=-devices](https://github.com/EAGrahamJr/kobots-devices)
  - and by extension [diozero](https://www.diozero.com/)
- Lightbend's [Typesafe config](https://github.com/lightbend/config) for the configuration.

### Cost

(discounting the LEGO pieces):

| Item                                      |       Price |
|-------------------------------------------|------------:|
| Raspberry Pi 4<br/><sup>(ref Covid)</sup> |     $225.00 |
| SparkFun Qwiic pHat v2                    |       $8.00 |
| 128x128 OLED display                      |      $23.00 |
| 128x32 OLED display                       |       $4.00 |
| NeoKey 1x4 I2C                            |      $10.00 |
| Keys                                      |       $3.50 |
| Key caps                                  |       $2.50 |
| Extra long STEMMA cables                  |      $10.00 |
| **TOTAL**                                 | **$286.00** |

### Mk 2

![Holy cow!](most-expensive.jpg)

- The secondary screen shows a continually updating list of temperatures (rooms, CPU, and "outside").
- Each button corresponds to a menu selection on the larger screen (next/prev buttons on others)
- Also has a "sleep" mode to prevent screen burn in (from Mk 1)<br/>![Snooze](sleeping.jpg)
  - Exit button is active, others just "wake up" the menu
