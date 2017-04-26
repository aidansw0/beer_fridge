# Beer Fridge
A beer fridge monitoring program that interfaces with an Arduino based sensor module to display several types of information about the fridge such as temperature, amount of beer left, beer history and beer ratings. The sensor module uses a digital load cell to measure the weight of the keg and uses this information to determine how full the keg is. The module also includes a temperature sensor which reads the temperature from inside the fridge (the temperature sensor is not actually inside the keg). 

The onboard wifi of the sensor posts data in JSON format to <a href="http://dweet.io/">dweet.io</a> via an HTTP get request. On the other end this data is polled from dweet.io and displayed on the GUI application.

The GUI application is exported as a .jar file and run on a touch screen Zero Client running off of a VM. The program uses the java-json library for parsing and writing data. The library can be downloaded <a href="http://www.java2s.com/Code/JarDownload/java/java-json.jar.zip">here</a> and added to your java class path if it isn't already included by your IDE.dsgds

The sensor module uses the <a href="https://store.ti.com/cc3200-launchxl.aspx">TI CC3200 Launchpad</a> which is essentially an Arduino with built in wifi and other peripherals. The <a href="http://energia.nu/">Energia</a> IDE is used to program the device. The library found <a href="https://github.com/bogde/HX711">here</a> is used to take readings from the load cell. Download the library as a zip or clone the repo and then add the unzipped folder to the libraries folder of your Energia IDE.

The project was initially intended to be used as a coffee pot monitoring program that would use a web based application to display coffee information. However, the concept was transfered to a beer fridge instead.

Credit to <a href="https://www.linkedin.com/in/greg-powell-b3b88515/">Greg Powell</a> for the original idea.

## Setting up the Sensor
The sensor module consists of the CC3200 Launchpad as well as a <a href="https://www.sparkfun.com/products/13879">HX711 load cell amplifier</a>, a <a href="https://www.sparkfun.com/products/13878?_ga=1.196320228.931348548.1481592610">load cell combinator board</a> and four <a href="https://www.sparkfun.com/products/10245">50 kg load cells</a>. Follow the respective documentation of the latter three items for exact information on their wire connections.

Once completed the load cell unit can be connected to the launchpad (refer to <a href="http://energia.nu/wordpress/wp-content/uploads/2014/06/LaunchPads-CC3200-%E2%80%94-Pins-Maps-12-28.jpeg">this</a> document for Launchpad pins):

#### HX711 -----> Launchpad
* Vcc   -----> 3.3V   (pin 1)
* Vdd   -----> 5V     (pin 2)
* DAT   -----> pin 27
* CLK   -----> pin 28
* GND   -----> GND

DAT and CLK can be connected to any digital pins on the Launchpad but must be configured as such in the .ino file.

Next start Energia and open the `arduino_sensor.ino` file. Near the top of the file fill in your wifi credentials in the fields `char ssid[]` and `char password[]` respectively. Lastly, enter a unqiue "thing name" for your device in the field `#define THING_NAME` also near the top of the file; this is used to relay sensor data to the Java application via dweet.io.

There is also un-implemented code in this file that can be used to send notifications to a <a href="https://slack.com/?cvosrc=ppc.google.slack&cvo_campaign=&cvo_crid=189426831117&Matchtype=p&utm_source=google&utm_medium=ppc&utm_campaign=generalbrand&c3api=5542,189426831117,slack&gclid=CM2m-ZT7wNMCFQt3fgodmmIHtQ">Slack</a> chat channel, however this code may need to be heavily editied for it to work.

Connect the Launchpad to the computer via USB and short pins ENTER_PIN_HERE and ENTER_PIN_HERE to set it to programming mode then upload the .ino file.

### Setting up the Java Application
CLone this repo and open the project in your Java IDE. Locate the file `src/backend/KegManager.java` and change the field `DWEET_URL` declared just below the class declaration to the **same** "thing name" that you used when editing `arduino_sensor.ino`. Compile the program to ensure there are no errors and then export the application as a runnable `.jar` file; place the `.jar` file where desired but note that data files for the program will be generated at the same location.

* In it's current state the GUI for the application is not completely responsive on a 16:9 aspect ratio display.

The GUI itself is straight forward to use and is most natural on a touch screen, however a mouse can still be used HOW_TO_ENABLE. Users are verified using a USB RFID card scanner; administrators can add beers along with other options found in the admin panel while regular users can only up-vote a single beer until an admin resets the voting. To add an admin locate the file `src/backend/KeyCardListener.java` and add the line `saveData.setAdmin("your key card id here", true);` (before exporting the `.jar` file) in the constructor below the initialization of `saveData`. See the RFID scanner setup section for info on how to find your key card ID. Once an initial admin has been set new ones can be added via the admin panel. 

### Setting up the RFID Scanner
The beer fridge uses a <a href="https://www.rfideas.com/products/readers/pcprox">RFIDeas pcProx card reader</a> to track and verify users however, with some modification to the source code this can be disabled to allow anyone to access the system. 

Start the pcProx <a href="https://www.rfideas.com/support/product-support/pcprox-plus">configuration utility</a> and connect the scanner. Navigate first to the `SDK` tab and ensure that the `Disable Keystrokes for SDK` field is unchecked and then move to the `Format` tab and check the fields `Send ID` and `Send ID as hexidecimal number`; click the `Write Settings` button and wait for the program to finish. At the bottom of the window there is a green text field, give this field focus by clicking on it and scan your RFID card to ensure that the it is working. Here you can also copy and paste your card ID (without the forward slash) into the `.setAdmin()` call in the Java program.

Simply plug the RFID scanner into whichever computer is being used to run the application while the application is running and key card should automatically be verified.

### Bringing the System Together
TODO
