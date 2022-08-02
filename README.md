# Adyen Android Assignment
- Git link: https://github.com/shahwaiz90/adyen-nearbyplaces

## YouTube Video Demo

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/b4LX7idyPOc/0.jpg)](https://www.youtube.com/watch?v=b4LX7idyPOc)


## 1. Tasks Completed
- When you click on the `Find near by places` chip, it will show near by places from the current position of the map.
- Handled Configuration Change. (Check with Landscape & Portrait Mode). `To support more devices`
- Permission Handling (Location Permission and LocationServicesEnabled Permission). `To achieve accurate results`
- Added animations on the List and Chip View (near by places). `To improve UX`
- Added a separate module for Network APIs. `To achieve modularization and reusablity`
- Test cases for View (viewmodel), Data (Api, Repo), Domain (interactor) layers 
- Test cases for CashRegister Module.

## 2. Tech Stack
- Implemented Architecture MVVM + Clean architecture + SOLID principles. `For separation of concerns and easier unit testing.`
- Dependency Injection: HILT. `To achieve dependency injection concept in the SOLID principles`
- Coroutines. `To achieve simplification in API requests and making less load on the memory`
- LocationServices: FusedLocationProviderClient. `To avoid draining the battery`
- Interface implementation `To communicate efficiently, mostly from adapter to activity click events.`

## 3. CashRegister Logic in processTransaction (Summary)
- If product price is greater than amount paid then we throw exception, if not then go to step 2.
- We reverse the list.
- We start from the end of the list, untill payBackAmount(amountPaid - productPrice) is minimum.
- We check the count of the current bill/coin.
  - If count is more than 1, then we make keep on subtracting those amounts until we get amount equal to payBackAmount or less.
  - If count is not more than 1, then we just subtract it go to the next index.
- Then we keep on subtracting the amount untill we get payBackAmount or less.
- If the amount is equal to payBackAmount we return the change, otherwise we throw exception that change is not available.
- I think it can be improved if we start from center and apply binary search on it.

## 4. Demo Screenshots
1 - CashRegister test cases

<img src="https://i.ibb.co/c6612Fc/Screenshot-2022-08-02-at-5-16-12-PM.png" width="700" /> 

2 - Showing Near By Places

<img src="https://i.ibb.co/TPC8vHP/Screenshot-2022-08-02-at-5-04-12-PM.png" width="200" />  

3 - Showing Current Location

<img src="https://i.ibb.co/NN715mg/Screenshot-2022-08-02-at-5-10-12-PM.png" width="200" /> 

4 - Asking to enable location services

<img src="https://i.ibb.co/d0QbFCy/Screenshot-2022-08-02-at-5-12-30-PM.png" width="200" />  

5 - Requesting Camera Permissions

<img src="https://i.ibb.co/dGTnYN4/Screenshot-2022-08-02-at-5-14-28-PM.png" width="200" />   

6 - Test cases for view, data, domain layers

<img src="https://i.ibb.co/WKcD73R/Screenshot-2022-08-02-at-3-44-23-PM.png" width="700" />  
