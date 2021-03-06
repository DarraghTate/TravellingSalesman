# TravellingSalesman
A Data Structures & Algorithms Assignment

To download, enter the command:

    git clone https://github.com/DarraghTate/TravellingSalesman.git
   
The repo includes a compiled Java .class file, so to execute the program use the command:

    java TravellingSalesman

This project was a substitute for a summer 2020 exam, which was cancelled due to the Covid-19 pandemic.

This project was about finding the fastest route through 1000 airports with the following stipulations:  
- Any airport visited must be more that 100km away from the current airport  
- An airplane is on the ground for 30 minutes per airport  
- Airports can be revisited  
- Every airport in the list must be visited at least once  
- The start & end points must be location 0 on the CSV (the latitude & longitude represents the location of Maynooth, Co. Kildare, Ireland)  
   
This algorithm uses a nearest neighbour heuristic to determine the route. It looks for the closest 3 airports (as calculated by using the Haversine formula with the relevent GPS coordinates), and will usually pick the closest unvisited airport, but with a small chance of picking a less optimal route. This is to try and alleviate some of the drawbacks inherant to the deterministic, greedy algorithm structure; while not picking the closest airport may not be optimal in that moment, it may result in a shoter journey overall.
This process repeats, with each route being timed, and if a quicker route is found then it is prited to the terminal and saved as the best route so far.
Once a fast route has been found, a subroutine iterates through the route, swapping adjacent locations to see if a slightly more optimised route can be found. This is memory intensive, so it is only used on the best solutions found so far.
The output is the distance travelled (in KM), the time taken for the journey (in hours), and the order of the airports visited (0-indexed, as read by the CSV)

This project uses object-orinted programming (each location is a class, with a "latitude" (double), "longitude" (double) and "visited" (boolean) flag), and uses ArrayLists to store the route. The algorithm is recursive, constantly calling the function to find the next airport until every airport has been flagged as visited.
