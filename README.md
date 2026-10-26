# Zoo Manager

## Overview

**What will the application do?**

*Zoo Manager* application can let the user build and manage a small zoo. The user can create enclosures, add animals with stuff like diet, assigning the animals to the enclosures, cleaning the enclosures and playing with the animals, managing animals health, as well as a zoo rating given by the visitors. 

**Who will use it and why is this project interesting for me?**
Anyone with an interest of a little sim managing game and likes animals will enjoy using this application. This project is interesting for me because my dream job is to become a game developer and this was one of my many ideas that I wanted to make myself before.

## How to Run

From the project folder (needs a JDK 8 or newer; it uses the one in `JAVA_HOME` if set):

- `.\run.cmd` plays the console version
- `.\run.cmd gui` plays the window (GUI) version
- `.\run.cmd test` runs the tests

In VS Code you can also pick **Zoo Manager (console)** or **Zoo Manager (GUI)** in the Run and Debug panel.
If the console shows odd symbols like `[32m` instead of colours, set the `NO_COLOR` environment variable.

 ## User Stories

 - As a user, I want to **create an enclosure** by specifying a name
 - As a user, I want to **add an animal** (species, name, age, health, happiness, hunger) to the zoo
 - As a user, I want to **play with an animal or all animals** 
 - As a user, I want to **feed an animal or all animals**
 - As a user, I want to **put an animal to sleep or all animals**
 - As a user, I want to **assign an animal to an enclosure** (this will be my add X to Y)
 - As a user, I want to **remove an animal from an enclosure**
 - As a user, I want to **clean all the enclosures**
 - As a user, I want to **restoring animals condition** 
 - As a user, I want to **view a list of animals** (list all the X's in my Y)
 - As a user, I want to **view a list of enclosures** (this all the X's in my Y)
 - As a user, I want to **see the current status for the zoo**
 - As a user, I want to **increase the income of the zoo**

 - As a user, I want to be able to **save the game** 
 - As a user, I want to be able to **load the game**

 ## Instructions for End User
 
 - As a user, you can view the enclosures you added by looking on the left side of the center panel
 - As a user, you can view the animals you added to each enclosure by clicking on the enclosure
 - As a user, you can generate the first required action related to the user story "adding multiples Xs to a Y" by clicking on the add animal button at the top panel.
 - As a user, you can generate the second required action related to the user story "adding multiples Xs to a Y" by clicking on the add enclosure button at the top panel.
 - As a user, you can generate the third action related to the user story "adding multiples Xs to a Y" by clicking on the assign animal to enclosure button at the top panel.
 - As a user, you can locate my visual component by looking at the top left corner
 - As a user, You can save the state of my application by clicking on the save button at the top panel.
 - As a user, you can reload the state of my application by clicking on the load button at the top panel.
 

## Phase 4 TASK 2

- Fri Nov 28 01:08:59 GMT-08:00 2025
Waiting Room added to gg.
Fri Nov 28 01:09:04 GMT-08:00 2025
ee added to gg.
Fri Nov 28 01:09:24 GMT-08:00 2025
ff added to gg.
Fri Nov 28 01:09:24 GMT-08:00 2025
ff added to Waiting Room.

## Phase 4 TASK 3
- If I had more time to work on this project, I think it would make sense to add a interface called Status and I would have my Animal and Enclosure class to implement Status. Status would have abstract methods like getStatus which would display the status of either Animal or Enclosure depending on which type it is. I think I can also make Animal and Enclosure class an abstract class. Then I would have Zoo class extend both Animal and Enclosure class. Then I can have my Animal and Enclosure class to keep track of all the Animals or Enclosures that was added. Doing this would decrease coupling which would Ultimately lead to a better design.
