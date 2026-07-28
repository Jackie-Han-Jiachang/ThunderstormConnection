# Thunderstorm Interactive Webpage

This project is used to manage and summarize the relationships of different characters of the book Thunderstorm. This will be used in the Chinese class, so make sure the GUI presented to the users is in Chinese. Only use English when necessary.

## Interface Design

Read the Mock-ups.pdf for reference to design the GUI. Following list shows detailed descriptions to each area and its function.

1. Use "Events Cards" to describe an event that takes place between different people within the play. The event card should include at least 5 components: 
    - The name of this event. The user should be able to type in the name.
    - A short Description of the event. Input by the user, and this one should have a 50 Chinese characters limit.
    - The character that initiate this event. This should be only one person. The characters from Thunderstorm should be presented as a list, so that the user can select one of them.
    - Characters that are affected by the event. This should also be a list, and the user can select some of them.
    - The change of "affection level (好感度)" This should be an integer between -10 to 10. This one should be able to be selected by the user.

2. The main view should be a web that connects some nodes that represent different characters from Thunderstorm. The web should be empty until the user input the first event. The distance between different nodes are determined by the affection level between them. The higher the affection level, the closer two nodes should be.

3. There should be two areas that can store the event cards. One is the "packet" that stores the events created by the user. The other one is the "stage" area where the events inside will affect the relationship in the web.

## Tech Stack

1. Use MVC design to arrange the directories and to design the project.
2. Use java and javascript mainly to write the program.
3. The deployment should be a .jar file. Make sure this jar file is usable on both mac and windows.
4. Created relations and event cards should be stored locally.