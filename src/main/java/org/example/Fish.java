package org.example;

import lombok.Getter;
import lombok.Setter;

import static org.example.Main.*;

@Getter
@Setter
public class Fish implements Runnable {
    private final int moves;
    private final int fishNum;
    private final Gender gender;
    private int numberOfMovesLeft;
    private int x;
    private int y;

    public Fish(int moves, int fishNum, int genderState) {
        this.moves = moves;
        this.fishNum = fishNum;
        this.gender = genderState == 0 ? Gender.MALE : Gender.FEMALE;
        this.numberOfMovesLeft = moves;

        //placing them on the aquarium
        setInitialRandomLocation();
    }

    @Override
    public void run() {
        for (int i = 0; i < moves && !isReachedLimit.get(); i++) {
            this.numberOfMovesLeft--;
            //setting new location
            setNextRandomLocation();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (isReachedLimit.get()) {
            System.out.println("The number of fishes in the aquarium has reached its max limit");
            return;
        }

        System.out.printf("\n%s has died!!!\n\n", this);
        synchronized (fishesInAquarium) {
            //removing current fish from the aquarium
            fishesInAquarium[this.x][this.y] = null;
        }
        if (gender.equals(Gender.MALE))
            numberOfMaleFish.decrementAndGet();
        else
            numberOfFemaleFish.decrementAndGet();

        numberOfFishesInAquarium.decrementAndGet();
    }

    public void printAquarium() {
//        try {
//            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        System.out.printf("""
                Number of male fishes = %s
                Number of female fishes = %s
                Number of all fishes in the aquarium = %s
                                    
                """, numberOfMaleFish, numberOfFemaleFish, numberOfFishesInAquarium);

        for (Fish[] fish : fishesInAquarium) {
            for (Fish fish1 : fish) {
                if (fish1 != null)
                    System.out.print(fish1);
                else System.out.print("|  |");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("***".repeat(12));
        System.out.println();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //it sets initial random location while putting the fishes into
    //the aquarium
    public void setInitialRandomLocation() {
        while (fishesInAquarium[x][y] != null) {
            x = getXRandomLocation();
            y = getYRandomLocation();
        }
        fishesInAquarium[x][y] = this;
    }

    public void setNextRandomLocation() {
        setRandomLocation();
    }

    //it sets random location while moving
    public void setRandomLocation() {
        synchronized (fishesInAquarium) {
            fishesInAquarium[x][y] = null;
            boolean isLocationFree = false;

            while (!isLocationFree) {
                x = getXRandomLocation();
                y = getYRandomLocation();

                isLocationFree = checkLocation();
                if (isReachedLimit.get()) return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
//            printAquarium();
        }
    }

    public int getXRandomLocation() {
        return random.nextInt(aquariumHeight);
    }

    public int getYRandomLocation() {
        return random.nextInt(aquariumWidth);
    }

    public boolean checkLocation() {
        boolean isLocationFree;
        Fish fish1;
        fish1 = fishesInAquarium[x][y];
        if (fish1 != null) {
            if (!fish1.getGender().equals(this.getGender())) {
                System.out.printf("%s and %s have met\n", this, fish1);

                //creating new fish
                createNewFish();
            } else
                System.out.printf("Choosing another location for %s\n\n", this);

            isLocationFree = false;
        } else {
            fishesInAquarium[x][y] = this;
            isLocationFree = true;
            System.out.printf("\n%s fish moved to [%s,%s], moves left: %s\n\n", this, x, y, numberOfMovesLeft);
        }
        return isLocationFree;
    }

    //creates new fish
    public void createNewFish() {
        int genderState = random.nextInt(2);
        int fishNumber;

        if (genderState == 1)
            fishNumber = numberOfFemaleFish.incrementAndGet();
        else
            fishNumber = numberOfMaleFish.incrementAndGet();

        numberOfFishesInAquarium.incrementAndGet();

        if (numberOfFishesInAquarium.get() >= availableSpaceInAquarium) {
            isReachedLimit.set(true);
            return;
        }

        System.out.printf("New Fish has been created: Name %s%s\n\n",
                genderState == 0 ? "M-" : "F-", fishNumber);

        //it creates newborn fish and starts inside the tread
        Thread thread = new Thread( new Fish(
                random.nextInt(3, averageAquariumSize),
                fishNumber,
                genderState));

        thread.start();
    }

    @Override
    public String toString() {
        String fishName = String.format("%s%s", (gender.equals(Gender.MALE)) ? "M-" : "F-", fishNum);
        return String.format("|%s|", fishName);
    }
}
