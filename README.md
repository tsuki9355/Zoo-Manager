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

## How to Play

You start with $1,000, 20 food and an empty Waiting Room. Each day, take as many actions as you like, then **end the day**.

- **Overnight**, animals get hungrier, sadder and a little less healthy, and every animal makes its enclosure dirtier. Hungry animals and dirty enclosures cost extra health, and animals stuck in the Waiting Room (or with no enclosure) get extra unhappy.
- **Visitors** pay for the animals on display (settled in a proper enclosure, not in the hospital). Each species draws a different amount, scaled by your star rating and zoo level. Every enclosure except the Waiting Room costs $10 a day in upkeep.
- **Food** is used up when you feed: bigger animals eat more per meal. Buy packs of 10 for $40.
- **Play** and **rest** work once per animal per day.
- **The hospital** fully restores an animal over 2 nights ($60). It starts with 1 bed; upgrade it for more.
- **Zoo levels** (1 to 4) unlock new species, from meerkats up to giant pandas, and boost ticket sales by 10% each.
- **Random events** happen some nights: school trips, donations, newspaper stories, storms and rats in the food store.
- If an animal's health reaches 0, the welfare inspector takes it away. If you end a day with less than $0, you're bankrupt and the game is over.

 ## User Stories

 - As a user, I want to **build an enclosure** by specifying a name (costs money), and **expand** it for more space
 - As a user, I want to **adopt an animal** by choosing a species from a shop and giving it a name
 - As a user, I want to **play with an animal or all animals** (once per animal per day)
 - As a user, I want to **feed an animal or all animals**, using up the zoo's food supply
 - As a user, I want to **let an animal or all animals rest** (once per animal per day)
 - As a user, I want to **assign an animal to an enclosure** (this will be my add X to Y)
 - As a user, I want to **remove an animal from an enclosure**
 - As a user, I want to **sell an animal** to another zoo for part of its price
 - As a user, I want to **clean all the enclosures** (costs money)
 - As a user, I want to **send an animal to the zoo hospital** to restore its condition, and **upgrade the hospital** for more beds
 - As a user, I want to **buy food supplies** for my animals
 - As a user, I want to **upgrade my zoo** to unlock new species
 - As a user, I want to **end the day** to collect ticket income, pay upkeep and see what happened overnight
 - As a user, I want to **view a list of animals** (list all the X's in my Y)
 - As a user, I want to **view a list of enclosures** (this all the X's in my Y)
 - As a user, I want to **see the current status for the zoo**

 - As a user, I want to be able to **save the game** 
 - As a user, I want to be able to **load the game**

 ## Instructions for End User
 
 - As a user, you can view the enclosures you added by looking on the left side of the center panel
 - As a user, you can view the animals you added to each enclosure by clicking on the enclosure
 - As a user, you can generate the first required action related to the user story "adding multiples Xs to a Y" by clicking on the adopt animal button at the top panel.
 - As a user, you can generate the second required action related to the user story "adding multiples Xs to a Y" by clicking on the build enclosure button at the top panel.
 - As a user, you can generate the third action related to the user story "adding multiples Xs to a Y" by clicking on the assign animal to enclosure button at the top panel.
 - As a user, you can locate my visual component by looking at the top left corner
 - As a user, You can save the state of my application by clicking on the save button at the top panel.
 - As a user, you can reload the state of my application by clicking on the load button at the top panel.

 