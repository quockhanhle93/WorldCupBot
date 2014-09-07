WorldCupBot
===========

FIFA World Cup 2014 bot that interfaces with IRC network(s) using PircBot. Pulls live stats from FIFA, records previous matches, manages live updates, minute to minute updates, and mirrors output to IRC

This bot was written shortly after the start of the 2014 FIFA World Cup in Brazil. My intentions were for a simple bot that mirrored changes in the score as output by the FIFA website, for use by myself and a small circle of friends. The final outcome is a bot that reliably manages channel topics and user modes, while also announcing goals, current score and clock time, as well as support for added time and penalty kick rounds. To my surprise, there was more demand for the bot than originally anticipated. By the end of the cup, WorldCupBot was running on two networks and ~10 channels, serving data to 800-1200 unique users.

I would like to do this again in 2018, preferably getting an earlier start and implementing additional services if there is a demand.

2018 TODO:
More completely utilize available API data
Implement interactive user outcome guessing ("bracket", betting?)
