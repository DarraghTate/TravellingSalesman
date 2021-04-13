// Darragh Tate

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TravellingSalesman {
    public static void main (String args[]) {

        // Creates a list of Location objects, each object representing GPS coordinates and the number it's assigned in the spreadsheet, only zero-indexed.
        List<Location> airports = readInFromSpreadSheet("EquipmentGPSCo-ordinates.csv");

       
        List<Integer> order = monteCarloNearestNeighbour();

        // Call it once - get's a default order. The loop gets more lists and compares them
        System.out.println("First guess: " + findTotalTime(airports, order));
        // Best time is the shortest travel time found so far
        int bestTime = findTotalTime(airports, order);

        // Number of loops that will be done to try and find a more optimal route
        int loops = 1000000;
        for (int i = 0; i < loops; ++i) {
            // Returns a list with a random order;
            List<Integer> currentGuess = monteCarloNearestNeighbour();
            int loopTime = findTotalTime(airports, currentGuess);
            if (loopTime < bestTime) {
                order = currentGuess;
                bestTime = loopTime;
                printSolution(currentGuess);
                printDistanceAndTime(airports, currentGuess);
            }
        }
        
        reorderSolution(order, airports);
        System.out.println("Final guess: ");
        printSolution(order);
        printDistanceAndTime(airports, order);
        
    }

    public static List<Integer> monteCarloNearestNeighbour() {
        List<Location> airports = readInFromSpreadSheet("EquipmentGPSCo-ordinates.csv");

        // Array to hold values representing weather or not an airport has been visited or not
        boolean[] locationsVisited = new boolean[airports.size()];

        // The order of cities visited
        List<Integer> order = new ArrayList<>();

        nearestNeighbour(airports, 0, locationsVisited, order);
        int bestTime = findTotalTime(airports, order);
        order = reorderSolution(order, airports);
        return order;
    }

    public static void nearestNeighbour(List<Location> inAirports, int inIndex, boolean[] inLocationsVisited, List<Integer> inOrder) {
        Location current = inAirports.get(inIndex);
        current.setVisited();
        inLocationsVisited[inIndex] = true;

        Location nextLocation = findNearestNeighbour (current, inAirports, inLocationsVisited, inOrder, inOrder.size());
        int locationNumber = current.getLocationNumber();

        int nextLocationNumber = nextLocation.getLocationNumber();
        inOrder.add(locationNumber);

        // If all airports have not yet been visited, call the method again, with the next location the algorithm has picked as a parameter
        if (allAirportsVisited(inLocationsVisited) == false) {
            nearestNeighbour(inAirports, nextLocationNumber, inLocationsVisited, inOrder);
        }
    }

    

    public static Location findNearestNeighbour (Location current, List <Location> airports, boolean[] locationsVisited,  List<Integer> inOrder, int inOrderSize) {
        //Need to instantiate closestValidlocation, I just use current; it'll be overwritten anyway
        Location closestValidLocation = current;
        Location secondBestValidLocation = current;
        Location thirdBestValidLocation = current;

        // Default massive distance; ensures all locations checked will be shorter than this
        int currentShortestDistance = 10000000;
        int secondShortestDistance = 10000000;
        int thirdShortestDistance = 10000000;
        int numLocations = airports.size();
        for (int i = 0; i<numLocations; ++i) {
            // The airport we're checking for suitability
            Location checkLocation = airports.get(i);
            // Find the distance from the current airport we're checking it against
            int checkLocationDistance = haversineFormula(current, checkLocation);
            // If it's valid - unvisited, more than 100 km away and closer than any other valid locations, it's set to be the best option so far
            if ((checkLocationDistance >= 100) && (checkLocationDistance < currentShortestDistance) && (checkLocation.getVisited() == false)) {
                thirdBestValidLocation = secondBestValidLocation;
                secondBestValidLocation = closestValidLocation;
                closestValidLocation = checkLocation;

                // The distance from the current location is saved as a variable
                thirdShortestDistance = secondShortestDistance;
                secondShortestDistance = currentShortestDistance;
                currentShortestDistance = checkLocationDistance;
            }
        }

        // If there are no airports left more than 100 km away, this will find the closest one that you can visit, then go there to try again
        // Method only used if the current best choice is too close, which only happens if every airport left is within 100km
       

        Random rng = new Random();
        int r = rng.nextInt(130);
        if ((r == 1||r==2) && secondShortestDistance<1000 && secondShortestDistance>100 && inOrder.size()<1800) {
            closestValidLocation = secondBestValidLocation;
        }

        int currentlySelectedLocationDistance = haversineFormula(current, closestValidLocation);
        if(currentlySelectedLocationDistance < 100) {
            int counter = 0;
            for (int i = 0; i<numLocations; ++i) {
                Location checkLocation = airports.get(i);
                int checkLocationDistance = haversineFormula(current, checkLocation);
                if (checkLocationDistance >= 100 && checkLocationDistance < currentShortestDistance) {
                    closestValidLocation = checkLocation;
                    currentShortestDistance = checkLocationDistance;
                    counter++;
                    if (counter == 30) {
                        break;
                    }
                   
                }
            }
        }
        closestValidLocation.setVisited();
        return closestValidLocation;
    }

    

    /*
        Checks to see if all loactions have been visited. If it meets a slot in the locationsVisited array that's 
        set to false, then it returns false, as that means that at least 1 airport has not yet been visited.
    */
    public static boolean allAirportsVisited(boolean[] inLocationsVisited){
        boolean complete = true;
        for (int i = 0; i< inLocationsVisited.length; ++i) {
            if (inLocationsVisited[i]==false) {
                complete = false;
                break;
            }
        }
        return complete;
    }

    public static List<Integer> reorderSolution(List<Integer>order, List<Location>airports) {
        List<Integer> newOrder = new ArrayList<>();
        for (int i = 0; i<order.size(); ++i) {
            newOrder.add(order.get(i));
        }

        int bestTime = findTotalTime(airports, order);
        int currentLocation = 0;
        int nextLocation = 0;

        for (int i = 1; i <= order.size()-2; ++i) {
            List<Integer> currentOrder = new ArrayList<>();
            for (int j = 0; j<newOrder.size(); ++j) {
                currentOrder.add(newOrder.get(j));
            }
            currentLocation = order.get(i);
            nextLocation = order.get(i+1);
            currentOrder.set(i, nextLocation);
            currentOrder.set(i+1, currentLocation);
            int currentTime = findTotalTime(airports,currentOrder);
           
            
            if (currentTime < bestTime && checkSolutionIsValid(currentOrder, airports)) {
                for (int j = 0; j< currentOrder.size(); ++j) {
                    newOrder.set(j, currentOrder.get(j));

                }
                
                
                bestTime = currentTime; 
            }
        }

        return newOrder;
    }

    public static boolean checkSolutionIsValid (List<Integer> order, List<Location> airports) {
        boolean isValid = true;
        for (int i = 1; i<order.size(); ++i) {
            if (haversineFormula(airports.get(order.get(i-1)), airports.get(order.get(i)))<100) {
                return false;
            }
        }

        for (int i = 0; i<airports.size()-1; ++i) {
            if (order.contains(i) == false) {
                return false;
            }
        }
        return isValid;
    }


    // Method for calculating distance - implementation of the Haversine formula.
    public static int haversineFormula(Location airportOne, Location airportTwo) {
        double latLocationOne = airportOne.getLatitude();
        double lonLocationOne = airportOne.getLongitude();

        double latLocationTwo = airportTwo.getLatitude();
        double lonLocationTwo = airportTwo.getLongitude();
        
        //Size of the earth's radius in km
        double earthRadius = 6371;

        //The difference between the 2 latitudes
        double deltaLatitude = Math.toRadians(Math.max(latLocationTwo, latLocationOne) - Math.min(latLocationOne, latLocationTwo));

        //The difference between the 2 longitudes
        double deltaLongitude = Math.toRadians(Math.max(lonLocationOne, lonLocationTwo) - Math.min(lonLocationOne, lonLocationTwo));
        
        //Getting the radians of the 2 latitudes
        latLocationOne = Math.toRadians(latLocationOne);
        latLocationTwo = Math.toRadians(latLocationTwo);

        //Implementation of the Haversine formula
        double haversineStepOne = (Math.sin(deltaLatitude/2)*Math.sin(deltaLatitude/2)) + (Math.sin(deltaLongitude/2) * Math.sin(deltaLongitude/2)) * Math.cos(latLocationOne) * Math.cos(latLocationTwo);
        
        double haversineStepTwo =  Math.asin(Math.sqrt(haversineStepOne)) * 2;

        double result =  earthRadius * haversineStepTwo;

        //Rounds the double down, and parses to an integer
        return (int)(Math.floor(result));
    }


    // Prints the solution
    public static void printSolution(List<Integer> order) {
        String solution = "";
        for (Integer position: order) {
            String currentString = position+",";
            solution+=currentString;
        }
        solution+="0";
        System.out.println(solution);
    }
    
    public static void printDistanceAndTime(List<Location> airports, List<Integer> order) {
        
        System.out.println ("Distance Travelled: " + findTotalDistance(airports, order)+"\nTime Taken: " + findTotalTime(airports, order));
    }

    public static int findTotalDistance(List<Location> airports, List<Integer> order) {
        double hoursTaken = 0;
        double speedInMPS = 800000/3600;
        double totalDistance = 0;
        for (int i =0; i<order.size()-1; ++i) {
            if(i==order.size()-2) {
                int distance=haversineFormula(airports.get(order.get(i)), airports.get(order.get(0)));
                totalDistance+=distance;
                hoursTaken = hoursTaken + distance/speedInMPS/3.6+0.5;
            }
            else {
                int distance =haversineFormula(airports.get(order.get(i)), airports.get(order.get(i+1)));
                totalDistance+=distance;
                hoursTaken = hoursTaken + distance/speedInMPS/3.6+0.5;
            }
        }
        return (int)totalDistance;
    }

    public static int findTotalTime(List<Location> airports, List<Integer> order) {
        double hoursTaken = 0;
        double speedInMPS = 800000/3600;
        for (int i =0; i<order.size()-1; ++i) {
            if(i==order.size()-2) {
                int distance=haversineFormula(airports.get(order.get(i)), airports.get(order.get(0)));
                hoursTaken = hoursTaken + distance/speedInMPS/3.6+0.5;
            }
            else {
                int distance =haversineFormula(airports.get(order.get(i)), airports.get(order.get(i+1)));
                hoursTaken = hoursTaken + distance/speedInMPS/3.6+0.5;
            }
        }
        return (int)hoursTaken;
    }

    private static List<Location> readInFromSpreadSheet (String csvName) {
        List<Location> airports = new ArrayList<>();
        Path fileLocation = Paths.get(csvName);
        try {
            BufferedReader reader = Files.newBufferedReader(fileLocation);
            String currentLine = reader.readLine();
            int locationNumber = 0;
            while (currentLine != null) {
                String inputData[] = currentLine.split(",");
                Location newLocation = createLocation(locationNumber, inputData);
                airports.add(newLocation);
                currentLine = reader.readLine();
                locationNumber++;
            }
        } catch(IOException ioexcepton) {
            ioexcepton.printStackTrace();
        }
        return airports;
    }

    private static Location createLocation(int locationNumberIn, String inputData[]) {
        int locationNumber = locationNumberIn;
        double latitude = Double.parseDouble(inputData[0]);
        double longitude =  Double.parseDouble(inputData[1]);

        return new Location(locationNumber, latitude, longitude);
    }
}


// Class that holds airport information.
class Location {
    // locationNumber represents which line it is in the csv file, 0-indexed.
    private int locationNumber;
    private double latitude;
    private double longitude;
    private int numLocationsConnected;

    // visited represents weather or not we have already made delivery there; false by default.
    private boolean visited;

    // Constructor
    public Location (int locationNumberIn, double latitudeIn, double longitudeIn) {
        this.locationNumber = locationNumberIn;
        this.latitude = latitudeIn;
        this.longitude = longitudeIn;
        this.numLocationsConnected = 0;
        this.visited = false;
    }

    // getters for the parameters.
    public int getLocationNumber() {
        return this.locationNumber;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public int getNumLocationsConnected() {
        return this.numLocationsConnected;
    }
    public boolean getVisited() {
        return this.visited;
    }

    // Change the visited boolean flag around
    public void setVisited() {
        this.visited = true;
    }

    public void setNumLocationsConnected() {
        this.numLocationsConnected++;
    }
}