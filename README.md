# EmploymentDataCollector
Graduation Project of Gachon University Software Department

## Team members
| Std No. | Name | Department | E-mail | Github |
|:-------:|:----:|:-------:|:-------:|:-------:|
|201835417|김동준|AI·소프트웨어학부(소프트웨어전공)|rlaehdwns99@gachon.ac.kr|[Kim-Dong-Jun99](https://github.com/Kim-Dong-Jun99)|
|201835443|김현종|AI·소프트웨어학부(소프트웨어전공)|guswhd5738@gachon.ac.kr|[DecisionDisorder](https://github.com/DecisionDisorder)|
|201835474|안해빈|AI·소프트웨어학부(소프트웨어전공)|gnh06280@gachon.ac.kr|[HaebinAHN](https://github.com/HaebinAHN)|
|201735863|이원재|AI·소프트웨어학부(소프트웨어전공)|dnjswo0213@gachon.ac.kr|[Wonjae98](https://github.com/Wonjae98)|
<br/>

## Introduction
It is a GUI program by collecting employment announcement data of Saramin.
- Employment announcement data is retrieved through Saramin OpenAPI.
- The user may set the start index and the number of job announcement data.
- Data is retrieved only for the development job group, and keywords of the announcement are extracted to select keywords that are not necessary.
- Keywords are sorted from the order of low frequency of appearance.
- Keywords excluded once are then excluded even after API requests.
- It is converted into JSON in a form that can be applied as trend data to the server and stored as a file.

## Run
You can run Employment Data Collector by downloading this <a href="/out/artifacts/EmploymentDataCollector_jar">jar file</a>.

## Screenshots
<img src="/display_images/Inital_Screen.PNG" title="Inital Screen" alt="Inital Screen"></img>
<img src="/display_images/API_Requesting.PNG" title="API Requesting" alt="API Requesting"></img>
<img src="/display_images/Data_Filtering.PNG" title="Data Filtering" alt="Data Filtering"></img>

## Development Environment
- Java JDK 1.8
- GUI: Swing & AWT
- JSON: Json simple v1.1.1 & GSON
- Saramin Open API

## External Resource Source
**[Font]**
- 네이버, 네이버 문화재단: D2Coding
