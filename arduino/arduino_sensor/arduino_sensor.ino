/*
  This file is to watch and report the amount of coffee
  left in the coffee pot for every minute.
*/
#include <Energia.h>
#include <Wire.h>
#include <HX711.h>
#include <WiFi.h>
#include <WiFiClient.h>

//#define DEBUG_HTTP
//#define DEBUG_SCALE
#define UPDATE_DWEET

// This is for the scale HX711
#define DOUT          8 //PIN_62
#define CLK           30 //PIN_50
#define SCALE         454

// Your network SSID and password
char ssid[] = "AndroidAP";
char password[] = "testpassword"; // test

// HTTP Request
#define HTTP_PORT           80
#define THING_NAME          "richardscoffeepot"
#define REQUEST_INTERVAL    2000

WiFiClient client;
unsigned long last_update = 0;
char dweet_server[] = "dweet.io";
char thingspeak_server[] = "api.thingspeak.com";
char slack_server[] = "hooks.slack.com";
int update_request = 0;
String received_data_string;

// Create HX711 object by passing the pin used on the launchpad
HX711 scale(DOUT, CLK);

// Signal processing
#define WINDOW_SIZE         20
#define STDEV_POUR_THRESH   300
#define STDEV_STABLE_THRESH 10
#define FULL_POT            1650 // in grams
unsigned long coffee_age = 0;
int data[WINDOW_SIZE];
int average_weight = 0;
bool pouring = false;
bool new_pot = false;

void setup() {
  //Initialize serial and wait for port to open:
  Serial.begin(115200);

  // Scale initialization
  scale.set_scale(SCALE);
  scale.tare(50);

  // Connect to Wifi
  Serial.print("Attempting to connect to Network named: ");
  Serial.println(ssid);

  // Connect to WPA/WPA2 network. Change this line if using open or WEP network:
  WiFi.begin(ssid, password);
  while ( WiFi.status() != WL_CONNECTED) {
    // print dots while we wait to connect
    Serial.print(".");
    delay(300);
  }

  Serial.println("\nYou're connected to the network");
  Serial.println("Waiting for an ip address");
  while (WiFi.localIP() == INADDR_NONE) {
    // print dots while we wait for an ip addresss
    Serial.print(".");
    delay(300);
  }

  Serial.println("\nIP Address obtained");
  // you're connected now, so print out the status
  printCurrentNet();
  printWifiData();

  last_update = millis();
  coffee_age = millis();

  //Initialize button interrupt for taring
  pinMode(PUSH1, INPUT_PULLUP);
  attachInterrupt(PUSH1, scale_tare, FALLING);
}

void loop() {
  int weight = round(scale.get_units(1));
#ifdef DEBUG_SCALE
  Serial.println(weight);
#endif

  // Add new data to queue for averaging
  enqueue_data(weight);

  if (average_weight > FULL_POT && new_pot == false) {
    Serial.println("New pot brewed");
    postToSlack(10);
    read_received_data();
    coffee_age = millis();
    new_pot = true;
  }
  else if (average_weight < FULL_POT - 50) {
    new_pot = false;
  }

#ifdef UPDATE_DWEET
  if (update_request > 0) {
    if (millis() - last_update > REQUEST_INTERVAL) {
      Serial.println("Updating dweet.io: someone is pouring");
      postToDweet(average_weight, true);
      read_received_data();
      check_received_data();
      last_update = millis();
      update_request--;
    }
  }
#endif

  // If standard deviation is high, someone must be pouring a cup
  if (stdev() > STDEV_POUR_THRESH || pouring == true) {
    if (pouring == false) {
      Serial.println("Someone is pouring a cup...");
      update_request = 1;
    }

    pouring = true;

    if (weight <= average_weight + 100) {
      pouring = false;
    }
  }
  // Weight only valid if standard deviation is less than threshold
  else if (stdev() < STDEV_STABLE_THRESH) {
    if (pouring == true) {
      pouring = false;
      Serial.print("Someone poured a cup: there was ");
      Serial.print(average_weight);
      Serial.print(", now there's ");
      Serial.println(mean());
    }

    average_weight = mean();

#ifdef UPDATE_DWEET
    if (update_request == 0) {
      if (millis() - last_update > REQUEST_INTERVAL) {
        postToDweet(average_weight, pouring);
        read_received_data();
        check_received_data();
        last_update = millis();
      }
    }
#endif

  } else {
    //Serial.println("[Exception]");
  }

  delay(500);
}

void printWifiData() {
  // print your WiFi IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);
  Serial.println(ip);

  // print your MAC address:
  byte mac[6];
  WiFi.macAddress(mac);
  Serial.print("MAC address: ");
  Serial.print(mac[5], HEX);
  Serial.print(":");
  Serial.print(mac[4], HEX);
  Serial.print(":");
  Serial.print(mac[3], HEX);
  Serial.print(":");
  Serial.print(mac[2], HEX);
  Serial.print(":");
  Serial.print(mac[1], HEX);
  Serial.print(":");
  Serial.println(mac[0], HEX);
}

void printCurrentNet() {
  // print the SSID of the network you're attached to:
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print the MAC address of the router you're attached to:
  byte bssid[6];
  WiFi.BSSID(bssid);
  Serial.print("BSSID: ");
  Serial.print(bssid[5], HEX);
  Serial.print(":");
  Serial.print(bssid[4], HEX);
  Serial.print(":");
  Serial.print(bssid[3], HEX);
  Serial.print(":");
  Serial.print(bssid[2], HEX);
  Serial.print(":");
  Serial.print(bssid[1], HEX);
  Serial.print(":");
  Serial.println(bssid[0], HEX);

  // print the received signal strength:
  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI):");
  Serial.println(rssi);

  // print the encryption type:
  byte encryption = WiFi.encryptionType();
  Serial.print("Encryption Type:");
  Serial.println(encryption, HEX);
  Serial.println();
}

void postToDweet(int weight, bool pouring) {
  int age_in_min;
  float temperature;
  bool new_pot;

  age_in_min = (millis() - coffee_age) / 1000 / 60;
  temperature = -0.0505 * age_in_min + 83.47;

  if (weight < 0) {
    weight = 0;
  }

  if (age_in_min < 5) {
    new_pot = true;
  }
  else {
    new_pot = false;
  }

  if (client.connect(dweet_server, HTTP_PORT)) {
#ifdef DEBUG_HTTP
    Serial.print("Sending ");
    Serial.println(weight);
#endif

    client.print(F("GET /dweet/for/"));
    client.print(THING_NAME);
    client.print(F("?weight="));
    client.print(weight);
    client.print(F("&pouring="));
    client.print(pouring);
    client.print(F("&age="));
    client.print(age_in_min);
    client.print(F("&temp="));
    client.print(temperature, 1);
    client.print(F("&newpot="));
    client.print(new_pot);
    client.println(F(" HTTP/1.1"));

    client.println(F("Host: dweet.io"));
    client.println(F("Connection: close"));
    client.println(F(""));
  }
}

bool postToSlack(int retries) {
  for (int i = 0; i < retries; i++) {
    if (client.connect(thingspeak_server, HTTP_PORT)) {
      Serial.println("Posting to slack");
      client.print(F("GET /apps/thinghttp/send_request?api_key=F901JQEU48SBZ7VL"));
      client.println(F(" HTTP/1.1"));

      client.println(F("Host: api.thingspeak.com"));
      client.println(F("Connection: close"));
      client.println(F(""));

      return true;
    }
    else {
      Serial.print("Posting failed.. attempt #");
      Serial.println(i + 1);
      read_received_data();
    }
  }
  return false;
}

bool postMessageToSlack(String msg) {
  const char* host = "hooks.slack.com";
  String slack_username = "coffeepot";
  String slack_hook_url = "/services/T4VQS0ZMM/B4V6J3WKU/SCP8SnUAQagbEmlMTGui1A8p";

  Serial.print("Connecting to ");
  Serial.println(host);

  // Use WiFiClient class to create TCP connections
  //WiFiClientSecure client;
  const int httpsPort = 443;
  if (!client.connect(host, httpsPort)) {
    Serial.println("Connection failed :-(");
    return false;
  }

  // We now create a URI for the request

  Serial.print("Posting to URL: ");
  Serial.println(slack_hook_url);

  String postData = "payload={\"link_names\": 1, \"username\": \"" +
                    slack_username + "\", \"text\": \"" + msg + "\"}";

  // This will send the request to the server
  client.print(String("POST ") + slack_hook_url + " HTTP/1.1\r\n" +
               "Host: " + host + "\r\n" +
               "Content-Type: application/x-www-form-urlencoded\r\n" +
               "Connection: close" + "\r\n" +
               "Content-Length:" + postData.length() + "\r\n" +
               "\r\n" + postData);
  Serial.println("Request sent");
  String line = client.readStringUntil('\n');
  Serial.print("Response code was: ");
  Serial.println(line);
  if (line.startsWith("HTTP/1.1 200 OK")) {
    return true;
  } else {
    return false;
  }
}

boolean read_received_data() {
  boolean valid_data = false;
  /* if there is incoming data from the connection
     then read it */
  if ( client.available() > 0 ) {
    /* copy each character into the data string */
    do {
      char c = client.read();
      received_data_string += c;
    } while (client.available());
    /* valid data received */
    valid_data = true;
  }

  return valid_data;
}

void check_received_data()
{
  int string_index;
  int data_length;

  /* get data length */
  data_length = received_data_string.length();
  /* check if the message starts as expected */
  if (received_data_string.startsWith("HTTP/1.1 200 OK")) {

    /* find "this:" field */
    string_index = received_data_string.indexOf("this");
    /* if the previous field is whithn the data string */
    if (string_index < data_length) {
      /* check if get request has succeeded */
#ifdef DEBUG_HTTP
      if (received_data_string.startsWith("succeeded", string_index + 7)) {
        Serial.println("Request acknowledged");
      }
      else {
        Serial.println("Request not acknowledged");
      }
#endif
    }
  }

  /* clean received data string */
  received_data_string.remove(0);
}

void scale_tare () {
  Serial.println("Taring!");
  scale.tare(50);
}

void enqueue_data(int new_data) {
  static int pointer = 0;
  data[pointer] = new_data;

  if (pointer < WINDOW_SIZE - 1) {
    pointer++;
  }
  else {
    pointer = 0;
  }
}

float stdev() {
  float sum = 0.0;
  float mean = 0.0;
  float std = 0.0;

  for (int i = 0; i < WINDOW_SIZE; i++) {
    sum += data[i];
  }
  mean = sum / WINDOW_SIZE;

  for (int i = 0; i < WINDOW_SIZE; i++) {
    std += pow(data[i] - mean, 2);
  }

  return sqrt(std / WINDOW_SIZE);
}

float mean() {
  float sum = 0.0;

  for (int i = 0; i < WINDOW_SIZE; i++) {
    sum += data[i];
  }

  return sum / WINDOW_SIZE;
}
