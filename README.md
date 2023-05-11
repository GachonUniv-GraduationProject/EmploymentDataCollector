# EmploymentDataCollector
Graduation Project of Gachon University Software Department

[Team members]
- 김동준 ()
- 김현종 (guswhd5738@gachon.ac.kr)
- 안해빈 ()
- 이원재 ()

## Introduction
It is a GUI program by collecting employment announcement data of Saramin.
- Employment announcement data is retrieved through Saramin OpenAPI.
- The user may set the start index and the number of job announcement data.
- Data is retrieved only for the development job group, and keywords of the announcement are extracted to select keywords that are not necessary.
- Keywords are sorted from the order of low frequency of appearance.
- Keywords excluded once are then excluded even after API requests.
- It is converted into JSON in a form that can be applied as trend data to the server and stored as a file.

## Screenshots
<img src="/display_images/API_Requesting.PNG" width="240" height="426" title="API Requesting" alt="API Requesting"></img>
<img src="/display_images/Data_Filtering.PNG" width="240" height="426" title="Data Filtering" alt="Data Filtering"></img>

## Development Environment
- Java JDK 1.8
- GUI: Swing & AWT
- JSON: Json simple v1.1.1 & GSON
- Saramin Open API

## External Resource Source
**[Font]**
- 네이버, 네이버 문화재단: D2Coding
