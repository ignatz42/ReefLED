/*
  Copyright (c) 2009-2016, iggy@nerdysouth.org
  All rights reserved.

  Redistribution and use in source and binary forms, with or without 
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice, 
    this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice, 
    this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
  * Neither the name of the nerdysouth.org nor the names of its contributors may
    be used to endorse or promote products derived from this software without 
	specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

#include <MeetAndroid.h>
#include <Time.h>
#include <Wire.h>
#include <DS1307RTC.h>
#include <OneWire.h>
#include <EEPROM.h>
#include <string.h>

/* EEPROM Memory Address mappings and helpers */
#define BLUE_ON_HOUR     0
#define BLUE_ON_MINUTE   1
#define BLUE_RS_HOUR     2
#define BLUE_RS_MINUTE   3
#define BLUE_OFF_HOUR    4
#define BLUE_OFF_MINUTE  5
#define BLUE_CHASE_PCT   6
#define BLUE_LED_MAX     7
#define WHITE_ON_HOUR    8
#define WHITE_ON_MINUTE  9
#define WHITE_RS_HOUR    10
#define WHITE_RS_MINUTE  11
#define WHITE_OFF_HOUR   12
#define WHITE_OFF_MINUTE 13
#define WHITE_CHASE_PCT  14
#define WHITE_LED_MAX    15
#define WARN_TEMP_HI     16
#define MAX_TEMP_HI      17
#define WARN_TEMP_LOW    18
#define MAX_TEMP_LOW     19

#define NUMBER_CHANNELS  2
#define NUMBER_OF_PINS   4
#define CHANNEL_SETTINGS 8
#define TEMP_SETTINGS    4

#define EEPROM_LEN       (NUMBER_CHANNELS * CHANNEL_SETTINGS + TEMP_SETTINGS)
#define FAN_PIN          5
#define TEMP_PIN         8

/* Application Variables Need to migrate to EEPROM. */
uint8_t appMem[ EEPROM_LEN ];

/* Instance Variables. */

/* Pin assignment order is
   Blue Right, Blue Left, White Right, White Left */
byte LEDPins[]   = {9, 11, 3, 10};
byte LEDValues[] = {0, 0, 0, 0};

time_t onTimes[NUMBER_CHANNELS];
time_t riseTimes[NUMBER_CHANNELS];
time_t setTimes[NUMBER_CHANNELS];
time_t offTimes[NUMBER_CHANNELS];

/* Maximum recorded temperature this run. */
int     maxTemperature;
int     currentTemperature;
uint8_t currentDay;
uint8_t currentMinute;
time_t  currentTime;

bool manualOverride;
bool appMemTimeChange;

volatile bool resetBlueToothConnection;

/* Instantiate the OneWire protocol for temp sensing on pin TEMP_PIN. */
OneWire ds(TEMP_PIN);
MeetAndroid meetAndroid;

/* Setup */
void setResonableDefaults()
{
  appMem[BLUE_ON_HOUR]     = 8;
  appMem[BLUE_ON_MINUTE]   = 0;
  appMem[BLUE_RS_HOUR]     = 2;
  appMem[BLUE_RS_MINUTE]   = 0;
  appMem[BLUE_OFF_HOUR]    = 20;
  appMem[BLUE_OFF_MINUTE]  = 0;
  appMem[BLUE_CHASE_PCT]   = 50;
  appMem[BLUE_LED_MAX]     = 80;
  appMem[WHITE_ON_HOUR]    = 10;
  appMem[WHITE_ON_MINUTE]  = 0;
  appMem[WHITE_RS_HOUR]    = 2;
  appMem[WHITE_RS_MINUTE]  = 0;
  appMem[WHITE_OFF_HOUR]   = 18;
  appMem[WHITE_OFF_MINUTE] = 0;
  appMem[WHITE_CHASE_PCT]  = 50;
  appMem[WHITE_LED_MAX]    = 80;
  appMem[WARN_TEMP_HI]     = 45;
  appMem[MAX_TEMP_HI]      = 49;
  appMem[WARN_TEMP_LOW]    = 0;
  appMem[MAX_TEMP_LOW]     = 0;
}

void readEEPROM()
{
  byte t = EEPROM.read(0);

  if ( t > 24)
  {
    setResonableDefaults();
    writeEEPROM();
  }
  else
  {
    appMem[BLUE_ON_HOUR] = t;
    for (byte b = 1; b < EEPROM_LEN; b++)
    {
      appMem[b] = EEPROM.read(b);
    }
  }
}

void writeEEPROM()
{
  for (byte b = 0; b < EEPROM_LEN; b++)
  {
    EEPROM.write(b, appMem[b]);
  }
}

bool sendBlueToothCommand(char command[])
{
  bool receivedResponse = false;
  bool greatSuccess = false;

  //Send the command to Bluetooth Bee and wait for a response.
  Serial.print(command);
  do
  {
    while (Serial.available() > 0)
    {
      char a = Serial.read();

      if (a == 'O')
      {
        char b = Serial.peek();
        if (b == 'K')
          greatSuccess = true;
      }

      receivedResponse = true;
    }
  } while (!receivedResponse);

  return greatSuccess;
}

void setupBlueToothConnection()
{
    sendBlueToothCommand("\r\n+STWMOD=0\r\n");
    delay(1000);

    sendBlueToothCommand("\r\n+STNA=BioCubeLED\r\n");
    delay(1000);

    sendBlueToothCommand("\r\n+STAUTO=0\r\n");
    sendBlueToothCommand("\r\n+STOAUT=1\r\n");
    sendBlueToothCommand("\r\n+STPIN=1234\r\n");
    delay(2000); // This delay is required.

    startBlueToothConnection();
}

void startBlueToothConnection()
{
  // If not the first run, the Bluetooh modem has status information available.
  resetBlueToothConnection = false;

  sendBlueToothCommand("\r\n+INQ=1\r\n");
  delay(2000); // This delay is required.
}

void requestBlueToothReset()
{
  resetBlueToothConnection = true;
}

void setup()
{
  // Connect the RTC.
  setSyncProvider(RTC.get);

  //Set BluetoothBee BaudRate to default baud rate 38400
  //Serial.begin(38400);

  // Amarino works best at this rate.
  Serial.begin(57600);

  // Setup the BluetoothBEE specific Bluetooth connection.
  setupBlueToothConnection();

  Serial.flush();

  /* Only in development */
  // setResonableDefaults();
  // writeEEPROM();

  // register callback functions called when an associated event occurs.
  meetAndroid.registerFunction(SetAutoModeEnabled, 'a');
  meetAndroid.registerFunction(SetLEDChannels, 'b');
  meetAndroid.registerFunction(SetBlueChannelsLimit, 'c');
  meetAndroid.registerFunction(SetWhiteChannelsLimit, 'd');
  meetAndroid.registerFunction(SetWarnTemp, 'e');
  meetAndroid.registerFunction(SetMaxTemp, 'f');
  meetAndroid.registerFunction(SetCurrentTime, 'g');
  meetAndroid.registerFunction(SetCurrentDate, 'h');
  meetAndroid.registerFunction(SetBlueOnTime, 'i');
  meetAndroid.registerFunction(SetBlueRsTime, 'j');
  meetAndroid.registerFunction(SetBlueOffTime, 'k');
  meetAndroid.registerFunction(SetBlueChasePct, 'l');
  meetAndroid.registerFunction(SetWhiteOnTime, 'm');
  meetAndroid.registerFunction(SetWhiteRsTime, 'n');
  meetAndroid.registerFunction(SetWhiteOffTime, 'o');
  meetAndroid.registerFunction(SetWhiteChasePct, 'p');

  meetAndroid.registerFunction(DoWriteEEPROM, 'q');
  meetAndroid.registerFunction(GetVariables, 'r');

  attachInterrupt(0, requestBlueToothReset, CHANGE);

  pinMode(13, OUTPUT);
  digitalWrite(13, HIGH);   // set the LED on

  // Set variable defaults here.
  manualOverride = false;
  appMemTimeChange = false;
  maxTemperature = 0;

  readEEPROM();

  meetAndroid.send("Finished setup.");
}

/* Callbacks */
void DoWriteEEPROM(byte flag, byte numOfValues)
{
  writeEEPROM();
  digitalWrite(13, HIGH);    // set the LED off
}

void GetVariables(byte flag, byte numOfValues)
{
  digitalWrite(13, LOW);    // set the LED off

  char output[50];
  char val[12];

  for (byte b = 0; b < EEPROM_LEN; b++)
  {
    char id[6];

    id[0] = 0;
    val[0] = 0;
    output[0] = 0;

    itoa(b, id, 10);
    itoa(appMem[b], val, 10);

    strcat(output, id);
    strcat(output, "=");
    strcat(output, val);
    meetAndroid.send(output);
  }

  for (byte b = 0; b < NUMBER_OF_PINS; b++)
  {
    char id[6];

    id[0] = 0;
    val[0] = 0;
    output[0] = 0;

    itoa(b + 20, id, 10);
    itoa(LEDValues[b], val, 10);

    strcat(output, id);
    strcat(output, "=");
    strcat(output, val);
    meetAndroid.send(output);
  }

  output[0] = 0;
  val[0] = 0;
  itoa(hour(currentTime), val, 10);
  strcat(output, "24=");
  strcat(output, val);
  meetAndroid.send( output );

  output[0] = 0;
  val[0] = 0;
  itoa(minute(currentTime), val, 10);
  strcat(output, "25=");
  strcat(output, val);
  meetAndroid.send( output );

  output[0] = 0;
  val[0] = 0;
  itoa(second(currentTime), val, 10);
  strcat(output, "26=");
  strcat(output, val);
  meetAndroid.send( output );

  output[0] = 0;
  val[0] = 0;
  itoa(month(currentTime), val, 10);
  strcat(output, "27=");
  strcat(output, val);
  meetAndroid.send( output );

  output[0] = 0;
  val[0] = 0;
  itoa(day(currentTime), val, 10);
  strcat(output, "28=");
  strcat(output, val);
  meetAndroid.send( output );

  output[0] = 0;
  val[0] = 0;
  itoa(year(currentTime), val, 10);
  strcat(output,"29=");
  strcat(output, val);
  meetAndroid.send( output );

  output[0] = 0;
  val[0] = 0;
  itoa(currentTemperature, val, 10);
  strcat(output,"30=");
  strcat(output, val);
  meetAndroid.send( output );

  output[0] = 0;
  val[0] = 0;
  itoa(maxTemperature, val, 10);
  strcat(output,"31=");
  strcat(output, val);
  meetAndroid.send( output );
}

void SetAutoModeEnabled(byte flag, byte numOfValues)
{
  int v = meetAndroid.getInt();
  manualOverride = (v > 0);
}

void SetLEDChannels(byte flag, byte numOfValues)
{
  // Expect 4 values.
  // -1 == do not set.
  int data[numOfValues];
  meetAndroid.getIntValues(data);

  // Values in the range of 0 - 100 will be translated to the proper PWM value.
  byte values[NUMBER_OF_PINS] = {255, 255, 255, 255};
  for (byte b = 0; b < numOfValues; b++)
  {
    if (data[b] != -1)
      values[b] = (byte) data[b];
  }

  setLEDValues(values, NUMBER_OF_PINS);
}

void SetBlueChannelsLimit(byte flag, byte numOfValues)
{
  appMem[BLUE_LED_MAX] = meetAndroid.getInt();
}

void SetWhiteChannelsLimit(byte flag, byte numOfValues)
{
  appMem[WHITE_LED_MAX] = meetAndroid.getInt();
}

void SetWarnTemp(byte flag, byte numOfValues)
{
  // Expect 2 values hi, low
  if (numOfValues == 2)
  {
    int data[numOfValues];
    meetAndroid.getIntValues(data);

    appMem[WARN_TEMP_HI]  = data[0];
    appMem[WARN_TEMP_LOW] = data[1];
  }
}

void SetMaxTemp(byte flag, byte numOfValues)
{
  // Expect 2 values hi, low
  if (numOfValues == 2)
  {
    int data[numOfValues];
    meetAndroid.getIntValues(data);

    appMem[MAX_TEMP_HI]  = data[0];
    appMem[MAX_TEMP_LOW] = data[1];
  }
}

void SetCurrentTime(byte flag, byte numOfValues)
{
  // Expect 3 values: Hour, Minute, Second.
  int data[numOfValues];
  meetAndroid.getIntValues(data);

  if (numOfValues == 3)
  {
    TimeElements helperTime;
    breakTime(currentTime, helperTime);

    for (int i = 0; i< numOfValues; i++)
    {
      switch(i)
      {
        case 0:
          helperTime.Hour   = data[i];
          break;
        case 1:
          helperTime.Minute = data[i];
          break;
        case 2:
          helperTime.Second = data[i];
          break;
      }
    }

    // set the RTC to the current time
    setTime(makeTime(helperTime));
    RTC.set(now());

    currentDay    = -1; // Cause setappMem() to be run.
    currentMinute = -1; // Cause the LED values to be updated.
  }
}

void SetCurrentDate(byte flag, byte numOfValues)
{
  // Expect 3 value: Month, Day, Year
  int data[numOfValues];
  meetAndroid.getIntValues(data);

  if (numOfValues == 3)
  {
    TimeElements helperTime;
    breakTime(currentTime, helperTime);

    for (int i = 0; i< numOfValues; i++)
    {
      switch(i)
      {
        case 0:
          helperTime.Month = data[i];
          break;
        case 1:
          helperTime.Day   = data[i];
          break;
        case 2:
          helperTime.Year  = (data[i] - 1970);
          break;
      }
    }

    // set the RTC to the current time
    setTime(makeTime(helperTime));
    RTC.set(now());

    currentDay    = -1; // Cause setappMem() to be run.
    currentMinute = -1; // Cause the LED values to be updated.
  }
}

void SetBlueOnTime(byte flag, byte numOfValues)
{
  // Expect 2 values Hour, Minute

  if (numOfValues == 2)
  {
    int data[numOfValues];
    meetAndroid.getIntValues(data);

    appMem[BLUE_ON_HOUR]   = data[0];
    appMem[BLUE_ON_MINUTE] = data[1];
  }

  appMemTimeChange = true;
}

void SetBlueRsTime(byte flag, byte numOfValues)
{
  // Expect 2 values Hour, Minute

  if (numOfValues == 2)
  {
    int data[numOfValues];
    meetAndroid.getIntValues(data);

    appMem[BLUE_RS_HOUR]   = data[0];
    appMem[BLUE_RS_MINUTE] = data[1];
  }

  appMemTimeChange = true;
}

void SetBlueOffTime(byte flag, byte numOfValues)
{
  // Expect 2 values Hour, Minute

  if (numOfValues == 2)
  {
    int data[numOfValues];
    meetAndroid.getIntValues(data);

    appMem[BLUE_OFF_HOUR]   = data[0];
    appMem[BLUE_OFF_MINUTE] = data[1];
  }

  appMemTimeChange = true;
}

void SetBlueChasePct(byte flag, byte numOfValues)
{
  // Expect one value.
  appMem[BLUE_CHASE_PCT] = meetAndroid.getInt();
}

void SetWhiteOnTime(byte flag, byte numOfValues)
{
  // Expect 2 values Hour, Minute

  if (numOfValues == 2)
  {
    int data[numOfValues];
    meetAndroid.getIntValues(data);

    appMem[WHITE_ON_HOUR]   = data[0];
    appMem[WHITE_ON_MINUTE] = data[1];
  }

  appMemTimeChange = true;
}

void SetWhiteRsTime(byte flag, byte numOfValues)
{
  // Expect 2 values Hour, Minute

  if (numOfValues == 2)
  {
    int data[numOfValues];
    meetAndroid.getIntValues(data);

    appMem[WHITE_RS_HOUR]   = data[0];
    appMem[WHITE_RS_MINUTE] = data[1];
  }

  appMemTimeChange = true;
}

void SetWhiteOffTime(byte flag, byte numOfValues)
{
  // Expect 2 values Hour, Minute

  if (numOfValues == 2)
  {
    int data[numOfValues];
    meetAndroid.getIntValues(data);

    appMem[WHITE_OFF_HOUR]   = data[0];
    appMem[WHITE_OFF_MINUTE] = data[1];
  }

  appMemTimeChange = true;
}

void SetWhiteChasePct(byte flag, byte numOfValues)
{
  appMem[WHITE_CHASE_PCT] = meetAndroid.getInt();
}

/* Main */
void loop()
{
  meetAndroid.receive(); // you need to keep this in your loop() to receive events
  currentTime = now();   // Loop based on current time.

  // Each day, refresh the persistent loop values.
  if ((appMemTimeChange) ||
      (day(currentTime) != currentDay))
  {
    currentDay = day(currentTime);
    setappMem();
    appMemTimeChange = false;
  }

  // Each minute, refresh the LED output.
  if (minute(currentTime) != currentMinute)
  {
    currentMinute = minute(currentTime);
    if (!manualOverride)
      setLEDPcts();
  }
  currentTemperature = checkTemp(); // Check the heatsink temperature.

  if (currentTemperature > maxTemperature)
    maxTemperature = currentTemperature;

  if (resetBlueToothConnection)
    startBlueToothConnection();
}

// Values are given as percentages and then converted to PWM values.
// If the value is out of range, discard.
// Byte value are in the range 0 - 255;
void setLEDValues(byte* data, byte numOfValues)
{
  byte fansOn = 0;

  for (byte b = 0; b < numOfValues; b++)
  {
    if(data[b] <= 100)
    {
      int pwmValue = 255;

      if (b < 2)
        pwmValue *= appMem[BLUE_LED_MAX];
      else
        pwmValue *= appMem[WHITE_LED_MAX];

      pwmValue = (pwmValue / 100) * data[b] / 100;

      // Automatic temperature compensation.
      if (currentTemperature >= (appMem[WARN_TEMP_HI] * 100 + appMem[WARN_TEMP_LOW]))
      {
        if (currentTemperature >= (appMem[MAX_TEMP_HI] * 100 + appMem[MAX_TEMP_LOW]))
        {
          pwmValue = 0;
          fansOn = 255;
        }
        else
        {
          pwmValue = 100 * pwmValue / 50;
          fansOn = 255;
        }
      }

      if (pwmValue > 0)
        fansOn = 255;

      LEDValues[b] = (byte) pwmValue;

      // Set the value.
      analogWrite(LEDPins[b], ~LEDValues[b]);
    }
  }

  // Fans on while the lights are on.
  analogWrite(FAN_PIN, fansOn);
}

// This device address: {0x28, 0x5A, 0xB3, 0xDF, 0x02, 0x00, 0x00, 0x2A}
// This functions returns the current degrees C * 100.
// The return / 100 == Whole number degrees
// The return abs(return % 100) == Fractional degrees.
int checkTemp()
{
  //byte presence = 0;
  byte data[12];
  int tempReading, absModTemp, infiniteLoop;

  // Master issues reset.  Device responds with Presence which is promptly ignored.
  // Master issues SKIP_ROM CCh command.
  // The next command will be perform on each slave device.
  // Master issue CONVERT_T 44h command.
  // This will cause the slave to measure the temp and store in scratchpad.
  //  12-bit percision requires ~750ms.

  ds.reset();
  ds.write(0xCC);
  ds.write(0x44);
  delay(775);
  // Master issues reset.  Device responds with Presence.
  //presence = ds.reset();

  // Master issues reset.  Device responds with Presence which is ignored.
  ds.reset();
  infiniteLoop = 0;
  do
  {
    // Master issue SKIP_ROM code.
    // There is only one slave device, otherwise use the device's address.
    // Master issues READ_SCRATCHBPAD BEh command.
    // Will cause data collsion with more than one slave.

    ds.write(0xCC);
    ds.write(0xBE);

    for ( int i = 0; i < 9; i++) // Master reads entire scratchpad including CRC.
    {
      data[i] = ds.read();
    }
    // Master then recalculates the CRC of the first eight data bytes from the
    // scratchpad and compares the calculated CRC with the read CRC (byte 9).
    // If they match, the master continues else the read operation is repeated.
    // Make sure to not allow an infinite loop.
    if (++infiniteLoop > 10)
      break;
  } while (OneWire::crc8( data, 8) != data[8]);

  tempReading = (data[1] << 8) + data[0];
  if (tempReading & 0x8000) // negative
  {
    tempReading = (tempReading ^ 0xffff) + 1; // 2's comp
    absModTemp = -1;
  }
  else
  {
    absModTemp = 1;
  }

  // multiply by (100 * 0.0625) or 6.25
  return ((6 * tempReading) + tempReading / 4) * absModTemp;
}

void setappMem()
{
  for (byte b = 0; b < NUMBER_CHANNELS; b++)  // Once per color.
  {
    byte offset = b * CHANNEL_SETTINGS;
    TimeElements helperTime;
    // Use currentTime value as default.
    breakTime(currentTime, helperTime);
    helperTime.Second = 0;

    // onTime = End of night.  Beginning of daytime.
    helperTime.Hour   = appMem[BLUE_ON_HOUR + offset];
    helperTime.Minute = appMem[BLUE_ON_MINUTE + offset];
    onTimes[b]        = makeTime(helperTime);

    // offTime = End of light period / beingging of Night..
    helperTime.Hour   = appMem[BLUE_OFF_HOUR + offset];
    helperTime.Minute = appMem[BLUE_OFF_MINUTE + offset];
    offTimes[b] = makeTime(helperTime);

    // This allows PM until AM runtime.
    if (offTimes[b] < onTimes[b])
    {
      if(hour(currentTime) <= hour(offTimes[b]))
      {
        breakTime(onTimes[b], helperTime);
        helperTime.Day += 1;
        onTimes[b] = makeTime(helperTime);
      }
      else
      {
        breakTime(offTimes[b], helperTime);
        helperTime.Day += 1;
        offTimes[b] = makeTime(helperTime);
      }
    }

    // riseTime = End of sun rise time / begging of full day.
    helperTime.Hour   = hour(onTimes[b]) + appMem[BLUE_RS_HOUR + offset];
    helperTime.Minute = minute(onTimes[b]) + appMem[BLUE_RS_MINUTE + offset];
    helperTime.Day    = day(onTimes[b]);
    if (helperTime.Hour < hour(onTimes[b]))
      helperTime.Day += 1;
    riseTimes[b] = makeTime(helperTime);

    // bsetTime = End of full day time / beginning of sunset.
    helperTime.Hour   = hour(offTimes[b]) - appMem[BLUE_RS_HOUR + offset];
    helperTime.Minute = minute(offTimes[b]) - appMem[BLUE_RS_MINUTE + offset];
    helperTime.Day    = day(offTimes[b]);
    if (helperTime.Hour > hour(offTimes[b]))
      helperTime.Day -= 1;
    setTimes[b] = makeTime(helperTime);
  }
}

void setLEDPcts()
{
  // Default to daytime run values.
  byte output[] = {100, 100, 100, 100};

  for (byte b = 0; b < NUMBER_CHANNELS; b++)
  {
    int offset = b * NUMBER_CHANNELS;
    int offset2 = b * CHANNEL_SETTINGS;

    // Nighttime.
    if ((currentTime < onTimes[b]) || (currentTime > offTimes[b]))
    {     //  Night
      output[0 + offset] = 0;
      output[1 + offset] = 0;
    }
    // Sunrise.
    else if (currentTime <= riseTimes[b])
    {     // Ramp up.
      int currentPercent = 100 * (currentTime - onTimes[b]) / (riseTimes[b] - onTimes[b]);
      if (currentPercent > appMem[BLUE_CHASE_PCT + offset2])
      {   // Second
        currentPercent = (currentPercent - appMem[BLUE_CHASE_PCT + offset2]) * (100 / (100 - appMem[BLUE_CHASE_PCT + offset2]));
        output[1 + offset] = (byte) currentPercent;
      }
      else
      {   // First
        currentPercent = currentPercent * 100 / appMem[BLUE_CHASE_PCT + offset2];
        output[1 + offset] = 0;
        output[0 + offset] = (byte) currentPercent;
      }
    }
    // Sunset.
    else if (currentTime >= setTimes[b])
    {     // Ramp down.
      int currentPercent = 100 * (currentTime - setTimes[b]) / (offTimes[b] - setTimes[b]);
      if (currentPercent > appMem[BLUE_CHASE_PCT + offset2])
      {   // Second
        currentPercent = 100 - ((currentPercent - appMem[BLUE_CHASE_PCT + offset2]) * (100 / (100 - appMem[BLUE_CHASE_PCT + offset2])));
        output[0 + offset] = 0;
        output[1 + offset] = (byte) currentPercent;
      }
      else
      {   // First
        currentPercent = 100 - ((currentPercent) * (100 / appMem[BLUE_CHASE_PCT + offset2]));
        output[0 + offset] = (byte) currentPercent;
      }
    }
  }

  setLEDValues(output, NUMBER_OF_PINS);
}
