# Beer Fridge
A beer fridge monitoring program that interfaces with an Arduino based sensor module to display several types of information about the fridge such as temperature, amount of beer left, beer history and beer ratings. The sensor module uses a digital load cell to measure the weight of the keg and uses this information to determine how full the keg is. The module also includes a temperature sensor which reads the temperature from inside the fridge (the temperature sensor is not actually inside the keg). 

The onboard wifi of the sensor posts data in JSON format to <a href="http://dweet.io/">dweet.io</a> via an HTTP get request. On the other end this data is polled from dweet.io and displayed on the GUI application.

The GUI application is exported as a .jar file and run on a touch screen Zero Client running off of a VM. The program uses the java-json library for parsing and writing data. The library can be downloaded <a href="http://www.java2s.com/Code/JarDownload/java/java-json.jar.zip">here</a> and added to your java class path if it isn't already included by your IDE.dsgds

The sensor module uses the <a href="https://store.ti.com/cc3200-launchxl.aspx">TI CC3200 Launchpad</a> which is essentially an Arduino with built in wifi and other peripherals. The <a href="http://energia.nu/">Energia</a> IDE is used to program the device. The library found <a href="https://github.com/bogde/HX711">here</a> is used to take readings from the load cell. Download the library as a zip or clone the repo and then add the unzipped folder to the libraries folder of your Energia IDE.

The project was initially intended to be used as a coffee pot monitoring program that would use a web based application to display coffee information. However, the concept was transfered to a beer fridge instead.

Credit to <a href="https://www.linkedin.com/in/greg-powell-b3b88515/">Greg Powell</a> for the original idea.

## How to Setup
