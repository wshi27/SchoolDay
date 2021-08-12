# SchoolDay
Server-Client based model simulating operations of a school day during Covid-19
All personnels are clients. Each time a new client sends a request the Server creates a new thread to service that client. 
Each thread executes command only from that client. It queues the requests if multiple requests comes in at once. 
This model simulates the following school day :
20 students fill out Covid Form and go to school.
Principal starts the day when more than 90% students arrive at the school yard.
Principal sends student home(thread terminates) if Covid form was not filled out.
1/3 of the students gets sent to Nurse for Covid test, rest goes to classes.
Nurse tests students in pairs. Probability of testig positive is 3%. Students tested positive will be sent home. 
If more than 3 students tested positive, school shuts down(all threads terminated).
Students lined up to attend the ELA, MATH or the PHYSICAL-ED classes. Both ELA and MATH classes takes 4 students max, and rest goes to PHYSICAL-ED
There are 3 sessions. All Students try to take ELA first, then MATH. If there's no more space, Students go to PHYSICAL-ED class
Principal keeps the time and determines when to end classes and also when to end the school day.
